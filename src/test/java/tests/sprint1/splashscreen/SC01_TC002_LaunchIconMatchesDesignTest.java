package tests.sprint1.splashscreen;

import org.testng.annotations.Test;
import pages.LaunchIcons;
import pages.LaunchIcons.DownloadedIcon;
import pages.WelcomePage;
import support.ApprovedDesign;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SC_01_TC_002 - Verify that the updated launch icon matches the approved design.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: Mobile application is installed on the device
 * Test data:     Approved launch icon design
 * Step 1:        Install the app & check the app icon on the device home screen & compare with the approved design
 * Expected:      Updated launch icon should match the approved design.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Test data: put the approved icon files in src/test/resources/approved-design/launch-icon/,
 * named like the app's icons (Icon-192.png, Icon-512.png, ...). Skipped until they are there.
 */
public class SC01_TC002_LaunchIconMatchesDesignTest extends SplashScreenTestBase {

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_002: Verify that the updated launch icon matches the approved design.")
    public void verifyLaunchIconMatchesApprovedDesign() {
        Map<String, byte[]> approved = ApprovedDesign.load("launch-icon");

        new WelcomePage(page).open();
        screenshot("pwa-opened");

        LaunchIcons launchIcons = new LaunchIcons(page);
        Map<String, byte[]> appIcons = new LinkedHashMap<>();
        for (LaunchIcons.Icon icon : launchIcons.declared()) {
            DownloadedIcon downloaded = launchIcons.download(icon);
            if (downloaded.status() == 200) {
                appIcons.putIfAbsent(LaunchIcons.fileName(icon), downloaded.body());
            }
        }

        assertMatchesApproved("SC_01_TC_002 launch icon", approved, appIcons);
    }
}
