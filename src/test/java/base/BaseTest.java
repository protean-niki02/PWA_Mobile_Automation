package base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Tracing;
import config.DeviceProfile;
import config.TestConfig;
import com.microsoft.playwright.options.HarContentPolicy;
import pages.BasePage;
import support.NetworkMonitor;
import support.NetworkThrottle;
import support.PerformanceMetrics;
import support.StalledRequestRetry;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One browser per engine per test class (Chromium for Android, WebKit for iOS), and a
 * fresh context (cookies, storage, service workers) per test method so tests never share state.
 *
 * Every test, pass or fail, leaves evidence in
 * {@code <evidenceDir>/<run timestamp>/<TC ID>_<device>_<TC title>/}:
 * step screenshots, a final screenshot, video and trace, each file prefixed with the TC ID,
 * plus performance (performance.json), network traffic (network.har, network-summary.txt)
 * and a row in the run's performance-summary.csv.
 */
public abstract class BaseTest {

    /** This run's evidence folder: {@code <evidenceDir>/<run timestamp>}. */
    public static final Path RUN_DIR = Paths.get(TestConfig.evidenceDir(),
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected DeviceProfile device;
    protected Path evidenceDir;

    private final Map<String, Browser> browsers = new HashMap<>();
    /** by-test-case/&lt;TC&gt;/&lt;device&gt;: the latest evidence of each TC, both devices side by side. */
    private Path latestEvidenceDir;
    private String filePrefix;
    private int stepNumber;
    private String testCaseId;
    private NetworkMonitor network;

    @BeforeClass(alwaysRun = true)
    public void startPlaywright() {
        playwright = Playwright.create();
    }

    /**
     * Device used when neither the suite's "device" parameter nor -Ddevice=... is given,
     * e.g. when a single class is run from the IDE. PWA tests override this, as only
     * phones get the PWA.
     */
    protected DeviceProfile defaultDevice() {
        return DeviceProfile.DESKTOP;
    }

    /** The device the current test runs on; null before it starts. */
    public DeviceProfile currentDevice() {
        return device;
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters("device")
    public void openPage(Method testMethod, @Optional String deviceName) {
        if (playwright == null) {
            throw new SkipException("Playwright did not start - see the startPlaywright failure for the cause");
        }
        String requested = deviceName != null ? deviceName : System.getProperty("device");
        device = requested == null || requested.isBlank() ? defaultDevice() : DeviceProfile.from(requested);
        browser = browsers.computeIfAbsent(device.browserEngine(), this::launch);
        TestCaseName tc = TestCaseName.of(testMethod);
        String platform = device.name().toLowerCase();
        evidenceDir = RUN_DIR.resolve(tc.folderName(platform));
        latestEvidenceDir = Paths.get(TestConfig.evidenceDir(), "by-test-case", tc.caseFolderName(), platform);
        filePrefix = tc.id() + "_" + platform + "_";
        testCaseId = tc.id();
        stepNumber = 0;

        Browser.NewContextOptions options = device.contextOptions()
            .setBaseURL(TestConfig.baseUrl());
        if (TestConfig.recordVideo()) {
            options.setRecordVideoDir(evidenceDir);
        }
        if (TestConfig.recordHar()) {
            // URLs, headers, timings and sizes; response bodies left out to keep the file small
            options.setRecordHarPath(evidenceFile("network.har"))
                .setRecordHarContent(HarContentPolicy.OMIT);
        }
        context = browser.newContext(options);
        PerformanceMetrics.install(context);
        context.setDefaultNavigationTimeout(TestConfig.navigationTimeoutMs());
        context.setDefaultTimeout(TestConfig.defaultTimeoutMs());
        if (TestConfig.retryStalledRequests()) {
            StalledRequestRetry.install(context, TestConfig.baseUrl());
        }
        if (TestConfig.recordTrace()) {
            context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setTitle(testMethod.getName()));
        }
        page = context.newPage();
        network = new NetworkMonitor(page);
        NetworkThrottle.apply(page, device.browserEngine(), TestConfig.networkProfile());
    }

    /**
     * Path for an evidence file, prefixed with the TC ID and device so it stays identifiable
     * even when copied out of its folder, e.g. SC_01_TC_001_ios_video.webm.
     */
    protected Path evidenceFile(String name) {
        return evidenceDir.resolve(filePrefix + name);
    }

    /** Saves a full-page screenshot as the next numbered step, e.g. SC_01_TC_001_01_welcome-screen.png. */
    protected void screenshot(String step) {
        Path file = nextStepFile(step);
        page.screenshot(new Page.ScreenshotOptions().setPath(file).setFullPage(true));
        log("Screenshot: " + file.toAbsolutePath());
    }

    /** Reserves the next numbered step file name, for screenshots taken some other way. */
    protected Path nextStepFile(String step) {
        stepNumber++;
        return evidenceFile(String.format("%02d_%s.png", stepNumber, step));
    }

    /** Saves any extra artifact (downloaded file, JSON, ...) into this test's evidence folder. */
    protected void saveEvidence(String fileName, byte[] content) {
        Path file = evidenceFile(fileName);
        try {
            Files.createDirectories(evidenceDir);
            Files.write(file, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        log("Evidence: " + file.toAbsolutePath());
    }

    /** Writes to the console and to the TestNG HTML report. */
    protected void log(String message) {
        Reporter.log(message, true);
    }

    @AfterMethod(alwaysRun = true)
    public void closePage(ITestResult result) {
        if (context == null) {
            return;
        }
        String outcome = switch (result.getStatus()) {
            case ITestResult.SUCCESS -> "PASS";
            case ITestResult.FAILURE -> "FAIL";
            default -> "SKIP";
        };
        // A page stuck loading can't be screenshotted; don't let that add a second failure
        boolean responsive = true;
        try {
            Path file = evidenceFile("final_" + outcome + ".png");
            page.screenshot(new Page.ScreenshotOptions().setPath(file).setFullPage(true).setTimeout(10_000));
            log("Final screenshot: " + file.toAbsolutePath());
        } catch (PlaywrightException e) {
            responsive = false;
            log("Could not save final screenshot: " + e.getMessage().lines().findFirst().orElse(""));
        }
        savePerformanceAndNetwork(outcome, responsive);
        if (TestConfig.recordTrace()) {
            Path trace = evidenceFile("trace.zip");
            context.tracing().stop(new Tracing.StopOptions().setPath(trace));
            log("Trace: " + trace.toAbsolutePath());
        }

        Path video = page.video() == null ? null : page.video().path();
        // The HAR file is written when the context closes
        context.close();
        context = null;
        if (TestConfig.recordHar()) {
            log("Network HAR: " + evidenceFile("network.har").toAbsolutePath());
        }
        // The video is only complete once the context is closed
        if (video != null) {
            try {
                Path target = evidenceFile("video.webm");
                Files.move(video, target, StandardCopyOption.REPLACE_EXISTING);
                log("Video: " + target.toAbsolutePath());
            } catch (IOException e) {
                log("Could not save video: " + e.getMessage());
            }
        }
        // A run that failed because the server stalled is no result; keep the previous evidence
        boolean stalled = Reporter.getOutput(result).stream().anyMatch(line -> line.startsWith(BasePage.STALL_WARNING));
        if (stalled && result.getStatus() != ITestResult.SUCCESS) {
            log("Server stall - by-test-case evidence left unchanged; this run's evidence: " + evidenceDir.toAbsolutePath());
        } else {
            copyToLatestEvidence();
        }
    }

    /**
     * performance.json and network-summary.txt in the evidence folder, and a row in the run's
     * performance-summary.csv. A page that no longer responds only loses its performance figures:
     * reading them has no time limit, so it is not tried on a page stuck loading.
     */
    private void savePerformanceAndNetwork(String outcome, boolean pageResponsive) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        if (!pageResponsive) {
            log("Performance figures not read: the page is not responding");
        } else {
            try {
                metrics = PerformanceMetrics.collect(page);
                saveEvidence("performance.json", PerformanceMetrics.toJson(metrics).getBytes(StandardCharsets.UTF_8));
                log("Performance: " + metrics);
            } catch (PlaywrightException e) {
                log("Could not read performance figures: " + e.getMessage().lines().findFirst().orElse(""));
            }
        }
        if (network != null) {
            String summary = "Network profile: " + TestConfig.networkProfile() + System.lineSeparator() + network.summary();
            saveEvidence("network-summary.txt", summary.getBytes(StandardCharsets.UTF_8));
            log("Network: " + summary.lines().skip(1).findFirst().orElse(""));
            if (!metrics.isEmpty()) {
                PerformanceMetrics.appendToSummary(RUN_DIR.resolve("performance-summary.csv"),
                    testCaseId, device.label(), outcome, metrics, network);
            }
        }
    }

    /**
     * Replaces by-test-case/&lt;TC&gt;/&lt;device&gt; with this run's evidence, so each TC has one
     * folder holding the latest Android and iOS evidence together.
     */
    private void copyToLatestEvidence() {
        try {
            if (Files.isDirectory(latestEvidenceDir)) {
                try (var old = Files.list(latestEvidenceDir)) {
                    for (Path file : old.toList()) {
                        Files.deleteIfExists(file);
                    }
                }
            }
            Files.createDirectories(latestEvidenceDir);
            if (Files.isDirectory(evidenceDir)) {
                try (var files = Files.list(evidenceDir)) {
                    for (Path file : files.filter(Files::isRegularFile).toList()) {
                        Files.copy(file, latestEvidenceDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            log("Latest evidence: " + latestEvidenceDir.toAbsolutePath());
        } catch (IOException e) {
            log("Could not update latest evidence: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        browsers.values().forEach(Browser::close);
        browsers.clear();
        if (playwright != null) {
            playwright.close();
        }
    }

    private Browser launch(String engine) {
        BrowserType type = switch (engine.toLowerCase()) {
            case "chromium" -> playwright.chromium();
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> throw new IllegalArgumentException("Unsupported browser: " + engine);
        };
        return type.launch(new BrowserType.LaunchOptions().setHeadless(TestConfig.headless()));
    }
}
