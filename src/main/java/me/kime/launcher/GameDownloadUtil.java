package me.kime.launcher;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Kime
 */
public class GameDownloadUtil {

    public static final String HOST = "http://download.kime.co/";
    public static final String MJ_HOST = "https://s3.amazonaws.com/Minecraft.Download/libraries/";
    public static final String MJ_RESOURCE = "https://s3.amazonaws.com/Minecraft.Resources/";

    private static int speed = 0;
    private static String downloadString = "";
    private static boolean isDone = false;

    public static void updateGame(boolean forceUpdate) {
        if (forceUpdate || !canPlayOffline()) {
            downloadGame();
            downloadLibraries();
            downloadResources();
        }
        isDone = true;
    }

    public static void downloadGame() {
        File kime = MinecraftUtil.getKimeFolder();
        String json = HOST + "Kime/Kime.json";
        downloadFile(kime, json);
        String game = HOST + "Kime/Kime.jar";
        downloadFile(kime, game);

    }

    public static void downloadLibraries() {
        List<String> urls = ConfigReader.getDownloadURL();

        File libFolder = MinecraftUtil.getLibrariesFolder();

        for (String url : urls) {
            String[] split = url.split("!");
            downloadFile(new File(libFolder, split[0].substring(0, split[0].lastIndexOf(File.separatorChar))), split[1]);
        }
    }

    public static void downloadResources() {
        try {
            URL resourceUrl = new URL(MJ_RESOURCE);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(resourceUrl.openConnection().getInputStream());
            NodeList nodeLst = doc.getElementsByTagName("Contents");
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node node = nodeLst.item(i);
                if (node.getNodeType() == 1) {
                    Element element = (Element) node;
                    String key = element.getElementsByTagName("Key").item(0).getChildNodes().item(0).getNodeValue();
                    String etag = element.getElementsByTagName("ETag") != null ? element.getElementsByTagName("ETag").item(0).getChildNodes().item(0).getNodeValue() : "-";
                    long size = Long.parseLong(element.getElementsByTagName("Size").item(0).getChildNodes().item(0).getNodeValue());

                    if (size > 0L) {
                        File file = new File(MinecraftUtil.getAssetsFolder(), key);
                        if (etag.length() > 1) {
                            etag = getEtag(etag);
                            if ((file.isFile()) && (file.length() == size)) {
                                String localMd5 = getMD5(file);
                                if (localMd5.equals(etag)) {
                                    continue;
                                }
                            }
                        }

                        downloadFile(file.getParentFile(), MJ_RESOURCE + key);
                    }
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(GameDownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GameDownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GameDownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(GameDownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void downloadFile(File folder, String url) {
        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
            URL inFile = new URL(url);
            String[] split = url.split("/");
            String filename = split[split.length - 1];
            File outFile = new File(folder, filename);

            if (outFile.exists()) {
                return;
            }

            URLConnection connection = inFile.openConnection();
            ReadableByteChannel source = Channels.newChannel(connection.getInputStream());
            FileChannel destination = new FileOutputStream(outFile).getChannel();

            long count = 0;
            long size = connection.getContentLengthLong();
            long startTime = System.nanoTime();
            while (count < size) {
                count += destination.transferFrom(source, count, 1 << 18);
                speed = (int) ((count + 0.0 / (System.nanoTime() - startTime)) / 1000);
                downloadString = "下载" + filename + "中 @ " + speed + " KB/sec";
            }

            destination.close();
        } catch (IOException ex) {
            Logger.getLogger(GameDownloadUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int getSpeed() {
        return speed;
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

    public static String getDownloadString() {
        return downloadString;
    }

    public static String getMD5(File file) {
        DigestInputStream stream = null;
        try {
            stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("MD5"));
            byte[] buffer = new byte[65536];

            while (stream.read(buffer) >= 1) {
                stream.read(buffer);
            }
        } catch (IOException ignored) {
            return null;
        } catch (NoSuchAlgorithmException ignored) {
            return null;
        } finally {
            closeSilently(stream);
        }

        return String.format("%1$032x", new Object[]{new BigInteger(1, stream.getMessageDigest().digest())});
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException localIOException) {
            }
        }
    }

    public static String getEtag(String etag) {
        if (etag == null) {
            etag = "-";
        } else if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
            etag = etag.substring(1, etag.length() - 1);
        }

        return etag;
    }
}
