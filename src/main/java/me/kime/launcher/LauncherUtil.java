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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Util tool of Launcher
 *
 * @author Kime
 */
public class LauncherUtil {

    /**
     * start minecraft program
     *
     * @param name the name of the player
     * @param forceUpdate if is force update the game
     * @throws IOException
     * @throws InterruptedException
     */
    public static void start(String name, boolean forceUpdate) throws IOException, InterruptedException {
        GameDownloadUtil.updateGame(forceUpdate);
        String fs = File.separator;
        String ps = File.pathSeparator;

        String binFolder = MinecraftUtil.getBinFolder().getAbsolutePath();
        ArrayList params = new ArrayList();
        params.add("java");
        params.add("-Xms512m");
        params.add("-Xmx2G");
        params.add("-cp");
        params.add(binFolder + fs + "minecraft.jar" + ps + binFolder + fs
                + "lwjgl.jar" + ps + binFolder + fs + "lwjgl_util.jar" + ps
                + binFolder + fs + "jinput.jar");
        params.add("-Djava.library.path=" + binFolder + fs + "natives");
        params.add("net.minecraft.client.Minecraft");
        params.add(name);
        ProcessBuilder b = new ProcessBuilder(params);
        b.start();
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

            BufferedImage combined = new BufferedImage(height / 4, width / 8, BufferedImage.TYPE_INT_ARGB);

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
            DataInputStream dis;
            if (cipher != null) {
                dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
            } else {
                dis = new DataInputStream(new FileInputStream(lastLogin));
            }
            username.setText(dis.readUTF());
            passworld.setText(dis.readUTF());
            dis.close();
        } catch (Exception e) {
            Logger.getLogger(LauncherUtil.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
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
            DataOutputStream dos;
            if (cipher != null) {
                dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
            } else {
                dos = new DataOutputStream(new FileOutputStream(lastLogin));
            }
            dos.writeUTF(username);
            if (rememberPassworld) {
                dos.writeUTF(passworld);
            } else {
                dos.writeUTF("");
            }
            dos.close();
        } catch (Exception e) {
            Logger.getLogger(LauncherUtil.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
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
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] byteData = md.digest(passworld.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xFF) + 256, 16).substring(1));
            }
            TrustManager[] trustAllCerts = {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        InputStream inStream = null;
                        try {
                            inStream = this.getClass().getResourceAsStream("/sf_bundle-g2.crt");
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");
                            X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
                            return new X509Certificate[]{cert};
                        } catch (CertificateException ex) {
                            Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        } finally {
                            try {
                                if (inStream != null) {
                                    inStream.close();
                                }

                            } catch (IOException ex) {
                                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                        throw new CertificateException("no trusted Certificate");
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                        X509Certificate[] ca = getAcceptedIssuers();
                        for (X509Certificate cert : xcs) {
                            try {
                                cert.verify(ca[0].getPublicKey());
                            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
                                throw new CertificateException("Certificate not trusted", ex);
                            }
                        }
                    }
                }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            String https_url = new StringBuilder().append("https://xki.me/auth/auth.php?user=").append(username).append("&pass=").append(sb.toString()).toString();
            URL url = new URL(https_url);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            if (con.getResponseCode() == 202) {
                return true;
            } else {
                return false;
            }

        } catch (IOException | NoSuchAlgorithmException | KeyManagementException ex) {
            Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private static Cipher getCipher(int mode, String password) throws Exception {
        Random random = new Random(43287234L);
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

        SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(mode, pbeKey, pbeParamSpec);
        return cipher;
    }
}
