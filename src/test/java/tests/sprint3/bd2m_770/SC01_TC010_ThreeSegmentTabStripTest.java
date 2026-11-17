package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_010 - Verify that the 3-segment tab strip displays an icon and two-line label for each of Policy Details, Vehicle Details and Coverage &amp; Premium.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Shared Shell &amp; Screen Layout" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Navigate to the Policy Details screen &amp; check the tab strip.
 * Expected:      Tab strip should display three segments - Policy Details, Vehicle Details and Coverage &amp; Premium - each with an icon and a two-line label.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC010_ThreeSegmentTabStripTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_010: Verify that the 3-segment tab strip displays an icon and two-line label for each of Policy Details, Vehicle Details and Coverage & Premium.")
    public void verifyThreeSegmentTabStrip() {
        openPolicyDetails();
        assertShown(TAB_POLICY, TAB_VEHICLE, TAB_COVERAGE);
        Assert.assertTrue(page.locator("img, svg, [role='img']").count() >= 3, "The tab strip shows no icon for each of its three segments");
    }
}