package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_013 - Verify that each optional/add-on cover displays a truncated description.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Every cover under Optional Covers must show its description cut short - ending in "...more" - under
 * its title. A cover with no description, or a full one with no "more", is named in the failure.
 */
public class SC01_TC013_CoverTruncatedDescriptionTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_013: Verify that each optional/add-on cover displays a truncated description.")
    public void verifyEachCoverShowsTruncatedDescription() {
        QuotesListingPage screen = openConfirmPlan();
        List<Cover> covers = covers(screen);
        covers.forEach(c -> log(c.title() + ": " + c.description()));
        Assert.assertFalse(covers.isEmpty(), "No optional covers are listed. Screen text: " + screen.semanticsText());
        screen.bringIntoView(screen.text(OPTIONAL_COVERS));
        screenshot("cover-descriptions");
        List<String> bad = covers.stream().filter(c -> !CUT_SHORT.matcher(c.description()).find())
            .map(c -> c.title() + (c.description().isEmpty() ? " (no description)" : " (\"" + c.description() + "\")")).toList();
        Assert.assertTrue(bad.isEmpty(), bad.size() + " of " + covers.size() + " cover(s) show no truncated (\"...more\") description: " + bad);
    }
}
