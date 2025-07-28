package g82;

import g82.managers.LayerManager;
import g82.models.LayerModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;


import org.junit.Before;
import org.junit.Test;
import java.awt.Color;

/**
 * Unit tests for LayerManager class.
 * LayerManager class manages a linked list of LayerModel objects
 * LayerModel object contains pixel grid of Color[][]
 */
public class LayerManagerTest {
    private LayerManager layers;
    private int h;
    private int w;

    /**
     * Create pixel grid for testing
     * @param h - Height of pixel grid
     * @param w - Width of pixel grid
     * @param rgb - Array of rgb values [red,blue,green]
     * 
     * @return pixelGrid - Pixel grid of specified dimensions, every pixel of rgb colour
    */
    public Color[][] createPixelGrid(int h, int w, int[] rgb){
        Color[][] pixelGrid = new Color[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                pixelGrid[i][j] = new Color((int)rgb[0], (int)rgb[1], (int)rgb[2]);
            }
        }
        return pixelGrid;
    }

    @Before
    public void setUp(){
        h = 20;
        w = 10;
        int[] rgb = {255,255,255};
        Color[][] pixelGrid = createPixelGrid(h, w, rgb);
        layers = new LayerManager(pixelGrid);
    }

    @Test
    public void initaliseLayerManagerTest(){
        assertFalse("Layer Manager object cannot initialise", (layers == null));
    }

    @Test
    public void addLayerTest(){
        Color[][] newGrid = new Color[h][w];

        for (int i = 0; i < 9; i++){
            try {
                layers.AddLayer(newGrid);
            } catch (Exception e) {
                String message = "AddLayer Test failed due to: " + e;
                fail(message);
            }
        }
        
        try {
            layers.AddLayer(newGrid);
            String message = "AddLayer Test failed due to: Exceeding layer limit";
            fail(message);
        } catch (Exception e) {
            //pass
        }
    }

    @Test
    public void removeLayerTest(){
        Color[][] newGrid = new Color[h][w];
        for (int i = 0; i < 9; i++){
            try {
                layers.AddLayer(newGrid);
            } catch (Exception e) {
                String message = "RemoveLayer Test failed due to: " + e;
                fail(message);
            }
        }

        for (int i = 0; i < 9; i++){
            try {
                layers.RemoveLayer(0);
            } catch (Exception e) {
                String message = "RemoveLayer Test failed due to: " + e;
                fail(message);
            }
        }
    }

    @Test
    public void findLayerTest(){
        Color[][] newGrid = new Color[h][w];
        try {
            layers.AddLayer(newGrid);
        } catch (Exception e) {
            //pass
        }
        LayerModel currentLayer = layers.FindLayer(1);
        assertSame(currentLayer.getPixelGrid(), newGrid);   
    }

    @Test
    public void getLayerPosTest(){
        Color[][] newGrid = new Color[h][w];
        Color[][] otherGrid = new Color[h][w];
        try{
            layers.AddLayer(otherGrid);
            layers.AddLayer(newGrid);
            LayerModel otherLayer = layers.FindLayer(1);
            LayerModel newLayer = layers.FindLayer(2);
            assertEquals(layers.GetLayerPos(otherLayer), 1);
            assertEquals(layers.GetLayerPos(newLayer), 2);
        } catch (Exception e) {
            String message = "GetLayerPos Test failed due to: " + e;
            fail(message);
        }
    }

    @Test
    public void changeLayerPosTest(){
        Color[][] newGrid = new Color[h][w];
        Color[][] otherGrid = new Color[h][w];
        try{
            layers.AddLayer(newGrid);
            layers.AddLayer(otherGrid);
            LayerModel newLayer = layers.FindLayer(1);
            LayerModel otherLayer = layers.FindLayer(2);
            layers.ChangeLayerPos(1, 2);
            assertSame(newLayer, layers.FindLayer(2));
            assertSame(otherLayer, layers.FindLayer(1));
        } catch (Exception e) {
            String message = "ChangeLayerPos Test failed due to: " + e;
            fail(message);
        }
    }

    //Condense layers when only one layer present
    @Test
    public void condenseLayersACTest1(){
        int[] rgb = {255,255,255};
        Color[][] condensedGrid = layers.CondenseLayersAC();
        for (int i = 0; i < h; i++){
            for (int j = 0; j < w; j++){
                if (condensedGrid[i][j].getRed()!=rgb[0] || condensedGrid[i][j].getGreen()!=rgb[1] || condensedGrid[i][j].getBlue()!=rgb[2]){
                    String message = "CondenseLayersAC Test failed due to: Unexpected color after condensing";
                    fail(message);
                }
            }
        }
    }

    @Test
    public void condenseLayersACTest2(){
        //Example colour combination
        int[] rgb1 = {44,209,55};
        int[] rgb2 = {173,62,120};
        int[] trueRGB = {108,136,88};
        Color[][] grid1 = createPixelGrid(h, w, rgb1);
        Color[][] grid2 = createPixelGrid(h, w, rgb2);
        try {
            layers.AddLayer(grid1); //Full opacity bottom layer
            layers.AddLayer(grid2,(float)0.5); //0.5 opacity second layer (on top)
        } catch (Exception e) {
            String message = "CondenseLayersAC Test failed due to failed layer creation: " + e;
            fail(message);
        }
        Color[][] condensedGrid = layers.CondenseLayersAC();

        //Allow 5 pt leeway for each RGB value due to different methods of calculating RGB values with opacity
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if ((Math.abs(condensedGrid[i][j].getRed()-trueRGB[0])<5 && Math.abs(condensedGrid[i][j].getGreen()-trueRGB[1])<5 && Math.abs(condensedGrid[i][j].getBlue()-trueRGB[2])<5)!=true){
                    String message = "CondenseLayersAC Test failed due to incorrect merging of layers: " + condensedGrid[i][j].getRed() + "," + condensedGrid[i][j].getGreen() + "," + condensedGrid[i][j].getBlue();
                    fail(message);
                }
            }
        }
    }
}
