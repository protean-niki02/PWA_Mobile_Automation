package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_011 - Verify that each optional/add-on cover displays its indicative premium.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Every cover under Optional Covers (all four groups) must show its indicative premium as a rupee amount
 * (e.g. "₹ 413") in the box at its left. A cover showing "-" or nothing is named in the failure. Every
 * cover and its premium is logged.
 */
public class SC01_TC011_CoverPremiumTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_011: Verify that each optional/add-on cover displays its indicative premium.")
    public void verifyEachCoverShowsPremium() {
        QuotesListingPage screen = openConfirmPlan();
        List<Cover> covers = covers(screen);
        covers.forEach(c -> log(c.group() + " | " + c.title() + " | " + c.price()));
        screen.bringIntoView(screen.text(OPTIONAL_COVERS));
        screenshot("optional-covers");
        Assert.assertFalse(covers.isEmpty(), "No optional covers are listed. Screen text: " + screen.semanticsText());
        List<String> without = covers.stream().filter(c -> !c.hasPrice()).map(c -> c.title() + " (shows \"" + c.price() + "\")").toList();
        Assert.assertTrue(without.isEmpty(), without.size() + " of " + covers.size() + " cover(s) show no indicative premium: " + without);
    }
}
