package tests.sprint1.splashscreen;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.LaunchIcons;
import pages.LaunchIcons.DownloadedIcon;
import pages.LaunchIcons.Icon;
import pages.WelcomePage;
import support.ImageCompare;

import java.util.List;
import java.util.Set;

/**
 * SC_01_TC_001 - Verify that the updated launch icon is displayed on the device home screen after installing the app.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: Mobile application is installed on the device
 * Test data:     None
 * Step 1:        Install the app & check the app icon on the device home screen
 * Expected:      Updated launch icon should be displayed on the device home screen after installing the app.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * The home screen is outside the browser, so this checks what the device installs the PWA
 * with: the manifest icons (Android) and apple-touch-icon (iOS) must load as PNGs of the
 * declared size, and none may still be Flutter's default icon.
 */
public class SC01_TC001_LaunchIconDisplayedTest extends SplashScreenTestBase {

    /**
     * SHA-256 of the icons Flutter's project template ships with (blue Flutter logo).
     * A launch icon matching one of these was never replaced with the Bima Sugam icon.
     */
    private static final Set<String> FLUTTER_TEMPLATE_ICONS = Set.of(
        "3dce99077602f70421c1c6b2a240bc9b83d64d86681d45f2154143310c980be3", // Icon-192.png
        "baccb205ae45f0b421be1657259b4943ac40c95094ab877f3bcbe12cd544dcbe", // Icon-512.png
        "d2c842e22a9f4ec9d996b23373a905c88d9a203b220c5c151885ad621f974b5c", // Icon-maskable-192.png
        "6aee06cdcab6b2aef74b1734c4778f4421d2da100b0ff9e52b21b55240202929"  // Icon-maskable-512.png
    );

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_001: Verify that the updated launch icon is displayed on the device "
            + "home screen after installing the app.")
    public void verifyUpdatedLaunchIconIsDisplayed() {
        new WelcomePage(page).open();
        screenshot("pwa-opened");
        LaunchIcons launchIcons = new LaunchIcons(page);

        List<Icon> icons = launchIcons.declared();
        Assert.assertTrue(icons.stream().anyMatch(i -> "192x192".equals(i.sizes()) && i.purpose().contains("any")),
            "Manifest must declare a 192x192 launcher icon: " + icons);
        Assert.assertTrue(icons.stream().anyMatch(i -> "512x512".equals(i.sizes()) && i.purpose().contains("any")),
            "Manifest must declare a 512x512 launcher icon: " + icons);

        List<DownloadedIcon> downloads = icons.stream().map(launchIcons::download).toList();
        imageSheet("launch-icons", "SC_01_TC_001 - launch icons the device installs",
            downloads.stream().map(d -> new SheetImage(
                d.icon().source() + "\n" + LaunchIcons.fileName(d.icon()) + "\n" + d.width() + "x" + d.height(),
                LaunchIcons.fileName(d.icon()), d.body())).toList());

        SoftAssert softly = new SoftAssert();
        for (DownloadedIcon downloaded : downloads) {
            Icon icon = downloaded.icon();
            String label = icon.source() + " " + icon.url();
            log("[" + device + "] " + label + " -> " + downloaded.status() + " "
                + downloaded.contentType() + " " + downloaded.width() + "x" + downloaded.height());
            saveEvidence("icon_" + icon.source() + "_" + LaunchIcons.fileName(icon), downloaded.body());

            softly.assertEquals(downloaded.status(), 200, label + " status");
            softly.assertTrue(downloaded.width() > 0, label + " is not a PNG image (" + downloaded.contentType() + ")");
            if (icon.sizes() != null) {
                softly.assertEquals(downloaded.width() + "x" + downloaded.height(), icon.sizes(),
                    label + " actual size vs declared size");
            }
            softly.assertFalse(FLUTTER_TEMPLATE_ICONS.contains(ImageCompare.sha256(downloaded.body())),
                label + " is still Flutter's default icon, not the updated Bima Sugam launch icon");
        }
        softly.assertAll();
    }
}
