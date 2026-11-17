package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.ScreenPixels;

import java.util.List;

/**
 * SC_01_TC_027 - Verify that tags/badges (e.g. "Mandatory by Law", "Tenure - 1 Year") display per design.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * On the Owner Driver card the "Mandatory by Law" tag and the "Tenure" options (1 Year / 3 Year) must be
 * shown inside the card and within the screen width. Each tag's position and chip contrast are logged,
 * and a picture of the card is saved for comparing with the Figma design.
 */
public class SC01_TC027_TagsBadgesTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_027: Verify that tags/badges (e.g. \"Mandatory by Law\", \"Tenure - 1 Year\") display per design.")
    public void verifyTagsAndBadges() {
        QuotesListingPage screen = openConfirmPlan();
        List<Cover> covers = covers(screen);
        Cover owner = find(covers, OWNER_DRIVER);
        Assert.assertNotNull(owner, "No \"" + OWNER_DRIVER + "\" cover on Confirm Plan. Screen text: " + screen.semanticsText());
        screen.bringIntoView(titleOf(screen, owner));
        BoundingBox card = titleOf(screen, owner).boundingBox();
        double width = page.viewportSize().width;
        saveEvidence("owner-driver-card.png", ScreenPixels.capture(page, 0, Math.max(0, card.y - 40), width, 320));

        SoftAssert softly = new SoftAssert();
        for (String tag : List.of(MANDATORY, "Tenure", "1 Year")) {
            Locator found = screen.text(tag);
            BoundingBox box = found.count() > 0 ? found.first().boundingBox() : null;
            boolean inCard = box != null && box.y > card.y - 5 && box.y < card.y + 320;
            boolean inside = box != null && box.x >= 0 && box.x + box.width <= width + 1;
            double spread = box == null ? 0 : ScreenPixels.brightnessSpread(ScreenPixels.capture(page, box.x - 6, box.y - 4, box.width + 12, box.height + 8));
            log(String.format("\"%s\": %s%s, chip contrast %.2f", tag, box == null ? "NOT shown" : String.format("at (%.0f, %.0f)", box.x, box.y),
                inCard ? " inside the Owner Driver card" : "", spread));
            softly.assertTrue(box != null && inCard, "\"" + tag + "\" is not shown on the " + OWNER_DRIVER + " card");
            softly.assertTrue(box == null || inside, "\"" + tag + "\" is cut off at the side of the screen");
        }
        screenshot("tags-badges");
        softly.assertAll();
    }
}
