package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.Reporter;

public abstract class BasePage {

    /** Start of every log line written when the UAT server stalled and something was requested again. */
    public static final String STALL_WARNING = "WARNING: UAT server stall";

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    /**
     * Navigates relative to the configured baseUrl. Waits for the DOM only, not the
     * "load" event, because slow third-party images on UAT can hold "load" back.
     * The UAT server sometimes never answers at all; then the page is requested again, up to
     * {@link #NAVIGATE_RETRIES} times, each logged with {@link #STALL_WARNING} so reports flag the run.
     */
    protected void navigate(String path) {
        Page.NavigateOptions options = new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED);
        for (int retry = 1; ; retry++) {
            try {
                page.navigate(path, options);
                return;
            } catch (TimeoutError stalled) {
                if (retry > NAVIGATE_RETRIES) {
                    throw stalled;
                }
                Reporter.log(STALL_WARNING + " - the page was not sent. Loading it again (" + retry + "/" + NAVIGATE_RETRIES + ").", true);
            }
        }
    }

    /** How many times a page the server did not send is requested again before the test fails. */
    private static final int NAVIGATE_RETRIES = 2;

    public String title() {
        return page.title();
    }

    public String url() {
        return page.url();
    }
}
