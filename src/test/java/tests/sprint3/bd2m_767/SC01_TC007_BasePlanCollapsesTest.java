package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_007 - Verify that the "What's included in your base plan?" section collapses.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The section is expanded first (its content appears), then its heading is tapped again: the content
 * must go away and the app stay on Confirm Plan.
 */
public class SC01_TC007_BasePlanCollapsesTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_007: Verify that the \"What's included in your base plan?\" section collapses.")
    public void verifyBasePlanCollapses() {
        QuotesListingPage screen = openConfirmPlan();
        String route = screen.route();
        List<String> opened = tapBasePlan(screen, "base plan (expand)");
        Assert.assertTrue(opened.size() >= 3, "\"" + BASE_PLAN + "\" did not expand, so its collapse can't be checked. New texts: " + opened);

        tapBasePlan(screen, "base plan (collapse)");
        List<String> after = screen.semanticsTexts();
        List<String> stillShown = opened.stream().filter(after::contains).toList();
        log("Content still shown after the second tap: " + stillShown);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(screen.route(), route, "Tapping \"" + BASE_PLAN + "\" again left Confirm Plan");
        softly.assertTrue(stillShown.size() <= opened.size() / 5, "\"" + BASE_PLAN + "\" did not collapse: "
            + stillShown.size() + " of its " + opened.size() + " texts are still shown " + stillShown);
        softly.assertAll();
    }
}
