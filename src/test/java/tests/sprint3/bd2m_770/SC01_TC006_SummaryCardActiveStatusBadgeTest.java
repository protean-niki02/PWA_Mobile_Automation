package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_006 - Verify that the policy summary card displays the green "Active" status badge.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Shared Shell &amp; Screen Layout" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Status = Active
 * Steps:         Navigate to the Policy Details screen &amp; check the policy summary card.
 * Expected:      Policy summary card should display the green "Active" status badge.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC006_SummaryCardActiveStatusBadgeTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_006: Verify that the policy summary card displays the green \"Active\" status badge.")
    public void verifySummaryCardActiveStatusBadge() {
        openPolicyDetails();
        assertShown("Active");
        Assert.assertTrue(hasColour(screenPicture(), new int[] {0x00, 0x94, 0x44}, 40), "No green status badge on the policy summary card");
    }
}