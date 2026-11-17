package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_025 - Verify that all Vehicle Details tab content is populated with the dummy data exactly as in the approved Mobile Figma.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Vehicle Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Dummy mock data as per approved Mobile Figma
 * Steps:         Navigate to the Vehicle Details tab &amp; expand the Car Details section &amp; compare the displayed data against the approved Mobile Figma.
 * Expected:      All Vehicle Details tab content should be populated with the dummy data exactly as in the approved Mobile Figma.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC025_VehicleDetailsTabMatchesFigmaTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_025: Verify that all Vehicle Details tab content is populated with the dummy data exactly as in the approved Mobile Figma.")
    public void verifyVehicleDetailsTabMatchesFigma() {
        openPolicyDetails();
        openTab(TAB_VEHICLE);
        ensureExpanded("Car Details", "Registration City");
        assertMatchesApproved("SC_01_TC_025");
    }
}