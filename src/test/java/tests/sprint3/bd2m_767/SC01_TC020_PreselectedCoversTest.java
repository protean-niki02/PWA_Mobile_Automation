package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_020 - Verify that pre-selected covers appear selected on load.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The covers already chosen before Confirm Plan opens - the add-on chips on the quotes listing (by
 * default "PA Owner Driver", kept when the PA cover question was confirmed) - must be ticked when
 * Confirm Plan loads. Every cover's state on load is logged.
 */
public class SC01_TC020_PreselectedCoversTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_020: Verify that pre-selected covers appear selected on load.")
    public void verifyPreselectedCoversSelected() {
        QuotesListingPage screen = openConfirmPlan();
        List<Cover> covers = covers(screen);
        Assert.assertFalse(covers.isEmpty(), "No optional covers are listed. Screen text: " + screen.semanticsText());
        List<String> selected = new ArrayList<>();
        for (Cover cover : covers) {
            if (isSelected(screen, cover)) {
                selected.add(cover.title());
            }
        }
        log("Chosen before Confirm Plan (listing chips): " + chosenChips + "; selected on load: " + selected);
        screen.bringIntoView(screen.text(OPTIONAL_COVERS));
        screenshot("covers-on-load");

        SoftAssert softly = new SoftAssert();
        softly.assertFalse(chosenChips.isEmpty(), "The listing showed no pre-selected add-on chips, so there is nothing to check");
        for (String chip : chosenChips) {
            boolean ticked = covers.stream().anyMatch(c -> chip.toLowerCase().contains(c.title().toLowerCase()) && selected.contains(c.title()));
            softly.assertTrue(ticked, "\"" + chip + "\" was selected before Confirm Plan but its cover is not selected on load. Selected: " + selected);
        }
        softly.assertAll();
    }
}
