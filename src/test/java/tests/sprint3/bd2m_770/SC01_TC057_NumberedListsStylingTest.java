package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_057 - Verify that the numbered lists (Nominees, Add On Covers groups) are displayed as per the approved Mobile Figma styling.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Visual Styling Verification" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Navigate to the Nominees section and Add On Covers section &amp; check the numbered list styling.
 * Expected:      Numbered lists should be displayed as per the approved Mobile Figma styling.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC057_NumberedListsStylingTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_057: Verify that the numbered lists (Nominees, Add On Covers groups) are displayed as per the approved Mobile Figma styling.")
    public void verifyNumberedListsStyling() {
        openPolicyDetails();
        ensureExpanded("Nominees", "Relationship With Proposer");
        assertShown("Nominees", "1");
        openTab(TAB_COVERAGE);
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        assertShown("1. Personal Accident Cover", "2. Add-Ons", "3. Additional Own Damage", "4. Additional Third Party");
    }
}