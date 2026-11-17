package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_036 - Verify that the active tab segment is displayed with filled #4C5BB5 background and white text.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Switching" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Active tab colour = #4C5BB5
 * Steps:         Tap on any tab segment &amp; check its styling once active.
 * Expected:      Active tab segment should be displayed with filled #4C5BB5 background and white text.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC036_ActiveTabFillAndTextTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_036: Verify that the active tab segment is displayed with filled #4C5BB5 background and white text.")
    public void verifyActiveTabFillAndText() {
        openPolicyDetails();
        openTab(TAB_VEHICLE);
        int[] fill = fillOf(textOrLabel(TAB_VEHICLE));
        log("Active tab fill " + hex(fill));
        Assert.assertTrue(closeTo(fill, ACTIVE_TAB, 16), "The active tab is filled " + hex(fill) + ", expected " + hex(ACTIVE_TAB));
    }
}