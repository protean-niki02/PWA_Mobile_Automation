package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SC_01_TC_031 - Verify that the screen displays the title "Here are your quotes sorted by price" with "sorted by price" highlighted.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * Once the quotes are loaded, the title under the header must:
 * <ul>
 *   <li>read "Here are your quotes sorted by price" (a quote count before it, e.g. "2 Here are ...",
 *       is logged);</li>
 *   <li>show "sorted by price" highlighted: drawn in an accent (coloured) ink, different from the
 *       ink of "Here are your quotes".</li>
 * </ul>
 * The title is one piece of text, so the two parts are told apart by where they are drawn: the ink at
 * the start of the title ("Here are ...") and at its end ("... by price") are sampled and their
 * colours compared. Both colours and a picture of the title are saved.
 */
public class SC01_TC031_QuotesScreenTitleTest extends QuotesListingTestBase {

    private static final Pattern TITLE = Pattern.compile("^\\s*(\\d+)?\\s*" + QuotesListingPage.SCREEN_TITLE.replace(" ", "\\s+") + "\\s*$",
        Pattern.CASE_INSENSITIVE);
    /** An ink is an accent (highlight) colour when its channels differ this much: orange, blue, green ... not grey or black. */
    private static final int MIN_ACCENT_SATURATION = 60;

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_031: Verify that the screen displays the title \"Here are your quotes sorted by price\" with \"sorted by price\" highlighted.")
    public void verifyTitleWithHighlight() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        Locator title = quotes.text(QuotesListingPage.SCREEN_TITLE);
        Assert.assertTrue(title.count() > 0, "The title \"" + QuotesListingPage.SCREEN_TITLE + "\" is not shown. Screen text: "
            + quotes.semanticsText());
        String shown = title.innerText().replaceAll("\\s+", " ").trim();
        Matcher m = TITLE.matcher(shown);
        BoundingBox box = title.boundingBox();
        byte[] png = ScreenPixels.capture(page, box.x, box.y, box.width, box.height);
        saveEvidence("title.png", png);

        int[] head = inkColour(decode(png), true);
        int[] tail = inkColour(decode(png), false);
        boolean tailAccent = tail != null && saturation(tail) >= MIN_ACCENT_SATURATION;
        boolean differs = head != null && tail != null && distance(head, tail) >= 60;
        log(String.format("Title \"%s\"%s; \"Here are your quotes\" ink %s%s, \"sorted by price\" ink %s%s", shown,
            m.matches() && m.group(1) != null ? " (quote count " + m.group(1) + " before it)" : "",
            hex(head), head != null && saturation(head) >= MIN_ACCENT_SATURATION ? " (accent)" : " (neutral)",
            hex(tail), tailAccent ? " (accent)" : " (neutral)"));
        screenshot("quotes-title");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(m.matches(), "The title reads \"" + shown + "\", expected \"" + QuotesListingPage.SCREEN_TITLE + "\"");
        softly.assertTrue(tailAccent && differs, String.format(
            "\"%s\" is not highlighted: it is drawn in %s while \"Here are your quotes\" is %s%s", QuotesListingPage.TITLE_HIGHLIGHT,
            hex(tail), hex(head), head != null && saturation(head) >= MIN_ACCENT_SATURATION && !tailAccent
                ? " - the highlight is on \"Here are your quotes\" instead" : ""));
        softly.assertAll();
    }

    /**
     * The average ink colour (pixels clearly darker or more coloured than the white background) at
     * the start of the title's first line ({@code start}) or the end of its last line.
     */
    private static int[] inkColour(BufferedImage img, boolean start) {
        int top = -1, bottom = -1, left = Integer.MAX_VALUE, right = -1;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (isInk(img.getRGB(x, y))) {
                    top = top < 0 ? y : top;
                    bottom = y;
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                }
            }
        }
        if (right < 0) {
            return null;
        }
        int lineHeight = Math.max(1, Math.min(bottom - top + 1, (int) (img.getHeight() * 0.6)));
        int y0 = start ? top : bottom - lineHeight + 1;
        int span = (right - left + 1) / 5;
        int x0 = start ? left + span / 4 : right - span;
        long r = 0, g = 0, b = 0, n = 0;
        for (int y = Math.max(0, y0); y < Math.min(img.getHeight(), y0 + lineHeight); y++) {
            for (int x = x0; x < x0 + span; x++) {
                int rgb = img.getRGB(x, y);
                if (isInk(rgb)) {
                    r += (rgb >> 16) & 0xff;
                    g += (rgb >> 8) & 0xff;
                    b += rgb & 0xff;
                    n++;
                }
            }
        }
        return n == 0 ? null : new int[] {(int) (r / n), (int) (g / n), (int) (b / n)};
    }

    private static boolean isInk(int rgb) {
        int r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
        return (r + g + b) / 3 < 170 || Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) > 90;
    }

    private static int saturation(int[] c) {
        return Math.max(c[0], Math.max(c[1], c[2])) - Math.min(c[0], Math.min(c[1], c[2]));
    }

    private static int distance(int[] a, int[] b) {
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]) + Math.abs(a[2] - b[2]);
    }

    private static String hex(int[] c) {
        return c == null ? "none" : String.format("#%02X%02X%02X", c[0], c[1], c[2]);
    }

    private static BufferedImage decode(byte[] png) {
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the title capture", e);
        }
    }
}
