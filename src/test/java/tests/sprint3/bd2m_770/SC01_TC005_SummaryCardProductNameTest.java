package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_005 - Verify that the policy summary card displays the product name.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Shared Shell &amp; Screen Layout" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Product Name = HDFC ERGO Bima Vahan
 * Steps:         Navigate to the Policy Details screen &amp; check the policy summary card.
 * Expected:      Policy summary card should display the product name.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC005_SummaryCardProductNameTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_005: Verify that the policy summary card displays the product name.")
    public void verifySummaryCardProductName() {
        openPolicyDetails();
        assertShown("HDFC ERGO Bima Vahan");
    }
}