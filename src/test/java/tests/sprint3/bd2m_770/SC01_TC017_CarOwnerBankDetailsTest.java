package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_017 - Verify that the Car Owner Details section displays the Bank Details sub-block under Car Owner Details (Account Holder Name, IFSC Code, Account Number, Bank Name and Account Type).
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Policy Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Bank details as per dummy mock data
 * Steps:         Expand the Car Owner Details section &amp; check the Bank Details sub-block.
 * Expected:      Bank Details sub-block should display Account Holder Name, IFSC Code, Account Number, Bank Name and Account Type.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC017_CarOwnerBankDetailsTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_017: Verify that the Car Owner Details section displays the Bank Details sub-block under Car Owner Details (Account Holder Name, IFSC Code, Account Number, Bank Name and Account Type).")
    public void verifyCarOwnerBankDetails() {
        openPolicyDetails();
        ensureExpanded("Car Owner Details", "Full Name");
        assertShown("Bank Details", "Account Holder Name", "IFSC Code", "Account Number", "Bank Name", "Account Type");
    }
}