package base;

import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TC ID and title of a test, read from its TestNG description, which is written as
 * {@code "SC_01_TC_001: Verify that ..."}. Tests without that format fall back to
 * {@code TestClass.method}.
 */
public record TestCaseName(String id, String title) {

    private static final Pattern DESCRIPTION = Pattern.compile("^\\s*([A-Za-z0-9_\\-]+)\\s*[:\\-]\\s*(.+)$");

    // Keeps evidence paths well under Windows' 260-character path limit
    private static final int MAX_TITLE_LENGTH = 60;

    public static TestCaseName of(Method testMethod) {
        Test test = testMethod.getAnnotation(Test.class);
        if (test != null) {
            Matcher m = DESCRIPTION.matcher(test.description());
            if (m.matches()) {
                return new TestCaseName(m.group(1), m.group(2).trim());
            }
        }
        return new TestCaseName(testMethod.getDeclaringClass().getSimpleName() + "." + testMethod.getName(), "");
    }

    /** e.g. SC_01_TC_001_android_Verify_that_the_updated_launch_icon_is_displayed_on_the */
    public String folderName(String device) {
        String safeTitle = safeTitle();
        return id + "_" + device + (safeTitle.isEmpty() ? "" : "_" + safeTitle);
    }

    /** The test case without a device, e.g. SC_01_TC_001_Verify_that_the_updated_launch_icon_is_displayed_on_the */
    public String caseFolderName() {
        String safeTitle = safeTitle();
        return safeTitle.isEmpty() ? id : id + "_" + safeTitle;
    }

    private String safeTitle() {
        String safeTitle = title
            .replaceAll("[^A-Za-z0-9]+", "_")
            .replaceAll("^_|_$", "");
        if (safeTitle.length() > MAX_TITLE_LENGTH) {
            safeTitle = safeTitle.substring(0, MAX_TITLE_LENGTH).replaceAll("_[^_]*$", "");
        }
        return safeTitle;
    }
}
