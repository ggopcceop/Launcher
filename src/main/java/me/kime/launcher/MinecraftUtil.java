package me.kime.launcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinecraftUtil {

    private static File workDir = null;
    private static File binDir = null;
    private static File resourcesDis = null;
    private static File optionsFile = null;
    private static File lastloginFile = null;
    private static File savesDir = null;
    private static File tempFolder = null;
    private static File nativesFolder = null;

    public static File getWorkingDirectory() {
        if (workDir == null) {
            workDir = getWorkingDirectory("minecraft");
        }
        return workDir;
    }

    public static File getBinFolder() {
        if (binDir == null) {
            binDir = new File(getWorkingDirectory(), "bin");
        }
        return binDir;
    }

    public static File getResourcesFolder() {
        if (resourcesDis == null) {
            resourcesDis = new File(getWorkingDirectory(), "resources");
        }
        return resourcesDis;
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

    public static File getNativesFolder() {
        if (nativesFolder == null) {
            nativesFolder = new File(getBinFolder(), "natives");
        }
        return nativesFolder;
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

    public static void resetVersion() {
        File versionFile = new File(getBinFolder(), "version");
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(versionFile))) {
            dos.writeUTF("0");
        } catch (IOException ex) {
            Logger.getLogger(MinecraftUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getFakeLatestVersion() {
        File file = new File(getBinFolder(), "version");
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            String version = dis.readUTF();
            if (version.equals("0")) {
                return "1285241960000";
            }
        } catch (IOException ex) {
            Logger.getLogger(MinecraftUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "1285241960000";
    }

    public static enum OS {

        linux, solaris, windows, macos, unknown;
    }
}
