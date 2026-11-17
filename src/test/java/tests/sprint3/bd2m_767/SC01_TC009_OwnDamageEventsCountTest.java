package tests.sprint3.bd2m_767;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Matcher;

/**
 * SC_01_TC_009 - Verify that the "N events covered" summary is displayed correctly for Own Damage Cover.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * With "What's included in your base plan?" expanded, the Own Damage Cover must show an "N events
 * covered" summary, and N must equal the number of events it lists.
 */
public class SC01_TC009_OwnDamageEventsCountTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_009: Verify that the \"N events covered\" summary is displayed correctly for Own Damage Cover.")
    public void verifyOwnDamageEventsCount() {
        QuotesListingPage screen = openConfirmPlan();
        List<String> texts = expandBasePlan(screen);
        String summary = texts.stream().filter(t -> EVENTS_COVERED.matcher(t).find()).findFirst().orElse(null);
        List<String> listed = texts.stream().filter(t -> t.length() < 90
            && OD_EVENTS.stream().anyMatch(p -> p.matcher(t).find())
            && !EVENTS_COVERED.matcher(t).find()
            // Exclude PA group rows; they contain "accident" but are not Own Damage events.
            && !t.toLowerCase().contains("personal accident cover")).toList();
        int n = -1;
        if (summary != null) {
            Matcher m = EVENTS_COVERED.matcher(summary);
            n = m.find() ? Integer.parseInt(m.group(1)) : -1;
        }
        log("Summary: " + summary + "; events listed (" + listed.size() + "): " + listed);
        screenshot("own-damage-events-count");

        SoftAssert softly = new SoftAssert();
        softly.assertNotNull(summary, "No \"N events covered\" summary is shown for the Own Damage Cover. Screen text: " + texts);
        softly.assertTrue(summary == null || n == listed.size(), "The summary says \"" + summary + "\" but " + listed.size()
            + " event(s) are listed: " + listed);
        softly.assertAll();
    }
}