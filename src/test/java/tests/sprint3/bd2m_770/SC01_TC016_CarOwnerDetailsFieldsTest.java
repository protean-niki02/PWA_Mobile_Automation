package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_016 - Verify that the Car Owner Details section displays Full Name, KYC Status, Date of Birth, Gender, Phone Number with eye icon, Email Address, Permanent Address and Communication Address.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Policy Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Owner details as per dummy mock data
 * Steps:         Expand the Car Owner Details section &amp; check the displayed fields.
 * Expected:      Car Owner Details section should display Full Name, KYC Status, Date of Birth, Gender, Phone Number (with eye icon), Email Address, Permanent Address and Communication Address.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC016_CarOwnerDetailsFieldsTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_016: Verify that the Car Owner Details section displays Full Name, KYC Status, Date of Birth, Gender, Phone Number with eye icon, Email Address, Permanent Address and Communication Address.")
    public void verifyCarOwnerDetailsFields() {
        openPolicyDetails();
        ensureExpanded("Car Owner Details", "Full Name");
        assertShown("Full Name", "KYC Status", "Date of Birth", "Gender", "Phone Number", "Email Address", "Permanent Address", "Communication Address");
        Assert.assertTrue(eyeIcon().count() > 0, "No eye icon beside the Phone Number");
    }
}