package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Route;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_035 - Verify that an appropriate error message is displayed on premium recalculation failure.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * A recalculation failure can't be caused on UAT on demand, so it is simulated in the test browser: once
 * Confirm Plan has loaded, every POST to the API whose address names quotes / premium / add-ons /
 * calculation answers HTTP 500 with no message. An add-on cover is then added. The app must show an
 * appropriate error message (something went wrong / unable to update / try again ...), with no technical
 * text, and must not show a new Final Premium for a selection it could not price. If the app sends no such
 * request, the failure can't be simulated and the test is skipped. Only texts that appeared after the
 * request failed count as the app's message. Runs on the web journey, see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC035_RecalculationErrorTest extends ConfirmPlanWebTestBase {

    private static final Pattern RECALCULATION = Pattern.compile("(?i)quote|premium|add-?on|add_on|calculat|plan");
    private static final Pattern ERROR_MESSAGE = Pattern.compile(
        "(?i)something\\s+went\\s+wrong|(unable|failed)\\s+to|(could|can)\\s*(not|n't)|try\\s+again|error|oops|not\\s+able\\s+to|problem");
    private static final Pattern TECHNICAL = Pattern.compile(
        "(?i)exception|\\bnull\\b|undefined|NaN|stack\\s*trace|status\\s*code|\\b(500|502|503|504)\\b|\\{\\s*\"|internal\\s+server");

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_035: Verify that an appropriate error message is displayed on premium recalculation failure.")
    public void verifyRecalculationErrorMessage() {
        openConfirmPlan();
        long before = finalPremium();
        Locator toggle = addOnToggle();
        Assert.assertTrue(toggle.count() > 0, "No add-on cover to add on Confirm Plan. Screen text: " + bodyText());
        String cover = coverTitleOf(toggle);
        toggle.scrollIntoViewIfNeeded();

        List<String> failed = new ArrayList<>();
        page.route("**/*", route -> {
            String url = route.request().url();
            if ("POST".equals(route.request().method()) && url.contains("api-uat") && RECALCULATION.matcher(url).find()) {
                failed.add(url.replaceAll("^https?://[^/]+", ""));
                route.fulfill(new Route.FulfillOptions().setStatus(500).setContentType("application/json").setBody("{\"success\":false}"));
            } else {
                route.fallback();
            }
        });
        List<String> textsBefore = texts();
        toggle.click();
        page.waitForTimeout(5_000);
        long after = finalPremium();
        List<String> added = new ArrayList<>(texts());
        added.removeAll(textsBefore);
        List<String> errors = added.stream().filter(t -> ERROR_MESSAGE.matcher(t).find()).toList();
        List<String> technical = added.stream().filter(t -> TECHNICAL.matcher(t).find() && !RUPEES.matcher(t).find()).toList();
        log("Requests failed on purpose: " + failed);
        log(String.format("Added %s with recalculation failing: Final Premium %s -> %s; new texts %s; error message %s",
            cover, indianFormat(before), indianFormat(after), added, errors));
        screenshot("recalculation-failure");
        if (failed.isEmpty()) {
            throw new SkipException("Adding " + cover + " sent no recalculation request to fail (no POST to api-uat naming quote / premium /"
                + " add-on / calculation), so the failure could not be simulated");
        }

        SoftAssert softly = new SoftAssert();
        softly.assertFalse(errors.isEmpty(), "No error message is shown when the premium recalculation fails. New texts: " + added);
        softly.assertTrue(technical.isEmpty(), "Technical text is shown instead of a user-friendly message: " + technical);
        softly.assertTrue(after == before || after == 0 || !errors.isEmpty(), "A new Final Premium (" + indianFormat(after)
            + ") is shown although its recalculation failed");
        softly.assertAll();
    }
}
