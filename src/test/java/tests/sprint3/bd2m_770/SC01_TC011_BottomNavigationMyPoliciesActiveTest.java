package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_011 - Verify that the persistent bottom navigation is displayed with My Policies shown as active with the orange indicator.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Shared Shell &amp; Screen Layout" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Navigate to the Policy Details screen &amp; check the bottom navigation.
 * Expected:      Bottom navigation should be displayed with My Policies shown as active with the orange indicator.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC011_BottomNavigationMyPoliciesActiveTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_011: Verify that the persistent bottom navigation is displayed with My Policies shown as active with the orange indicator.")
    public void verifyBottomNavigationMyPoliciesActive() {
        openPolicyDetails();
        assertShown("My Policies");
        Assert.assertTrue(hasColourNear(textOrLabel("My Policies"), new int[] {0xFF, 0x88, 0x2D}, 60, 30), "No orange indicator at My Policies in the bottom navigation");
    }
}