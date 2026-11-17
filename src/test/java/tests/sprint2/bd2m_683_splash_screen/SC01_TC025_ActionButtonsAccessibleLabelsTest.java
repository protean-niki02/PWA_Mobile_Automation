package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * SC_01_TC_025 - Verify that the action buttons have accessible labels for screen readers.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     Screen reader enabled
 * Step 1:        Enable screen reader on the device & launch the app & land on the splash screen & focus on the action buttons
 * Expected:      Action buttons should have accessible labels announced by the screen reader.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Reads what a screen reader would announce for each action button shown (its accessible name
 * in Flutter's semantics tree): each must be announced as a button with a meaningful name, not
 * silence or a generic word like "button". Whether both buttons are there is SC_01_TC_013.
 */
public class SC01_TC025_ActionButtonsAccessibleLabelsTest extends SplashLandingTestBase {

    private static final Set<String> GENERIC_NAMES = Set.of("button", "btn", "image", "icon", "tap", "click here");

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_025: Verify that the action buttons have accessible labels for screen readers.")
    public void verifyActionButtonsHaveAccessibleLabels() {
        SplashLandingScreen screen = openSplash();
        List<Locator> buttons = assertAnyActionButtons(screen);

        List<String> unlabelled = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i++) {
            String name = SplashLandingScreen.nameOf(buttons.get(i));
            log("Button " + (i + 1) + " is announced as: button, \"" + name + "\"");
            if (name.isEmpty() || GENERIC_NAMES.contains(name.toLowerCase())) {
                unlabelled.add(String.format("button %d at y %.0f (\"%s\")", i + 1, buttons.get(i).boundingBox().y, name));
            }
        }
        Assert.assertTrue(unlabelled.isEmpty(), "Action button(s) without a meaningful accessible label: " + unlabelled);
    }
}
