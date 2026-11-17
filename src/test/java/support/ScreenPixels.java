package support;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.Clip;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Looks at the pixels of part of the screen, for things Flutter draws but does not label,
 * such as a green completion tick or highlighted words in a title.
 */
public final class ScreenPixels {

    private ScreenPixels() {
    }

    /** Screenshot of one region of the page (CSS pixels). */
    public static byte[] capture(Page page, double x, double y, double width, double height) {
        var viewport = page.viewportSize();
        double left = Math.max(0, x);
        double top = Math.max(0, y);
        double w = Math.min(width, viewport.width - left);
        double h = Math.min(height, viewport.height - top);
        return page.screenshot(new Page.ScreenshotOptions().setClip(new Clip(left, top, w, h)));
    }

    /** A full-width strip of the screen around an element: the row it sits in. */
    public static byte[] captureRow(Page page, BoundingBox box, double padding) {
        return capture(page, 0, box.y - padding, page.viewportSize().width, box.height + 2 * padding);
    }

    /**
     * Pixels that are clearly green (like a completion tick): green channel strong and well
     * above red and blue. Anti-aliased edges and greys don't count.
     */
    public static int countGreenPixels(byte[] png) {
        BufferedImage image = decode(png);
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (g > 100 && g > r + 50 && g > b + 20) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Width in image pixels of the widest solid green shape: the longest run of side-by-side
     * columns that are each at least 20% green. A tick icon gives a wide run; a thin green
     * border line passing through the region gives only a few pixels.
     */
    public static int widestGreenShape(byte[] png) {
        BufferedImage image = decode(png);
        int longest = 0;
        int current = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            int green = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (g > 100 && g > r + 50 && g > b + 20) {
                    green++;
                }
            }
            current = green * 5 >= image.getHeight() ? current + 1 : 0;
            longest = Math.max(longest, current);
        }
        return longest;
    }

    /**
     * A modal dialog: a white box across the screen with the darkened page (modal barrier)
     * above it. Scans a column near the dialog's left edge (12% of the width, clear of its icon
     * and text) for the tallest white run whose pixels just above are dark. Returns
     * {top, bottom} in image pixels, or null when there is none.
     */
    public static int[] centredDialogRows(byte[] png) {
        BufferedImage image = decode(png);
        int x = (int) (image.getWidth() * 0.12);
        int bestTop = -1;
        int bestBottom = -1;
        int runStart = -1;
        for (int y = 0; y <= image.getHeight(); y++) {
            boolean white = y < image.getHeight() && brightness(image.getRGB(x, y)) > 240;
            if (white && runStart < 0) {
                runStart = y;
            } else if (!white && runStart >= 0) {
                if (y - runStart > bestBottom - bestTop && isDarkAbove(image, x, runStart)) {
                    bestTop = runStart;
                    bestBottom = y;
                }
                runStart = -1;
            }
        }
        int height = bestBottom - bestTop;
        return height >= image.getHeight() * 0.15 && height <= image.getHeight() * 0.7
            ? new int[] {bestTop, bestBottom} : null;
    }

    /** Average brightness of the 20 pixels above y (the modal barrier is clearly darker than the page). */
    private static boolean isDarkAbove(BufferedImage image, int x, int y) {
        if (y < 25) {
            return false;
        }
        long sum = 0;
        for (int dy = 5; dy < 25; dy++) {
            sum += brightness(image.getRGB(x, y - dy));
        }
        return sum / 20 < 170;
    }

    private static int brightness(int rgb) {
        return (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
    }

    /**
     * True when the region's average colour is a neutral grey (a greyed-out button) rather than a
     * colour such as the app's navy blue: red, green and blue within 20 of each other.
     */
    public static boolean isGrey(byte[] png) {
        BufferedImage image = decode(png);
        long r = 0;
        long g = 0;
        long b = 0;
        long n = (long) image.getWidth() * image.getHeight();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                r += (rgb >> 16) & 0xFF;
                g += (rgb >> 8) & 0xFF;
                b += rgb & 0xFF;
            }
        }
        r /= n;
        g /= n;
        b /= n;
        return Math.abs(r - g) < 20 && Math.abs(g - b) < 20 && Math.abs(r - b) < 20;
    }

    /** Share (0-1) of near-white pixels in the region, e.g. a white icon or button on a dark header. */
    public static double whiteShare(byte[] png) {
        BufferedImage image = decode(png);
        long white = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (brightness(image.getRGB(x, y)) > 230) {
                    white++;
                }
            }
        }
        return (double) white / ((long) image.getWidth() * image.getHeight());
    }

    /** Average brightness (0-255) of the region, e.g. to see a button fade while it is busy. */
    public static double averageBrightness(byte[] png) {
        BufferedImage image = decode(png);
        long sum = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                sum += brightness(image.getRGB(x, y));
            }
        }
        return (double) sum / ((long) image.getWidth() * image.getHeight());
    }

    /**
     * Standard deviation of brightness in the region: near 0 for an empty, flat area; clearly
     * higher where shapes, edges or pictures are drawn.
     */
    public static double brightnessSpread(byte[] png) {
        BufferedImage image = decode(png);
        double mean = averageBrightness(png);
        double sum = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                double d = brightness(image.getRGB(x, y)) - mean;
                sum += d * d;
            }
        }
        return Math.sqrt(sum / ((long) image.getWidth() * image.getHeight()));
    }

    /** Image pixels per CSS pixel of a screenshot (the device scale factor). */
    public static double scale(byte[] png, double cssWidth) {
        return decode(png).getWidth() / cssWidth;
    }

    /**
     * Distinct text colours in a region: dark or saturated pixels, grouped into coarse colour
     * buckets so anti-aliasing doesn't add colours. Plain text gives 1; text with a
     * highlighted part in another colour gives 2 or more.
     */
    public static int countTextColours(byte[] png) {
        return countColours(png, 0.10);
    }

    /**
     * Distinct colours in a region, as in {@link #countTextColours(byte[])}, counting each colour
     * that covers at least {@code minShare} (0-1) of the non-background pixels. A low share
     * picks up the smaller colours of a picture dominated by one large shape.
     */
    public static int countColours(byte[] png, double minShare) {
        BufferedImage image = decode(png);
        Set<Integer> buckets = new HashSet<>();
        int[] counts = new int[4096];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                boolean background = max > 200 && max - min < 40;
                if (!background) {
                    counts[(r >> 4) << 8 | (g >> 4) << 4 | (b >> 4)]++;
                }
            }
        }
        int total = 0;
        for (int c : counts) {
            total += c;
        }
        for (int bucket = 0; bucket < counts.length; bucket++) {
            if (total > 0 && counts[bucket] >= minShare * total) {
                buckets.add(bucket);
            }
        }
        return buckets.size();
    }

    private static BufferedImage decode(byte[] png) {
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
