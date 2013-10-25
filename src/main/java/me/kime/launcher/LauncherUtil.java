package me.kime.launcher;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Util tool of Launcher
 *
 * @author Kime
 */
public class LauncherUtil {

    /**
     * start minecraft program version 1.6 or after
     *
     * @param name the name of the player
     * @param forceUpdate if is force update the game
     */
    public static void start(String name, boolean forceUpdate) {
        GameDownloadUtil.updateGame(forceUpdate);
        FileUtil.cleanUpNatives();
        FileUtil.extractNatives();
        String fs = File.separator;
        String ps = File.pathSeparator;

        ArrayList params = new ArrayList();
        params.add("javaw");

        params.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
        params.add("-Dfml.ignorePatchDiscrepancies=true");

        params.add("-Djava.library.path=" + MinecraftUtil.getNativesFolder().getAbsolutePath());

        params.add("-cp");
        File lib = MinecraftUtil.getLibrariesFolder();
        StringBuilder sb = new StringBuilder();
        List<String> list = ConfigReader.getLibraries();
        for (String s : list) {
            sb.append(lib.getAbsoluteFile()).append(fs).append(s).append(ps);
        }
        sb.append(new File(MinecraftUtil.getKimeFolder(), "Kime.jar").getAbsolutePath());
        params.add(sb.toString());

        params.add("net.minecraft.launchwrapper.Launch");
        params.add("--username");
        params.add(name);
        params.add("--version");
        params.add("Kime");
        params.add("--gameDir");
        params.add(MinecraftUtil.getWorkingDirectory().getAbsolutePath());
        params.add("--assetsDir");
        params.add(MinecraftUtil.getAssetsFolder().getAbsolutePath());
        params.add("--tweakClass");
        params.add("cpw.mods.fml.common.launcher.FMLTweaker");

        for (Iterator it = params.iterator(); it.hasNext();) {
            String s = (String) it.next();
            System.out.println(s);
        }

        File commands = new File(MinecraftUtil.getWorkingDirectory() + fs + "Commands.txt");
        File output = new File(MinecraftUtil.getWorkingDirectory() + fs + "ProcessLog.txt");
        File errors = new File(MinecraftUtil.getWorkingDirectory() + fs + "ErrorLog.txt");

        ProcessBuilder b = new ProcessBuilder(params);

        try {
            b.start();
        } catch (IOException ex) {
            Logger.getLogger(LauncherUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * read avatar from kime skin service
     *
     * @param name username of player
     * @return avatar image of the player
     */
    public static Image readAvatar(String name) {
        if (name == null || "".equals(name)) {
            name = "littleKime";
        }
        try {
            URL url = new URL("http://skin.kime.co/" + name + ".png");
            BufferedImage img = ImageIO.read(url);
            int height = img.getHeight();
            int width = img.getWidth();

            BufferedImage head = img.getSubimage(width / 8, height / 4, width / 8, height / 4);
            BufferedImage hat = img.getSubimage(width / 8 * 5, height / 4, width / 8, height / 4);

            BufferedImage combined = new BufferedImage(height / 4, width / 8,
                    BufferedImage.TYPE_INT_ARGB);

            // paint both images, preserving the alpha channels
            Graphics g = combined.getGraphics();
            g.drawImage(head, 0, 0, null);
            g.drawImage(hat, 0, 0, null);
            g.dispose();

            return combined.getScaledInstance(128, 128, 0);
        } catch (IOException ex) {
            Logger.getLogger(LauncherUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * read username of the player from lastLogin file
     *
     * @param username TextField to enther username
     * @param passworld PassworldField to enther passworld
     */
    public static void readUsername(JTextField username, JPasswordField passworld) {
        try {
            File lastLogin = MinecraftUtil.getLoginFile();
            Cipher cipher = getCipher(2, "passwordfile");
            DataInputStream dis = new DataInputStream(new CipherInputStream(
                    new FileInputStream(lastLogin), cipher));
            username.setText(dis.readUTF());
            passworld.setText(dis.readUTF());

            dis.close();
        } catch (Exception e) {
        }
    }

    /**
     * write the current login information into lastLogin file
     *
     * @param username the name of the player
     * @param passworld the passworld of the player
     * @param rememberPassworld if is to remember the password
     */
    public static void writeUsername(String username, String passworld, boolean rememberPassworld) {
        try {
            File lastLogin = MinecraftUtil.getLoginFile();
            Cipher cipher = getCipher(1, "passwordfile");
            DataOutputStream dos = new DataOutputStream(new CipherOutputStream(
                    new FileOutputStream(lastLogin), cipher));
            dos.writeUTF(username);
            if (rememberPassworld) {
                dos.writeUTF(passworld);
            } else {
                dos.writeUTF("");

            }
            dos.close();

        } catch (Exception e) {
            Logger.getLogger(LauncherUtil.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, e);
        }
    }

    /**
     * method to auth the username and passworld to kime auth service
     *
     * @param username the name of the player
     * @param passworld the passworld of the player
     * @return if the auth succssce
     */
    public static boolean authUser(String username, String passworld) {
        SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] byteData = md.digest(passworld.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xFF) + 256, 16).substring(1));
            }

            TrustManager[] trustAllCerts = {new KimeTrustManager()};
            SSLContext sc = SSLContext.getInstance("SSL");

            sc.init(null, trustAllCerts, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            String https_url = new StringBuilder().append("https://xki.me/auth/auth.php?user=")
                    .append(username).append("&pass=").append(sb.toString()).toString();
            URL url = new URL(https_url);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            if (con.getResponseCode() == 202) {
                return true;
            } else {
                return false;

            }
        } catch (Exception ex) {
            Logger.getLogger(LauncherUtil.class
                    .getName()).log(Level.SEVERE, null, ex);

            return false;
        } finally {
            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
        }
    }

    private static Cipher getCipher(int mode, String password) throws Exception {
        Random random = new Random(43287234L);
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

        SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
                .generateSecret(new PBEKeySpec(password.toCharArray()));
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(mode, pbeKey, pbeParamSpec);
        return cipher;
    }
}
