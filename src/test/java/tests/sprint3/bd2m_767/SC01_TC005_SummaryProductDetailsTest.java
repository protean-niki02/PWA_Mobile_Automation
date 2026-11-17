package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_005 - Verify that tapping the chevron/Product Details on the plan summary card navigates to the insurer detail.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The plan summary card opens the insurer's product detail two ways: "Product Details" at the bottom of
 * the card and the chevron "&gt;" at the right of the insurer name. Both are checked: each must open the
 * product detail (a new screen, or a sheet with new content) naming the insurer; after the first the
 * test goes back to Confirm Plan. The chevron has no label, so it is found by its pixels and tapped there.
 */
public class SC01_TC005_SummaryProductDetailsTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_005: Verify that tapping the chevron/Product Details on the plan summary card navigates to the insurer detail.")
    public void verifySummaryProductDetails() {
        QuotesListingPage screen = openConfirmPlan();
        Locator link = screen.text(QuotesListingPage.PRODUCT_DETAILS);
        Assert.assertTrue(link.count() > 0, "No \"Product Details\" on the plan summary card. Screen text: " + screen.semanticsText());
        String confirmRoute = screen.route();

        SoftAssert softly = new SoftAssert();
        screen.bringIntoView(link);
        softly.assertTrue(opensDetail(screen, "Product Details", () -> screen.pressOn(link)),
            "Tapping \"Product Details\" on the plan summary card did not open " + chosenInsurer + "'s product detail");
        if (!screen.route().equals(confirmRoute)) {
            page.goBack();
            waitFor(() -> screen.route().equals(confirmRoute) && screen.shows(HEADING), 20_000);
            page.waitForTimeout(1_500);
        }

        Locator name = screen.text(chosenInsurer);
        screen.scrollToTop();
        BoundingBox row = name.boundingBox();
        double x = chevronX(row);
        double y = row.y + row.height / 2;
        log(String.format("Chevron found at (%.0f, %.0f)", x, y));
        softly.assertTrue(opensDetail(screen, "chevron", () -> screen.tapAt(x, y)),
            "Tapping the chevron on the plan summary card did not open " + chosenInsurer + "'s product detail");
        softly.assertAll();
    }

    /** Taps and returns whether the product detail opened: new route or 3+ new texts, naming the insurer. */
    private boolean opensDetail(QuotesListingPage screen, String what, Runnable tap) {
        String routeBefore = screen.route();
        List<String> before = screen.semanticsTexts();
        tap.run();
        page.waitForTimeout(2_500);
        screen.waitForImagesToSettle();
        List<String> added = new ArrayList<>(screen.semanticsTexts());
        added.removeAll(before);
        boolean newScreen = !screen.route().equals(routeBefore);
        String brand = chosenInsurer.split("\\s+")[0];
        boolean namesInsurer = screen.shows(brand);
        screenshot("product-detail-via-" + what.replaceAll("[^A-Za-z]+", "-").toLowerCase());
        log(String.format("Tapped %s: route %s -> %s, %d new text(s) %s, names %s: %s", what, routeBefore, screen.route(),
            added.size(), added.size() > 8 ? added.subList(0, 8) + "..." : added, brand, namesInsurer));
        return (newScreen || added.size() >= 3) && namesInsurer;
    }

    /** The right-most dark mark between the end of the insurer name and the card's right edge. */
    private double chevronX(BoundingBox row) {
        double left = row.x + row.width;
        double width = page.viewportSize().width - left;
        BufferedImage strip = captureAndSave("chevron-strip.png", left, row.y, width, row.height);
        double scale = strip.getWidth() / width;
        int right = -1, start = -1;
        for (int x = strip.getWidth() - 1; x >= 0; x--) {
            boolean dark = false;
            for (int y = 0; y < strip.getHeight() && !dark; y++) {
                dark = brightness(strip.getRGB(x, y)) < 160;
            }
            if (dark) {
                right = right < 0 ? x : right;
                start = x;
            } else if (right >= 0) {
                break;
            }
        }
        return right >= 0 ? left + (start + right) / 2.0 / scale : page.viewportSize().width - 30;
    }
}
