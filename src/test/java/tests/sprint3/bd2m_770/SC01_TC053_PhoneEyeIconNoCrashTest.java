package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_053 - Verify that tapping the phone-reveal eye icon does not cause the screen to crash.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "CTA &amp; Non-Functional Button Validation" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Expand the Car Owner Details section &amp; tap the phone-reveal eye icon next to the Phone Number.
 * Expected:      Screen should not crash on tapping the phone-reveal eye icon.
 * Type:          Negative, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC053_PhoneEyeIconNoCrashTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_053: Verify that tapping the phone-reveal eye icon does not cause the screen to crash.")
    public void verifyPhoneEyeIconNoCrash() {
        openPolicyDetails();
        ensureExpanded("Car Owner Details", "Full Name");
        pressControl(eyeIcon(), "phone-reveal eye icon");
        assertScreenIntact("after tapping the eye icon");
    }
}