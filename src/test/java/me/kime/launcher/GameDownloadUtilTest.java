/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kime.launcher;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

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
    public void downloadFileTest(){
        new File(MinecraftUtil.getBinFolder(), "minecraft.jar").delete();
        GameDownloadUtil.downloadJars();
        File file = new File(MinecraftUtil.getBinFolder(), "minecraft.jar");
        assertTrue("File downloaded!", file.exists());
    }
}