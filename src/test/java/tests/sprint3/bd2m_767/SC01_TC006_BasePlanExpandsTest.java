package tests.sprint3.bd2m_767;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_006 - Verify that the "What's included in your base plan?" section expands.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The section opens collapsed (a "+" at its right). Tapping it must expand it in place: the app stays
 * on Confirm Plan and the base plan's content appears (3+ new texts, naming the Own Damage and / or
 * Third Party cover).
 */
public class SC01_TC006_BasePlanExpandsTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_006: Verify that the \"What's included in your base plan?\" section expands.")
    public void verifyBasePlanExpands() {
        QuotesListingPage screen = openConfirmPlan();
        String route = screen.route();
        List<String> added = tapBasePlan(screen, "base plan");
        boolean namesCovers = added.stream().anyMatch(t -> t.matches("(?is).*(own damage|third party).*"));

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(screen.route(), route, "Tapping \"" + BASE_PLAN + "\" left Confirm Plan instead of expanding the section");
        softly.assertTrue(added.size() >= 3, "\"" + BASE_PLAN + "\" did not expand - new texts: " + added);
        softly.assertTrue(namesCovers, "The expanded base plan does not name its Own Damage / Third Party cover. New texts: " + added);
        softly.assertAll();
    }
}
