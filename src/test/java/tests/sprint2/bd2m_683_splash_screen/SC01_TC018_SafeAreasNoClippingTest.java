package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SC_01_TC_018 - Verify that the splash screen respects safe areas on supported device sizes without clipping.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     Supported mobile device sizes (see supportedSizes)
 * Step 1:        Launch the app on a supported device size & land on the splash screen & check for clipping
 * Expected:      Splash screen should respect safe areas without any clipping.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * At each supported size (screenshot of each):
 * <ul>
 *   <li>no text is cut off at the left or right screen edge;</li>
 *   <li>the heading, tagline, support line and action buttons stay out of the status bar /
 *       notch at the top and the gesture bar / home indicator at the bottom (safeAreaInsets).</li>
 * </ul>
 * The full-width image grid may run to the top edge: it is decoration, not content.
 * Browser emulation has no real notch, so the insets are those of notched phones; a real
 * notched device is the final check.
 */
public class SC01_TC018_SafeAreasNoClippingTest extends SplashLandingTestBase {

    /** Text partly off the left or right edge of the screen (fully off-screen slides don't count). */
    private static final String CLIPPED_TEXT = """
        () => {
            const width = innerWidth;
            const clipped = [];
            for (const el of document.querySelectorAll('flt-semantics')) {
                if (el.querySelector('flt-semantics')) continue;
                const text = (el.getAttribute('aria-label') || el.innerText || '').replace(/\\s+/g, ' ').trim();
                if (!text) continue;
                const r = el.getBoundingClientRect();
                const partlyOff = (r.left < -1 && r.right > 0) || (r.right > width + 1 && r.left < width);
                if (partlyOff) clipped.push(text.slice(0, 50) + ' [' + Math.round(r.left) + '..' + Math.round(r.right) + ' of ' + width + ' px]');
            }
            return clipped;
        }
        """;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_018: Verify that the splash screen respects safe areas on supported device sizes without clipping.")
    @SuppressWarnings("unchecked")
    public void verifySafeAreasWithoutClipping() {
        SplashLandingScreen screen = openSplash();
        double[] insets = safeAreaInsets();
        log(String.format("Safe area: %.0f px at the top, %.0f px at the bottom", insets[0], insets[1]));

        SoftAssert softly = new SoftAssert();
        for (int[] size : supportedSizes()) {
            String name = size[0] + "x" + size[1];
            resizeTo(screen, size[0], size[1]);
            screenshot("size-" + name);

            List<String> clipped = (List<String>) page.evaluate(CLIPPED_TEXT);
            log(name + ": " + (clipped.isEmpty() ? "no text cut off" : "text cut off: " + clipped));
            softly.assertTrue(clipped.isEmpty(), name + ": text is cut off at the screen edge: " + clipped);

            List<String> unsafe = new ArrayList<>();
            for (Map.Entry<String, Locator> element : keyElements(screen).entrySet()) {
                screen.scrollToTop();
                BoundingBox top = box(element.getValue());
                if (top != null && top.y < insets[0]) {
                    unsafe.add(String.format("%s under the status bar/notch (top at %.0f px)", element.getKey(), top.y));
                }
                // Lower down it may need scrolling to; once in view it must clear the bottom bar
                if (screen.bringIntoView(element.getValue())) {
                    BoundingBox inView = element.getValue().boundingBox();
                    if (inView.y + inView.height > size[1] - insets[1]) {
                        unsafe.add(String.format("%s under the gesture bar/home indicator (bottom at %.0f of %d px)",
                            element.getKey(), inView.y + inView.height, size[1]));
                    }
                } else {
                    unsafe.add(element.getKey() + " can't be brought fully on screen");
                }
            }
            log(name + ": " + (unsafe.isEmpty() ? "all content inside the safe area" : unsafe));
            softly.assertTrue(unsafe.isEmpty(), name + ": content outside the safe area: " + unsafe);
        }
        softly.assertAll();
    }

    /** Heading, tagline, support line (when shown) and the action buttons, by name. */
    private Map<String, Locator> keyElements(SplashLandingScreen screen) {
        Map<String, Locator> elements = new LinkedHashMap<>();
        elements.put("Heading", screen.welcomeHeading());
        if (screen.tagline().count() > 0) {
            elements.put("Tagline", screen.tagline());
        }
        if (screen.supportLine().count() > 0) {
            elements.put("Support line", screen.supportLine());
        }
        for (Locator button : screen.actionButtons()) {
            elements.put("Button \"" + SplashLandingScreen.nameOf(button) + "\"", button);
        }
        return elements;
    }
}
