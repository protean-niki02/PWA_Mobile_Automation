package support;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Compares an approved design image with what the app serves.
 *
 * Raster images (PNG/JPEG/GIF) are compared pixel by pixel, so a re-export of the same
 * artwork still matches. Formats Java can't decode (SVG, WebP) must match byte for byte.
 */
public final class ImageCompare {

    /** A pixel counts as different when any colour channel differs by more than this (0-255). */
    private static final int CHANNEL_TOLERANCE = 16;

    /**
     * @param matches    true when the share of different pixels is within the allowed ratio
     * @param diffRatio  share of pixels that differ, 0.0 - 1.0
     * @param detail     human-readable explanation for reports
     */
    public record Result(boolean matches, double diffRatio, String detail) {
    }

    private ImageCompare() {
    }

    public static Result compare(byte[] approved, byte[] actual, double maxDiffRatio) {
        BufferedImage expectedImage = decode(approved);
        BufferedImage actualImage = decode(actual);
        if (expectedImage == null || actualImage == null) {
            boolean same = Arrays.equals(approved, actual);
            return new Result(same, same ? 0 : 1, same
                ? "identical file (compared byte for byte)"
                : "files differ (compared byte for byte; format can't be decoded for a pixel comparison)");
        }
        if (expectedImage.getWidth() != actualImage.getWidth() || expectedImage.getHeight() != actualImage.getHeight()) {
            return new Result(false, 1, "size differs: approved " + size(expectedImage) + ", app " + size(actualImage));
        }

        long different = 0;
        for (int y = 0; y < expectedImage.getHeight(); y++) {
            for (int x = 0; x < expectedImage.getWidth(); x++) {
                if (!similar(expectedImage.getRGB(x, y), actualImage.getRGB(x, y))) {
                    different++;
                }
            }
        }
        double ratio = (double) different / ((long) expectedImage.getWidth() * expectedImage.getHeight());
        return new Result(ratio <= maxDiffRatio, ratio,
            String.format("%.2f%% of pixels differ (allowed %.2f%%)", ratio * 100, maxDiffRatio * 100));
    }

    /**
     * A picture of where two same-sized images differ: the approved image faded, with every
     * differing pixel painted red. Null when they can't be decoded or differ in size.
     */
    public static byte[] diffImage(byte[] approved, byte[] actual) {
        BufferedImage expectedImage = decode(approved);
        BufferedImage actualImage = decode(actual);
        if (expectedImage == null || actualImage == null
            || expectedImage.getWidth() != actualImage.getWidth() || expectedImage.getHeight() != actualImage.getHeight()) {
            return null;
        }
        BufferedImage diff = new BufferedImage(expectedImage.getWidth(), expectedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < diff.getHeight(); y++) {
            for (int x = 0; x < diff.getWidth(); x++) {
                int rgb = expectedImage.getRGB(x, y);
                if (!similar(rgb, actualImage.getRGB(x, y))) {
                    diff.setRGB(x, y, 0xFF0000);
                } else {
                    int grey = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
                    int faded = 200 + grey * 55 / 255;
                    diff.setRGB(x, y, faded << 16 | faded << 8 | faded);
                }
            }
        }
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            ImageIO.write(diff, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    /** True when the bytes are an image a browser can show: a decodable raster image or an SVG document. */
    public static boolean isRenderableImage(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        if (decode(bytes) != null) {
            return true;
        }
        String head = new String(bytes, 0, Math.min(bytes.length, 512), StandardCharsets.UTF_8);
        return head.contains("<svg");
    }

    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static BufferedImage decode(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static boolean similar(int argb1, int argb2) {
        for (int shift = 0; shift <= 24; shift += 8) {
            if (Math.abs(((argb1 >> shift) & 0xFF) - ((argb2 >> shift) & 0xFF)) > CHANNEL_TOLERANCE) {
                return false;
            }
        }
        return true;
    }

    private static String size(BufferedImage image) {
        return image.getWidth() + "x" + image.getHeight();
    }
}
