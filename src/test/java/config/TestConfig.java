package config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Reads settings from config.properties; a -Dkey=value system property wins over the file.
 */
public final class TestConfig {

    private static final Properties PROPERTIES = load();

    private TestConfig() {
    }

    public static String baseUrl() {
        return get("baseUrl");
    }

    public static String browser() {
        return get("browser");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless"));
    }

    public static double navigationTimeoutMs() {
        return Double.parseDouble(get("navigationTimeoutMs"));
    }

    public static double defaultTimeoutMs() {
        return Double.parseDouble(get("defaultTimeoutMs"));
    }

    public static String evidenceDir() {
        return get("evidenceDir");
    }

    public static boolean recordVideo() {
        return Boolean.parseBoolean(get("recordVideo"));
    }

    public static boolean recordTrace() {
        return Boolean.parseBoolean(get("recordTrace"));
    }

    /** Re-request app files the UAT server leaves unanswered (see support.StalledRequestRetry). */
    public static boolean retryStalledRequests() {
        return Boolean.parseBoolean(get("retryStalledRequests"));
    }

    /** Save each test's network traffic as a HAR file (on unless recordHar=false). */
    public static boolean recordHar() {
        return !optional("recordHar").equalsIgnoreCase("false");
    }

    /** Simulated network speed: none (default), fast3g or slow3g - see support.NetworkThrottle. */
    public static String networkProfile() {
        String profile = optional("networkProfile");
        return profile.isEmpty() ? "none" : profile;
    }

    /**
     * Test data that may not have been provided yet; "" when missing. Callers skip the
     * test rather than fail when it is empty.
     */
    public static String optional(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        return value == null ? "" : value.trim();
    }

    private static String get(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing config value: " + key);
        }
        return value.trim();
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on the test classpath");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }
}
