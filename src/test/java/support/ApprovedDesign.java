package support;

import org.testng.SkipException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Approved design files (from Figma / the design team) that "matches the approved design"
 * test cases compare against. They live in src/test/resources/approved-design/&lt;area&gt;/,
 * named exactly like the file the app serves (e.g. Icon-192.png, person_one.jpeg).
 */
public final class ApprovedDesign {

    public static final Path ROOT = Paths.get("src", "test", "resources", "approved-design");

    private static final Pattern IMAGE_FILE = Pattern.compile("\\.(png|jpe?g|webp|gif|svg)$", Pattern.CASE_INSENSITIVE);

    private ApprovedDesign() {
    }

    /**
     * Approved images of one area, by file name. Skips the test when none have been added
     * yet, as a comparison without a baseline proves nothing.
     */
    public static Map<String, byte[]> load(String area) {
        Path dir = ROOT.resolve(area);
        Map<String, byte[]> files = new LinkedHashMap<>();
        if (Files.isDirectory(dir)) {
            try (Stream<Path> paths = Files.list(dir)) {
                for (Path file : paths.sorted().toList()) {
                    if (IMAGE_FILE.matcher(file.getFileName().toString()).find()) {
                        files.put(file.getFileName().toString(), Files.readAllBytes(file));
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        if (files.isEmpty()) {
            throw new SkipException("No approved design images in " + dir.toAbsolutePath()
                + " - add the approved files there, named like the app's files, to run this test case");
        }
        return files;
    }
}
