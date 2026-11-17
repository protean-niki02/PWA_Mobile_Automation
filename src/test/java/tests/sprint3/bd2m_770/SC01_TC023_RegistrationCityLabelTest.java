package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_023 - Verify that the Car Details field label is displayed correctly as "Registration City".
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Vehicle Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Navigate to the Vehicle Details tab &amp; check the Registration City field label.
 * Expected:      Field label should be displayed correctly as "Registration City".
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC023_RegistrationCityLabelTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_023: Verify that the Car Details field label is displayed correctly as \"Registration City\".")
    public void verifyRegistrationCityLabel() {
        openPolicyDetails();
        openTab(TAB_VEHICLE);
        ensureExpanded("Car Details", "Registration City");
        assertShown("Registration City");
    }
}