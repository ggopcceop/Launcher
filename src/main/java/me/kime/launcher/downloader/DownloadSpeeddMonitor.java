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

/**
 *
 * @author Kime
 */
public class DownloadSpeeddMonitor {

    private static final byte[] lock = new byte[0];
    private static long downloaded = 0;
    private static long lastTime = 0L;

    public static void count(long count) {
        synchronized (lock) {
            downloaded += count;
        }
    }

    public static long getSpeed() {
        synchronized (lock) {
            Long currentTime = System.currentTimeMillis();
            long timePeriod = currentTime - lastTime;
            long speed = downloaded / timePeriod;
            downloaded = 0;
            lastTime = currentTime;
            return speed;
        }
    }

}
