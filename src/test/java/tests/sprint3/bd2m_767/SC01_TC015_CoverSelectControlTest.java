package tests.sprint3.bd2m_767;

import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_015 - Verify that each optional/add-on cover displays a select/deselect control.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Every cover under Optional Covers must show a select box at the right of its title. The box has no
 * label, so each cover's spot is captured and must show a drawn box: an outline or a filled square, not
 * blank card. Each cover's state (selected or not) is logged and a picture of every box saved.
 */
public class SC01_TC015_CoverSelectControlTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_015: Verify that each optional/add-on cover displays a select/deselect control.")
    public void verifyEachCoverShowsSelectControl() {
        QuotesListingPage screen = openConfirmPlan();
        List<Cover> covers = covers(screen);
        Assert.assertFalse(covers.isEmpty(), "No optional covers are listed. Screen text: " + screen.semanticsText());
        List<String> missing = new ArrayList<>();
        for (Cover cover : covers) {
            BoundingBox control = controlOf(screen, cover);
            BufferedImage box = captureAndSave("control_" + cover.title().replaceAll("[^A-Za-z0-9]+", "-").toLowerCase() + ".png",
                control.x - 4, control.y - 4, control.width + 8, control.height + 8);
            int drawn = 0;
            for (int y = 0; y < box.getHeight(); y++) {
                for (int x = 0; x < box.getWidth(); x++) {
                    drawn += brightness(box.getRGB(x, y)) < 200 ? 1 : 0;
                }
            }
            boolean shown = drawn >= box.getWidth() * box.getHeight() * 0.08;
            log(String.format("%s: select box %s at (%.0f, %.0f)%s", cover.title(), shown ? "shown" : "NOT shown", control.x, control.y,
                shown ? (isSelected(control) ? ", selected" : ", not selected") : ""));
            if (!shown) {
                missing.add(cover.title());
            }
        }
        Assert.assertTrue(missing.isEmpty(), missing.size() + " of " + covers.size() + " cover(s) show no select/deselect control: " + missing);
    }
}
