package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_019 - Verify that the Nominees section displays the nominee Bank Details sub-block with Account Holder Name, IFSC Code, Account Number, Bank Name and Account Type.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Policy Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Nominee bank details as per dummy mock data
 * Steps:         Expand the Nominees section &amp; check the nominee Bank Details sub-block.
 * Expected:      Nominee Bank Details sub-block should display Account Holder Name, IFSC Code, Account Number, Bank Name and Account Type.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC019_NomineeBankDetailsTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_019: Verify that the Nominees section displays the nominee Bank Details sub-block with Account Holder Name, IFSC Code, Account Number, Bank Name and Account Type.")
    public void verifyNomineeBankDetails() {
        openPolicyDetails();
        ensureExpanded("Nominees", "Relationship With Proposer");
        assertShown("Bank Details", "Account Holder Name", "IFSC Code", "Account Number", "Bank Name", "Account Type");
    }
}