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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
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
    private final String hash;

    public Downloadable(File folder, String url, String hash, Runnable run) {
        this.folder = folder;
        this.url = url;
        this.hash = hash;
        this.onFinished = run;
    }

    public Downloadable(File folder, String url) {
        this(folder, url, null, null);
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
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            connection.setRequestProperty("Expires", "0");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);

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
            source.close();

            if (hash != null) {
                try {
                    if (!hash.equalsIgnoreCase(getSHA1Checksum(outFile.getAbsolutePath(), 40))) {
                        run();
                        return;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Downloadable.class.getName()).log(Level.SEVERE, null, ex);
                    run();
                    return;
                }
            }

            if (onFinished != null) {
                onFinished.run();
            }
        } catch (IOException ex) {
            Logger.getLogger(Downloadable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[65536];
        MessageDigest complete = MessageDigest.getInstance("SHA-1");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getSHA1Checksum(String filename, int hashLength) throws Exception {
        byte[] b = createChecksum(filename);

        String result = String.format("%1$0" + hashLength + "x", new Object[]{new BigInteger(1, b)});

        return result;
    }

}
