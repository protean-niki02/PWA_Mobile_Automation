package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.ScreenPixels;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_026 - Verify that the action buttons provide an adequate touch target size.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Expected:      Action buttons should provide an adequate touch target size.
 * Status:        In Progress
 *
 * Measured at the target device's screen size (Galaxy S24 Ultra 384 x 832, iPhone 17 Pro
 * 402 x 874). Each action button shown must have a tappable area of at least the platform minimum in both
 * width and height: 48 x 48 dp on Android (Material Design), 44 x 44 pt on iOS (Apple
 * guidelines). Whether both buttons are there is SC_01_TC_013.
 */
public class SC01_TC026_ActionButtonsTouchTargetTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_026: Verify that the action buttons provide an adequate touch target size.")
    public void verifyActionButtonsTouchTargetSize() {
        SplashLandingScreen screen = openSplash();
        resizeToTargetDevice(screen);
        screenshot("target-device-size");
        List<Locator> buttons = assertAnyActionButtons(screen);
        double min = minTouchTarget();

        List<String> tooSmall = new ArrayList<>();
        for (Locator button : buttons) {
            String name = SplashLandingScreen.nameOf(button);
            screen.bringIntoView(button);
            BoundingBox box = button.boundingBox();
            saveEvidence("button-" + name.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase() + ".png",
                ScreenPixels.captureRow(page, box, 12));
            log(String.format("\"%s\": %.0f x %.0f px (minimum %.0f x %.0f)", name, box.width, box.height, min, min));
            if (box.width < min || box.height < min) {
                tooSmall.add(String.format("\"%s\" %.0f x %.0f", name, box.width, box.height));
            }
        }
        Assert.assertTrue(tooSmall.isEmpty(), String.format(
            "Action button(s) smaller than the %.0f x %.0f px minimum touch target on %s: %s", min, min, targetDeviceName(), tooSmall));
    }
}
