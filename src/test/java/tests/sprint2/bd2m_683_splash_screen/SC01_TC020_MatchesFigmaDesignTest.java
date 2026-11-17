package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.ApprovedDesign;
import support.ImageCompare;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SC_01_TC_020 - Verify that the splash screen matches the approved Figma design in terms of spacing, fonts, colours and button styles.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Test data:     Approved Figma design
 * Status:        In Progress
 *
 * Compares the screen with the approved Figma frame of the target device, pixel by pixel, which
 * covers spacing, fonts, colours and button styles together:
 * <ul>
 *   <li>the app is shown at the target device's size - Galaxy S24 Ultra 384 px wide, iPhone 17 Pro
 *       402 px wide - with the height and scale taken from the Figma export (1x, 2x or 3x);</li>
 *   <li>the auto-scrolling image grid never looks the same twice, so only the screen below it
 *       is compared;</li>
 *   <li>the middle of the screen is a carousel, so each slide is compared in turn and the
 *       closest one counts;</li>
 *   <li>up to 5% of pixels may differ, for font smoothing between Figma and a browser.</li>
 * </ul>
 * Evidence: approved and app images side by side, and a difference picture with every differing
 * pixel in red.
 *
 * Approved files go in src/test/resources/approved-design/splash-screen-bd2m-683/, one per
 * platform with the platform in the name: splash-screen_android.png (Galaxy S24 Ultra frame,
 * 384 px wide at 1x) and splash-screen_ios.png (iPhone 17 Pro frame, 402 px wide at 1x). Skipped
 * while there is none for the platform.
 */
public class SC01_TC020_MatchesFigmaDesignTest extends SplashLandingTestBase {

    private static final String DESIGN_AREA = "splash-screen-bd2m-683";

    /** Share of pixels allowed to differ: font smoothing differs between Figma and a browser. */
    private static final double MAX_DESIGN_DIFF = 0.05;

    private static final int MAX_SLIDES = 4;
    private static final double SLIDE_WAIT_MS = 15_000;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_020: Verify that the splash screen matches the approved Figma design in terms of spacing, fonts, colours and button styles.")
    public void verifySplashScreenMatchesFigmaDesign() {
        String platform = device.name().toLowerCase();
        Map.Entry<String, byte[]> approved = ApprovedDesign.load(DESIGN_AREA).entrySet().stream()
            .filter(file -> file.getKey().toLowerCase().contains(platform)).findFirst()
            .orElseThrow(() -> new SkipException("No approved " + device.label() + " design in "
                + ApprovedDesign.ROOT.resolve(DESIGN_AREA).toAbsolutePath() + " - add one with '" + platform + "' in its name"));
        BufferedImage design = decode(approved.getValue());
        int targetWidth = targetDeviceSize()[0];
        double scale = (double) design.getWidth() / targetWidth;
        int targetHeight = (int) Math.round(design.getHeight() / scale);
        log(String.format("Approved design %s: %d x %d px = %d x %d at %.2fx", approved.getKey(),
            design.getWidth(), design.getHeight(), targetWidth, targetHeight, scale));

        SplashLandingScreen screen = openSplash();
        resizeTo(screen, targetWidth, targetHeight);
        int top = (int) Math.round(screen.gridBottom() * scale);
        byte[] designBelowGrid = png(design.getSubimage(0, top, design.getWidth(), design.getHeight() - top));

        ImageCompare.Result best = null;
        byte[] bestApp = null;
        int bestSlide = 0;
        List<String> results = new ArrayList<>();
        String slideText = screen.semanticsText();
        for (int slide = 1; slide <= MAX_SLIDES; slide++) {
            BufferedImage app = scaleTo(decode(page.screenshot()), design.getWidth(), design.getHeight());
            byte[] appBelowGrid = png(app.getSubimage(0, top, app.getWidth(), app.getHeight() - top));
            ImageCompare.Result result = ImageCompare.compare(designBelowGrid, appBelowGrid, MAX_DESIGN_DIFF);
            results.add("slide " + slide + ": " + result.detail());
            log("Slide " + slide + " vs " + approved.getKey() + ": " + result.detail());
            if (best == null || result.diffRatio() < best.diffRatio()) {
                best = result;
                bestApp = appBelowGrid;
                bestSlide = slide;
            }
            if (result.matches() || slide == MAX_SLIDES || !screen.waitForNextSlide(slideText, SLIDE_WAIT_MS)) {
                break;
            }
            page.waitForTimeout(1_200);
            slideText = screen.semanticsText();
        }

        saveEvidence("approved-design.png", designBelowGrid);
        saveEvidence("app-closest-slide.png", bestApp);
        byte[] diff = ImageCompare.diffImage(designBelowGrid, bestApp);
        List<SheetImage> sheet = new ArrayList<>(List.of(
            new SheetImage("APPROVED\n" + approved.getKey(), "approved.png", designBelowGrid),
            new SheetImage("APP\nslide " + bestSlide, "app.png", bestApp)));
        if (diff != null) {
            saveEvidence("difference.png", diff);
            sheet.add(new SheetImage("DIFFERENCE\nred = differs", "diff.png", diff));
        }
        imageSheet("approved-vs-app", "Splash screen below the image grid - approved Figma design vs app", sheet);

        Assert.assertTrue(best.matches(), "The splash screen does not match the approved Figma design "
            + approved.getKey() + " - closest was slide " + bestSlide + ": " + best.detail() + " (all: " + results + ")");
    }

    private static BufferedImage scaleTo(BufferedImage image, int width, int height) {
        if (image.getWidth() == width && image.getHeight() == height) {
            return image;
        }
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    private static BufferedImage decode(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] png(BufferedImage image) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
