package me.kime.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kime
 */
public class GameDownloadUtil {

    private static final String HOST = "http://download.kime.co/";
    private static int speed = 0;

    public static void downloadJars() {
        File folder = MinecraftUtil.getBinFolder();
        downloadFile("minecraft.jar", folder);
        downloadFile("lwjgl.jar", folder);
        downloadFile("jinput.jar", folder);
        downloadFile("lwjgl_util.jar", folder);
    }

    public static void downloadNatives() {
        File folder = MinecraftUtil.getNativesFolder();
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
    }

    private static void uncompressFile(String filename){
        
    }
    private static void downloadFile(String fileName, File folder) {
        try {
            URL inFile = new URL(HOST + fileName);
            File outFile = new File(folder, fileName);
            URLConnection connection = inFile.openConnection();
            try (ReadableByteChannel source = Channels.newChannel(connection.getInputStream())) {
                try (FileChannel destination = new FileOutputStream(outFile).getChannel()) {
                    long count = 0;
                    long size = connection.getContentLengthLong();
                    long startTime = System.currentTimeMillis();
                    while (count < size) {
                        count += destination.transferFrom(source, count, 1 << 18);
                        speed = (int) (count / (startTime - System.currentTimeMillis()));
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GameDownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int getSpeed() {
        return speed;
    }
}
