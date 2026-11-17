package tests.sprint1.splashscreen;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.LoginPage;
import support.AssetRecorder;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * SC_01_TC_011 - Verify that the branding updates are applied consistently on the login screen.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: User is on the login screen
 * Test data:     None
 * Step 1:        Launch the app & navigate to the login screen & check branding updates
 * Expected:      Branding updates should be applied consistently on the login screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * On the login screen: the title and the screen say "Bima Sugam" and a Bima Sugam logo is loaded.
 */
public class SC01_TC011_BrandingOnLoginTest extends SplashScreenTestBase {

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_011: Verify that the branding updates are applied consistently on the login screen.")
    public void verifyBrandingOnLoginScreen() {
        AssetRecorder assets = new AssetRecorder(page);
        LoginPage login = new LoginPage(page).open();
        assertThat(login.mobileNumberField()).isVisible();
        screenshot("login-screen");

        SoftAssert softly = new SoftAssert();
        assertBranding(softly, login, assets, "Login screen");
        softly.assertAll();
    }
}
