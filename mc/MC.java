/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mc;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Kime
 */
public class MC {

    /**
     * @param args the command line arguments
     */
    public static void start(String name) throws IOException, InterruptedException {
        String applicationData = System.getenv("APPDATA");
        ArrayList params = new ArrayList();
        params.add("java");
        params.add("-Xms512m");
        params.add("-Xmx2G");
        params.add("-cp");
        params.add("\"" + applicationData+ "\\.minecraft\\bin\\*\"");
        params.add("-Djava.library.path=\"" + applicationData + "\\.minecraft\\bin\\natives\"");
        params.add("net.minecraft.client.Minecraft");
        params.add(name);
        
        ProcessBuilder b = new ProcessBuilder(params);
        //b.directory(new File(applicationData+ "\\.minecraft\\bin"));
        Process p = b.start();
    }
}
