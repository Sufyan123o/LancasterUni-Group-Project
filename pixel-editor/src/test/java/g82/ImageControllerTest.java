package g82;

import g82.controllers.ImageController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import java.awt.Color;

/**
 * Unit tests for ImageController class.
 * ImageController class represents pixel grid containing Color objects.
 * Testing import, export, resize, rotate and flip.
 */
public class ImageControllerTest {

    /**
     * @param height - height of pixelGrid in pixels
     * @param width - width of pixelGrid in pixels
     * @return All white (255,255,255) pixel grid of given dimension
     */
    public Color[][] createPixelGrid(int height, int width){
        Color[][] pixelGrid = new Color[height][width];
        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                pixelGrid[i][j] = new Color(255,255,255);
            }
        }
        return(pixelGrid);
    }

    /**
     * Compares pixel grid converted from imported image with expected pixel grid representation
     * Fails if image cannot be imported
     * Fails if pixels do not match
     */
    @Test
    public void ImportImageTest(){
        Color [][] correctGrid = createPixelGrid(2, 3);
        correctGrid[0][0] = new Color(0,0,0);
        correctGrid[1][2] = new Color(0,0,0);
        correctGrid[1][0] = new Color(191,38,38);
        correctGrid[1][1] = new Color(38,90,191);
        try{
            Color[][] importedGrid = ImageController.ImportImage("./src/test/java/g82/testImages/testImg.png");
            checkSame:
            for (int i = 0; i > correctGrid.length; i++){
                for (int j = 0; j > correctGrid[0].length; j++){
                    if (correctGrid[i][j].getRGB() != importedGrid[i][j].getRGB()){
                        fail("One or more pixels do not match, index[" +  i + "][" + j + "]");
                        break checkSame;
                    }
                }
            }
        } catch (Exception e){
            fail("Image could not be imported due to error: " + e);
        }

        
    }

    /**
     * Attempts to export pixel grid to png image
     * Fails if error thrown/export unsuccessful
     */
    @Test
    public void ExportImage2PNGTest(){
        Color [][] pixelGrid = createPixelGrid(10, 20);

        try{
            ImageController.ExportImage2PNG(pixelGrid,"./src/test/java/g82/testImages/exportTest.png");
        } catch (Exception e) {
            fail("Export from pixelGrid to png image failed due to exception: " + e);
        }
    }

    /**
     * Attempts to increase size of image by integer factor
     * Fails if error thrown
     */
    @Test
    public void IncreaseImageSizeTest(){
        Color [][] pixelGrid = createPixelGrid(20, 10);
        try{
            Color [][] newPixelGrid = ImageController.IncreaseImageSize(pixelGrid,2);
            int height = newPixelGrid.length;
            int width = newPixelGrid[0].length;
            assertTrue((height==40)&&(width==20));
        } catch (Exception e){
            fail("Cannot increase image size due to exception: " + e);
        }
    }

    /**
     * Attempts to decrease size of image by divisible integer factor
     * Fails if error thrown
     */
    @Test
    public void DecreaseImageSizeTest(){
        Color [][] pixelGrid = createPixelGrid(40, 20);
        try{
            Color [][] newPixelGrid = ImageController.DecreaseImageSize(pixelGrid, 4);
            int height = newPixelGrid.length;
            int width = newPixelGrid[0].length;
            assertTrue((height==10)&&(width==5));
        } catch (Exception e){
            fail("Cannot decrease image size due to exception: " + e);
        }
    }

    /**
     * Attempts to rotate image to the right, compares rotated pixel grid to expected pixel grid
     * Fails if dimensions are not the same as expected
     * Fails if pixels of rotated and expected grids do not match
     */
    @Test
    public void RotateImageRightTest(){
        Color [][] pixelGrid = createPixelGrid(2, 3);
        Color [][] correctGrid = createPixelGrid(3, 2);
        pixelGrid[0][1] = new Color(0,0,0);
        correctGrid[1][1] = new Color(0,0,0);

        Color [][] rotatedGrid = ImageController.RotateImageRight(pixelGrid);
        assertEquals(correctGrid.length,rotatedGrid.length);
        assertEquals(correctGrid[0].length,rotatedGrid[0].length);

        checkSame:
        for (int i = 0; i < correctGrid.length; i++){
            for (int j = 0; j < correctGrid[0].length; j++){
                if (correctGrid[i][j].getRGB() != rotatedGrid[i][j].getRGB()){
                    fail("One or more pixels do not match, index[" +  i + "][" + j + "]");
                    break checkSame;
                }
            }
        }
    }

    /**
     * Attempts to rotate image to the left, compares rotated pixel grid to expected pixel grid
     * Fails if dimensions are not the same as expected
     * Fails if pixels of rotated and expected grids do not match
     */
    @Test
    public void RotateImageLeftTest(){
        Color [][] pixelGrid = createPixelGrid(2, 3);
        Color [][] correctGrid = createPixelGrid(3, 2);
        pixelGrid[0][1] = new Color(0,0,0);
        correctGrid[1][0] = new Color(0,0,0);

        Color [][] rotatedGrid = ImageController.RotateImageLeft(pixelGrid);
        assertEquals(correctGrid.length,rotatedGrid.length);
        assertEquals(correctGrid[0].length,rotatedGrid[0].length);

        checkSame:
        for (int i = 0; i < correctGrid.length; i++){
            for (int j = 0; j < correctGrid[0].length; j++){
                if (correctGrid[i][j].getRGB() != rotatedGrid[i][j].getRGB()){
                    fail("One or more pixels do not match, index[" +  i + "][" + j + "]");
                    break checkSame;
                }
            }
        }
    }

    /**
     * Attempts to flip image horizontally, compares flipped pixel grid to expected pixel grid
     * Fails if flipped and expected pixels do not match
     */
    @Test
    public void FlipImageHorizontalTest(){
        Color [][] pixelGrid = createPixelGrid(2, 3);
        pixelGrid[0][0] = new Color(0,0,0);
        pixelGrid[1][2] = new Color(42,42,42);
        Color [][] correctGrid = createPixelGrid(2, 3);
        correctGrid[0][2] = new Color(0,0,0);
        correctGrid[1][0] = new Color(42,42,42);

        Color [][] flippedGrid = ImageController.FlipImageHorizontal(pixelGrid);

        checkSame:
        for(int i = 0; i < correctGrid.length; i++){
            for(int j = 0; j < correctGrid[0].length; j++){
                if(correctGrid[i][j].getRGB() != flippedGrid[i][j].getRGB()){
                    fail("One or more pixels do not match, index[" + i + "][" + j + "]");
                    break checkSame;
                }
            }
        }
    }

    /**
     * Attempts to flip image vertically, compares flipped pixel grid to expected pixel grid
     * Fails if flipped and expected pixels do not match
     */
    @Test
    public void FlipImageVerticalTest(){
        Color [][] pixelGrid = createPixelGrid(2, 3);
        pixelGrid[0][0] = new Color(0,0,0);
        pixelGrid[1][2] = new Color(42,42,42);
        Color [][] correctGrid = createPixelGrid(2, 3);
        correctGrid[1][0] = new Color(0,0,0);
        correctGrid[0][2] = new Color(42,42,42);

        Color [][] flippedGrid = ImageController.FlipImageVertical(pixelGrid);

        checkSame:
        for(int i = 0; i < correctGrid.length; i++){
            for(int j = 0; j < correctGrid[0].length; j++){
                if(correctGrid[i][j].getRGB() != flippedGrid[i][j].getRGB()){
                    fail("One or more pixels do not match, index[" + i + "][" + j + "]");
                    break checkSame;
                }
            }
        }
    }
}
