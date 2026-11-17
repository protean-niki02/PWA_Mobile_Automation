package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_015 - Verify that the Product Details sub-block displays the Product Name, Policy Type and Policy Term.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Policy Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Product Name = HDFC ERGO Bima Vahan; Policy Type = Comprehensive; Policy Term = 1 Yr OD + 3 Yr TP
 * Steps:         Expand the Policy Information section &amp; check the Product Details sub-block.
 * Expected:      Product Details sub-block should display the Product Name, Policy Type (Comprehensive) and Policy Term (1 Yr OD + 3 Yr TP).
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC015_ProductDetailsSubBlockTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_015: Verify that the Product Details sub-block displays the Product Name, Policy Type and Policy Term.")
    public void verifyProductDetailsSubBlock() {
        openPolicyDetails();
        ensureExpanded("Policy Information", "Policy Start Date");
        assertShown("Product Details", "Product Name", "Policy Type", "Policy Term", "HDFC ERGO Bima Vahan", "Comprehensive", "1 Yr OD + 3 Yr TP");
    }
}