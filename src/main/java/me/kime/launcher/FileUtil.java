/*
 * Copyright (C) 2014 Kime
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.kime.launcher;

import me.kime.launcher.config.ConfigReader;
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
                    if (!entry.isDirectory() && (!entry.getName().contains("/"))) {
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
