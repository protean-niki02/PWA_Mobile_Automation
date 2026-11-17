package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_026 - Verify that the premium breakdown reflects the current cover selections.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * An add-on cover (e.g. Zero Depreciation) is selected and the Final Premium allowed to update; then
 * "View Breakup" is opened. The breakdown must reflect the selections: it lists the selected add-on and
 * the mandatory Owner Driver cover, and its Final Premium equals the plan summary card's.
 */
public class SC01_TC026_BreakupReflectsSelectionsTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_026: Verify that the premium breakdown reflects the current cover selections.")
    public void verifyBreakupReflectsSelections() {
        QuotesListingPage screen = openConfirmPlan();
        long base = finalPremium(screen);
        Cover cover = firstUnselected(screen, covers(screen), "add-on");
        Assert.assertNotNull(cover, "No unselected add-on cover with a premium to select. Screen text: " + screen.semanticsText());
        tapControl(screen, cover);
        screen.scrollToTop();
        long summary = waitForFinalPremiumChange(screen, base, 30_000);
        log("Selected " + cover.title() + "; Final Premium " + indianFormat(base) + " -> " + indianFormat(summary));

        List<String> breakup = tapAndCollect(screen, screen.text(QuotesListingPage.VIEW_BREAKUP), QuotesListingPage.VIEW_BREAKUP);
        String all = String.join(" | ", breakup);
        boolean listsCover = Pattern.compile("(?i)" + Pattern.quote(cover.title()).replace(" ", "\\E\\s*\\Q")).matcher(all).find()
            || all.toLowerCase().contains(cover.title().split("\\s+")[0].toLowerCase());
        boolean listsOwner = all.toLowerCase().contains("owner driver") || all.toLowerCase().contains("personal accident");
        long breakupFinal = 0;
        for (int i = 0; i < breakup.size(); i++) {
            if (breakup.get(i).toLowerCase().contains("final premium")) {
                for (int j = i; j < Math.min(breakup.size(), i + 3); j++) {
                    long amount = rupees(breakup.get(j));
                    if (amount > 0) {
                        breakupFinal = amount;
                        break;
                    }
                }
            }
        }
        log("Breakdown: " + breakup + "; lists " + cover.title() + ": " + listsCover + "; lists Owner Driver: " + listsOwner
            + "; breakdown Final Premium " + indianFormat(breakupFinal));

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(summary > base, "Selecting " + cover.title() + " did not update the Final Premium, so the breakdown can't reflect it");
        softly.assertTrue(listsCover, "The breakdown does not list the selected cover " + cover.title() + ": " + breakup);
        softly.assertTrue(listsOwner, "The breakdown does not list the mandatory Owner Driver cover: " + breakup);
        softly.assertEquals(breakupFinal, summary, "The breakdown's Final Premium (" + indianFormat(breakupFinal)
            + ") differs from the plan summary card's (" + indianFormat(summary) + ")");
        softly.assertAll();
    }
}
