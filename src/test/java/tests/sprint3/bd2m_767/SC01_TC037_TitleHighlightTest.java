package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * SC_01_TC_037 - Verify that the screen displays the title "Confirm your Bima Vahan Plan" with "Bima Vahan Plan" highlighted.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The screen title must read "Confirm your Bima Vahan Plan", with "Bima Vahan Plan" highlighted: in its own
 * element drawn in an accent (coloured) ink, different from the neutral ink of "Confirm your". The colours of
 * both parts are read from the page and logged with a picture of the title. Runs on the web journey, see
 * {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC037_TitleHighlightTest extends ConfirmPlanWebTestBase {

    private static final int MIN_ACCENT_SATURATION = 60;

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_037: Verify that the screen displays the title \"Confirm your Bima Vahan Plan\" with \"Bima Vahan Plan\" highlighted.")
    public void verifyTitleHighlight() {
        openConfirmPlan();
        scrollArea().evaluate("e => { e.scrollTop = 0; }");
        Locator title = page.locator("div.text-3xl").filter(new Locator.FilterOptions().setHasText(HEADING)).first();
        Assert.assertTrue(title.count() > 0, "The title \"" + HEADING + "\" is not shown. Screen text: " + bodyText());
        String shown = title.innerText().replaceAll("\\s+", " ").trim();
        Locator highlight = title.locator("span").filter(new Locator.FilterOptions().setHasText(HEADING_HIGHLIGHT)).first();
        boolean ownElement = highlight.count() > 0;
        int[] head = colourOf(title, "color");
        int[] tail = ownElement ? colourOf(highlight, "color") : head;
        title.screenshot(new Locator.ScreenshotOptions().setPath(evidenceFile("title.png")));
        boolean tailAccent = saturation(tail) >= MIN_ACCENT_SATURATION;
        boolean headNeutral = saturation(head) < MIN_ACCENT_SATURATION;
        log(String.format("Title \"%s\"; \"Confirm your\" ink %s, \"%s\" %sink %s", shown, hex(head), HEADING_HIGHLIGHT,
            ownElement ? "" : "(no own element) ", hex(tail)));
        screenshot("confirm-plan-title");

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(shown, HEADING, "The title reads \"" + shown + "\", expected \"" + HEADING + "\"");
        softly.assertTrue(ownElement, "\"" + HEADING_HIGHLIGHT + "\" is not set apart in its own element");
        softly.assertTrue(tailAccent && headNeutral, String.format("\"%s\" is not highlighted: it is drawn in %s while \"Confirm your\" is %s",
            HEADING_HIGHLIGHT, hex(tail), hex(head)));
        softly.assertAll();
    }
}
