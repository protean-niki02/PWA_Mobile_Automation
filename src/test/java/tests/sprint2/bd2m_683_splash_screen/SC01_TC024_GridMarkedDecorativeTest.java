package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_024 - Verify that the decorative image grid is marked as decorative for accessibility purposes.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Test data:     Screen reader enabled
 * Expected:      Decorative image grid should be marked as decorative and should not trap accessibility focus.
 * Status:        In Progress
 *
 * Checked at the target device's screen size (Galaxy S24 Ultra 384 x 832, iPhone 17 Pro
 * 402 x 874). Screen readers read Flutter's accessibility (semantics) tree, which the tests switch on.
 * <ul>
 *   <li>Marked as decorative: nothing in the grid area is announced - no image, label, text or
 *       focusable element there.</li>
 *   <li>No focus trap: moving through the screen with the keyboard (Tab, as screen-reader
 *       swiping does) never lands inside the grid.</li>
 * </ul>
 * TalkBack / VoiceOver themselves can't run in the browser; this checks what they would read.
 */
public class SC01_TC024_GridMarkedDecorativeTest extends SplashLandingTestBase {

    private static final int TAB_PRESSES = 15;

    /** Accessibility nodes lying mostly (80%+) inside the grid area that a screen reader would announce. */
    private static final String ANNOUNCED_IN_GRID = """
        gridBottom => {
            const announced = [];
            for (const el of document.querySelectorAll('flt-semantics')) {
                const r = el.getBoundingClientRect();
                if (r.width === 0 || r.height === 0) continue;
                const inside = Math.max(0, Math.min(r.bottom, gridBottom) - Math.max(r.top, 0));
                if (inside < 0.8 * r.height) continue;
                const label = el.getAttribute('aria-label') || '';
                const role = el.getAttribute('role') || '';
                const text = [...el.childNodes].filter(n => n.nodeType === 3).map(n => n.textContent).join('').trim();
                const focusable = el.hasAttribute('tabindex');
                if (label || text || role === 'img' || role === 'button' || role === 'link' || focusable) {
                    announced.push((role || 'node') + ' "' + (label || text).slice(0, 40) + '"'
                        + (focusable ? ' (focusable)' : '') + ' at y ' + Math.round(r.top) + '..' + Math.round(r.bottom));
                }
            }
            return announced;
        }
        """;

    /** The focused accessibility node; null for the page itself or Flutter's (zero-size) host element. */
    private static final String FOCUSED = """
        () => {
            const e = document.activeElement;
            if (!e || e === document.body || e.tagName !== 'FLT-SEMANTICS') return null;
            if (e.getBoundingClientRect().height === 0) return null;
            const r = e.getBoundingClientRect();
            return [r.top, r.bottom, (e.getAttribute('aria-label') || e.innerText || e.tagName).slice(0, 40)];
        }
        """;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_024: Verify that the decorative image grid is marked as decorative for accessibility purposes.")
    @SuppressWarnings("unchecked")
    public void verifyImageGridIsMarkedDecorative() {
        SplashLandingScreen screen = openSplash();
        resizeToTargetDevice(screen);
        // Without the accessibility tree there is nothing to judge, and "nothing announced"
        // would be a false pass
        Assert.assertTrue(screen.welcomeHeading().count() > 0,
            "The accessibility tree is not available after resizing, so what a screen reader reads can't be checked");
        screenshot("target-device-size");
        double gridBottom = screen.gridBottom();

        List<String> announced = (List<String>) page.evaluate(ANNOUNCED_IN_GRID, gridBottom);
        log("Announced inside the image grid: " + (announced.isEmpty() ? "nothing" : announced));

        List<String> focusInGrid = new ArrayList<>();
        for (int i = 1; i <= TAB_PRESSES; i++) {
            page.keyboard().press("Tab");
            List<Object> focused = (List<Object>) page.evaluate(FOCUSED);
            if (focused != null) {
                double top = ((Number) focused.get(0)).doubleValue();
                double bottom = ((Number) focused.get(1)).doubleValue();
                log("Tab " + i + ": \"" + focused.get(2) + "\" at y " + Math.round(top) + ".." + Math.round(bottom));
                if (top >= -1 && bottom <= gridBottom + 1) {
                    focusInGrid.add("Tab " + i + ": \"" + focused.get(2) + "\"");
                }
            }
        }
        screenshot("after-tabbing");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(announced.isEmpty(), "The image grid is not marked as decorative - a screen reader would announce: "
            + announced);
        softly.assertTrue(focusInGrid.isEmpty(), "Accessibility focus goes into the decorative image grid: " + focusInGrid);
        softly.assertAll();
    }
}
