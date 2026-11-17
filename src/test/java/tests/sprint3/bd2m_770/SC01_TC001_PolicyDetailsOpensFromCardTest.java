package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_001 - Verify that the Policy Details screen opens from a policy card in My Policies.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Shared Shell &amp; Screen Layout" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Navigate to My Policies &amp; tap on a policy card.
 * Expected:      Policy Details screen should open for the selected policy.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC001_PolicyDetailsOpensFromCardTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_001: Verify that the Policy Details screen opens from a policy card in My Policies.")
    public void verifyPolicyDetailsOpens() {
        openPolicyDetails();
        assertShown(TAB_POLICY, TAB_VEHICLE, TAB_COVERAGE);
    }
}