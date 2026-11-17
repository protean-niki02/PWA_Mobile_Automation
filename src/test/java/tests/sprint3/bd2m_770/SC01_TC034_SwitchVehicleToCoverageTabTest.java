package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_034 - Verify that the customer is able to switch from the Vehicle Details tab to the Coverage &amp; Premium tab.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Switching" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Tap on the Coverage &amp; Premium tab segment.
 * Expected:      Screen should switch to the Coverage &amp; Premium tab and display its content.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC034_SwitchVehicleToCoverageTabTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_034: Verify that the customer is able to switch from the Vehicle Details tab to the Coverage & Premium tab.")
    public void verifySwitchVehicleToCoverageTab() {
        openPolicyDetails();
        openTab(TAB_VEHICLE);
        openTab(TAB_COVERAGE);
        assertShown("Base Coverage");
    }
}