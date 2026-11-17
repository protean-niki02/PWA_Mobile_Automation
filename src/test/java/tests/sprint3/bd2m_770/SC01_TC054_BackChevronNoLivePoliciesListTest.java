package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_054 - Verify that the back chevron does not navigate to a live policies list and does not cause the screen to crash.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "CTA &amp; Non-Functional Button Validation" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Tap the back chevron on the header.
 * Expected:      Back chevron should not navigate to a live policies list and the screen should not crash.
 * Type:          Negative, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC054_BackChevronNoLivePoliciesListTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_054: Verify that the back chevron does not navigate to a live policies list and does not cause the screen to crash.")
    public void verifyBackChevronNoLivePoliciesList() {
        openPolicyDetails();
        java.util.List<String> sent = apiRequestsDuring(() -> pressControl(backControl(), "back chevron"));
        Assert.assertTrue(sent.stream().noneMatch(r -> r.toLowerCase().contains("polic")), "The back chevron loaded a live policies list: " + sent);
        assertScreenIntact("after tapping the back chevron");
    }
}