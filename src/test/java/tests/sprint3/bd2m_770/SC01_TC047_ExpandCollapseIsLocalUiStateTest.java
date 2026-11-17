package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_047 - Verify that the expand/collapse state is retained as local UI state without triggering any data action.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Expand/Collapse Interactions" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Expand and collapse any section on the Policy Details screen &amp; check that no data action is triggered.
 * Expected:      Expand/collapse action should only update the local UI state and should not trigger any data action.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC047_ExpandCollapseIsLocalUiStateTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_047: Verify that the expand/collapse state is retained as local UI state without triggering any data action.")
    public void verifyExpandCollapseIsLocalUiState() {
        openPolicyDetails();
        java.util.List<String> sent = apiRequestsDuring(() -> {
            tap("Policy Information");
            tap("Policy Information");
        });
        Assert.assertTrue(sent.isEmpty(), "Expanding and collapsing triggered data requests: " + sent);
        assertScreenIntact("after expanding and collapsing");
    }
}