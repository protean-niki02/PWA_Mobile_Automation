package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_028 - Verify that the Add On Covers section displays the numbered group "2. Add-Ons" with Zero Depreciation, Breakdown Assistance (RSA), Engine &amp; Gear Protection Cover, Consumable Cover, Electric &amp; Hybrid Protection Cover, Return to Invoice and Geographical Extension as green Coverageltem cards.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Coverage &amp; Premium Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Add-Ons details as per dummy mock data
 * Steps:         Expand the Add On Covers section &amp; check the group "2. Add-Ons".
 * Expected:      Group "2. Add-Ons" should display Zero Depreciation, Breakdown Assistance (RSA), Engine &amp; Gear Protection Cover, Consumable Cover, Electric &amp; Hybrid Protection Cover, Return to Invoice and Geographical Extension as green Coverageltem cards.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC028_AddOnsGroupTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_028: Verify that the Add On Covers section displays the numbered group \"2. Add-Ons\" with Zero Depreciation, Breakdown Assistance (RSA), Engine & Gear Protection Cover, Consumable Cover, Electric & Hybrid Protection Cover, Return to Invoice and Geographical Extension as green Coverageltem cards.")
    public void verifyAddOnsGroup() {
        openPolicyDetails();
        openTab(TAB_COVERAGE);
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        assertShown("2. Add-Ons", "Zero Depreciation", "Breakdown Assistance (RSA)", "Engine & Gear Protection Cover", "Consumable Cover", "Electric & Hybrid Protection Cover", "Return to Invoice", "Geographical Extension");
    }
}