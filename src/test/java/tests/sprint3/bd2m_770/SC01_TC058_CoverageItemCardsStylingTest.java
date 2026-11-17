package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_058 - Verify that the green Coverageltem cards are displayed with the title band, attribute rows and Premium Paid as per the approved Mobile Figma styling.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Visual Styling Verification" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Colours #263990 / #E5F6EC / #009444
 * Steps:         Navigate to the Coverage &amp; Premium tab &amp; check the styling of the green Coverageltem cards.
 * Expected:      Coverageltem card should display the title band, attribute rows and Premium Paid as per the approved Mobile Figma styling.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC058_CoverageItemCardsStylingTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_058: Verify that the green Coverageltem cards are displayed with the title band, attribute rows and Premium Paid as per the approved Mobile Figma styling.")
    public void verifyCoverageItemCardsStyling() {
        openPolicyDetails();
        openTab(TAB_COVERAGE);
        ensureExpanded("Add On Covers", "Personal Accident Cover");
        byte[] picture = screenPicture();
        Assert.assertTrue(hasColour(picture, new int[] {0x26, 0x39, 0x90}, 12), "No #263990 (title band) on the Coverageltem cards");
        Assert.assertTrue(hasColour(picture, new int[] {0xE5, 0xF6, 0xEC}, 8), "No #E5F6EC (card fill) on the Coverageltem cards");
        Assert.assertTrue(hasColour(picture, new int[] {0x00, 0x94, 0x44}, 12), "No #009444 (Premium Paid green) on the Coverageltem cards");
    }
}