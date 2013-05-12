package me.kime.launcher;

import SevenZip.LzmaAlone;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kime
 */
public class GameDownloadUtil {

    private static final String HOST = "http://download.kime.co/";
    private static final String[] gameFiles = {"lwjgl.jar", "jinput.jar",
        "lwjgl_util.jar", "minecraft.jar"};
    private static int speed = 0;
    private static String downloadString = "";
    private static boolean isDone = false;

    public static void updateGame(boolean forceUpdate) {
        if (forceUpdate || !canPlayOffline()) {
            downloadJars();
            downloadNatives();
        }
        isDone = true;
    }

    public static void downloadJars() {
        File folder = MinecraftUtil.getBinFolder();
        if (!folder.exists()) {
            folder.mkdir();
        }
        for (String file : gameFiles) {
            downloadFile(file, folder);
        }
    }

    public static void downloadNatives() {
        File folder = MinecraftUtil.getNativesFolder();
        if (!folder.exists()) {
            folder.mkdir();
        }
        String nativeJar;
        switch (MinecraftUtil.getPlatform()) {
            case windows:
                nativeJar = "windows_natives.jar.lzma";
                break;
            case linux:
                nativeJar = "linux_natives.jar.lzma";
                break;
            case macos:
                nativeJar = "macosx_natives.jar.lzma";
                break;
            case solaris:
                nativeJar = "solaris_natives.jar.lzma";
                break;
            default:
                return;
        }
        downloadFile(nativeJar, folder);
        uncompressFile(nativeJar);
    }

    private static void uncompressFile(String filename) {
        File inFile = new File(MinecraftUtil.getNativesFolder(), filename);
        File outFile = new File(MinecraftUtil.getNativesFolder(),
                filename.replace(".lzma", ""));
        try {
            LzmaAlone.decompress(inFile, outFile);
            try (JarFile jar = new JarFile(outFile)) {
                Enumeration<JarEntry> entities = jar.entries();
                downloadString = "解压缩中";
                while (entities.hasMoreElements()) {
                    JarEntry entry = (JarEntry) entities.nextElement();
                    if (!entry.isDirectory() && (entry.getName().indexOf('/') == -1)) {
                        File file = new File(MinecraftUtil.getNativesFolder(), entry.getName());
                        if ((!file.exists() || file.delete())) {
                            try (ReadableByteChannel in = Channels.newChannel(jar.getInputStream(entry));
                                    FileChannel out = new FileOutputStream(file).getChannel()) {
                                out.transferFrom(in, 0, entry.getSize());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GameDownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        inFile.delete();
        outFile.delete();
    }

    public static void downloadFile(String fileName, File folder) {
        try {
            URL inFile = new URL(HOST + fileName);
            File outFile = new File(folder, fileName);
            if (outFile.exists()) {
                outFile.delete();
            }
            URLConnection connection = inFile.openConnection();
            try (ReadableByteChannel source = Channels.newChannel(connection.getInputStream());
                    FileChannel destination = new FileOutputStream(outFile).getChannel()) {

                long count = 0;
                long size = connection.getContentLengthLong();
                long startTime = System.currentTimeMillis();
                while (count < size) {
                    count += destination.transferFrom(source, count, 1 << 18);
                    speed = (int) (count / (System.currentTimeMillis() - startTime));
                    downloadString = "下载" + fileName + "中 @ " + speed + " KB/sec";
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GameDownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int getSpeed() {
        return speed;
    }

    public static boolean canPlayOffline() {
        if ((!MinecraftUtil.getBinFolder().exists())
                || (!MinecraftUtil.getBinFolder().isDirectory())) {
            return false;
        }
        if ((!MinecraftUtil.getNativesFolder().exists())
                || (!MinecraftUtil.getNativesFolder().isDirectory())) {
            return false;
        }
        if (MinecraftUtil.getBinFolder().list().length < 5) {
            return false;
        }
        if (MinecraftUtil.getNativesFolder().list().length < 1) {
            return false;
        }
        String[] bins = MinecraftUtil.getBinFolder().list();
        for (String necessary : gameFiles) {
            boolean isThere = false;
            for (String found : bins) {
                if (necessary.equalsIgnoreCase(found)) {
                    isThere = true;
                    break;
                }
            }
            if (!isThere) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDone() {
        return isDone;
    }

    public static String getDownloadString() {
        return downloadString;
    }
}
