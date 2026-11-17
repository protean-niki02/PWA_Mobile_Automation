package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_030 - Verify that the Close (X) button exits the flow.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * Once the quotes are loaded, tapping the header's Close (X) must take the user out of the 4 Wheeler
 * journey. If the app first asks to confirm leaving, the dialog is recorded (screenshot and its
 * texts) and confirmed with "Leave" (or Yes / Exit / Confirm). Then, within 15s:
 * <ul>
 *   <li>the app must have left the quotes listing and the 4 Wheeler journey - not on a #/journey
 *       route, the review screen or the listing;</li>
 *   <li>a screen must be shown (not blank) - where the user lands (home / dashboard ...) is logged.</li>
 * </ul>
 */
public class SC01_TC030_QuotesCloseExitsFlowTest extends QuotesListingTestBase {

    private static final int EXIT_TIMEOUT_MS = 15_000;
    private static final List<String> LEAVE_BUTTONS = List.of("Leave", "Yes", "Exit", "Confirm", "Yes, Exit", "Yes, Leave");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_030: Verify that the Close (X) button exits the flow.")
    public void verifyCloseExitsFlow() {
        QuotesListingPage quotes = openLoadedQuotes();
        String listingRoute = quotes.route();
        screenshot("before-close");

        List<String> before = quotes.semanticsTexts();
        if (quotes.closeButton().count() > 0) {
            quotes.pressOn(quotes.closeButton());
        } else {
            quotes.tapAt(page.viewportSize().width - 34, quotes.headerCentreY());
        }
        page.waitForTimeout(1_500);
        String confirmedWith = confirmLeavingIfAsked(quotes, before);

        boolean exited = waitForExit(quotes);
        quotes.waitForImagesToSettle();
        List<String> landed = quotes.semanticsTexts();
        log(String.format("Close (X): #%s -> #%s; confirmation %s; exited the flow: %s; landed on: %s", listingRoute, quotes.route(),
            confirmedWith == null ? "not asked" : "confirmed with \"" + confirmedWith + "\"", exited,
            landed.size() > 10 ? landed.subList(0, 10) + "..." : landed));
        screenshot("after-close");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(exited, "Tapping Close (X) did not exit the 4 Wheeler flow within " + EXIT_TIMEOUT_MS / 1000
            + "s; still on #" + quotes.route() + ". Screen text: " + quotes.semanticsText());
        softly.assertFalse(landed.isEmpty(), "After Close (X) the app shows a blank screen (#" + quotes.route() + ")");
        softly.assertAll();
    }

    /**
     * If a "leave the journey?" dialog came up, records it and confirms it. Returns the button used,
     * or null when nothing was asked.
     */
    private String confirmLeavingIfAsked(QuotesListingPage quotes, List<String> before) {
        for (String name : LEAVE_BUTTONS) {
            Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
            if (button.count() > 0 && button.first().isVisible()) {
                List<String> dialog = new ArrayList<>(quotes.semanticsTexts());
                dialog.removeAll(before);
                log("Close (X) asked to confirm leaving: " + dialog);
                screenshot("close-confirmation");
                button.first().click();
                return name;
            }
        }
        return null;
    }

    private boolean waitForExit(QuotesListingPage quotes) {
        try {
            page.waitForCondition(() -> hasExitedFlow(quotes), new Page.WaitForConditionOptions().setTimeout(EXIT_TIMEOUT_MS));
            page.waitForTimeout(1_000);
            return true;
        } catch (TimeoutError stillInFlow) {
            return false;
        }
    }
}
