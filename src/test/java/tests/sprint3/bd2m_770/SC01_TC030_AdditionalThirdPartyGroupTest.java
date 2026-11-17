package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_030 - Verify that the Add On Covers section displays the numbered group "4. Additional Third Party" with Legal Liability to Employees &amp; Paid Driver as a green Coverageltem card.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Coverage &amp; Premium Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Additional Third Party details as per dummy mock data
 * Steps:         Expand the Add On Covers section &amp; check the group "4. Additional Third Party".
 * Expected:      Group "4. Additional Third Party" should display Legal Liability to Employees &amp; Paid Driver as a green Coverageltem card.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC030_AdditionalThirdPartyGroupTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_030: Verify that the Add On Covers section displays the numbered group \"4. Additional Third Party\" with Legal Liability to Employees & Paid Driver as a green Coverageltem card.")
    public void verifyAdditionalThirdPartyGroup() {
        openPolicyDetails();
        openTab(TAB_COVERAGE);
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        assertShown("4. Additional Third Party", "Legal Liability to Employees & Paid Driver");
    }
}