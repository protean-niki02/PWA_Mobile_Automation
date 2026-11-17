package support;

import com.google.gson.JsonObject;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Page;
import org.testng.Reporter;

/**
 * Simulated mobile network speeds (networkProfile in config.properties): none, fast3g, slow3g.
 * Uses the Chrome DevTools protocol, so it works for Chromium (Android, desktop) only;
 * WebKit (iOS) runs unthrottled and the run log says so.
 */
public final class NetworkThrottle {

    private NetworkThrottle() {
    }

    public static void apply(Page page, String browserEngine, String profile) {
        String name = profile == null ? "none" : profile.trim().toLowerCase();
        if (name.isEmpty() || name.equals("none")) {
            return;
        }
        if (!browserEngine.equalsIgnoreCase("chromium")) {
            Reporter.log("Network profile '" + name + "' not applied: throttling is only supported in Chromium (Android)", true);
            return;
        }
        // latency (ms), download and upload in bytes per second - Chrome DevTools presets
        long[] settings = switch (name) {
            case "slow3g" -> new long[] {400, 50_000, 50_000};
            case "fast3g" -> new long[] {150, 200_000, 93_750};
            default -> throw new IllegalArgumentException("Unknown networkProfile '" + name + "' - use none, fast3g or slow3g");
        };
        CDPSession cdp = page.context().newCDPSession(page);
        cdp.send("Network.enable");
        JsonObject conditions = new JsonObject();
        conditions.addProperty("offline", false);
        conditions.addProperty("latency", settings[0]);
        conditions.addProperty("downloadThroughput", settings[1]);
        conditions.addProperty("uploadThroughput", settings[2]);
        cdp.send("Network.emulateNetworkConditions", conditions);
        Reporter.log("Network profile '" + name + "' applied: " + settings[0] + " ms latency, "
            + settings[1] * 8 / 1000 + " kbit/s down, " + settings[2] * 8 / 1000 + " kbit/s up", true);
    }
}
