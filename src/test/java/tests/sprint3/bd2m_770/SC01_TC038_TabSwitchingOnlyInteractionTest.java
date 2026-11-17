package tests.sprint3.bd2m_770;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SC_01_TC_038 - Verify that tab switching is the only interactive behaviour available on the tab strip.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Tab Switching" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Tap on each tab segment &amp; check that only the tab content switches.
 * Expected:      Only the tab content should switch on tap; no other action should be triggered from the tab strip.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC038_TabSwitchingOnlyInteractionTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_038: Verify that tab switching is the only interactive behaviour available on the tab strip.")
    public void verifyTabSwitchingOnlyInteraction() {
        openPolicyDetails();
        String url = page.url();
        java.util.List<String> sent = apiRequestsDuring(() -> {
            openTab(TAB_VEHICLE);
            openTab(TAB_COVERAGE);
            openTab(TAB_POLICY);
        });
        Assert.assertEquals(page.url(), url, "Switching tabs navigated away from the Policy Details screen");
        Assert.assertTrue(sent.stream().noneMatch(r -> !r.startsWith("GET")), "Switching tabs triggered other actions: " + sent);
        assertScreenIntact("after switching tabs");
    }
}