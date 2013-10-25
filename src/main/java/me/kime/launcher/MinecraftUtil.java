package me.kime.launcher;

import java.io.File;

public class MinecraftUtil {

    private static File workDir = null;
    private static File KimeFolder = null;
    private static File versionsFolder = null;
    private static File assetsFolder = null;
    private static File nativesFolder = null;
    private static File librariesFolder = null;
    private static File optionsFile = null;
    private static File lastloginFile = null;
    private static File savesDir = null;
    private static File tempFolder = null;

    private static final long time = System.currentTimeMillis();

    public static File getWorkingDirectory() {
        if (workDir == null) {
            workDir = getWorkingDirectory("minecraft");
            if (!workDir.exists()) {
                workDir.mkdirs();
            }
        }
        return workDir;
    }

    public static File getKimeFolder() {
        if (KimeFolder == null) {
            KimeFolder = new File(getVersionsFolder(), "Kime");
            if (!KimeFolder.exists()) {
                KimeFolder.mkdirs();
            }
        }
        return KimeFolder;
    }

    public static File getVersionsFolder() {
        if (versionsFolder == null) {
            versionsFolder = new File(getWorkingDirectory(), "versions");
            if (!versionsFolder.exists()) {
                versionsFolder.mkdirs();
            }
        }
        return versionsFolder;
    }

    public static File getAssetsFolder() {
        if (assetsFolder == null) {
            assetsFolder = new File(getWorkingDirectory(), "assets");
            if (!assetsFolder.exists()) {
                assetsFolder.mkdirs();
            }
        }
        return assetsFolder;
    }

    public static File getNativesFolder() {
        if (nativesFolder == null) {
            nativesFolder = new File(getKimeFolder(), "Kime-natives-" + time);
            if (!nativesFolder.exists()) {
                nativesFolder.mkdirs();
            }
        }
        return nativesFolder;
    }

    public static File getLibrariesFolder() {
        if (librariesFolder == null) {
            librariesFolder = new File(getWorkingDirectory(), "libraries");
            if (!librariesFolder.exists()) {
                librariesFolder.mkdirs();
            }
        }
        return librariesFolder;
    }

    public static File getOptionsFile() {
        if (optionsFile == null) {
            optionsFile = new File(getWorkingDirectory(), "options.txt");
        }
        return optionsFile;
    }

    public static File getLoginFile() {
        if (lastloginFile == null) {
            lastloginFile = new File(getWorkingDirectory(), "lastlogin");
        }
        return lastloginFile;
    }

    public static File getSavesFolder() {
        if (savesDir == null) {
            savesDir = new File(getWorkingDirectory(), "saves");
        }
        return savesDir;
    }

    public static File getTempFolder() {
        if (tempFolder == null) {
            tempFolder = new File(System.getProperties().getProperty("java.io.tmpdir"), "MCBKPMNGR");
        }
        if (!tempFolder.exists()) {
            tempFolder.mkdirs();
        }
        return tempFolder;
    }

    public static File getWorkingDirectory(String applicationName) {
        String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (getPlatform().ordinal()) {
            case 0:
            case 1:
                workingDirectory = new File(userHome, '.' + applicationName + '/');
                break;
            case 2:
                String applicationData = System.getenv("APPDATA");
                if (applicationData != null) {
                    workingDirectory = new File(applicationData, "." + applicationName + '/');
                } else {
                    workingDirectory = new File(userHome, '.' + applicationName + '/');
                }
                break;
            case 3:
                workingDirectory = new File(userHome, "Library/Application Support/"
                        + applicationName);
                break;
            default:
                workingDirectory = new File(userHome, applicationName + '/');
        }
        if ((!workingDirectory.exists()) && (!workingDirectory.mkdirs())) {
            throw new RuntimeException("The working directory could not be created: "
                    + workingDirectory);
        }
        return workingDirectory;
    }

    public static OS getPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OS.windows;
        }
        if (osName.contains("mac")) {
            return OS.macos;
        }
        if (osName.contains("solaris")) {
            return OS.solaris;
        }
        if (osName.contains("sunos")) {
            return OS.solaris;
        }
        if (osName.contains("linux")) {
            return OS.linux;
        }
        if (osName.contains("unix")) {
            return OS.linux;
        }
        return OS.unknown;
    }

    public static enum OS {

        linux, solaris, windows, macos, unknown;
    }
}
