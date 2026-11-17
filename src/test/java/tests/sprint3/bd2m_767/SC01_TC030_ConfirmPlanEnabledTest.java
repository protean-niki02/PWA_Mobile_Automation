package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;

/**
 * SC_01_TC_030 - Verify that "Confirm Plan" button is enabled appropriately.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Sanity
 *
 * Once Confirm Plan has loaded a plan (a Final Premium is shown and the mandatory Owner Driver cover is
 * added), the "Confirm Plan" button must be shown fully on screen once scrolled to, enabled - not disabled -
 * and not drawn greyed out (its fill is the dark brand colour). Where the button sits on the screen is
 * checked by TC 39 (pinned at the bottom), not here. Runs on the web journey, see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC030_ConfirmPlanEnabledTest extends ConfirmPlanWebTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "sanity"},
        description = "SC_01_TC_030: Verify that \"Confirm Plan\" button is enabled appropriately.")
    public void verifyConfirmPlanEnabled() {
        openConfirmPlan();
        long premium = finalPremium();
        List<String> added = addedCovers();
        boolean ownerAdded = added.stream().anyMatch(t -> t.equalsIgnoreCase(OWNER_DRIVER));
        Locator button = confirmPlanButton();
        Assert.assertTrue(button.count() > 0, "No \"Confirm Plan\" button on the screen. Screen text: " + bodyText());
        button.scrollIntoViewIfNeeded();
        BoundingBox box = button.boundingBox();
        boolean enabled = button.isEnabled() && !"true".equals(button.getAttribute("aria-disabled"));
        int[] fill = colourOf(button, "backgroundColor");
        boolean darkFill = brightness(fill) < 120;
        double width = page.viewportSize().width;
        double height = page.viewportSize().height;
        boolean fullyOnScreen = box.x >= 0 && box.x + box.width <= width + 1 && box.y >= 0 && box.y + box.height <= height + 1;
        log(String.format("Final Premium %s, added covers %s; Confirm Plan button at (%.0f, %.0f) %.0fx%.0f on a %.0fx%.0f screen, enabled %s, fill %s",
            indianFormat(premium), added, box.x, box.y, box.width, box.height, width, height, enabled, hex(fill)));
        screenshot("confirm-plan-button");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(premium > 0 && ownerAdded, "The plan is not ready (Final Premium " + premium + ", Owner Driver added "
            + ownerAdded + "), so the button's state can't be judged");
        softly.assertTrue(fullyOnScreen, "The Confirm Plan button is not shown fully on the screen once scrolled to");
        softly.assertTrue(enabled, "The Confirm Plan button is disabled although the plan is ready");
        softly.assertTrue(darkFill, "The Confirm Plan button is drawn greyed out (fill " + hex(fill) + ") although the plan is ready");
        softly.assertAll();
    }
}
