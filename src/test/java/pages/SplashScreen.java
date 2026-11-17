package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import config.TestConfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * App launch. The splash has two parts:
 * <ol>
 *   <li>an HTML splash in index.html ({@code img.splash-logo}, bima_sugam_logo.png) shown
 *       while Flutter downloads, and</li>
 *   <li>the Flutter {@code #/splash} route once the app has booted,</li>
 * </ol>
 * after which the app moves on to #/onboarding or #/login.
 *
 * Semantics are deliberately not switched on here, so nothing slows the launch down.
 */
public class SplashScreen extends FlutterPage {

    public static final String FLUTTER_SPLASH_ROUTE = "/splash";

    private final Locator htmlSplashLogo = page.locator("img.splash-logo");
    private final List<String> routesSeen = new CopyOnWriteArrayList<>();

    /** Create before {@link #launch()} so every route the app passes through is recorded. */
    public SplashScreen(Page page) {
        super(page);
        // Hash route changes are same-document navigations, which Playwright reports too
        page.onFrameNavigated(frame -> {
            if (frame == page.mainFrame()) {
                String route = routeOf(frame.url());
                if (routesSeen.isEmpty() || !routesSeen.get(routesSeen.size() - 1).equals(route)) {
                    routesSeen.add(route);
                }
            }
        });
    }

    /** Opens the app from its start URL, like tapping the installed icon. */
    public SplashScreen launch() {
        navigate("/");
        return this;
    }

    public Locator htmlSplashLogo() {
        return htmlSplashLogo;
    }

    /** Waits until the HTML splash logo has actually downloaded and decoded (not a broken image). */
    public void waitForHtmlSplashLogoRendered() {
        page.waitForFunction("""
            () => {
                const img = document.querySelector('img.splash-logo');
                return img && img.complete && img.naturalWidth > 0;
            }
            """, null, new Page.WaitForFunctionOptions().setTimeout(TestConfig.navigationTimeoutMs()));
    }

    /** Waits until Flutter has booted and routed anywhere; returns that first route. */
    public String waitForFirstAppRoute() {
        waitForAppStart(() -> waitForRouteMatching(route -> !route.isEmpty()));
        return route();
    }

    /** Waits until the app has left the splash (HTML or #/splash) for its first real screen. */
    public String waitUntilSplashFinished() {
        waitForRouteMatching(route -> !route.isEmpty() && !route.equals(FLUTTER_SPLASH_ROUTE));
        return route();
    }

    /** Routes in the order the app went through them, e.g. ["", "/splash", "/onboarding"]. */
    public List<String> routesSeen() {
        return List.copyOf(routesSeen);
    }

    public boolean flutterSplashWasShown() {
        return routesSeen.contains(FLUTTER_SPLASH_ROUTE);
    }

    public static boolean isSplashRoute(String route) {
        return route.isEmpty() || route.equals(FLUTTER_SPLASH_ROUTE);
    }
}
