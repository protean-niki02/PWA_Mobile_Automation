package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_048 - Verify that tapping the "Contact Insurer" button does not perform any data action.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "CTA &amp; Non-Functional Button Validation" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Tap the "Contact Insurer" button on the policy summary card.
 * Expected:      No data action should be performed on tapping the "Contact Insurer" button.
 * Type:          Negative, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC048_ContactInsurerNoActionTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_048: Verify that tapping the \"Contact Insurer\" button does not perform any data action.")
    public void verifyContactInsurerNoAction() {
        openPolicyDetails();
        String url = page.url();
        java.util.List<String> sent = apiRequestsDuring(() -> tap("Contact Insurer"));
        Assert.assertEquals(page.url(), url, "Tapping Contact Insurer navigated away");
        Assert.assertTrue(sent.isEmpty(), "Tapping Contact Insurer triggered data requests: " + sent);
    }
}