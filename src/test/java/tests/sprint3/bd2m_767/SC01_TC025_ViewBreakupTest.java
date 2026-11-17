package tests.sprint3.bd2m_767;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_025 - Verify that tapping "View Breakup" expands the premium breakdown.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * "View Breakup" on the plan summary card is tapped. The premium breakdown must appear (2+ new texts)
 * with breakdown lines (Base Premium, Personal Accident / add-ons, NCB, Taxes (GST), Final Premium ...)
 * and rupee amounts, without leaving Confirm Plan.
 */
public class SC01_TC025_ViewBreakupTest extends ConfirmPlanTestBase {

    static final Pattern BREAKUP_LINE = Pattern.compile(
        "(?i)base\\s*premium|own\\s*damage|third\\s*party|personal\\s*accident|owner\\s*driver|add-?on|NCB|GST|tax|final\\s*premium|total|discount");

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_025: Verify that tapping \"View Breakup\" expands the premium breakdown.")
    public void verifyViewBreakupExpands() {
        QuotesListingPage screen = openConfirmPlan();
        String route = screen.route();
        List<String> added = tapAndCollect(screen, screen.text(QuotesListingPage.VIEW_BREAKUP), QuotesListingPage.VIEW_BREAKUP);
        List<String> lines = added.stream().filter(t -> BREAKUP_LINE.matcher(t).find()).toList();
        List<String> amounts = added.stream().filter(t -> RUPEES.matcher(t).find()).toList();
        log("Breakdown lines: " + lines + "; amounts: " + amounts);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(screen.route(), route, "Tapping \"View Breakup\" left Confirm Plan");
        softly.assertTrue(added.size() >= 2, "Tapping \"View Breakup\" showed no premium breakdown. New texts: " + added);
        softly.assertFalse(lines.isEmpty(), "The breakdown shows no premium lines (Base Premium / GST / Final Premium ...). New texts: " + added);
        softly.assertFalse(amounts.isEmpty(), "The breakdown shows no rupee amounts. New texts: " + added);
        softly.assertAll();
    }
}
