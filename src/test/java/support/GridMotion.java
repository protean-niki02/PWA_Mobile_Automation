package support;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

/**
 * Measures how an animated region (the splash screen's auto-scrolling image grid) moved
 * between two screenshots: the horizontal shift that best lines the second frame up with
 * the first, and how well it lines up.
 *
 * A band (a share of the frame's height) can be measured on its own, for content whose parts
 * move at different speeds, such as grid rows.
 *
 * Frames are compared in greyscale at CSS-pixel size, so shifts and speeds are in CSS px
 * whatever the device's pixel ratio.
 */
public final class GridMotion {

    /** A screenshot of the region and when it was taken (ms, System.nanoTime based). */
    public record Frame(long atMs, byte[] png) {
    }

    /**
     * Movement between two frames. {@code pixels} is the content's horizontal shift (positive =
     * moved right); {@code error} the average grey-level difference after lining the frames up by
     * it; {@code unmovedError} the difference without any shift.
     */
    public record Shift(double pixels, double error, double unmovedError, long elapsedMs) {

        /** CSS px per second. */
        public double speed() {
            return elapsedMs <= 0 ? 0 : pixels * 1000.0 / elapsedMs;
        }

        /**
         * The content clearly moved: a non-zero shift lines the frames up much better than
         * none. Below 1 grey level of difference nothing visible changed.
         */
        public boolean moved() {
            return Math.abs(pixels) >= 0.5 && unmovedError >= 1.0 && error < unmovedError * 0.7;
        }

        @Override
        public String toString() {
            return String.format("shift %.1f px in %d ms (%.1f px/s), match error %.1f, unmoved %.1f",
                pixels, elapsedMs, speed(), error, unmovedError);
        }
    }

    private GridMotion() {
    }

    /** {@link #shift(Frame, Frame, double, int, double, double)} over the whole frame. */
    public static Shift shift(Frame from, Frame to, double cssWidth, int maxShiftPx) {
        return shift(from, to, cssWidth, maxShiftPx, 0, 1);
    }

    /**
     * The horizontal shift, within {@code maxShiftPx} either way (at most half the frame's
     * width), that best lines {@code to} up with {@code from}, refined to a fraction of a pixel.
     * Only the band between
     * {@code topShare} and {@code bottomShare} of the frame's height (0-1) is compared.
     */
    public static Shift shift(Frame from, Frame to, double cssWidth, int maxShiftPx, double topShare, double bottomShare) {
        double[][] a = band(grey(from.png(), cssWidth), topShare, bottomShare);
        double[][] b = band(grey(to.png(), cssWidth), topShare, bottomShare);
        int width = Math.min(a[0].length, b[0].length);
        int limit = Math.min(maxShiftPx, width / 2);
        double[] errors = new double[2 * limit + 1];
        int best = -limit;
        for (int s = -limit; s <= limit; s++) {
            errors[s + limit] = error(a, b, s, width);
            // On a tie the smaller shift wins: identical (e.g. blank) frames mean no movement
            double diff = errors[s + limit] - errors[best + limit];
            if (diff < -1e-9 || (Math.abs(diff) <= 1e-9 && Math.abs(s) < Math.abs(best))) {
                best = s;
            }
        }
        double pixels = best;
        // Parabola through the best shift and its neighbours: sub-pixel position of the minimum
        if (best > -limit && best < limit) {
            double left = errors[best + limit - 1];
            double centre = errors[best + limit];
            double right = errors[best + limit + 1];
            double curve = left - 2 * centre + right;
            if (curve > 0) {
                pixels += 0.5 * (left - right) / curve;
            }
        }
        return new Shift(pixels, errors[best + limit], errors[limit], to.atMs() - from.atMs());
    }

    /** Average grey-level difference (0-255) between two frames, without lining them up. */
    public static double difference(byte[] first, byte[] second, double cssWidth) {
        double[][] a = grey(first, cssWidth);
        double[][] b = grey(second, cssWidth);
        return error(a, b, 0, Math.min(a[0].length, b[0].length));
    }

    /** Average |a(x) - b(x + s)| over the columns both frames share after the shift. */
    private static double error(double[][] a, double[][] b, int s, int width) {
        int rows = Math.min(a.length, b.length);
        double sum = 0;
        long n = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = Math.max(0, -s); x < Math.min(width, width - s); x++) {
                sum += Math.abs(a[y][x] - b[y][x + s]);
                n++;
            }
        }
        return n == 0 ? Double.MAX_VALUE : sum / n;
    }

    private static double[][] band(double[][] grey, double topShare, double bottomShare) {
        int top = (int) Math.floor(grey.length * topShare);
        int bottom = Math.max(top + 1, (int) Math.ceil(grey.length * bottomShare));
        return Arrays.copyOfRange(grey, top, Math.min(grey.length, bottom));
    }

    /** Greyscale pixels, scaled down to one value per CSS pixel. */
    private static double[][] grey(byte[] png, double cssWidth) {
        BufferedImage image = decode(png);
        int width = Math.max(1, (int) Math.round(cssWidth));
        int height = Math.max(1, (int) Math.round(image.getHeight() * cssWidth / image.getWidth()));
        BufferedImage small = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = small.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        double[][] grey = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = small.getRGB(x, y);
                grey[y][x] = 0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF) + 0.114 * (rgb & 0xFF);
            }
        }
        return grey;
    }

    private static BufferedImage decode(byte[] png) {
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
