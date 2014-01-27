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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.kime.launcher.MinecraftUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Kime
 */
public class IndexReader {

    private static JSONObject json;

    static {
        readJSON();
    }

    public static void readJSON() {
        File jsonFile = new File(MinecraftUtil.getIndexFolder(), ConfigReader.getBaseGameVersion() + ".json");
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

    public static List<String> getResourceHash() {
        JSONObject resources = (JSONObject) json.get("objects");
        List list = new LinkedList<String>();
        for (Object obj : resources.values()) {
            JSONObject res = (JSONObject) obj;

            String hash = (String) res.get("hash");
            list.add(hash);

        }
        return list;
    }
}
