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
 * @version 1.0
 */
@SuppressWarnings("WeakerAccess")
public class AppInfo {

    /**
     * The name of this application.
     */
    public static final String APP_NAME = "OpenFocuser-Manager";
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
    private String currentVersion;

    /**
     * Class constructor. Loads the current version name.
     *
     * @throws IOException if the manifest file couldn't be read.
     * @see <a href="http://stackoverflow.com/questions/1272648/reading-my-own-jars-manifest">Reading my own Jar's Manifest</a>
     */
    public AppInfo() throws IOException {
        Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            Attributes attributes = manifest.getMainAttributes();
            if (attributes.getValue("Specification-Title").equals(APP_NAME)) {
                currentVersion = attributes.getValue("Specification-Version");
            }
        }
        /*currentVersion = new Manifest(getClass().getResourceAsStream("META-INF/MANIFEST.MF")).getMainAttributes()
                .getValue("Specification-Version");*/
        if (currentVersion == null) {
            throw new IllegalStateException("Version not specified in MANIFEST.MF");
        }
    }

    /**
     * Looks for updates.
     *
     * @return {@code true} if an update is available.
     */
    public boolean checkForUpdates() {
        try {
            Scanner scan = new Scanner(new URL(VERSION_CHECK_URL).openStream());
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.contains("tag_name")) {
                    latestVersion = line.replace("\"tag_name\"", "");
                    latestVersion = latestVersion.substring(latestVersion.indexOf("\"") + 1, latestVersion.lastIndexOf("\""));
                    String extension;
                    if (System.getProperty("os.name").toLowerCase().equals("linux")) {
                        extension = ".deb";

                    } else {
                        extension = ".jar";
                    }
                    latestVersionLink = VERSION_DOWNLOAD_URL.replace("{version}", latestVersion).replace("{extension}", extension);
                    break;
                }
            }
            scan.close();
            return ((latestVersion != null && currentVersion != null)) &&
                    (Integer.valueOf(latestVersion.replace("v", "").replace(".", "")) >
                            Integer.valueOf(currentVersion.replace("v", "").replace(".", "")));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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