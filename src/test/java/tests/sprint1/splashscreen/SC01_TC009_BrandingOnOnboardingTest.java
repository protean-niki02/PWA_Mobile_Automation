package tests.sprint1.splashscreen;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.WelcomePage;
import support.AssetRecorder;

/**
 * SC_01_TC_009 - Verify that the branding updates are applied consistently on the onboarding screens.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: User is on an onboarding screen
 * Test data:     None
 * Step 1:        Launch the app & navigate through the onboarding screens & check branding updates
 * Expected:      Branding updates should be applied consistently on the onboarding screens.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * On every onboarding slide: the title and the screen say "Bima Sugam" and a Bima Sugam
 * logo is loaded. A screenshot of each slide is kept as evidence.
 */
public class SC01_TC009_BrandingOnOnboardingTest extends SplashScreenTestBase {

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_009: Verify that the branding updates are applied consistently on the onboarding screens.")
    public void verifyBrandingOnOnboardingScreens() {
        AssetRecorder assets = new AssetRecorder(page);
        WelcomePage onboarding = new WelcomePage(page).open();

        SoftAssert softly = new SoftAssert();
        String slide = onboarding.semanticsText();
        for (int i = 1; i <= WelcomePage.SLIDE_COUNT; i++) {
            if (i > 1) {
                slide = onboarding.nextSlide(slide);
            }
            screenshot("onboarding-slide-" + i);
            assertBranding(softly, onboarding, assets, "Onboarding slide " + i);
        }
        softly.assertAll();
    }
}
