package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import pages.DesktopHomePage;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class DesktopSmokeTest extends BaseTest {

    @Test(groups = "smoke")
    public void homePageLoads() {
        DesktopHomePage home = new DesktopHomePage(page).open();

        System.out.println("[" + device + "] Page title: " + home.title() + " | URL: " + home.url());

        assertThat(page).hasTitle(Pattern.compile("Bima Sugam", Pattern.CASE_INSENSITIVE));
        assertThat(home.homeLogo()).isVisible();
    }

    @Test(groups = "smoke")
    public void loginFormIsShownWithOtpDisabled() {
        DesktopHomePage home = new DesktopHomePage(page).open();

        assertThat(home.loginHeading()).isVisible();
        // OTP login stays disabled until a mobile number is entered
        assertThat(home.loginViaOtpButton()).isDisabled();
    }
}
