package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_039 - Verify that the customer is able to expand and collapse the policy summary card using the caret.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Expand/Collapse Interactions" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Tap the caret on the policy summary card &amp; tap it again.
 * Expected:      Policy summary card should expand on the first tap and collapse on the second tap.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC039_PolicySummaryCardExpandsAndCollapsesTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_039: Verify that the customer is able to expand and collapse the policy summary card using the caret.")
    public void verifySummaryCardExpandsAndCollapses() {
        openPolicyDetails();
        expandThenCollapse(() -> pressControl(summaryCaret(), "summary card caret"), "Policy summary card");
    }
}