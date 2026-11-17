package support;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Watches every request a test's page makes: how many, which failed (no response), which got
 * an error status (4xx/5xx) and which were slow. The full detail is in the test's HAR file;
 * this is the short summary for reports.
 */
public class NetworkMonitor {

    /** A request taking longer than this counts as slow. */
    public static final long SLOW_MS = 3_000;

    private record Timed(String url, long millis) {
    }

    private final Map<Request, Long> started = new ConcurrentHashMap<>();
    private final List<Timed> finished = new CopyOnWriteArrayList<>();
    private final List<String> failed = new CopyOnWriteArrayList<>();
    private final List<String> errors = new CopyOnWriteArrayList<>();
    private final AtomicInteger requests = new AtomicInteger();

    public NetworkMonitor(Page page) {
        page.onRequest(request -> {
            requests.incrementAndGet();
            started.put(request, System.currentTimeMillis());
        });
        page.onRequestFinished(request -> {
            Long start = started.remove(request);
            if (start != null) {
                finished.add(new Timed(request.url(), System.currentTimeMillis() - start));
            }
        });
        page.onRequestFailed(request -> {
            started.remove(request);
            failed.add(request.method() + " " + shorten(request.url()) + " - " + request.failure());
        });
        page.onResponse(response -> {
            if (response.status() >= 400) {
                errors.add(response.status() + " " + response.request().method() + " " + shorten(response.url()));
            }
        });
    }

    public int requestCount() {
        return requests.get();
    }

    public List<String> failedRequests() {
        return List.copyOf(failed);
    }

    public List<String> errorResponses() {
        return List.copyOf(errors);
    }

    public long slowRequestCount() {
        return finished.stream().filter(t -> t.millis() >= SLOW_MS).count();
    }

    /** A plain-text report: totals, failed and error requests, and the slowest requests. */
    public String summary() {
        List<String> lines = new ArrayList<>();
        lines.add("Requests: " + requests.get() + ", failed (no response): " + failed.size()
            + ", error status (4xx/5xx): " + errors.size() + ", slow (>= " + SLOW_MS / 1000 + "s): " + slowRequestCount()
            + ", still pending at the end: " + started.size());
        if (!failed.isEmpty()) {
            lines.add("");
            lines.add("Failed requests:");
            failed.forEach(f -> lines.add("  " + f));
        }
        if (!errors.isEmpty()) {
            lines.add("");
            lines.add("Error responses:");
            errors.forEach(e -> lines.add("  " + e));
        }
        lines.add("");
        lines.add("Slowest requests:");
        finished.stream().sorted(Comparator.comparingLong(Timed::millis).reversed()).limit(10)
            .forEach(t -> lines.add(String.format("  %6d ms  %s", t.millis(), shorten(t.url()))));
        return String.join(System.lineSeparator(), lines);
    }

    private static String shorten(String url) {
        return url.length() > 160 ? url.substring(0, 157) + "..." : url;
    }
}
