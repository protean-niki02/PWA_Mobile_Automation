package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_062 - Verify that no content is clipped on any of the three tabs.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Responsive Layout &amp; Scroll Behaviour" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Expand all sections on each of the three tabs &amp; check for clipped content.
 * Expected:      No content should be clipped on any of the three tabs.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC062_NoContentClippedTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_062: Verify that no content is clipped on any of the three tabs.")
    public void verifyNoContentClipped() {
        openPolicyDetails();
        ensureExpanded("Policy Information", "Policy Start Date");
        ensureExpanded("Car Owner Details", "Full Name");
        ensureExpanded("Nominees", "Relationship With Proposer");
        assertNothingClipped("Policy Details tab");
        openTab(TAB_VEHICLE);
        ensureExpanded("Car Details", "Registration City");
        assertNothingClipped("Vehicle Details tab");
        openTab(TAB_COVERAGE);
        ensureExpanded("Base Coverage", "Base Premium");
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        assertNothingClipped("Coverage & Premium tab");
    }
}