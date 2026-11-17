package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_059 - Verify that the screen layout is displayed correctly across supported device sizes.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Responsive Layout &amp; Scroll Behaviour" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Supported device sizes
 * Steps:         Open the Policy Details screen on supported device sizes &amp; check the layout.
 * Expected:      Screen layout should be displayed correctly across all supported device sizes.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC059_LayoutAcrossDeviceSizesTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_059: Verify that the screen layout is displayed correctly across supported device sizes.")
    public void verifyLayoutAcrossDeviceSizes() {
        openPolicyDetails();
        int[][] sizes = {{360, 800}, {393, 852}, {412, 915}, {430, 932}};
        for (int[] size : sizes) {
            page.setViewportSize(size[0], size[1]);
            page.waitForTimeout(1_500);
            assertScreenIntact("at " + size[0] + "x" + size[1]);
            assertNothingClipped(size[0] + "x" + size[1]);
        }
    }
}