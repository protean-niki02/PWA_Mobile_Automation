package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.ImageCompare;
import support.ScreenPixels;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_024 - Verify that tapping "I don't wish to proceed further" navigates the user to the drop-off/exit path.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 *
 * "I don't wish to proceed further" is not on the first screen of the listing (an earlier run saw only
 * the first quote card), so once the quotes are loaded the list is scrolled down step by step, to its
 * end, looking for it. Then:
 * <ul>
 *   <li>it must be on the quotes listing - if it is not there even at the end of the list, the test
 *       fails and says how far it scrolled and what the screen shows;</li>
 *   <li>tapping it must take the user into the drop-off / exit path within 15s: another screen (new
 *       route), or a drop-off sheet / dialog with new content (reasons, feedback, confirm exit ...).</li>
 * </ul>
 * What the drop-off path shows is logged (route and texts) with screenshots of the link and of the
 * path. Nothing is submitted on the drop-off screen.
 */
public class SC01_TC024_DontWishToProceedDropOffTest extends QuotesListingTestBase {

    private static final int STEP_PX = 400;
    private static final int MAX_STEPS = 60;
    private static final int DROP_OFF_TIMEOUT_MS = 15_000;

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_024: Verify that tapping \"I don't wish to proceed further\" navigates the user to the drop-off/exit path.")
    public void verifyDontWishToProceedOpensDropOff() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        Locator link = quotes.text(QuotesListingPage.DONT_WISH_TO_PROCEED);

        // The end of the list is where the drawn screen stops changing; the accessibility text can't
        // tell, as it holds every card of the list, on screen or not
        int steps = 0;
        boolean reachedEnd = false;
        byte[] screen = middleOfScreen();
        int unchanged = 0;
        while (!quotes.isOnScreen(link) && steps < MAX_STEPS) {
            quotes.scrollDown(STEP_PX);
            steps++;
            byte[] now = middleOfScreen();
            unchanged = ImageCompare.compare(screen, now, 0.005).matches() ? unchanged + 1 : 0;
            screen = now;
            if (unchanged >= 2) {
                reachedEnd = true;
                break;
            }
        }
        boolean found = quotes.isOnScreen(link);
        log(String.format("\"%s\" %s after %d scroll step(s) of %d px%s", QuotesListingPage.DONT_WISH_TO_PROCEED,
            found ? "found" : "NOT found", steps, STEP_PX, found ? "" : reachedEnd ? " (end of the list reached)" : " (stopped scrolling)"));
        screenshot(found ? "dont-wish-to-proceed-link" : "end-of-list-no-link");
        Assert.assertTrue(found, "\"" + QuotesListingPage.DONT_WISH_TO_PROCEED + "\" is not on the quotes listing (scrolled "
            + steps + " x " + STEP_PX + " px" + (reachedEnd ? " to the end of the list" : "") + "). Screen shows: " + quotes.semanticsText());

        quotes.bringIntoView(link);
        String listingRoute = quotes.route();
        List<String> before = quotes.semanticsTexts();
        quotes.pressOn(link);
        boolean opened = waitForDropOff(quotes, listingRoute, before);
        quotes.waitForImagesToSettle();
        List<String> added = new ArrayList<>(quotes.semanticsTexts());
        added.removeAll(before);
        log(String.format("Tapped \"%s\": route #%s -> #%s; %d new text(s): %s", QuotesListingPage.DONT_WISH_TO_PROCEED, listingRoute,
            quotes.route(), added.size(), added.size() > 15 ? added.subList(0, 15) + "..." : added));
        screenshot("drop-off-path");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(opened, "Tapping \"" + QuotesListingPage.DONT_WISH_TO_PROCEED + "\" did not open the drop-off / exit path"
            + " within " + DROP_OFF_TIMEOUT_MS / 1000 + "s - still on the listing with no new content. Screen shows: " + quotes.semanticsText());
        softly.assertFalse(quotes.showsError(), "The drop-off / exit path shows an error. Screen shows: " + quotes.semanticsText());
        softly.assertAll();
    }

    /** The list area between the header and the bottom bar, as drawn. */
    private byte[] middleOfScreen() {
        double height = page.viewportSize().height;
        return ScreenPixels.capture(page, 0, 140, page.viewportSize().width, height - 140 - 110);
    }

    /** Waits for the drop-off path: a new route, or 2+ new texts with the link's own screen changed. */
    private boolean waitForDropOff(QuotesListingPage quotes, String listingRoute, List<String> before) {
        long end = System.currentTimeMillis() + DROP_OFF_TIMEOUT_MS;
        while (System.currentTimeMillis() < end) {
            page.waitForTimeout(1_000);
            if (!quotes.route().equals(listingRoute)) {
                return true;
            }
            List<String> added = new ArrayList<>(quotes.semanticsTexts());
            added.removeAll(before);
            if (added.size() >= 2) {
                return true;
            }
        }
        return false;
    }
}
