package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_031 - Verify that each Coverageltem card displays the Premium Paid value in green.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Coverage &amp; Premium Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Premium Paid values as per dummy mock data
 * Steps:         Expand the Add On Covers section &amp; check the Premium Paid value on any Coverageltem card.
 * Expected:      Coverageltem card should display the Premium Paid value in green.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC031_CoverageItemPremiumPaidTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_031: Verify that each Coverageltem card displays the Premium Paid value in green.")
    public void verifyCoverageItemPremiumPaid() {
        openPolicyDetails();
        openTab(TAB_COVERAGE);
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        assertShown("Premium Paid");
        Assert.assertTrue(hasColour(screenPicture(), new int[] {0x00, 0x94, 0x44}, 40), "No green Premium Paid value on the Coverageltem cards");
    }
}