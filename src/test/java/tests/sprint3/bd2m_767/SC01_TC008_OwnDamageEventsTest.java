package tests.sprint3.bd2m_767;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_008 - Verify that the Own Damage Cover lists all covered events.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * "What's included in your base plan?" is expanded. Its Own Damage Cover must list every standard
 * covered event of a private car package policy: fire / explosion / self-ignition / lightning;
 * burglary / housebreaking / theft; riot and strike; earthquake; flood / storm / cyclone / hailstorm;
 * accidental external means; malicious act; terrorist activity; transit by road / rail / waterway /
 * lift / air; landslide / rockslide. The events listed are logged; any missing one is named.
 */
public class SC01_TC008_OwnDamageEventsTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_008: Verify that the Own Damage Cover lists all covered events.")
    public void verifyOwnDamageEvents() {
        QuotesListingPage screen = openConfirmPlan();
        List<String> texts = expandBasePlan(screen);
        String all = String.join(" | ", texts);
        List<String> listed = texts.stream().filter(t -> t.length() < 90 && OD_EVENTS.stream().anyMatch(p -> p.matcher(t).find())).toList();
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < OD_EVENTS.size(); i++) {
            if (!OD_EVENTS.get(i).matcher(all).find()) {
                missing.add(OD_EVENT_NAMES.get(i));
            }
        }
        log("Own Damage events listed: " + listed);
        screenshot("own-damage-events");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(all.toLowerCase().contains("own damage"), "The base plan does not show an Own Damage Cover. Screen text: " + all);
        softly.assertTrue(missing.isEmpty(), "The Own Damage Cover does not list these covered events: " + missing
            + ". Listed: " + listed);
        softly.assertAll();
    }
}
