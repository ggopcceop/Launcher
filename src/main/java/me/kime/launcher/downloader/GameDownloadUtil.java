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
package me.kime.launcher.downloader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import me.kime.launcher.config.ConfigReader;
import me.kime.launcher.config.IndexReader;
import me.kime.launcher.MinecraftUtil;

/**
 *
 * @author Kime
 */
public class GameDownloadUtil {

    public static final String HOST = "http://download.kime.co/";
    public static final String MJ_HOST = "https://libraries.minecraft.net/";
    public static final String MJ_INDEX = "https://s3.amazonaws.com/Minecraft.Download/indexes/";
    public static final String MJ_RESOURCE = "http://resources.download.minecraft.net/";

    private static boolean isDone = false;
    private static boolean startDownload = false;

    public static boolean completeGame = false;
    public static boolean completeIndex = false;
    public static boolean completeLib = false;
    public static boolean completeRes = false;

    public static void updateGame(boolean forceUpdate) {
        if (forceUpdate || !canPlayOffline()) {
            //thread pool for maxmize cpu performce
            ThreadDownLoader.init();
            startDownload = true;
            downloadGame();

            while (!completeGame || !completeIndex || !completeLib || !completeRes) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {

                }
            }

            ThreadDownLoader.stop();
            startDownload = false;
        }
        isDone = true;
    }

    public static void downloadGame() {
        File kime = MinecraftUtil.getKimeFolder();
        String game = HOST + "Kime/Kime.jar";
        downloadFile(kime, game);
        String json = HOST + "Kime/Kime.json";
        downloadFile(kime, json, new Runnable() {

            @Override
            public void run() {
                downloadIndex();
                downloadLibraries();
            }
        });
        completeGame = true;
    }

    public static void downloadIndex() {
        String version = ConfigReader.getBaseGameVersion();
        File folder = MinecraftUtil.getIndexFolder();
        String url = MJ_INDEX + version + ".json";
        downloadFile(folder, url, new Runnable() {

            @Override
            public void run() {
                downloadResources();
            }
        });
        completeIndex = true;

    }

    public static void downloadLibraries() {
        List<String> urls = ConfigReader.getDownloadURL();

        File libFolder = MinecraftUtil.getLibrariesFolder();

        for (String url : urls) {
            String[] split = url.split("!");
            downloadFile(new File(libFolder, split[0].substring(0, split[0].lastIndexOf(File.separatorChar))), split[1]);
        }

        completeLib = true;
    }

    public static void downloadResources() {
        List<String> hashs = IndexReader.getResourceHash();

        for (String hash : hashs) {
            String subdir = hash.substring(0, 2);
            String url = MJ_RESOURCE + subdir + "/" + hash;
            File dir = new File(MinecraftUtil.getObjectFolder(), subdir);
            downloadFile(dir, url);
        }
        completeRes = true;
    }

    public static void downloadFile(File folder, String url, Runnable run) {
        ThreadDownLoader.addDownloadTask(new Downloadable(folder, url, run));
    }

    public static void downloadFile(File folder, String url) {
        ThreadDownLoader.addDownloadTask(new Downloadable(folder, url));
    }

    public static String getDownloadString() {
        if (startDownload) {
            int speed = (int) DownloadSpeeddMonitor.getSpeed();
            return "下载中, 噢噢噢! @ " + speed + " KB / sec";
        } else {
            return "";
        }
    }

    public static boolean canPlayOffline() {
        if (!MinecraftUtil.getAssetsFolder().exists()
                || !MinecraftUtil.getAssetsFolder().isDirectory()) {
            return false;
        }
        if (!MinecraftUtil.getKimeFolder().exists()
                || !MinecraftUtil.getKimeFolder().isDirectory()) {
            return false;
        }
        if (!new File(MinecraftUtil.getKimeFolder(), "Kime.json").exists()
                || new File(MinecraftUtil.getKimeFolder(), "Kime.json").isDirectory()) {
            return false;
        }
        if (!new File(MinecraftUtil.getKimeFolder(), "Kime.jar").exists()
                || new File(MinecraftUtil.getKimeFolder(), "Kime.jar").isDirectory()) {
            return false;
        }

        for (String path : ConfigReader.getLibraries()) {
            File file = new File(MinecraftUtil.getLibrariesFolder(), path);
            if (!file.exists() || file.isDirectory()) {
                return false;
            }
        }

        for (String path : ConfigReader.getNatives()) {
            File file = new File(MinecraftUtil.getLibrariesFolder(), path);
            if (!file.exists() || file.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDone() {
        return isDone;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException localIOException) {
            }
        }
    }
}
