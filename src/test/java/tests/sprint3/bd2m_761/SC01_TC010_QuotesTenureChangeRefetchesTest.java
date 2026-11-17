package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_010 - Verify that changing the coverage tenure re-fetches/updates the quotes.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 * Status:        In Progress
 *
 * On the quotes listing, notes the premiums and the current coverage tenure, taps the coverage-tenure
 * control and picks the other policy type in "Choose Policy Type" (Standalone TP / 1 Year Third
 * Party, or Comprehensive Package / 1 Year OD + 1 Year TP - whichever is not selected; the app
 * remembers the last choice for the car, so either can be current). Picking it applies it at once.
 * Then, within 2 minutes:
 * <ul>
 *   <li>the quotes must be fetched again - the "We are fetching the premium ..." loader comes back,
 *       or the premiums change;</li>
 *   <li>the coverage-tenure control must show the new tenure.</li>
 * </ul>
 * Premiums and tenure before and after are logged; screenshots of the editor and the updated
 * quotes are saved. When "Choose Policy Type" offers only the current tenure (seen on UAT build
 * 2.0.5+3), there is nothing to change to and the test is SKIPPED as blocked.
 */
public class SC01_TC010_QuotesTenureChangeRefetchesTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_010: Verify that changing the coverage tenure re-fetches/updates the quotes.")
    public void verifyTenureChangeRefetchesQuotes() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        List<String> premiumsBefore = quotes.premiums();
        Locator control = quotes.tenureControl();
        Assert.assertTrue(control.count() > 0, "The coverage-tenure control is not on the quotes listing. Screen text: "
            + quotes.semanticsText());
        String tenureBefore = nameOf(control);
        log("Before: tenure \"" + tenureBefore + "\", premiums " + premiumsBefore);

        List<String> editor = tapAndCollectNewTexts(quotes, control, "coverage tenure");
        Locator option = otherOption(quotes, editor, tenureBefore);
        String picked = nameOf(option);
        quotes.pressOn(option);
        log("Picked \"" + picked + "\"");

        // The editor closes and applies the choice; give the loader a moment to come up
        long closeBy = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < closeBy && page.getByText("Choose Policy Type").count() > 0) {
            page.waitForTimeout(250);
        }
        long loaderBy = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < loaderBy && !quotes.isFetching()) {
            page.waitForTimeout(250);
        }
        log("After picking: editor " + (page.getByText("Choose Policy Type").count() == 0 ? "closed" : "still open")
            + ", loader " + (quotes.isFetching() ? "shown" : "not seen within 5s"));

        boolean refetched = quotes.waitForRefetch(premiumsBefore, QUOTES_TIMEOUT_MS);
        quotes.scrollToTop();
        screenshot("quotes-after-tenure-change");
        String tenureAfter = quotes.tenureControl().count() > 0 ? nameOf(quotes.tenureControl()) : "(not found)";
        log("After: tenure \"" + tenureAfter + "\", premiums " + quotes.premiums());

        Assert.assertNotEquals(tenureAfter, tenureBefore, "The coverage tenure did not change after picking \"" + picked + "\"");
        Assert.assertTrue(refetched, "Changing the coverage tenure from \"" + tenureBefore + "\" to \"" + tenureAfter
            + "\" did not re-fetch or update the quotes (premiums still " + quotes.premiums() + ")");
    }

    /** The policy type that is not selected: an unticked radio button, else the offered tenure that isn't current. */
    private Locator otherOption(QuotesListingPage quotes, List<String> editor, String current) {
        Locator radios = page.getByRole(AriaRole.RADIO);
        for (int i = 0; i < radios.count(); i++) {
            if (!radios.nth(i).isChecked()) {
                return radios.nth(i);
            }
        }
        String other = editor.stream().filter(t -> QuotesListingPage.TENURE.matcher(t).find())
            .filter(t -> !t.toLowerCase().contains(current.toLowerCase()) && !current.toLowerCase().contains(t.toLowerCase()))
            .findFirst().orElse(null);
        if (other == null) {
            screenshot("only-current-policy-type");
            throw new SkipException("Blocked: \"Choose Policy Type\" offers only the current tenure (\"" + current
                + "\") for this car right now, so the tenure can't be changed. Editor texts: " + editor);
        }
        return quotes.text(other);
    }

    private static String nameOf(Locator element) {
        String label = element.getAttribute("aria-label");
        String text = label != null && !label.isBlank() ? label : element.textContent();
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }
}
