package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_037 - Verify that the inactive tab segments are displayed in their default (non-active) styling.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Switching" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Tap on any tab segment &amp; check the styling of the remaining two segments.
 * Expected:      Inactive tab segments should be displayed in their default (non-active) styling.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC037_InactiveTabsDefaultStylingTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_037: Verify that the inactive tab segments are displayed in their default (non-active) styling.")
    public void verifyInactiveTabsDefaultStyling() {
        openPolicyDetails();
        int[] vehicle = fillOf(textOrLabel(TAB_VEHICLE));
        int[] coverage = fillOf(textOrLabel(TAB_COVERAGE));
        log("Inactive tab fills " + hex(vehicle) + ", " + hex(coverage));
        Assert.assertFalse(closeTo(vehicle, ACTIVE_TAB, 16) || closeTo(coverage, ACTIVE_TAB, 16), "An inactive tab is drawn in the active colour");
    }
}