package me.kime.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kime
 */
public class FileUtil {

    public static void cleanUpNatives() {
        File kimeFolder = MinecraftUtil.getKimeFolder();
        File[] files = kimeFolder.listFiles();
        for (File f : files) {
            if (f.getName().startsWith("Kime-natives-")) {
                deleteDirectory(f);
            }
        }
    }

    public static void extractNatives() {
        File libFolder = MinecraftUtil.getLibrariesFolder();
        List<String> list = ConfigReader.getNatives();
        for (String path : list) {
            try {
                JarFile jar = new JarFile(new File(libFolder, path));
                Enumeration<JarEntry> entities = jar.entries();
                while (entities.hasMoreElements()) {
                    JarEntry entry = (JarEntry) entities.nextElement();
                    if (!entry.isDirectory() && (entry.getName().indexOf('/') == -1)) {
                        File file = new File(MinecraftUtil.getNativesFolder(), entry.getName());
                        if ((!file.exists() || file.delete())) {
                            ReadableByteChannel in = Channels.newChannel(jar.getInputStream(entry));
                            FileChannel out = new FileOutputStream(file).getChannel();
                            out.transferFrom(in, 0, entry.getSize());
                            out.close();
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return (directory.delete());
    }
}
