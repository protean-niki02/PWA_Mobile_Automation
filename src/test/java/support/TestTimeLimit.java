package support;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Gives every test a hard time limit of 5 minutes, unless it sets its own timeOut.
 *
 * Playwright's own timeouts stop a wait when the page is slow, but not when the browser
 * itself freezes (seen with WebKit while the UAT server stalled: one wait hung for 14 minutes).
 * A slow test, including a server stall and one reload, finishes well inside 5 minutes.
 * The Confirm Plan tests (BD2M-767) go through the quotes listing first and the app start can
 * take up to 3 minutes of reloads on a stalled UAT, so they get 8 minutes.
 * Registered for every run in META-INF/services/org.testng.ITestNGListener.
 */
public class TestTimeLimit implements IAnnotationTransformer {

    private static final long LIMIT_MS = 5 * 60 * 1000;
    private static final long LONG_JOURNEY_LIMIT_MS = 8 * 60 * 1000;
    private static final String LONG_JOURNEY_PACKAGE = "tests.sprint3.bd2m_767";

    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (annotation.getTimeOut() <= 0) {
            boolean longJourney = testMethod != null && testMethod.getDeclaringClass().getPackageName().equals(LONG_JOURNEY_PACKAGE);
            annotation.setTimeOut(longJourney ? LONG_JOURNEY_LIMIT_MS : LIMIT_MS);
        }
    }
}
