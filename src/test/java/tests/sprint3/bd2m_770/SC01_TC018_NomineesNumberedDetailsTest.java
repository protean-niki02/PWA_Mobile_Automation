package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_018 - Verify that the Nominees section displays the numbered nominee details with Full Name, Date of Birth, Gender, Phone Number, Email ID, Relationship With Proposer, Share Percentage, City/District and State.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Policy Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Nominee 1 details as per dummy mock data
 * Steps:         Expand the Nominees section &amp; check the numbered nominee details.
 * Expected:      Nominees section should display the numbered nominee with Full Name, Date of Birth, Gender, Phone Number, Email ID, Relationship With Proposer, Share Percentage, City/District and State.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC018_NomineesNumberedDetailsTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_018: Verify that the Nominees section displays the numbered nominee details with Full Name, Date of Birth, Gender, Phone Number, Email ID, Relationship With Proposer, Share Percentage, City/District and State.")
    public void verifyNomineesNumberedDetails() {
        openPolicyDetails();
        ensureExpanded("Nominees", "Relationship With Proposer");
        assertShown("Nominees", "Full Name", "Date of Birth", "Gender", "Phone Number", "Email ID", "Relationship With Proposer", "Share Percentage", "City/District", "State");
    }
}