package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_060 - Verify that long content scrolls correctly on each of the three tabs.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Responsive Layout &amp; Scroll Behaviour" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Expand all sections on each of the three tabs &amp; scroll through the content.
 * Expected:      Long content should scroll correctly on each of the three tabs.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC060_LongContentScrollsOnEachTabTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_060: Verify that long content scrolls correctly on each of the three tabs.")
    public void verifyLongContentScrollsOnEachTab() {
        openPolicyDetails();
        ensureExpanded("Policy Information", "Policy Start Date");
        ensureExpanded("Car Owner Details", "Full Name");
        ensureExpanded("Nominees", "Relationship With Proposer");
        assertContentScrolls("Policy Details tab");
        openTab(TAB_VEHICLE);
        ensureExpanded("Car Details", "Registration City");
        assertContentScrolls("Vehicle Details tab");
        openTab(TAB_COVERAGE);
        ensureExpanded("Base Coverage", "Base Premium");
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        assertContentScrolls("Coverage & Premium tab");
    }
}