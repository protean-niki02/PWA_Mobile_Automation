package tests.sprint2.bd2m_683_splash_screen;

import com.google.gson.JsonObject;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitUntilState;
import config.DeviceProfile;
import config.TestConfig;
import org.testng.Assert;
import org.testng.SkipException;
import pages.SplashLandingScreen;
import support.AssetRecorder.Asset;
import support.GridMotion;
import support.GridMotion.Frame;
import support.GridMotion.Shift;
import support.ScreenPixels;
import tests.sprint1.splashscreen.SplashScreenTestBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared steps for BD2M-683 / SC_01 - Validate the "Splash Screen" functionality.
 * Module: Customer Creation & other functionality, requirement US-UCM-01, designed by Sumit.
 *
 * The image grid is drawn on Flutter's canvas, so its movement is measured from screenshots
 * of the grid ({@link GridMotion}): how far each row of photos shifted sideways between two
 * frames. The grid is sampled from its middle 70% of width, clear of the "UAT" / build ribbons.
 *
 * Expected button labels and destinations come from config.properties (splash.*); tests that
 * need one are SKIPPED while it is empty.
 */
public abstract class SplashLandingTestBase extends SplashScreenTestBase {

    /**
     * Least distance the grid is searched for between two frames (CSS px either way). Frames
     * further apart get a wider search, up to half the sampled width (see maxMeasurableShift).
     */
    protected static final int MAX_SHIFT_PX = 80;

    /** Fastest a grid row is expected to scroll (CSS px/s), to size the search for slow captures. */
    private static final double MAX_GRID_SPEED = 40;

    /** Recommended minimum touch target (CSS px): Android Material 48 dp, Apple HIG 44 pt. */
    private static final double ANDROID_MIN_TARGET = 48;
    private static final double IOS_MIN_TARGET = 44;

    /** Share of the screen width sampled for grid movement, centred (the corners carry ribbons). */
    private static final double GRID_SAMPLE_WIDTH = 0.7;

    /**
     * The grid's two rows of photos (CSS px from the top of the screen), leaving out where the
     * tilted rows meet, about 120-150 px down on both phones. The rows scroll at different
     * speeds (build 2.0.4+8 on Android: top 20 px/s, bottom 12 px/s), so each is measured on
     * its own; the bottom row reaches down to the grid's bottom edge.
     */
    private static final double[][] ROW_SPANS = {{10, 120}, {150, Double.MAX_VALUE}};
    protected static final List<String> ROW_NAMES = List.of("top row", "bottom row");

    /** A row with less than this much (CSS px) inside the sampled area is not measured. */
    private static final double MIN_ROW_HEIGHT = 20;

    /** Top of the sampled area (CSS px), clear of nothing but the screen edge. */
    private static final double SAMPLE_TOP = 10;

    /** A grid capture flatter than this (brightness spread) is blank: no photos were captured. */
    private static final double MIN_GRID_SPREAD = 5;
    private static final int BLANK_CAPTURE_TRIES = 3;

    /** Bottom of the sampled area (CSS px), set by the last grid capture. */
    private double sampleBottom;

    /** How each grid row moved between two frames. */
    protected record GridStep(List<Shift> rows) {

        /** Every row moved. */
        public boolean moved() {
            return rows.stream().allMatch(Shift::moved);
        }

        @Override
        public String toString() {
            List<String> parts = new ArrayList<>();
            for (int row = 0; row < rows.size(); row++) {
                parts.add(ROW_NAMES.get(row) + " " + rows.get(row) + (rows.get(row).moved() ? "" : " NOT MOVING"));
            }
            return String.join(" | ", parts);
        }
    }

    /** The page that hides the app while it is "in the background", or the frozen page's session. */
    private Page coverPage;
    private CDPSession frozenSession;

    protected SplashLandingScreen openSplash() {
        SplashLandingScreen screen = new SplashLandingScreen(page).open();
        screenshot("splash-screen");
        return screen;
    }

    // ---- Image grid movement ----

    /**
     * Screenshot of the grid's sample area, stamped with the middle of the capture time. The
     * iOS browser (WebKit) sometimes returns a blank white capture of part of the screen while
     * the app is drawing; such a capture is taken again, up to {@link #BLANK_CAPTURE_TRIES} times.
     */
    protected Frame captureGrid(SplashLandingScreen screen) {
        double width = page.viewportSize().width;
        sampleBottom = SAMPLE_TOP + Math.max(40, screen.gridBottom() - 2 * SAMPLE_TOP);
        return captureUnlessBlank(width * (1 - GRID_SAMPLE_WIDTH) / 2, SAMPLE_TOP,
            width * GRID_SAMPLE_WIDTH, sampleBottom - SAMPLE_TOP);
    }

    /**
     * Screenshot of a region (CSS px), taken again while it comes back blank, up to
     * {@link #BLANK_CAPTURE_TRIES} times. A region that really is blank stays blank.
     */
    protected Frame captureUnlessBlank(double x, double y, double width, double height) {
        Frame frame = null;
        for (int attempt = 1; attempt <= BLANK_CAPTURE_TRIES; attempt++) {
            long start = System.nanoTime();
            byte[] png = ScreenPixels.capture(page, x, y, width, height);
            long end = System.nanoTime();
            frame = new Frame((start + end) / 2 / 1_000_000, png);
            if (ScreenPixels.brightnessSpread(png) >= MIN_GRID_SPREAD) {
                return frame;
            }
            log("Capture " + attempt + " came back blank - taking it again");
            page.waitForTimeout(200);
        }
        return frame;
    }

    /** {@code count} grid frames about {@code intervalMs} apart. */
    protected List<Frame> sampleGrid(SplashLandingScreen screen, int count, long intervalMs) {
        List<Frame> frames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                page.waitForTimeout(intervalMs);
            }
            frames.add(captureGrid(screen));
        }
        return frames;
    }

    /**
     * Number of grid rows measured: both, unless the screen is so short that too little of the
     * bottom row shows (call after a capture).
     */
    protected int rowCount() {
        int rows = 0;
        while (rows < ROW_SPANS.length
            && Math.min(ROW_SPANS[rows][1], sampleBottom) - Math.max(ROW_SPANS[rows][0], SAMPLE_TOP) >= MIN_ROW_HEIGHT) {
            rows++;
        }
        return rows;
    }

    /**
     * Movement of one grid row between two frames, searched as far as the row could have gone
     * in the time between them (a slow screenshot can leave several seconds).
     */
    protected Shift rowShift(Frame from, Frame to, int row) {
        double height = sampleBottom - SAMPLE_TOP;
        double top = (Math.max(ROW_SPANS[row][0], SAMPLE_TOP) - SAMPLE_TOP) / height;
        double bottom = (Math.min(ROW_SPANS[row][1], sampleBottom) - SAMPLE_TOP) / height;
        int reach = (int) Math.ceil(MAX_GRID_SPEED * Math.abs(to.atMs() - from.atMs()) / 1000.0);
        return GridMotion.shift(from, to, gridSampleWidth(), Math.max(MAX_SHIFT_PX, reach), top, bottom);
    }

    /** Largest shift that can be measured between two frames: half the sampled width. */
    protected double maxMeasurableShift() {
        return gridSampleWidth() / 2;
    }

    /** Movement of every measured grid row between two frames. */
    protected GridStep step(Frame from, Frame to) {
        List<Shift> rows = new ArrayList<>();
        for (int row = 0; row < rowCount(); row++) {
            rows.add(rowShift(from, to, row));
        }
        return new GridStep(rows);
    }

    /** Movement between each pair of consecutive frames, logged one per line. */
    protected List<GridStep> steps(List<Frame> frames) {
        List<GridStep> steps = new ArrayList<>();
        for (int i = 1; i < frames.size(); i++) {
            GridStep step = step(frames.get(i - 1), frames.get(i));
            log(String.format("Frame %d -> %d: %s%s", i, i + 1, step, step.moved() ? "" : "  (NOT MOVING)"));
            steps.add(step);
        }
        return steps;
    }

    /** Typical (median) speed of each row over the steps, CSS px/s, signed (negative = right to left). */
    protected static double[] rowSpeeds(List<GridStep> steps) {
        double[] speeds = new double[steps.isEmpty() ? 0 : steps.get(0).rows().size()];
        for (int row = 0; row < speeds.length; row++) {
            int r = row;
            speeds[row] = median(steps.stream().map(s -> s.rows().get(r).speed()).toList());
        }
        return speeds;
    }

    /** Fails unless every grid row is scrolling sideways, and returns each row's typical speed. */
    protected double[] assertGridScrolling(SplashLandingScreen screen, String when) {
        List<GridStep> steps = steps(sampleGrid(screen, 4, 400));
        Assert.assertTrue(steps.stream().allMatch(GridStep::moved),
            "The image grid is not scrolling horizontally " + when + ": " + steps);
        double[] speeds = rowSpeeds(steps);
        log("Grid speed " + when + ": " + describeSpeeds(speeds));
        return speeds;
    }

    protected static String describeSpeeds(double[] speeds) {
        List<String> rows = new ArrayList<>();
        for (int row = 0; row < speeds.length; row++) {
            rows.add(String.format("%s %.1f px/s %s", ROW_NAMES.get(row), Math.abs(speeds[row]),
                speeds[row] < 0 ? "right to left" : "left to right"));
        }
        return String.join(", ", rows);
    }

    /** Evidence: the first frames, side by side, plus the first and last frame as files. */
    protected void saveGridFrames(String step, List<Frame> frames) {
        if (frames.isEmpty()) {
            return;
        }
        List<SheetImage> sheet = new ArrayList<>();
        long start = frames.get(0).atMs();
        int every = Math.max(1, frames.size() / 6);
        for (int i = 0; i < frames.size() && sheet.size() < 6; i += every) {
            sheet.add(new SheetImage(String.format("frame %d\n+%d ms", i + 1, frames.get(i).atMs() - start),
                "frame.png", frames.get(i).png()));
        }
        imageSheet(step, "Image grid frames (" + frames.size() + " captured)", sheet);
        saveEvidence(step + "_first-frame.png", frames.get(0).png());
        saveEvidence(step + "_last-frame.png", frames.get(frames.size() - 1).png());
    }

    protected double gridSampleWidth() {
        return page.viewportSize().width * GRID_SAMPLE_WIDTH;
    }

    // ---- Grid photos ----

    /**
     * A photo of the image grid. The app loads them (person_one.jpeg, person_two_0.jpeg, ...)
     * from the CMS - the configured source, splash.gridImageSource - and also bundles its own
     * copies under {@link #GRID_TILE_PATH} as a fallback.
     */
    protected static boolean isGridPhoto(Asset asset) {
        return asset.fileName().toLowerCase().startsWith("person_");
    }

    /** The configured source of the grid photos (URL prefix); skips the test while it is not set. */
    protected String gridImageSource() {
        return required("splash.gridImageSource", "The image grid's configured source");
    }

    // ---- Background / foreground ----

    /**
     * Makes the page report itself hidden (or visible again) and tells the app, exactly as a
     * phone's browser does when the user switches apps: document.visibilityState / hidden change
     * and a "visibilitychange" event fires. Flutter turns that event into its app lifecycle
     * (hidden / resumed), which is what an app pauses its animations on.
     */
    private static final String SET_PAGE_HIDDEN = """
        hidden => {
            if (!window.__bgOverride) {
                Object.defineProperty(document, 'visibilityState', {configurable: true, get: () => window.__bgHidden ? 'hidden' : 'visible'});
                Object.defineProperty(document, 'hidden', {configurable: true, get: () => !!window.__bgHidden});
                window.__bgOverride = true;
            }
            window.__bgHidden = hidden;
            window.dispatchEvent(new Event(hidden ? 'blur' : 'focus'));
            document.dispatchEvent(new Event('visibilitychange'));
            window.dispatchEvent(new Event(hidden ? 'pagehide' : 'pageshow'));
        }
        """;

    /**
     * Puts the app in the background, like switching to another app on the phone:
     * <ol>
     *   <li>where the browser really hides the page when another tab comes to the front, that is
     *       used as it is;</li>
     *   <li>otherwise the page is made to report itself hidden, with the same event a phone's
     *       browser sends (see SET_PAGE_HIDDEN) - on Android (Chromium) the page is then also
     *       frozen, as Chrome does with background tabs.</li>
     * </ol>
     * Returns how it was done, for the log.
     */
    protected String sendToBackground() {
        coverPage = context.newPage();
        coverPage.bringToFront();
        page.waitForTimeout(500);
        if ("hidden".equals(page.evaluate("() => document.visibilityState"))) {
            return "another tab brought to the front - the app's page is hidden";
        }
        closeCoverPage();
        page.evaluate(SET_PAGE_HIDDEN, true);
        String how = "page reported hidden to the app (visibilitychange), as when the user switches apps";
        if (device.browserEngine().equalsIgnoreCase("chromium")) {
            try {
                frozenSession = context.newCDPSession(page);
                JsonObject frozen = new JsonObject();
                frozen.addProperty("state", "frozen");
                frozenSession.send("Page.setWebLifecycleState", frozen);
                how += ", then frozen as Chrome does with background tabs";
            } catch (RuntimeException e) {
                frozenSession = null;
                log("Could not also freeze the page: " + e.getMessage());
            }
        }
        return how;
    }

    protected void bringToForeground() {
        if (coverPage != null) {
            page.bringToFront();
            closeCoverPage();
        } else {
            if (frozenSession != null) {
                JsonObject active = new JsonObject();
                active.addProperty("state", "active");
                frozenSession.send("Page.setWebLifecycleState", active);
                frozenSession.detach();
                frozenSession = null;
            }
            page.evaluate(SET_PAGE_HIDDEN, false);
        }
        page.waitForFunction("() => document.visibilityState === 'visible'");
    }

    private void closeCoverPage() {
        if (coverPage != null) {
            coverPage.close();
            // Only the app page's recording is evidence
            if (coverPage.video() != null) {
                coverPage.video().delete();
            }
            coverPage = null;
        }
    }

    // ---- Buttons ----

    /** Expected test data from config.properties; skips the test while it is empty. */
    protected String required(String key, String what) {
        String value = TestConfig.optional(key);
        if (value.isEmpty()) {
            throw new SkipException(what + " is not provided - set " + key
                + " in src/test/resources/config.properties (from the FSD / Figma) to run this test case");
        }
        return value;
    }

    /** Fails unless the screen shows at least two action buttons; returns them top to bottom. */
    protected List<Locator> assertActionButtons(SplashLandingScreen screen) {
        List<Locator> buttons = screen.actionButtons();
        List<String> names = buttons.stream().map(SplashLandingScreen::nameOf).toList();
        log("Action buttons (top to bottom): " + names);
        Assert.assertTrue(buttons.size() >= 2,
            "Expected a primary and a secondary action button; found " + buttons.size() + ": " + names);
        return buttons;
    }

    /**
     * Fails unless the screen shows at least one action button; returns them top to bottom. For
     * checks of each button's own qualities - a missing button is SC_01_TC_013's finding.
     */
    protected List<Locator> assertAnyActionButtons(SplashLandingScreen screen) {
        List<Locator> buttons = screen.actionButtons();
        log("Action buttons (top to bottom): " + buttons.stream().map(SplashLandingScreen::nameOf).toList());
        Assert.assertFalse(buttons.isEmpty(), "No action button is displayed on the splash screen");
        return buttons;
    }

    /** Taps the button and returns the route the app then shows, after up to 15s. */
    protected String tapAndWaitForNewRoute(SplashLandingScreen screen, Locator button) {
        String before = screen.route();
        screen.pressOn(button);
        try {
            page.waitForURL(url -> !SplashLandingScreen.routeOf(url).equals(before),
                new Page.WaitForURLOptions().setTimeout(15_000));
        } catch (com.microsoft.playwright.TimeoutError stayed) {
            log("Route did not change within 15s");
        }
        screen.waitForImagesToSettle();
        return screen.route();
    }

    /**
     * The button set in config.properties under {@code labelKey} when the screen has it,
     * otherwise the action button at {@code position} (0 = top-most).
     */
    protected Locator configuredOrNthButton(SplashLandingScreen screen, String labelKey, int position) {
        String label = TestConfig.optional(labelKey);
        if (!label.isEmpty() && screen.button(label).count() > 0) {
            return screen.button(label);
        }
        List<Locator> buttons = screen.actionButtons();
        Assert.assertTrue(buttons.size() > position, "Expected at least " + (position + 1)
            + " action button(s) on the splash screen; found " + buttons.size());
        return buttons.get(position);
    }

    protected double minTouchTarget() {
        return device == DeviceProfile.IOS ? IOS_MIN_TARGET : ANDROID_MIN_TARGET;
    }

    // ---- Device sizes ----

    /**
     * Supported screen sizes of this platform (CSS px): small, common and large phones as the
     * browser's viewport (the screen minus the browser's own bars), plus the target device's
     * full screen, as the installed PWA shows it - Samsung Galaxy S24 Ultra 384 x 832 (default
     * FHD+ resolution), iPhone 17 Pro 402 x 874.
     */
    protected List<int[]> supportedSizes() {
        return device == DeviceProfile.IOS
            ? List.of(new int[] {375, 548}, new int[] {393, 659}, new int[] {430, 746}, targetDeviceSize())
            : List.of(new int[] {360, 640}, new int[] {393, 786}, new int[] {412, 839}, new int[] {412, 915},
                targetDeviceSize());
    }

    /**
     * Screen size (CSS px) of this platform's target device, as the installed PWA fills it:
     * Samsung Galaxy S24 Ultra 384 x 832 (default FHD+ resolution), iPhone 17 Pro 402 x 874.
     */
    protected int[] targetDeviceSize() {
        return device == DeviceProfile.IOS ? new int[] {402, 874} : new int[] {384, 832};
    }

    protected String targetDeviceName() {
        return device == DeviceProfile.IOS ? "iPhone 17 Pro" : "Samsung Galaxy S24 Ultra";
    }

    /** Shows the screen at the target device's size. */
    protected void resizeToTargetDevice(SplashLandingScreen screen) {
        int[] size = targetDeviceSize();
        resizeTo(screen, size[0], size[1]);
        log("Screen size: " + targetDeviceName() + ", " + size[0] + " x " + size[1]);
    }

    /**
     * Safe area the content must stay inside (CSS px): {top, bottom} insets for the status bar /
     * notch and the gesture bar / home indicator. Browser emulation has no notch, so these are
     * the insets of the platform's notched phones.
     */
    protected double[] safeAreaInsets() {
        return device == DeviceProfile.IOS ? new double[] {47, 34} : new double[] {24, 16};
    }

    /**
     * Changes the screen size and waits for the app to lay itself out again.
     * <ul>
     *   <li>Android (Chromium): the running app re-lays itself out. A resize can drop Flutter's
     *       accessibility tree, which every check reads, so it is switched back on if so.</li>
     *   <li>iOS (Playwright's WebKit): the app stops drawing after any resize - only the loading
     *       logo shows, while the accessibility tree lives on. A phone never resizes like this,
     *       so the app is restarted at the new size instead.</li>
     * </ul>
     * Either way the app must then be drawn, or the test fails: a check on a blank screen means nothing.
     */
    protected void resizeTo(SplashLandingScreen screen, int width, int height) {
        page.setViewportSize(width, height);
        if (device.browserEngine().equalsIgnoreCase("webkit")) {
            page.reload(new Page.ReloadOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            screen.enableSemantics();
            screen.waitForLanding();
        } else {
            page.waitForTimeout(1_500);
            if (screen.welcomeHeading().count() == 0 && page.locator("flt-semantics-placeholder").count() > 0) {
                log("Accessibility tree dropped after resizing - switching it on again");
                screen.enableSemantics();
            }
        }
        waitUntilShown(() -> screen.welcomeHeading().count() > 0, 10_000);
        screen.scrollToTop();
        assertAppDrawn(screen, width + " x " + height);
    }

    /** Fails unless the app's image grid is actually drawn (not a blank or loading screen). */
    protected void assertAppDrawn(SplashLandingScreen screen, String where) {
        byte[] grid = captureUnlessBlank(0, 0, page.viewportSize().width, Math.max(40, screen.gridBottom())).png();
        Assert.assertTrue(ScreenPixels.brightnessSpread(grid) >= MIN_GRID_SPREAD, "The app is not drawn at "
            + where + " - the screen is blank or still loading, so nothing on it can be checked");
    }

    /**
     * Waits up to {@code timeoutMs} for something to be shown, checking every half second,
     * e.g. text on a later slide of the screen's carousel.
     */
    protected boolean waitUntilShown(java.util.function.BooleanSupplier shown, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (!shown.getAsBoolean()) {
            if (System.currentTimeMillis() > end) {
                return false;
            }
            page.waitForTimeout(500);
        }
        return true;
    }

    // ---- Helpers ----

    protected static BoundingBox box(Locator locator) {
        return locator.count() > 0 && locator.first().isVisible() ? locator.first().boundingBox() : null;
    }

    protected static double median(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Double> sorted = values.stream().sorted().toList();
        int mid = sorted.size() / 2;
        return sorted.size() % 2 == 1 ? sorted.get(mid) : (sorted.get(mid - 1) + sorted.get(mid)) / 2;
    }
}
