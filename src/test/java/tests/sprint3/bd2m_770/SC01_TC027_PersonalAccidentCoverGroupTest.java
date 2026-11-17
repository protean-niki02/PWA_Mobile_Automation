package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_027 - Verify that the Add On Covers section displays the numbered group "1. Personal Accident Cover" with Personal Accident (Owner Driver), Un-named Passenger PA and Paid Driver PA as green Coverageltem cards.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Content Rendering - Coverage &amp; Premium Tab" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Personal Accident Cover details as per dummy mock data
 * Steps:         Expand the Add On Covers section &amp; check the group "1. Personal Accident Cover".
 * Expected:      Group "1. Personal Accident Cover" should display Personal Accident (Owner Driver), Un-named Passenger PA and Paid Driver PA as green Coverageltem cards.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC027_PersonalAccidentCoverGroupTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_027: Verify that the Add On Covers section displays the numbered group \"1. Personal Accident Cover\" with Personal Accident (Owner Driver), Un-named Passenger PA and Paid Driver PA as green Coverageltem cards.")
    public void verifyPersonalAccidentCoverGroup() {
        openPolicyDetails();
        openTab(TAB_COVERAGE);
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        assertShown("1. Personal Accident Cover", "Personal Accident (Owner Driver)", "Un-named Passenger PA", "Paid Driver PA");
    }
}