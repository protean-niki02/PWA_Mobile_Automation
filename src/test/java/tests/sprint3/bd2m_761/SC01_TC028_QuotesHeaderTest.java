package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.ScreenPixels;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * SC_01_TC_028 - Verify that the header displays the back arrow, "Product Listing" title and Close (X) icon.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * The quotes listing header is a dark bar with a back arrow at its left, the title, and a Close (X)
 * at its right. Once the quotes are loaded:
 * <ul>
 *   <li>the title must read "Insurer Listing" - the Figma design renamed the TC's "Product Listing"
 *       to "Insurer Listing";</li>
 *   <li>a back arrow must be shown left of the title - a "Back" button, else the white button with a
 *       dark arrow found by its pixels;</li>
 *   <li>a Close (X) must be shown right of the title - a "Close" button, else the white X found by
 *       its pixels.</li>
 * </ul>
 * The icons have no labels on this app, so their spots on the header row are checked; pictures of the
 * header and of each icon spot are saved.
 */
public class SC01_TC028_QuotesHeaderTest extends QuotesListingTestBase {

    /** The title per the current Figma design, which renamed "Product Listing" to "Insurer Listing". */
    private static final String EXPECTED_TITLE = QuotesListingPage.TITLE;

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_028: Verify that the header displays the back arrow, \"Product Listing\" title and Close (X) icon.")
    public void verifyHeaderBackTitleAndClose() {
        QuotesListingPage quotes = openLoadedQuotes();
        Locator title = quotes.headerTitle();
        String shownTitle = title.count() > 0 ? title.innerText().trim() : "";
        BoundingBox titleBox = title.count() > 0 ? title.boundingBox() : null;
        double rowY = quotes.headerCentreY();
        double width = page.viewportSize().width;
        double titleLeft = titleBox != null ? titleBox.x : 70;
        saveEvidence("header.png", ScreenPixels.capture(page, 0, Math.max(0, rowY - 45), width, 90));

        boolean back = visible(quotes.backButton())
            || iconOnWhiteButton(capture("header-back-arrow.png", 4, rowY - 20, Math.max(20, titleLeft - 10), 40));
        boolean close = visible(quotes.closeButton())
            || whiteMark(capture("header-close.png", width - 75, rowY - 20, 55, 40));
        log(String.format("Header: back arrow %s | title \"%s\" | Close (X) %s", back ? "shown" : "NOT shown", shownTitle,
            close ? "shown" : "NOT shown"));
        screenshot("quotes-header");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(back, "The header does not display the back arrow at its left");
        softly.assertEquals(shownTitle, EXPECTED_TITLE, "The header title is \"" + shownTitle + "\", expected \"" + EXPECTED_TITLE + "\"");
        softly.assertTrue(close, "The header does not display the Close (X) icon at its right");
        softly.assertAll();
    }

    private static boolean visible(Locator button) {
        return button.count() > 0 && button.first().isVisible();
    }

    /** Captures part of the screen (CSS px), saves it as evidence and returns it decoded. */
    private BufferedImage capture(String name, double x, double y, double w, double h) {
        byte[] png = ScreenPixels.capture(page, x, Math.max(0, y), w, h);
        saveEvidence(name, png);
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the header capture " + name, e);
        }
    }

    /** A white button (10%+ of the spot is near-white) with a dark mark inside it - the back arrow's button. */
    private static boolean iconOnWhiteButton(BufferedImage spot) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = -1, maxY = -1, white = 0;
        for (int y = 0; y < spot.getHeight(); y++) {
            for (int x = 0; x < spot.getWidth(); x++) {
                if (brightness(spot.getRGB(x, y)) > 230) {
                    white++;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (white < spot.getWidth() * spot.getHeight() / 10) {
            return false;
        }
        int dark = 0;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                dark += brightness(spot.getRGB(x, y)) < 120 ? 1 : 0;
            }
        }
        return dark >= (maxX - minX + 1) * (maxY - minY + 1) / 100;
    }

    /** A white mark on the dark header (0.5% - 40% of the spot near-white) - the X's strokes. */
    private static boolean whiteMark(BufferedImage spot) {
        int white = 0;
        for (int y = 0; y < spot.getHeight(); y++) {
            for (int x = 0; x < spot.getWidth(); x++) {
                white += brightness(spot.getRGB(x, y)) > 220 ? 1 : 0;
            }
        }
        double share = white / (double) (spot.getWidth() * spot.getHeight());
        return share >= 0.005 && share <= 0.4;
    }

    private static int brightness(int rgb) {
        return (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) / 3;
    }
}
