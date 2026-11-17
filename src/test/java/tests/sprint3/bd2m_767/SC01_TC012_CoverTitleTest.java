package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_012 - Verify that each optional/add-on cover displays its title.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Every cover under Optional Covers must show a title next to its premium: a name of its own (not empty,
 * not a price, not its description) that is shown on screen. Every title is logged.
 */
public class SC01_TC012_CoverTitleTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_012: Verify that each optional/add-on cover displays its title.")
    public void verifyEachCoverShowsTitle() {
        QuotesListingPage screen = openConfirmPlan();
        List<Cover> covers = covers(screen);
        log(covers.size() + " cover title(s): " + covers.stream().map(Cover::title).toList());
        Assert.assertFalse(covers.isEmpty(), "No optional covers are listed. Screen text: " + screen.semanticsText());
        List<String> bad = covers.stream()
            .filter(c -> c.title().isBlank() || RUPEES.matcher(c.title()).find() || CUT_SHORT.matcher(c.title()).find()
                || c.title().length() > 60 || titleOf(screen, c).count() == 0)
            .map(c -> c.group() + " / premium " + c.price() + " -> \"" + c.title() + "\"").toList();
        screen.bringIntoView(screen.text(OPTIONAL_COVERS));
        screenshot("cover-titles");
        Assert.assertTrue(bad.isEmpty(), "These covers show no proper title: " + bad);
    }
}
