package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_055 - Verify that the read-only rows across all three tabs are displayed as per the approved Mobile Figma styling.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Visual Styling Verification" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Navigate through all three tabs &amp; check the read-only rows styling.
 * Expected:      Read-only rows should be displayed as per the approved Mobile Figma styling.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC055_ReadOnlyRowsMatchFigmaTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_055: Verify that the read-only rows across all three tabs are displayed as per the approved Mobile Figma styling.")
    public void verifyReadOnlyRowsMatchFigma() {
        openPolicyDetails();
        ensureExpanded("Policy Information", "Policy Start Date");
        assertMatchesApproved("SC_01_TC_055_policy");
        openTab(TAB_VEHICLE);
        ensureExpanded("Car Details", "Registration City");
        assertMatchesApproved("SC_01_TC_055_vehicle");
        openTab(TAB_COVERAGE);
        ensureExpanded("Base Coverage", "Base Premium");
        assertMatchesApproved("SC_01_TC_055_coverage");
    }
}