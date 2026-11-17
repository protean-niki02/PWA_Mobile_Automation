package tests.sprint3.bd2m_770;

import org.testng.annotations.Test;

/**
 * SC_01_TC_061 - Verify that the bottom navigation remains persistent while scrolling through the content.
 *
 * Sprint:        Sprint 3 - BD2M-770
 * Module:        Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile)
 * Requirement:   US-UCM-01, designed by Sumit
 * Scenario:      Validate the "Responsive Layout &amp; Scroll Behaviour" functionality.
 * Pre-requisite: Customer logged in &amp; has an active motor policy &amp; has opened the Policy Details screen from a
 *                policy card in My Policies.
 * Test data:     Not applicable
 * Steps:         Expand all sections on any tab &amp; scroll through the content.
 * Expected:      Bottom navigation should remain persistent on screen while scrolling.
 * Type:          Positive, Status in the sheet: Pass
 *
 * Runs once {@code policies.mobileNumber} and {@code policies.otp} (or {@code policyDetails.startUrl}) are set, see {@link PolicyDetailsTestBase}.
 */
public class SC01_TC061_BottomNavigationStaysPinnedTest extends PolicyDetailsTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_770", "regression"},
        description = "SC_01_TC_061: Verify that the bottom navigation remains persistent while scrolling through the content.")
    public void verifyBottomNavigationStaysPinned() {
        openPolicyDetails();
        ensureExpanded("Policy Information", "Policy Start Date");
        ensureExpanded("Car Owner Details", "Full Name");
        ensureExpanded("Nominees", "Relationship With Proposer");
        assertBottomNavPinned();
    }
}