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
package me.kime.launcher.config;

import me.kime.launcher.downloader.GameDownloadUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.kime.launcher.MinecraftUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Kime
 */
public class ConfigReader {

    private static JSONObject json;

    static {
        readJSON();
    }

    public static void readJSON() {
        File jsonFile = new File(MinecraftUtil.getKimeFolder(), "Kime.json");
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(new FileReader(jsonFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<String> getLibraries() {
        JSONArray libs = (JSONArray) json.get("libraries");
        char fs = File.separatorChar;
        List list = new LinkedList<String>();
        for (Object obj : libs) {
            JSONObject lib = (JSONObject) obj;
            if (!lib.containsKey("extract")) {
                String name = (String) lib.get("name");
                String[] split = name.split(":");
                String replace = split[0].replace('.', fs);
                String path = replace + fs + split[1] + fs + split[2] + fs + split[1] + '-' + split[2] + ".jar";

                list.add(path);
            }
        }
        return list;
    }

    public static List<String> getNatives() {
        JSONArray libs = (JSONArray) json.get("libraries");
        char fs = File.separatorChar;
        String os;
        switch (MinecraftUtil.getPlatform()) {
            case windows:
                os = "windows";
                break;
            case macos:
                os = "osx";
                break;
            default:
                os = "linux";
        }
        List list = new LinkedList<String>();
        for (Object obj : libs) {
            JSONObject lib = (JSONObject) obj;
            if (lib.containsKey("extract")) {
                String name = (String) lib.get("name");
                String[] split = name.split(":");
                String replace = split[0].replace('.', fs);
                String nativeString = (String) ((JSONObject) lib.get("natives")).get(os);
                String path = replace + fs + split[1] + fs + split[2] + fs
                        + split[1] + '-' + split[2] + "-" + nativeString + ".jar";
                path = path.replace("${arch}", MinecraftUtil.getArch());
                list.add(path);
            }
        }
        return list;
    }

    public static List<String> getDownloadURL() {
        JSONArray libs = (JSONArray) json.get("libraries");
        char fs = File.separatorChar;
        String os;
        switch (MinecraftUtil.getPlatform()) {
            case windows:
                os = "windows";
                break;
            case macos:
                os = "osx";
                break;
            default:
                os = "linux";
        }
        List list = new LinkedList<String>();
        for (Object obj : libs) {
            JSONObject lib = (JSONObject) obj;

            String name = (String) lib.get("name");
            String[] split = name.split(":");
            String replace = split[0].replace('.', fs);
            String path;
            if (lib.containsKey("extract")) {
                String nativeString = (String) ((JSONObject) lib.get("natives")).get(os);
                path = replace + fs + split[1] + fs + split[2] + fs
                        + split[1] + '-' + split[2] + "-" + nativeString + ".jar";
            } else {
                path = replace + fs + split[1] + fs + split[2] + fs + split[1] + '-' + split[2] + ".jar";
            }

            path = path.replace("${arch}", MinecraftUtil.getArch());

            if (lib.containsKey("url")) {
                list.add(path + "!" + ((String) lib.get("url")) + path.replace(fs, '/'));
            } else {
                list.add(path + "!" + GameDownloadUtil.MJ_HOST + path.replace(fs, '/'));
            }

        }
        return list;
    }

    public static String getBaseGameVersion() {
        return (String) json.get("assets");
    }

    public static String getArgument() {
        return (String) json.get("minecraftArguments");
    }

    public static String getMainClass() {
        return (String) json.get("mainClass");
    }
}
