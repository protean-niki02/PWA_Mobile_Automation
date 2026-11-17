package tests;

import base.BaseTest;
import config.DeviceProfile;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.WelcomePage;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Installability checks for the Flutter PWA (mobile user agents only).
 * Run with: mvn test -Dsuite=testng-pwa.xml
 */
public class PwaComplianceTest extends BaseTest {

    @Override
    protected DeviceProfile defaultDevice() {
        return DeviceProfile.ANDROID;
    }

    @Test(groups = "pwa")
    public void isServedOverHttps() {
        WelcomePage welcome = new WelcomePage(page).open();

        Assert.assertTrue(welcome.url().startsWith("https://"), "PWA must be served over HTTPS: " + welcome.url());
    }

    @Test(groups = "pwa")
    public void hasResponsiveViewportMeta() {
        new WelcomePage(page).open();

        assertThat(page.locator("meta[name='viewport']"))
            .hasAttribute("content", Pattern.compile("width=device-width"));
    }

    @Test(groups = "pwa")
    @SuppressWarnings("unchecked")
    public void manifestIsInstallable() {
        new WelcomePage(page).open();

        assertThat(page.locator("link[rel='manifest']")).hasCount(1);
        Map<String, Object> manifest = (Map<String, Object>) page.evaluate(
            "async () => (await fetch(document.querySelector(\"link[rel='manifest']\").href)).json()");

        Assert.assertNotNull(manifest.get("name"), "manifest name");
        Assert.assertNotNull(manifest.get("start_url"), "manifest start_url");
        Assert.assertEquals(manifest.get("display"), "standalone", "manifest display");

        List<Map<String, Object>> icons = (List<Map<String, Object>>) manifest.get("icons");
        Assert.assertTrue(icons.stream().anyMatch(i -> "192x192".equals(i.get("sizes"))), "192x192 icon");
        Assert.assertTrue(icons.stream().anyMatch(i -> "512x512".equals(i.get("sizes"))), "512x512 icon");
    }

    @Test(groups = "pwa")
    public void registersServiceWorker() {
        new WelcomePage(page).open();

        // Resolves true once a service worker is active, false if none appears within 10s
        Object registered = page.evaluate("""
            () => 'serviceWorker' in navigator && Promise.race([
                navigator.serviceWorker.ready.then(() => true),
                new Promise(resolve => setTimeout(() => resolve(false), 10000))
            ])
            """);

        // Fails today: the deployed flutter_service_worker.js is Flutter's deprecated stub that
        // unregisters itself on activate, so the PWA has no offline support.
        Assert.assertEquals(registered, Boolean.TRUE, "No service worker became active");
    }
}
