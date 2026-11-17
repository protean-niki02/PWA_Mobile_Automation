package support;

import base.BaseTest;
import base.TestCaseName;
import config.DeviceProfile;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import pages.FlutterPage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * How long each test case took on each platform (Android, iOS, Desktop).
 *
 * At the end of every run it prints a TC x platform table with totals and writes
 * timing-summary.csv (opens in Excel) into the run's evidence folder.
 * Registered for every run in META-INF/services/org.testng.ITestNGListener.
 *
 * Times cover the test steps themselves (from opening the app to the last check), not
 * browser start-up or saving the evidence afterwards.
 */
public class TimingReport implements ITestListener, IExecutionListener {

    private record Timing(String tcId, String title, String platform, String status, long millis) {
    }

    private static final List<Timing> TIMINGS = new CopyOnWriteArrayList<>();

    private static final String EXECUTION_DATE = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

    @Override
    public void onTestSuccess(ITestResult result) {
        record(result, "PASS");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        record(result, "FAIL");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        record(result, "SKIP");
    }

    @Override
    public void onExecutionFinish() {
        if (TIMINGS.isEmpty()) {
            return;
        }
        printTable();
        writeCsv();
    }

    /**
     * A run where the server stalled and the app had to be reloaded is marked "(stall)":
     * its time includes the stall and should not be reported as the app's time.
     */
    private static void record(ITestResult result, String status) {
        if (Reporter.getOutput(result).stream().anyMatch(line -> line.startsWith(FlutterPage.STALL_WARNING))) {
            status += " (stall)";
        }
        TestCaseName tc = TestCaseName.of(result.getMethod().getConstructorOrMethod().getMethod());
        DeviceProfile device = result.getInstance() instanceof BaseTest test ? test.currentDevice() : null;
        TIMINGS.add(new Timing(tc.id(), tc.title(), device == null ? "Unknown" : device.label(), status,
            Math.max(0, result.getEndMillis() - result.getStartMillis())));
    }

    private static void printTable() {
        Set<String> platforms = new LinkedHashSet<>();
        Map<String, Map<String, Timing>> byTc = new LinkedHashMap<>();
        for (Timing t : TIMINGS) {
            platforms.add(t.platform());
            byTc.computeIfAbsent(t.tcId(), id -> new LinkedHashMap<>()).put(t.platform(), t);
        }
        int idWidth = Math.max(12, byTc.keySet().stream().mapToInt(String::length).max().orElse(0));

        StringBuilder out = new StringBuilder("\n===== Execution time by platform =====\n");
        out.append(pad("Test case", idWidth));
        platforms.forEach(p -> out.append(" | ").append(pad(p, 20)));
        out.append('\n');

        Map<String, Long> totals = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Timing>> row : byTc.entrySet()) {
            out.append(pad(row.getKey(), idWidth));
            for (String platform : platforms) {
                Timing t = row.getValue().get(platform);
                out.append(" | ").append(pad(t == null ? "-" : duration(t.millis()) + " " + t.status(), 20));
                if (t != null) {
                    totals.merge(platform, t.millis(), Long::sum);
                }
            }
            out.append('\n');
        }
        out.append(pad("TOTAL", idWidth));
        platforms.forEach(p -> out.append(" | ").append(pad(duration(totals.getOrDefault(p, 0L)), 20)));
        out.append("\n");
        System.out.println(out);
    }

    /**
     * One row per test case, sorted by TC ID, with status and duration per platform side by
     * side, so the rows can be pasted next to the matching rows of the test case sheet.
     */
    private static void writeCsv() {
        List<String> platforms = TIMINGS.stream().map(Timing::platform).distinct().toList();
        Map<String, Map<String, Timing>> byTc = new TreeMap<>();
        Map<String, String> titles = new LinkedHashMap<>();
        for (Timing t : TIMINGS) {
            byTc.computeIfAbsent(t.tcId(), id -> new LinkedHashMap<>()).put(t.platform(), t);
            titles.putIfAbsent(t.tcId(), t.title());
        }

        List<String> header = new ArrayList<>(List.of("Test Case ID", "Test Case Description"));
        for (String platform : platforms) {
            header.add(platform + " Status");
            header.add(platform + " Duration (h:mm:ss)");
            header.add(platform + " Duration (sec)");
        }
        header.add("Execution Date");

        List<String> lines = new ArrayList<>();
        lines.add(String.join(",", header));
        for (Map.Entry<String, Map<String, Timing>> row : byTc.entrySet()) {
            List<String> cells = new ArrayList<>(List.of(csv(row.getKey()), csv(titles.get(row.getKey()))));
            for (String platform : platforms) {
                Timing t = row.getValue().get(platform);
                cells.add(t == null ? "" : t.status());
                cells.add(t == null ? "" : hmmss(t.millis()));
                cells.add(t == null ? "" : String.format("%.1f", t.millis() / 1000.0));
            }
            cells.add(EXECUTION_DATE);
            lines.add(String.join(",", cells));
        }

        List<String> totals = new ArrayList<>(List.of("TOTAL", ""));
        for (String platform : platforms) {
            long sum = TIMINGS.stream().filter(t -> t.platform().equals(platform)).mapToLong(Timing::millis).sum();
            totals.add("");
            totals.add(hmmss(sum));
            totals.add(String.format("%.1f", sum / 1000.0));
        }
        totals.add(EXECUTION_DATE);
        lines.add(String.join(",", totals));

        Path file = BaseTest.RUN_DIR.resolve("timing-summary.csv");
        try {
            Files.createDirectories(BaseTest.RUN_DIR);
            Files.write(file, lines);
            System.out.println("Timing summary: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Could not write timing summary: " + e.getMessage());
        }
    }

    /** h:mm:ss, e.g. 0:01:05 - with only mm:ss Excel would read 01:05 as 1 hour 5 minutes. */
    private static String hmmss(long millis) {
        long seconds = Math.round(millis / 1000.0);
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    private static String duration(long millis) {
        long seconds = Math.round(millis / 1000.0);
        return seconds >= 60 ? (seconds / 60) + "m " + (seconds % 60) + "s" : String.format("%.1fs", millis / 1000.0);
    }

    private static String pad(String text, int width) {
        return text.length() >= width ? text : text + " ".repeat(width - text.length());
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
