package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_056 - Verify that the sub-block headers "Product Details", "Bank Details" and "Appointee" are displayed as per the approved Mobile Figma styling.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Visual Styling Verification" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Navigate to the relevant sections &amp; check the styling of the "Product Details", "Bank Details" and "Appointee" sub-block headers.
 * Expected:      Sub-block headers should be displayed as per the approved Mobile Figma styling.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC056_SubBlockHeadersTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_056: Verify that the sub-block headers \"Product Details\", \"Bank Details\" and \"Appointee\" are displayed as per the approved Mobile Figma styling.")
    public void verifySubBlockHeaders() {
        openPolicyDetails();
        ensureExpanded("Policy Information", "Policy Start Date");
        ensureExpanded("Car Owner Details", "Full Name");
        ensureExpanded("Nominees", "Relationship With Proposer");
        assertShown("Product Details", "Bank Details", "Appointee");
    }
}