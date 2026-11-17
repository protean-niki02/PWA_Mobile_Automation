package pages;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The icons a device uses for the installed PWA's home-screen launcher: the web app
 * manifest icons (Android/Chrome) and the apple-touch-icon (iOS).
 */
public class LaunchIcons {

    /** One declared icon. {@code sizes} is the declared "WxH", or null if not declared. */
    public record Icon(String source, String url, String sizes, String purpose) {
    }

    /** An icon as downloaded. Width/height are -1 when the body is not a PNG. */
    public record DownloadedIcon(Icon icon, int status, String contentType, byte[] body, int width, int height) {
    }

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private final Page page;

    public LaunchIcons(Page page) {
        this.page = page;
    }

    @SuppressWarnings("unchecked")
    public List<Icon> declared() {
        List<Icon> icons = new ArrayList<>();

        WebAppManifest manifest = WebAppManifest.load(page);
        for (Map<String, Object> icon : manifest.icons()) {
            icons.add(new Icon("manifest",
                URI.create(manifest.url()).resolve((String) icon.get("src")).toString(),
                (String) icon.get("sizes"),
                (String) icon.getOrDefault("purpose", "any")));
        }

        for (Object href : (List<Object>) page.evaluate(
                "() => [...document.querySelectorAll(\"link[rel='apple-touch-icon']\")].map(l => l.href)")) {
            icons.add(new Icon("apple-touch-icon", (String) href, null, "any"));
        }
        return icons;
    }

    /** Downloads an icon with the page's context, so the mobile user agent is sent. */
    public DownloadedIcon download(Icon icon) {
        APIResponse response = page.context().request().get(icon.url());
        byte[] body = response.body();
        boolean png = isPng(body);
        return new DownloadedIcon(icon, response.status(), response.headers().get("content-type"), body,
            png ? ByteBuffer.wrap(body, 16, 4).getInt() : -1,
            png ? ByteBuffer.wrap(body, 20, 4).getInt() : -1);
    }

    /** File name of an icon's URL, e.g. Icon-192.png. */
    public static String fileName(Icon icon) {
        return icon.url().substring(icon.url().lastIndexOf('/') + 1);
    }

    private static boolean isPng(byte[] body) {
        if (body == null || body.length < 24) {
            return false;
        }
        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (body[i] != PNG_SIGNATURE[i]) {
                return false;
            }
        }
        return true;
    }
}
