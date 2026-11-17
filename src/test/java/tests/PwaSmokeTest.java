package tests;

import base.BaseTest;
import config.DeviceProfile;
import org.testng.annotations.Test;
import pages.WelcomePage;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Smoke tests for the Flutter PWA. Mobile browsers only - desktop gets the Next.js site. */
public class PwaSmokeTest extends BaseTest {

    @Override
    protected DeviceProfile defaultDevice() {
        return DeviceProfile.ANDROID;
    }

    @Test(groups = "smoke")
    public void welcomeScreenLoads() {
        WelcomePage welcome = new WelcomePage(page).open();

        System.out.println("[" + device + "] Page title: " + welcome.title() + " | URL: " + welcome.url());

        // Build 2.0.4+8 titles the PWA "BIMA SUGAM"
        assertThat(page).hasTitle(Pattern.compile("Bima Sugam", Pattern.CASE_INSENSITIVE));
        assertThat(page).hasURL(Pattern.compile("/onboarding"));
        assertThat(welcome.welcomeHeading()).isVisible();
        assertThat(welcome.getStartedButton()).isVisible();
    }
}
