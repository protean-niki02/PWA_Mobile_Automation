package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_008 - Verify that the policy summary card displays the "Contact Insurer" outlined action button.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Shared Shell &amp; Screen Layout" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Navigate to the Policy Details screen &amp; check the policy summary card.
 * Expected:      Policy summary card should display the "Contact Insurer" outlined action button.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC008_SummaryCardContactInsurerButtonTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_008: Verify that the policy summary card displays the \"Contact Insurer\" outlined action button.")
    public void verifySummaryCardContactInsurerButton() {
        openPolicyDetails();
        assertShown("Contact Insurer");
    }
}