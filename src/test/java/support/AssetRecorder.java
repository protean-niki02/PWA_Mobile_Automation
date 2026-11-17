package support;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import pages.FlutterPage;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Records every image the page downloads, with the app route it was on at the time.
 *
 * Flutter draws images on a canvas, so they never appear as &lt;img&gt; elements; the
 * network is the only place to see which image files a screen actually used.
 * Create it before navigating, so nothing is missed.
 */
public class AssetRecorder {

    private static final Pattern IMAGE_FILE = Pattern.compile("\\.(png|jpe?g|webp|gif|svg)$", Pattern.CASE_INSENSITIVE);

    public record Asset(String url, String fileName, String route, int status, String contentType, Response response) {

        /** Response body; empty for redirects and errors. */
        public byte[] body() {
            return status >= 200 && status < 300 ? response.body() : new byte[0];
        }

        public boolean ok() {
            return status >= 200 && status < 300;
        }
    }

    private final List<Asset> assets = new CopyOnWriteArrayList<>();

    public AssetRecorder(Page page) {
        page.onResponse(response -> {
            // WebKit also reports data: and blob: URLs, which have no path and are not files the app serves
            if (!response.url().startsWith("http")) {
                return;
            }
            String path = URI.create(response.url()).getPath();
            if (path == null) {
                return;
            }
            String contentType = response.headers().getOrDefault("content-type", "");
            if (IMAGE_FILE.matcher(path).find() || contentType.startsWith("image/")) {
                assets.add(new Asset(response.url(), fileName(path), FlutterPage.routeOf(page.url()),
                    response.status(), contentType, response));
            }
        });
    }

    public List<Asset> all() {
        return List.copyOf(assets);
    }

    /** Assets whose URL contains {@code urlPart}, e.g. "/onboarding/grid_image/". */
    public List<Asset> matching(String urlPart) {
        return where(asset -> asset.url().contains(urlPart));
    }

    public List<Asset> where(Predicate<Asset> condition) {
        return assets.stream().filter(condition).toList();
    }

    private static String fileName(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        return URLDecoder.decode(name, StandardCharsets.UTF_8);
    }
}
