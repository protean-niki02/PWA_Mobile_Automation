package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_051 - Verify that tapping the "Policy Copy" button does not cause the screen to crash.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "CTA &amp; Non-Functional Button Validation" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Tap the "Policy Copy" button on the policy summary card.
 * Expected:      Screen should not crash on tapping the "Policy Copy" button.
 * Type:          Negative, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC051_PolicyCopyNoCrashTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_051: Verify that tapping the \"Policy Copy\" button does not cause the screen to crash.")
    public void verifyPolicyCopyNoCrash() {
        openPolicyDetails();
        tap("Policy Copy");
        assertScreenIntact("after tapping Policy Copy");
    }
}