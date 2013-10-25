package me.kime.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

                list.add(path);
            }
        }
        return list;
    }

    public static List<String> getDownloadURL() {
        return null;
    }
}
