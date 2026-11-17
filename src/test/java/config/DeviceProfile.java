package config;

import com.microsoft.playwright.Browser;

/**
 * Devices the PWA is tested on, each with the browser engine of its platform.
 * Playwright for Java has no built-in device list, so the phones mirror Playwright's
 * "Pixel 7" and "iPhone 15" descriptors.
 *
 * These are emulations on this machine (screen size, touch, user agent and engine), not
 * real phones; real-device timings need a device cloud such as BrowserStack.
 */
public enum DeviceProfile {

    DESKTOP("Desktop", null) {
        @Override
        public Browser.NewContextOptions contextOptions() {
            return new Browser.NewContextOptions()
                .setViewportSize(1366, 768);
        }
    },

    /** Android phone, Chrome's engine. */
    ANDROID("Android", "chromium") {
        @Override
        public Browser.NewContextOptions contextOptions() {
            return new Browser.NewContextOptions()
                .setViewportSize(412, 839)
                .setDeviceScaleFactor(2.625)
                .setIsMobile(true)
                .setHasTouch(true)
                .setUserAgent("Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36");
        }
    },

    /** iPhone, Safari's engine (WebKit). */
    IOS("iOS", "webkit") {
        @Override
        public Browser.NewContextOptions contextOptions() {
            return new Browser.NewContextOptions()
                .setViewportSize(393, 659)
                .setDeviceScaleFactor(3)
                .setIsMobile(true)
                .setHasTouch(true)
                .setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 "
                    + "(KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1");
        }
    };

    private final String label;
    private final String browserEngine;

    DeviceProfile(String label, String browserEngine) {
        this.label = label;
        this.browserEngine = browserEngine;
    }

    public abstract Browser.NewContextOptions contextOptions();

    /** Name for reports, e.g. "iOS". */
    public String label() {
        return label;
    }

    /** chromium, webkit or firefox. Desktop uses the browser set in config.properties. */
    public String browserEngine() {
        return browserEngine != null ? browserEngine : TestConfig.browser();
    }

    /** Accepts desktop, android, ios, and "mobile" as the older name for android. */
    public static DeviceProfile from(String name) {
        String normalized = name.trim().toUpperCase();
        return normalized.equals("MOBILE") ? ANDROID : valueOf(normalized);
    }
}
