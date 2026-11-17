package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_014 - Verify that the Policy Information section displays the Insured Declared Value.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Policy Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Expand the Policy Information section &amp; check the Insured Declared Value row.
 * Expected:      Policy Information section should display the Insured Declared Value.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC014_PolicyInformationInsuredDeclaredValueTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_014: Verify that the Policy Information section displays the Insured Declared Value.")
    public void verifyPolicyInformationInsuredDeclaredValue() {
        openPolicyDetails();
        ensureExpanded("Policy Information", "Policy Start Date");
        assertShown("Insured Declared Value");
    }
}