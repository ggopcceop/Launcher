/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kime.launcher;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kime
 */
public class GameDownloadUtilTest {

    public GameDownloadUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void downloadFileTest() {
        new File(MinecraftUtil.getTempFolder(), "file_1GB").delete();
        new Thread() {
            public void run() {
                while (!GameDownloadUtil.isDone()) {
                    System.out.println(GameDownloadUtil.getDownloadString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameDownloadUtilTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }.start();
        GameDownloadUtil.downloadFile("file_1GB", MinecraftUtil.getTempFolder());
        File file = new File(MinecraftUtil.getTempFolder(), "file_1GB");
        assertTrue("File downloaded!", file.exists());
    }
}