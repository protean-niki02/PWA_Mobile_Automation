package tests.sprint1.splashscreen;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashScreen;
import pages.WebAppManifest;
import support.AssetRecorder;

/**
 * SC_01_TC_010 - Verify that the branding updates are applied consistently on the splash screen.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: User is on the splash screen
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & check branding updates
 * Expected:      Branding updates should be applied consistently on the splash screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Covers both splash screens a user can see:
 * - the in-browser splash: Bima Sugam logo and the page title while it is shown;
 * - the splash Android shows when the installed app opens, which Android builds from the
 *   manifest's name and background_color, so those must be Bima Sugam's, not Flutter's
 *   template values.
 */
public class SC01_TC010_BrandingOnSplashTest extends SplashScreenTestBase {

    /** Values left from Flutter's project template when branding was not applied. */
    private static final String FLUTTER_TEMPLATE_COLOR = "#0175C2";
    private static final String FLUTTER_TEMPLATE_DESCRIPTION = "A new Flutter project.";

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_010: Verify that the branding updates are applied consistently on the splash screen.")
    public void verifyBrandingOnSplashScreen() {
        AssetRecorder assets = new AssetRecorder(page);
        SplashScreen splash = new SplashScreen(page);
        SoftAssert softly = new SoftAssert();

        splash.launch();
        splash.waitForHtmlSplashLogoRendered();
        screenshot("html-splash");

        String logoSrc = splash.htmlSplashLogo().getAttribute("src");
        String logoAlt = splash.htmlSplashLogo().getAttribute("alt");
        log("HTML splash logo: src=" + logoSrc + " alt=" + logoAlt);
        softly.assertTrue(logoSrc != null && logoSrc.contains("bima_sugam_logo"),
            "HTML splash logo is not the Bima Sugam logo: " + logoSrc);
        softly.assertEquals(logoAlt, BRAND_NAME, "HTML splash logo alt text");

        String splashTitle = page.title();
        log("Title during splash: " + splashTitle);
        softly.assertTrue(mentionsBrand(splashTitle), "Page title during the splash is '" + splashTitle + "'");

        WebAppManifest manifest = WebAppManifest.load(page);
        String name = manifest.text("name");
        String shortName = manifest.text("short_name");
        String background = manifest.text("background_color");
        String description = manifest.text("description");
        log("Manifest (installed app splash): name=" + name + " short_name=" + shortName
            + " background_color=" + background + " description=" + description);
        softly.assertTrue(isBrandName(name), "Manifest name (shown on the installed app's splash) is '" + name + "'");
        softly.assertTrue(isBrandName(shortName), "Manifest short_name (shown under the app icon) is '" + shortName + "'");
        softly.assertFalse(FLUTTER_TEMPLATE_COLOR.equalsIgnoreCase(background),
            "Manifest background_color (installed app's splash background) is still Flutter's template blue " + background);
        softly.assertNotEquals(description, FLUTTER_TEMPLATE_DESCRIPTION, "Manifest description is still Flutter's template text");

        if (SplashScreen.FLUTTER_SPLASH_ROUTE.equals(splash.waitForFirstAppRoute())) {
            screenshot("flutter-splash");
        }
        splash.waitUntilSplashFinished();
        softly.assertFalse(brandLogos(assets).isEmpty(),
            "No Bima Sugam logo image (" + BRAND_LOGO_FILES + ") was loaded during the splash");
        softly.assertAll();
    }

    /** "Bima Sugam", ignoring case, spaces and punctuation (so "BimaSugam" and "Bima-Sugam" count). */
    private static boolean isBrandName(String value) {
        return value != null && value.replaceAll("[^A-Za-z]", "").toLowerCase().contains("bimasugam");
    }
}
