package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

/**
 * SC_01_TC_003 - Verify that the plan summary card displays the correct IDV.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The IDV on the selected quote card is noted on the listing; the plan summary card on Confirm Plan must
 * show the same IDV.
 */
public class SC01_TC003_SummaryIdvTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_003: Verify that the plan summary card displays the correct IDV.")
    public void verifySummaryIdv() {
        QuotesListingPage screen = openConfirmPlan();
        long shown = summaryIdv(screen);
        log("IDV on the selected quote card: " + indianFormat(chosenIdv) + "; on the plan summary card: " + indianFormat(shown));
        screenshot("summary-idv");
        Assert.assertTrue(shown > 0, "The plan summary card shows no IDV. Screen text: " + screen.semanticsText());
        Assert.assertEquals(shown, chosenIdv, "The plan summary card shows IDV " + indianFormat(shown)
            + ", but the selected quote's IDV was " + indianFormat(chosenIdv));
    }
}
