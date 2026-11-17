package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

/**
 * SC_01_TC_004 - Verify that the plan summary card displays the correct Final Premium for the selected quote.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Sanity
 *
 * The premium on the selected quote card's button is noted on the listing (e.g. "₹53,313"); the plan
 * summary card's Final Premium on Confirm Plan must be the same amount.
 */
public class SC01_TC004_SummaryFinalPremiumTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "sanity"},
        description = "SC_01_TC_004: Verify that the plan summary card displays the correct Final Premium for the selected quote.")
    public void verifySummaryFinalPremium() {
        QuotesListingPage screen = openConfirmPlan();
        long selected = rupees(chosenPremium);
        long shown = finalPremium(screen);
        log("Premium of the selected quote: " + indianFormat(selected) + "; Final Premium on the plan summary card: " + indianFormat(shown));
        screenshot("summary-final-premium");
        Assert.assertTrue(shown > 0, "The plan summary card shows no Final Premium. Screen text: " + screen.semanticsText());
        Assert.assertEquals(shown, selected, "The plan summary card's Final Premium is " + indianFormat(shown)
            + ", but the selected quote's premium was " + indianFormat(selected));
    }
}
