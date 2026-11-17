package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import support.ImageCompare;
import support.ScreenPixels;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_038 - Verify that the screen matches the provided design.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * At the target device's size Confirm Plan must hold together as designed: no sideways overflow; the
 * heading "Confirm your Bima Vahan Plan", the plan summary card (insurer, IDV, Final Premium, Product
 * Details), the plan's breakup, the base plan sections, Optional Covers with the indicative tag, every
 * cover's title, "Go Back to Quotes" and the Confirm Plan button inside the screen width once scrolled to.
 * Where the TC names an element the app words differently ("View Breakup" / "PREMIUM BREAKDOWN", "What's
 * included in your base plan?" / "Own Damage Cover"), either wording counts. The quotes' data differ per
 * fetch, so whole screens can't be compared with the Figma frames; screenshots are saved for that. If an
 * approved header image is put in src/test/resources/approved-design/confirm-plan/header_WxH.png it is
 * compared too (at most 3% differs). Runs on the web journey, see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC038_DesignTest extends ConfirmPlanWebTestBase {

    private static final Path APPROVED = Path.of("src", "test", "resources", "approved-design", "confirm-plan");

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_038: Verify that the screen matches the provided design.")
    public void verifyScreenMatchesDesign() {
        openConfirmPlan();
        double width = page.viewportSize().width;
        List<String> problems = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Number> widths = (List<Number>) page.evaluate(
            "() => [document.documentElement.scrollWidth, window.innerWidth, document.querySelector('div.flex-1.overflow-y-auto').scrollWidth,"
                + " document.querySelector('div.flex-1.overflow-y-auto').clientWidth]");
        if (widths.get(0).doubleValue() > widths.get(1).doubleValue() + 1 || widths.get(2).doubleValue() > widths.get(3).doubleValue() + 1) {
            problems.add(String.format("content is %.0f px wide in a %.0f px area (page %.0f of %.0f)", widths.get(2).doubleValue(),
                widths.get(3).doubleValue(), widths.get(0).doubleValue(), widths.get(1).doubleValue()));
        }

        // Each entry: the wording(s) that count for one element of the design
        List<List<String>> items = new ArrayList<>(List.of(
            List.of(HEADING), List.of("Insured Declared Value"), List.of(FINAL_PREMIUM), List.of("Product Details"),
            List.of("View Breakup", "PREMIUM BREAKDOWN"), List.of(BASE_PLAN, "Own Damage Cover"), List.of(OPTIONAL_COVERS),
            List.of(INDICATIVE)));
        Locator insurer = page.locator("div.truncate.text-xl").first();
        if (insurer.count() > 0) {
            items.add(1, List.of(insurer.innerText().trim()));
        }
        @SuppressWarnings("unchecked")
        List<String> coverTitles = (List<String>) page.locator(".self-start .font-semibold.text-purple").evaluateAll(
            "els => els.map(e => e.textContent.trim()).filter(t => t.length > 0)");
        coverTitles.forEach(t -> items.add(List.of(t)));
        items.add(List.of(GO_BACK_TO_QUOTES));
        items.add(List.of(CONFIRM_PLAN + "@button"));

        for (List<String> wordings : items) {
            Locator element = null;
            String used = null;
            for (String wording : wordings) {
                Locator candidate = wording.endsWith("@button") ? confirmPlanButton()
                    : page.getByText(wording, new com.microsoft.playwright.Page.GetByTextOptions().setExact(true));
                if (candidate.count() > 0) {
                    element = candidate;
                    used = wording.replace("@button", "");
                    break;
                }
            }
            if (element == null) {
                problems.add("\"" + wordings.get(0).replace("@button", "") + "\" not shown" + (wordings.size() > 1 ? " (nor " + wordings.subList(1, wordings.size()) + ")" : ""));
                continue;
            }
            element.first().scrollIntoViewIfNeeded();
            BoundingBox box = element.first().boundingBox();
            if (box == null || box.x < -1 || box.x + box.width > width + 1) {
                problems.add(String.format("\"%s\" cut off at the side", used));
            }
        }
        scrollArea().evaluate("e => { e.scrollTop = 0; }");
        screenshot("design-top");
        String header = compareHeader((int) width, (int) page.viewportSize().height);
        log("Layout " + (problems.isEmpty() ? "OK" : "problems " + problems) + "; approved header: " + header);

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(problems.isEmpty(), "Confirm Plan does not hold together as designed: " + problems);
        softly.assertFalse(header.startsWith("differs"), "The header does not match the approved design: " + header);
        softly.assertAll();
    }

    private String compareHeader(int width, int height) {
        Path approved = APPROVED.resolve("header_" + width + "x" + height + ".png");
        byte[] actual = ScreenPixels.capture(page, 0, 0, width, 110);
        saveEvidence("header_" + width + "x" + height + ".png", actual);
        if (!Files.exists(approved)) {
            return "none provided (" + approved + ")";
        }
        try {
            ImageCompare.Result result = ImageCompare.compare(Files.readAllBytes(approved), actual, 0.03);
            return (result.matches() ? "matches - " : "differs - ") + result.detail();
        } catch (IOException e) {
            return "could not read " + approved + ": " + e.getMessage();
        }
    }
}
