package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_022 - Verify that the Car Details section displays Registration City, RTO Code, Make &amp; Model, Fuel Type, Variant, Registration Date, Financier Name, Manufacturing Month &amp; Year, PUC Validity Date, Engine Number, Chassis Number and second-hand flag.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Vehicle Details Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Vehicle details as per dummy mock data
 * Steps:         Navigate to the Vehicle Details tab &amp; expand the Car Details section.
 * Expected:      Car Details section should display Registration City, RTO Code, Make &amp; Model, Fuel Type, Variant, Registration Date, Financier Name, Manufacturing Month &amp; Year, PUC Validity Date, Engine Number, Chassis Number and second-hand flag.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC022_CarDetailsFieldsTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_022: Verify that the Car Details section displays Registration City, RTO Code, Make & Model, Fuel Type, Variant, Registration Date, Financier Name, Manufacturing Month & Year, PUC Validity Date, Engine Number, Chassis Number and second-hand flag.")
    public void verifyCarDetailsFields() {
        openPolicyDetails();
        openTab(TAB_VEHICLE);
        ensureExpanded("Car Details", "Registration City");
        assertShown("Registration City", "RTO Code", "Make & Model", "Fuel Type", "Variant", "Registration Date", "Financier Name", "Manufacturing Month & Year", "PUC Validity Date", "Engine Number", "Chassis Number", "Second");
    }
}