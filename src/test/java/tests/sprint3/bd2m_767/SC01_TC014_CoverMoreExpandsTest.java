package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.FlutterPage;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_014 - Verify that tapping "more" on an optional/add-on cover expands the full description.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * On the first cover with a cut-short description (Owner Driver), "more" at the end of the description
 * is tapped. The full description must then be shown: no "...more" any longer, and longer than before
 * (it starts with the same words). The app must stay on Confirm Plan.
 */
public class SC01_TC014_CoverMoreExpandsTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_014: Verify that tapping \"more\" on an optional/add-on cover expands the full description.")
    public void verifyMoreExpandsDescription() {
        QuotesListingPage screen = openConfirmPlan();
        Cover cover = covers(screen).stream().filter(c -> CUT_SHORT.matcher(c.description()).find()).findFirst().orElse(null);
        Assert.assertNotNull(cover, "No cover shows a cut-short (\"...more\") description. Screen text: " + screen.semanticsText());
        String start = cover.description().substring(0, Math.min(30, cover.description().length()));
        Locator description = screen.text(Pattern.compile(FlutterPage.regexQuote(start)));
        screen.bringIntoView(description);
        screenshot("description-before-more");
        String route = screen.route();

        BoundingBox box = description.boundingBox();
        screen.tapAt(box.x + box.width - 14, box.y + box.height - 8);
        page.waitForTimeout(1_500);
        String after = screen.semanticsTexts().stream().filter(t -> t.startsWith(start)).findFirst().orElse("");
        log(cover.title() + " description before (" + cover.description().length() + " chars): " + cover.description());
        log(cover.title() + " description after tapping more (" + after.length() + " chars): " + after);
        screenshot("description-after-more");

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(screen.route(), route, "Tapping \"more\" left Confirm Plan");
        softly.assertFalse(CUT_SHORT.matcher(after).find(), "Tapping \"more\" on " + cover.title()
            + " did not expand its description - it still ends in \"...more\": " + after);
        softly.assertTrue(after.length() > cover.description().length(), "The description of " + cover.title()
            + " is not longer after tapping \"more\" (" + cover.description().length() + " -> " + after.length() + " chars)");
        softly.assertAll();
    }
}
