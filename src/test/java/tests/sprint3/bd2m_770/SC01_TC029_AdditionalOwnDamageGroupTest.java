package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_029 - Verify that the Add On Covers section displays the numbered group "3. Additional Own Damage" with Non-Electrical Accessories Cover, Electrical/Electronic Accessories and CNG/LPG/Bi-Fuel Kit as green Coverageltem cards.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Coverage &amp; Premium Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Additional Own Damage details as per dummy mock data
 * Steps:         Expand the Add On Covers section &amp; check the group "3. Additional Own Damage".
 * Expected:      Group "3. Additional Own Damage" should display Non-Electrical Accessories Cover, Electrical/Electronic Accessories and CNG/LPG/Bi-Fuel Kit as green Coverageltem cards.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC029_AdditionalOwnDamageGroupTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_029: Verify that the Add On Covers section displays the numbered group \"3. Additional Own Damage\" with Non-Electrical Accessories Cover, Electrical/Electronic Accessories and CNG/LPG/Bi-Fuel Kit as green Coverageltem cards.")
    public void verifyAdditionalOwnDamageGroup() {
        openPolicyDetails();
        openTab(TAB_COVERAGE);
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        assertShown("3. Additional Own Damage", "Non-Electrical Accessories Cover", "Electrical/Electronic Accessories", "CNG/LPG/Bi-Fuel Kit");
    }
}