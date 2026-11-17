package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_010 - Verify that the Third Party Liability Cover description is displayed.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * "What's included in your base plan?" is expanded and its "Third Party Liability Cover" opened (tapped
 * when it is a tab or a collapsed heading). A description of it must be shown: a sentence (40+
 * characters) about liability for injury / death of others or damage to their property.
 */
public class SC01_TC010_ThirdPartyDescriptionTest extends ConfirmPlanTestBase {

    // Use Pattern flags instead of inline (?i) so the source stays JS-regex compatible for Playwright text selectors.
    private static final Pattern THIRD_PARTY = Pattern.compile("third\\s*party\\s*liability", Pattern.CASE_INSENSITIVE);
    private static final Pattern DESCRIBES = Pattern.compile("third.?part|liabilit|injur|death|bodily|property|legal", Pattern.CASE_INSENSITIVE);

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_010: Verify that the Third Party Liability Cover description is displayed.")
    public void verifyThirdPartyDescription() {
        QuotesListingPage screen = openConfirmPlan();
        expandBasePlan(screen);
        Locator heading = screen.text(THIRD_PARTY);
        SoftAssert softly = new SoftAssert();
        softly.assertTrue(heading.count() > 0, "The base plan shows no \"Third Party Liability Cover\". Screen text: " + screen.semanticsText());
        if (heading.count() > 0) {
            tapAndCollect(screen, heading, "Third Party Liability Cover");
        }
        List<String> descriptions = screen.semanticsTexts().stream()
            .filter(t -> t.length() >= 40 && DESCRIBES.matcher(t).find() && !THIRD_PARTY.matcher(t).matches()).toList();
        log("Third Party descriptions shown: " + descriptions);
        screenshot("third-party-description");
        softly.assertFalse(descriptions.isEmpty(), "No Third Party Liability Cover description is shown. Screen text: "
            + screen.semanticsText());
        softly.assertAll();
    }
}