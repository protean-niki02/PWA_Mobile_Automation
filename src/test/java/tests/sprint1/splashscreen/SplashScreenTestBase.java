package tests.sprint1.splashscreen;

import base.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import config.DeviceProfile;
import config.TestConfig;
import org.testng.asserts.SoftAssert;
import pages.FlutterPage;
import pages.SplashScreen;
import support.AssetRecorder;
import support.AssetRecorder.Asset;
import support.ImageCompare;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared checks for SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Module: Customer Creation & other functionality, requirement US-UCM-01.
 *
 * Runs on the mobile profile, as only mobile browsers get the Flutter PWA.
 */
public abstract class SplashScreenTestBase extends BaseTest {

    /** Share of pixels allowed to differ from the approved design (export noise). */
    protected static final double MAX_PIXEL_DIFF = 0.01;

    /** Where the onboarding / splash / login grid tile photos are served from. */
    protected static final String GRID_TILE_PATH = "/onboarding/grid_image/";

    protected static final String BRAND_NAME = "Bima Sugam";

    /** Logo files of the Bima Sugam brand in the app. */
    protected static final List<String> BRAND_LOGO_FILES =
        List.of("bima_sugam_logo.png", "bima_sugam_logo.svg", "bsif_logo_blue.png");

    @Override
    protected DeviceProfile defaultDevice() {
        return DeviceProfile.ANDROID;
    }

    /** One image on an evidence sheet. */
    public record SheetImage(String caption, String fileName, byte[] bytes) {
    }

    /**
     * Evidence: saves one screenshot showing the given images side by side with captions,
     * e.g. every grid tile, or approved vs app pairs.
     */
    protected void imageSheet(String step, String heading, List<SheetImage> images) {
        StringBuilder html = new StringBuilder("<body style='font-family:sans-serif;margin:0'>"
            + "<div id='sheet' style='display:inline-block;padding:16px;background:#fff;max-width:1100px'>"
            + "<h3>" + escape(heading) + "</h3>"
            + "<div style='display:flex;flex-wrap:wrap;gap:16px'>");
        for (SheetImage image : images) {
            html.append("<figure style='margin:0;text-align:center;font-size:12px;width:140px'>")
                .append("<img style='width:128px;height:128px;object-fit:contain;border:1px solid #ccc' src='data:")
                .append(mimeType(image.fileName())).append(";base64,")
                .append(Base64.getEncoder().encodeToString(image.bytes())).append("'>")
                .append("<figcaption>").append(escape(image.caption()).replace("\n", "<br>"))
                .append("</figcaption></figure>");
        }
        html.append("</div></div></body>");

        Page sheet = context.newPage();
        try {
            sheet.setContent(html.toString());
            Path file = nextStepFile(step);
            sheet.locator("#sheet").screenshot(new Locator.ScreenshotOptions().setPath(file));
            log("Screenshot: " + file.toAbsolutePath());
        } finally {
            sheet.close();
            // Only the app page's recording is evidence
            if (sheet.video() != null) {
                sheet.video().delete();
            }
        }
    }

    /**
     * Checks every image downloaded successfully and is a real, displayable image.
     * Saves each one as evidence and puts them all on one sheet.
     */
    protected void assertImagesLoaded(SoftAssert softly, List<Asset> assets, String what) {
        Map<String, Asset> byUrl = new LinkedHashMap<>();
        assets.forEach(asset -> byUrl.putIfAbsent(asset.url(), asset));

        List<SheetImage> sheet = new ArrayList<>();
        for (Asset asset : byUrl.values()) {
            String label = asset.fileName() + " (" + (asset.route().isEmpty() ? "launch" : asset.route()) + ")";
            softly.assertTrue(asset.ok(), label + " failed to load: HTTP " + asset.status());
            if (!asset.ok()) {
                continue;
            }
            byte[] body = asset.body();
            softly.assertTrue(ImageCompare.isRenderableImage(body),
                label + " is not a displayable image (" + asset.contentType() + ")");
            saveEvidence(what + "_" + asset.fileName(), body);
            sheet.add(new SheetImage(asset.fileName(), asset.fileName(), body));
        }
        log(what + ": " + byUrl.size() + " image(s) loaded");
        if (!sheet.isEmpty()) {
            imageSheet(what, what + " - " + sheet.size() + " image(s) loaded from " + TestConfig.baseUrl(), sheet);
        }
    }

    /**
     * Compares every approved design file with the app's file of the same name and saves
     * an approved-vs-app evidence sheet. Fails for each file that is missing or differs.
     */
    protected void assertMatchesApproved(String what, Map<String, byte[]> approved, Map<String, byte[]> appFiles) {
        SoftAssert softly = new SoftAssert();
        List<SheetImage> sheet = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : approved.entrySet()) {
            String name = entry.getKey();
            sheet.add(new SheetImage("APPROVED\n" + name, name, entry.getValue()));
            byte[] app = appFiles.get(name);
            if (app == null) {
                log(name + ": not used by the app");
                softly.fail(name + " (approved) is not used by the app. App files: " + appFiles.keySet());
                continue;
            }
            sheet.add(new SheetImage("APP\n" + name, name, app));
            saveEvidence("app_" + name, app);

            ImageCompare.Result result = ImageCompare.compare(entry.getValue(), app, MAX_PIXEL_DIFF);
            log(name + ": " + (result.matches() ? "MATCHES" : "DIFFERS") + " - " + result.detail());
            softly.assertTrue(result.matches(), name + " does not match the approved design: " + result.detail());
        }
        imageSheet("approved-vs-app", what + " - approved design vs app", sheet);
        softly.assertAll();
    }

    /**
     * Branding on a Flutter screen (semantics must be on): the page title and the screen
     * both say "Bima Sugam", and a Bima Sugam logo image has been loaded.
     */
    protected void assertBranding(SoftAssert softly, FlutterPage screen, AssetRecorder assets, String where) {
        String title = page.title();
        log(where + ": title '" + title + "'");
        softly.assertTrue(mentionsBrand(title), where + ": page title is '" + title + "', not " + BRAND_NAME);

        String text = screen.semanticsText();
        softly.assertTrue(mentionsBrand(text), where + ": '" + BRAND_NAME + "' is not shown. Screen text: " + text);

        softly.assertFalse(brandLogos(assets).isEmpty(),
            where + ": no Bima Sugam logo image (" + BRAND_LOGO_FILES + ") was loaded");
    }

    /** "Bima Sugam" in any letter case - build 2.0.4+8 titles the app "BIMA SUGAM". */
    protected static boolean mentionsBrand(String text) {
        return text != null && text.toLowerCase().contains(BRAND_NAME.toLowerCase());
    }

    protected List<Asset> brandLogos(AssetRecorder assets) {
        return assets.where(asset -> asset.ok() && BRAND_LOGO_FILES.contains(asset.fileName()));
    }

    /** Image files by name, first occurrence wins. */
    protected static Map<String, byte[]> byFileName(List<Asset> assets) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        for (Asset asset : assets) {
            if (asset.ok() && !files.containsKey(asset.fileName())) {
                files.put(asset.fileName(), asset.body());
            }
        }
        return files;
    }

    /**
     * An image shown on the splash: loaded during the HTML splash or #/splash. The favicon
     * (browser tab icon) is left out - only some browsers fetch it at launch, and it is
     * not part of the splash screen.
     */
    protected static boolean isSplashImage(Asset asset) {
        return SplashScreen.isSplashRoute(asset.route()) && !asset.fileName().toLowerCase().startsWith("favicon");
    }

    private static String mimeType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/png";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
    }
}
