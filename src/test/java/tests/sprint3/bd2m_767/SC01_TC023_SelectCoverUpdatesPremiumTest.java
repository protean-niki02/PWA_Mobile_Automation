package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

/**
 * SC_01_TC_023 - Verify that selecting an optional/add-on cover updates the Final Premium.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Sanity
 *
 * The first unselected add-on cover with a premium (under "Add-ons covers", e.g. Zero Depreciation) is
 * selected. Within 30s its box must show selected and the plan summary card's Final Premium must go up.
 * The rise is logged next to the cover's indicative premium (it may differ: the premium is indicative
 * and taxes apply).
 */
public class SC01_TC023_SelectCoverUpdatesPremiumTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "sanity"},
        description = "SC_01_TC_023: Verify that selecting an optional/add-on cover updates the Final Premium.")
    public void verifySelectingCoverUpdatesPremium() {
        QuotesListingPage screen = openConfirmPlan();
        long before = finalPremium(screen);
        Cover cover = firstUnselected(screen, covers(screen), "add-on");
        Assert.assertNotNull(cover, "No unselected add-on cover with a premium to select. Screen text: " + screen.semanticsText());
        screenshot("before-selecting");

        tapControl(screen, cover);
        boolean selected = isSelected(screen, cover);
        screenshot("cover-selected");
        screen.scrollToTop();
        long after = waitForFinalPremiumChange(screen, before, 30_000);
        log(String.format("Selected %s (%s): %s; Final Premium %s -> %s (+%s)", cover.title(), cover.price(),
            selected ? "box selected" : "box NOT selected", indianFormat(before), indianFormat(after), indianFormat(after - before)));
        screenshot("final-premium-after-selecting");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(selected, "Tapping " + cover.title() + "'s box did not select it");
        softly.assertTrue(after > before, "Selecting " + cover.title() + " (" + cover.price() + ") did not update the Final Premium: "
            + indianFormat(before) + " -> " + indianFormat(after));
        softly.assertAll();
    }
}
