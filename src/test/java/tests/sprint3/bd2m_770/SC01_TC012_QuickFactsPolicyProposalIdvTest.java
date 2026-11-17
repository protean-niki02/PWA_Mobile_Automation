package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_012 - Verify that the Policy Details tab displays the summary quick-facts: Policy Number, Proposal Number and Insured Declared Value.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Policy Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Policy Number = LIC1212121212, Proposal Number = 1234567890
 * Steps:         Navigate to the Policy Details screen &amp; check the summary quick-facts.
 * Expected:      Policy Details tab should display the summary quick-facts: Policy Number, Proposal Number, Insured Declared Value.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC012_QuickFactsPolicyProposalIdvTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_012: Verify that the Policy Details tab displays the summary quick-facts: Policy Number, Proposal Number and Insured Declared Value.")
    public void verifyQuickFacts() {
        openPolicyDetails();
        assertShown("Policy Number", "Proposal Number", "Insured Declared Value", "LIC1212121212", "1234567890");
    }
}