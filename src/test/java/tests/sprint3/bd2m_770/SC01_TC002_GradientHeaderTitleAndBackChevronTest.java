package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_002 - Verify that the gradient header displays the title "Policies" along with a back chevron.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Shared Shell &amp; Screen Layout" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Navigate to the Policy Details screen &amp; check the header.
 * Expected:      Gradient header should display the title "Policies" along with a back chevron.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC002_GradientHeaderTitleAndBackChevronTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_002: Verify that the gradient header displays the title \"Policies\" along with a back chevron.")
    public void verifyHeaderTitleAndBackChevron() {
        openPolicyDetails();
        assertShown("Policies");
        Assert.assertTrue(backControl().count() > 0, "No back chevron on the header");
    }
}