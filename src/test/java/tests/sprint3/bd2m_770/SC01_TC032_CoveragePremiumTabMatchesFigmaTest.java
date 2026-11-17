package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_032 - Verify that all Coverage &amp; Premium tab content is populated with the dummy data exactly as in the approved Mobile Figma.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Coverage &amp; Premium Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Dummy mock data as per approved Mobile Figma
 * Steps:         Navigate to the Coverage &amp; Premium tab &amp; expand all sections &amp; compare the displayed data against the approved Mobile Figma.
 * Expected:      All Coverage &amp; Premium tab content should be populated with the dummy data exactly as in the approved Mobile Figma.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC032_CoveragePremiumTabMatchesFigmaTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_032: Verify that all Coverage & Premium tab content is populated with the dummy data exactly as in the approved Mobile Figma.")
    public void verifyCoveragePremiumTabMatchesFigma() {
        openPolicyDetails();
        openTab(TAB_COVERAGE);
        ensureExpanded("Base Coverage", "Base Premium");
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        assertMatchesApproved("SC_01_TC_032");
    }
}