package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_013 - Verify that the Policy Information section displays the Policy Start Date, Policy End Date, TP Start Date, TP End Date, OD Start Date and OD End Date.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Policy Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Policy dates as per dummy mock data
 * Steps:         Expand the Policy Information section &amp; check the dates.
 * Expected:      Policy Information section should display Policy Start Date, Policy End Date, TP Start Date, TP End Date, OD Start Date and OD End Date.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC013_PolicyInformationDatesTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_013: Verify that the Policy Information section displays the Policy Start Date, Policy End Date, TP Start Date, TP End Date, OD Start Date and OD End Date.")
    public void verifyPolicyInformationDates() {
        openPolicyDetails();
        ensureExpanded("Policy Information", "Policy Start Date");
        assertShown("Policy Start Date", "Policy End Date", "TP Start Date", "TP End Date", "OD Start Date", "OD End Date");
    }
}