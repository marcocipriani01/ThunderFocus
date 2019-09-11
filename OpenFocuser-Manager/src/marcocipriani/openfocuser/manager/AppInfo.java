package marcocipriani.openfocuser.manager;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Gathers information about the current and latest version of the manager.
 * Stores info about the app name, website and GitHub page.
 *
 * @author marcocipriani01
 * @version 1.1
 */
@SuppressWarnings("WeakerAccess")
public class AppInfo {

    /**
     * The name of this application.
     */
    public static final String APP_NAME = "OpenFocuser-Manager";
    /**
     * Current version codename.
     */
    public static final String CODENAME = "Bellatrix";
    /**
     * Bug report page URL.
     */
    public static final String ISSUE_REPORT = "https://github.com/marcocipriani01/OpenFocuser/issues/new";
    /**
     * Website URL.
     */
    public static final String WEBSITE = "https://marcocipriani01.github.io/";
    /**
     * Project page (GitHub).
     */
    public static final String GITHUB_REPO = "https://github.com/marcocipriani01/OpenFocuser";
    /**
     * GitHub API. To fetch information about the latest tag.
     */
    private static final String VERSION_CHECK_URL = "https://api.github.com/repos/marcocipriani01/OpenFocuser/releases/latest";
    /**
     * Download URL.
     */
    private static final String VERSION_DOWNLOAD_URL = "https://github.com/marcocipriani01/OpenFocuser/releases/download/{version}/OpenFocuser-Manager.{extension}";

    /**
     * The latest app version code.
     */
    private String latestVersion = null;
    /**
     * The latest app update link.
     */
    private String latestVersionLink = null;
    /**
     * The current version.
     */
    private String currentVersion = null;

    /**
     * Class constructor. Loads the current version name.
     *
     * @throws IOException           if the manifest file couldn't be read.
     * @throws IllegalStateException if the version couldn't be found in the manifest.
     * @see <a href="http://stackoverflow.com/questions/1272648/reading-my-own-jars-manifest">Reading my own Jar's Manifest</a>
     */
    public AppInfo() throws IOException, IllegalStateException, NullPointerException {
        Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            Attributes attributes = manifest.getMainAttributes();
            if (attributes.getValue("Specification-Title").equals(APP_NAME)) {
                currentVersion = attributes.getValue("Specification-Version");
            }
        }
        if (currentVersion == null) {
            throw new IllegalStateException("Version not specified in MANIFEST.MF");
        }
    }

    /**
     * Looks for updates.
     *
     * @return {@code true} if an update is available, {@code false} otherwise.
     * @throws IOException           if a network error occurred.
     * @throws IllegalStateException if the version tag couldn't be found in the remote file.
     * @throws NumberFormatException if the version has an illegal format.
     */
    public boolean checkForUpdates() throws IOException, IllegalStateException, NumberFormatException {
        Scanner scan = new Scanner(new URL(VERSION_CHECK_URL).openStream()).useDelimiter("\\s*,\\s*");
        while (scan.hasNext()) {
            String line = scan.next();
            if (line.contains("tag_name")) {
                latestVersion = line.replace("\"tag_name\":", "").replace("\"", "").trim();
                String extension;
                if (Main.COMPUTER_OS == Main.OperatingSystem.Linux) {
                    extension = ".deb";

                } else {
                    extension = ".jar";
                }
                latestVersionLink = VERSION_DOWNLOAD_URL.replace("{version}", latestVersion).replace("{extension}", extension);
                break;
            }
        }
        scan.close();
        if (latestVersion == null) {
            throw new IllegalStateException("Unable to look for newer versions!");
        }
        return Integer.parseInt(latestVersion.replace("v", "").replace(".", "").trim()) >
                Integer.parseInt(currentVersion.replace("v", "").replace(".", "").trim());
    }

    /**
     * Returns the latest version.
     *
     * @return the latest version string.
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * Returns the version name.
     *
     * @return the version name.
     */
    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * @return the link where you ca download the latest version.
     */
    public String getLatestVersionLink() {
        return latestVersionLink;
    }
}