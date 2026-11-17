package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_046 - Verify that the customer is able to toggle "View Less/View More" within the Add On Covers section.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Expand/Collapse Interactions" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Tap "View Less" within the Add On Covers section &amp; tap "View More".
 * Expected:      Add On Covers section should collapse extra Coverageltem cards on "View Less" and expand them again on "View More".
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC046_AddOnCoversViewLessViewMoreTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_046: Verify that the customer is able to toggle \"View Less/View More\" within the Add On Covers section.")
    public void verifyAddOnCoversViewLessViewMore() {
        openPolicyDetails();
        openTab(TAB_COVERAGE);
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        int full = screenText().length();
        tap("View Less");
        int less = screenText().length();
        assertShown("View More");
        tap("View More");
        int more = screenText().length();
        log("Add On Covers text: " + full + " -> " + less + " (View Less) -> " + more + " (View More)");
        Assert.assertTrue(less < full, "View Less did not collapse the extra Coverageltem cards");
        Assert.assertTrue(more > less, "View More did not expand the extra Coverageltem cards again");
    }
}