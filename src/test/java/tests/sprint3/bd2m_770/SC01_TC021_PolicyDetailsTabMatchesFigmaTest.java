package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_021 - Verify that all Policy Details tab content is populated with the dummy data exactly as in the approved Mobile Figma.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Policy Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Dummy mock data as per approved Mobile Figma
 * Steps:         Navigate to the Policy Details tab &amp; expand all sections &amp; compare the displayed data against the approved Mobile Figma.
 * Expected:      All Policy Details tab content should be populated with the dummy data exactly as in the approved Mobile Figma.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC021_PolicyDetailsTabMatchesFigmaTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_021: Verify that all Policy Details tab content is populated with the dummy data exactly as in the approved Mobile Figma.")
    public void verifyPolicyDetailsTabMatchesFigma() {
        openPolicyDetails();
        ensureExpanded("Policy Information", "Policy Start Date");
        ensureExpanded("Car Owner Details", "Full Name");
        ensureExpanded("Nominees", "Relationship With Proposer");
        assertMatchesApproved("SC_01_TC_021");
    }
}