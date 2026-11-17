package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;
import support.ImageCompare;
import support.ScreenPixels;

import java.util.List;

/**
 * SC_01_TC_009 - Verify that the coverage-tenure control is editable.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 * Status:        In Progress
 *
 * On the quotes listing, taps the coverage-tenure control (e.g. "1 Year OD + 1 Year TP ✎" or
 * "1 Year Third Party ✎", next to the IDV control). It opens "Choose Policy Type" with one radio
 * button per tenure - "Standalone TP / 1 Year Third Party" and "Comprehensive Package / 1 Year OD +
 * 1 Year TP" - the current one ticked. The tenure must be editable: tapping the unticked option must
 * tick it. (If the options are not radio buttons, the option is found by its text instead and must
 * be visibly highlighted or change the editor.)
 *
 * The change is not applied; SC_01_TC_010 covers re-fetching the quotes. Screenshots of the editor
 * before and after are saved, with the option's row as option-before.png / option-after.png.
 */
public class SC01_TC009_QuotesTenureEditableTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_009: Verify that the coverage-tenure control is editable.")
    public void verifyTenureControlIsEditable() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        Locator control = quotes.tenureControl();
        Assert.assertTrue(control.count() > 0, "The coverage-tenure control is not on the quotes listing. Screen text: "
            + quotes.semanticsText());
        String current = nameOf(control);
        log("Current coverage tenure: \"" + current + "\"");
        List<String> editor = tapAndCollectNewTexts(quotes, control, "coverage tenure");

        Locator radios = page.getByRole(AriaRole.RADIO);
        Locator option = null;
        for (int i = 0; i < radios.count(); i++) {
            if (!radios.nth(i).isChecked()) {
                option = radios.nth(i);
                break;
            }
        }
        if (option == null) {
            // Not radio buttons: the first offered tenure that isn't the current one, by its text
            String other = editor.stream().filter(t -> QuotesListingPage.TENURE.matcher(t).find())
                .filter(t -> !t.toLowerCase().contains(current.toLowerCase()) && !current.toLowerCase().contains(t.toLowerCase()))
                .findFirst().orElse(null);
            Assert.assertNotNull(other, "Tapping the coverage-tenure control did not offer another tenure to choose. "
                + "Editor texts: " + editor + " | radio buttons: " + radios.count());
            option = quotes.text(other);
        }
        String name = nameOf(option);
        log("Tenure options: " + radios.count() + " radio button(s); picking the unticked \"" + name + "\"");

        quotes.bringIntoView(option);
        BoundingBox box = option.boundingBox();
        byte[] rowBefore = ScreenPixels.captureRow(page, box, 10);
        List<String> textsBefore = quotes.semanticsTexts();
        quotes.pressOn(option);
        page.waitForTimeout(1_500);
        byte[] rowAfter = ScreenPixels.captureRow(page, box, 10);
        saveEvidence("option-before.png", rowBefore);
        saveEvidence("option-after.png", rowAfter);
        screenshot("tenure-option-picked");

        boolean hadRadios = !name.isEmpty() && radios.count() > 0;
        if (radios.count() == 0 && quotes.text(QuotesListingPage.TENURE).count() > 0 && !textsBefore.isEmpty()
            && page.getByText("Choose Policy Type").count() == 0) {
            // Picking a policy type applied it at once and closed the editor: the control must show it now
            quotes.waitForQuotes(QUOTES_TIMEOUT_MS);
            String now = nameOf(quotes.tenureControl());
            screenshot("tenure-applied");
            log("The editor closed on picking \"" + name + "\"; the coverage tenure now reads \"" + now + "\" (was \"" + current + "\")");
            Assert.assertNotEquals(now, current, "The coverage tenure is not editable: picking \"" + name + "\" left it at \"" + current + "\"");
            return;
        }
        boolean ticked = hadRadios && option.isChecked();
        boolean highlighted = !ImageCompare.compare(rowBefore, rowAfter, 0.01).matches();
        boolean textChanged = !quotes.semanticsTexts().equals(textsBefore);
        log(String.format("Picked \"%s\": ticked %s, row highlighted %s, editor changed %s", name, ticked, highlighted, textChanged));
        Assert.assertTrue(hadRadios ? ticked : (highlighted || textChanged), "The coverage tenure is not editable: "
            + "picking \"" + name + "\" did not select it");
    }

    private static String nameOf(Locator element) {
        String label = element.getAttribute("aria-label");
        String text = label != null && !label.isBlank() ? label : element.textContent();
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }
}
