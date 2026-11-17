package support;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.testng.Reporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Page performance from the browser's own timing APIs: time to first byte, DOM ready, page
 * load, first paint, first contentful paint, largest contentful paint, and the number and total
 * size of downloaded files. WebKit (iOS) reports no first paint or sizes; those stay empty.
 *
 * Times are milliseconds from the start of the page load. The Flutter app keeps working after
 * "load" (downloading main.dart.js, CanvasKit, fonts), so "load" is when the HTML page finished,
 * not when the app was usable.
 */
public final class PerformanceMetrics {

    /** Records largest contentful paint as it happens; must be added before the page loads. */
    private static final String LCP_RECORDER = """
        try {
            new PerformanceObserver(list => {
                const entries = list.getEntries();
                window.__pwaLcp = Math.round(entries[entries.length - 1].startTime);
            }).observe({type: 'largest-contentful-paint', buffered: true});
        } catch (e) { /* not supported by this browser (e.g. WebKit) */ }
        """;

    private static final String COLLECT = """
        () => {
            const nav = performance.getEntriesByType('navigation')[0];
            const paint = name => {
                const p = performance.getEntriesByName(name)[0];
                return p ? Math.round(p.startTime) : null;
            };
            const resources = performance.getEntriesByType('resource');
            const round = v => (v == null || v <= 0) ? null : Math.round(v);
            return {
                timeToFirstByteMs: nav ? round(nav.responseStart) : null,
                domContentLoadedMs: nav ? round(nav.domContentLoadedEventEnd) : null,
                pageLoadMs: nav ? round(nav.loadEventEnd) : null,
                firstPaintMs: paint('first-paint'),
                firstContentfulPaintMs: paint('first-contentful-paint'),
                largestContentfulPaintMs: window.__pwaLcp ?? null,
                resourceCount: resources.length,
                // WebKit reports no transfer sizes (all 0); null rather than a misleading figure
                transferredKB: resources.some(r => r.transferSize > 1024)
                    ? Math.round(resources.reduce((sum, r) => sum + (r.transferSize || 0), 0) / 1024) : null
            };
        }
        """;

    private PerformanceMetrics() {
    }

    public static void install(BrowserContext context) {
        context.addInitScript(LCP_RECORDER);
    }

    /** The page's metrics, in a fixed order; values are null when the browser doesn't report them. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> collect(Page page) {
        return new LinkedHashMap<>((Map<String, Object>) page.evaluate(COLLECT));
    }

    /** The metrics as one JSON object, one value per line. */
    public static String toJson(Map<String, Object> metrics) {
        StringBuilder json = new StringBuilder("{");
        metrics.forEach((key, value) -> json.append(json.length() > 1 ? "," : "")
            .append(System.lineSeparator()).append("  \"").append(key).append("\": ").append(number(value)));
        return json.append(System.lineSeparator()).append("}").toString();
    }

    /**
     * Adds one row to the run's performance-summary.csv (one row per TC per device), creating
     * the file with its header first.
     */
    public static synchronized void appendToSummary(Path csv, String testCase, String device, String outcome,
                                                    Map<String, Object> metrics, NetworkMonitor network) {
        try {
            Files.createDirectories(csv.getParent());
            if (!Files.exists(csv)) {
                Files.writeString(csv, "TC ID,Device,Result," + String.join(",", metrics.keySet())
                    + ",requests,failedRequests,errorResponses,slowRequests" + System.lineSeparator());
            }
            StringBuilder row = new StringBuilder(testCase + "," + device + "," + outcome);
            metrics.values().forEach(value -> row.append(",").append(value == null ? "" : number(value)));
            row.append(",").append(network.requestCount())
                .append(",").append(network.failedRequests().size())
                .append(",").append(network.errorResponses().size())
                .append(",").append(network.slowRequestCount())
                .append(System.lineSeparator());
            Files.writeString(csv, row, StandardOpenOption.APPEND);
        } catch (IOException e) {
            Reporter.log("Could not update " + csv + ": " + e.getMessage(), true);
        }
    }

    /** Numbers come back from the browser as doubles; whole numbers print without ".0". */
    private static String number(Object value) {
        if (value instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())) {
            return Long.toString(n.longValue());
        }
        return String.valueOf(value);
    }
}
