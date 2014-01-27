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
public class Downloadable implements Runnable {

    private final File folder;
    private final String url;
    private Runnable onFinished;

    public Downloadable(File folder, String url, Runnable run) {
        this.folder = folder;
        this.url = url;
        this.onFinished = run;
    }

    public Downloadable(File folder, String url) {
        this(folder, url, null);
    }

    @Override
    public void run() {
        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
            URL inFile = new URL(url);
            String[] split = url.split("/");
            String filename = split[split.length - 1];
            File outFile = new File(folder, filename);

            if (outFile.exists()) {
                outFile.delete();
            }

            URLConnection connection = inFile.openConnection();
            ReadableByteChannel source = Channels.newChannel(connection.getInputStream());
            FileChannel destination = new FileOutputStream(outFile).getChannel();

            long count = 0;
            long size = connection.getContentLengthLong();
            while (count < size) {
                long d = destination.transferFrom(source, count, 1 << 18);
                count += d;
                DownloadSpeeddMonitor.count(d);
            }

            destination.close();
            
            if(onFinished != null){
                onFinished.run();
            }
        } catch (IOException ex) {
            Logger.getLogger(GameDownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
