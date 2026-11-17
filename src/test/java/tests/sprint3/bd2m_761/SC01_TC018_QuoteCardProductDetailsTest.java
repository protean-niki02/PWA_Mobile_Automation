package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.ScreenPixels;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_018 - Verify that tapping the chevron/Product Details on a quote card navigates to the insurer product detail.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * A quote card opens its insurer's product detail two ways: the underlined "Product Details" link at
 * the bottom of the card, and the chevron "&gt;" at the right of the insurer's name. Both are checked
 * on the first quote card:
 * <ol>
 *   <li>tap "Product Details" - the product detail must open (a new screen, or a sheet with new
 *       content) and name the card's insurer; then go back to the listing;</li>
 *   <li>tap the chevron - the same must happen.</li>
 * </ol>
 * The chevron has no label, so it is found by its pixels (the right-most dark mark on the insurer-name
 * row) and tapped there.
 * Screenshots of each product detail are saved.
 */
public class SC01_TC018_QuoteCardProductDetailsTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_018: Verify that tapping the chevron/Product Details on a quote card navigates to the insurer product detail.")
    public void verifyProductDetailsOpens() {
        QuotesListingPage quotes = openLoadedQuotes();
        Locator link = quotes.text(QuotesListingPage.PRODUCT_DETAILS);
        Assert.assertTrue(link.count() > 0, "No \"" + QuotesListingPage.PRODUCT_DETAILS + "\" on the quote cards. Screen text: "
            + quotes.semanticsText());
        quotes.bringIntoView(link);
        String insurer = insurerAbove(quotes, link.boundingBox().y);
        Assert.assertNotNull(insurer, "Could not tell which insurer the first \"" + QuotesListingPage.PRODUCT_DETAILS + "\" belongs to");
        log("First quote card: " + insurer);

        String listingRoute = quotes.route();
        SoftAssert softly = new SoftAssert();
        // 1. "Product Details"
        softly.assertTrue(opensDetail(quotes, insurer, "Product Details", () -> quotes.pressOn(link)),
            "Tapping \"" + QuotesListingPage.PRODUCT_DETAILS + "\" on " + insurer + "'s quote card did not open its product detail");
        backToListing(quotes, listingRoute);

        // 2. The chevron at the end of the insurer-name row
        Locator name = quotes.text(insurer);
        quotes.bringIntoView(name);
        BoundingBox row = name.boundingBox();
        double chevronX = chevronX(row);
        double chevronY = row.y + row.height / 2;
        log(String.format("Chevron found at (%.0f, %.0f) CSS px", chevronX, chevronY));
        softly.assertTrue(opensDetail(quotes, insurer, "chevron", () -> quotes.tapAt(chevronX, chevronY)),
            "Tapping the chevron on " + insurer + "'s quote card did not open its product detail");
        softly.assertAll();
    }

    /**
     * Taps (via {@code tap}) and returns whether the product detail opened: the app moved to another
     * screen or showed new content (3+ new texts), and it names the insurer.
     */
    private boolean opensDetail(QuotesListingPage quotes, String insurer, String what, Runnable tap) {
        String routeBefore = quotes.route();
        List<String> before = quotes.semanticsTexts();
        tap.run();
        page.waitForTimeout(2_500);
        quotes.waitForImagesToSettle();
        List<String> added = new ArrayList<>(quotes.semanticsTexts());
        added.removeAll(before);
        boolean newScreen = !quotes.route().equals(routeBefore);
        String brand = insurer.split("\\s+")[0];
        boolean namesInsurer = quotes.shows(brand);
        screenshot("product-detail-via-" + what.replaceAll("[^A-Za-z]+", "-").toLowerCase());
        log(String.format("Tapped %s: route %s -> %s, %d new text(s) %s, names %s: %s", what, routeBefore, quotes.route(),
            added.size(), added.size() > 8 ? added.subList(0, 8) + "..." : added, brand, namesInsurer));
        return (newScreen || added.size() >= 3) && namesInsurer;
    }

    /** Back to the quotes listing: browser back for a new screen, else close the sheet. */
    private void backToListing(QuotesListingPage quotes, String listingRoute) {
        if (!quotes.route().equals(listingRoute)) {
            page.goBack();
        } else {
            for (String name : List.of("Close", "Dismiss", "Back")) {
                Locator close = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
                if (close.count() > 0 && close.first().isVisible()) {
                    close.first().click();
                    break;
                }
            }
        }
        page.waitForTimeout(1_500);
        quotes.waitForQuotes(QUOTES_TIMEOUT_MS);
        log("Back on #" + quotes.route());
    }

    /**
     * Where the chevron is (CSS px across): the right-most dark mark in the strip between the end of the
     * insurer's name and the screen edge, at the name's height. Falls back to 30 px from the edge.
     */
    private double chevronX(BoundingBox row) {
        double left = row.x + row.width;
        double width = page.viewportSize().width - left;
        try {
            BufferedImage strip = ImageIO.read(new ByteArrayInputStream(ScreenPixels.capture(page, left, row.y, width, row.height)));
            double scale = strip.getWidth() / width;
            int right = -1;
            int start = -1;
            for (int x = strip.getWidth() - 1; x >= 0; x--) {
                boolean dark = false;
                for (int y = 0; y < strip.getHeight() && !dark; y++) {
                    int rgb = strip.getRGB(x, y);
                    dark = (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) / 3 < 160;
                }
                if (dark) {
                    right = right < 0 ? x : right;
                    start = x;
                } else if (right >= 0) {
                    break;
                }
            }
            if (right >= 0) {
                return left + (start + right) / 2.0 / scale;
            }
        } catch (IOException e) {
            log("Could not read the chevron strip: " + e.getMessage());
        }
        return page.viewportSize().width - 30;
    }
}
