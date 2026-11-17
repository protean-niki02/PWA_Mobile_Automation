package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SC_01_TC_019 - Verify that the splash screen layout holds correctly across supported device sizes.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Status:        In Progress
 *
 * At each supported size (screenshot of each) - small, common and large phones, and the target
 * devices (Galaxy S24 Ultra 384 x 832, iPhone 17 Pro 402 x 874) - the layout must hold:
 * <ul>
 *   <li>the heading and the action button(s) (and tagline / support line when shown) are all there;</li>
 *   <li>none of them overlap and each fits the screen width;</li>
 *   <li>the heading is above the buttons, and the top-to-bottom order is the same at every size.</li>
 * </ul>
 * How many action buttons the screen should have is SC_01_TC_013's check, not this one's.
 * Emulated sizes on this PC, not the real devices.
 */
public class SC01_TC019_LayoutAcrossDeviceSizesTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_019: Verify that the splash screen layout holds correctly across supported device sizes.")
    public void verifyLayoutAcrossDeviceSizes() {
        SplashLandingScreen screen = openSplash();

        SoftAssert softly = new SoftAssert();
        List<String> firstOrder = null;
        for (int[] size : supportedSizes()) {
            String name = size[0] + "x" + size[1];
            resizeTo(screen, size[0], size[1]);
            screenshot("size-" + name);

            Map<String, BoundingBox> boxes = layout(screen);
            List<String> order = boxes.entrySet().stream()
                .sorted(Comparator.comparingDouble(e -> e.getValue().y)).map(Map.Entry::getKey).toList();
            log(name + ": top to bottom " + order);

            List<String> problems = new ArrayList<>();
            if (!boxes.containsKey("Heading")) {
                problems.add("heading missing");
            }
            if (boxes.keySet().stream().noneMatch(k -> k.startsWith("Button"))) {
                problems.add("no action button");
            }
            List<String> names = new ArrayList<>(boxes.keySet());
            for (int i = 0; i < names.size(); i++) {
                BoundingBox a = boxes.get(names.get(i));
                if (a.x < -1 || a.x + a.width > size[0] + 1) {
                    problems.add(String.format("%s does not fit the width (%.0f..%.0f of %d px)",
                        names.get(i), a.x, a.x + a.width, size[0]));
                }
                for (int j = i + 1; j < names.size(); j++) {
                    if (overlap(a, boxes.get(names.get(j)))) {
                        problems.add(names.get(i) + " overlaps " + names.get(j));
                    }
                }
            }
            BoundingBox heading = boxes.get("Heading");
            boxes.entrySet().stream().filter(e -> e.getKey().startsWith("Button"))
                .filter(e -> heading != null && e.getValue().y < heading.y + heading.height)
                .forEach(e -> problems.add(e.getKey() + " is above the heading"));
            if (firstOrder == null) {
                firstOrder = order;
            } else if (!order.equals(firstOrder)) {
                problems.add("order changed from " + firstOrder + " to " + order);
            }
            log(name + ": " + (problems.isEmpty() ? "layout holds" : problems));
            softly.assertTrue(problems.isEmpty(), name + ": layout does not hold - " + problems);
        }
        softly.assertAll();
    }

    /** Positions of the screen's key elements, with the screen scrolled to the top. */
    private Map<String, BoundingBox> layout(SplashLandingScreen screen) {
        Map<String, BoundingBox> boxes = new LinkedHashMap<>();
        putIfShown(boxes, "Heading", screen.welcomeHeading());
        putIfShown(boxes, "Tagline", screen.tagline());
        putIfShown(boxes, "Support line", screen.supportLine());
        for (Locator button : screen.actionButtons()) {
            boxes.put("Button \"" + SplashLandingScreen.nameOf(button) + "\"", button.boundingBox());
        }
        return boxes;
    }

    private static void putIfShown(Map<String, BoundingBox> boxes, String name, Locator locator) {
        BoundingBox box = box(locator);
        if (box != null) {
            boxes.put(name, box);
        }
    }

    /** True when the boxes share more than a pixel of area. */
    private static boolean overlap(BoundingBox a, BoundingBox b) {
        return a.x + 1 < b.x + b.width && b.x + 1 < a.x + a.width
            && a.y + 1 < b.y + b.height && b.y + 1 < a.y + a.height;
    }
}
