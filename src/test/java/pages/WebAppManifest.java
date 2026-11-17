package pages;

import com.microsoft.playwright.Page;

import java.util.List;
import java.util.Map;

/**
 * The PWA's web app manifest. Besides the launch icons, Android builds the installed
 * app's launch splash from it: name, background_color and icon.
 */
public final class WebAppManifest {

    private final String url;
    private final Map<String, Object> json;

    private WebAppManifest(String url, Map<String, Object> json) {
        this.url = url;
        this.json = json;
    }

    /** Reads the manifest linked from the current page. */
    @SuppressWarnings("unchecked")
    public static WebAppManifest load(Page page) {
        String url = (String) page.locator("link[rel='manifest']").evaluate("link => link.href");
        Map<String, Object> json = (Map<String, Object>) page.evaluate(
            "async url => (await fetch(url)).json()", url);
        return new WebAppManifest(url, json);
    }

    public String url() {
        return url;
    }

    /** A text field such as name, short_name, description, background_color; null if missing. */
    public String text(String key) {
        Object value = json.get(key);
        return value == null ? null : value.toString();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> icons() {
        Object icons = json.get("icons");
        return icons == null ? List.of() : (List<Map<String, Object>>) icons;
    }
}
