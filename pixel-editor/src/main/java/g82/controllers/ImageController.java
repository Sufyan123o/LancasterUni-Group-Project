package g82.controllers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import g82.CanvasPanel;

public class ImageController {
    // gets image from location and stores as a buffered image
    public static Color[][] ImportImage(String fileLoc) throws Exception {
        if (!fileLoc.endsWith(".png")) {
            throw new Exception("Filename did not include .png suffix");
        }

        File input = new File(fileLoc);
        if (!input.exists()) {
            throw new Exception("Provided file does not exist");
        }
        BufferedImage img = ImageIO.read(input);
        
        Color[][] newGrid = new Color[img.getHeight()][img.getWidth()];
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                newGrid[j][i] = new Color(img.getRGB(i, j), true);
            }
        }
        
        if (img.getHeight() > 800 || img.getWidth() > 800) {
            newGrid = NNIResize(newGrid, 800, 800);
            CanvasPanel.setCols(800);
            CanvasPanel.setRows(800);
        } else {
            CanvasPanel.setCols(img.getHeight());
            CanvasPanel.setRows(img.getWidth());
        }

        return newGrid;
    }

    // stores image at location given, this can be a relative or full path also include .png at end of file name
    public static void ExportImage2PNG(Color[][] pixelGrid, String fileLoc) throws Exception {
        if (pixelGrid.length == 0 || pixelGrid[0].length == 0) {
            throw new Exception("Empty pixelGrid");
        }

        if (!fileLoc.endsWith(".png")) {
            throw new Exception("Filename did not include .png suffix");
        }

        int width = pixelGrid[0].length;
        int height = pixelGrid.length;   
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelGrid[y][x]; 
                int rgb = color.getRGB();    
                image.setRGB(x, y, rgb);     
            }
        }

        File outputFile = new File(fileLoc);
        ImageIO.write(image, "png", outputFile);
    }

    // Nearest Neighbour Interpolation is now used.
    @Deprecated
    public static Color[][] IncreaseImageSize(Color[][] pixelGrid, int factor) throws Exception {
        if (pixelGrid.length == 0 || pixelGrid[0].length == 0) {
            return new Color[0][0];
        }
    
        if (factor < 1) {
            throw new Exception("Factor out of range");
        }

        int prevHeight = pixelGrid.length;     
        int prevWidth = pixelGrid[0].length;    
        int newHeight = prevHeight*factor;
        int newWidth = prevWidth*factor;
        Color[][] newGrid = new Color[newHeight][newWidth]; 
        for (int i = 0; i < prevHeight; i++) {
            for (int j = 0; j < prevWidth; j++) {
                for (int x = i*factor; x < (i*factor)+factor; x++) {
                    for (int y = j*factor; y < (j*factor)+factor; y++) {
                        newGrid[x][y] = pixelGrid[i][j];
                    }
                }
            }
        }
        return newGrid;
    }

    // Nearest Neighbour Interpolation is now used.
    @Deprecated
    public static Color[][] DecreaseImageSize(Color[][] pixelGrid, int factor) throws Exception {
        if (pixelGrid.length == 0 || pixelGrid[0].length == 0) {
            return new Color[0][0];
        }
    
        int prevHeight = pixelGrid.length;     
        int prevWidth = pixelGrid[0].length;    
        if (factor < 1 || factor > prevHeight || factor > prevWidth || prevWidth % factor != 0 || prevHeight % factor != 0) {
            throw new Exception("Factor out of range or not divisible");
        }

        int newHeight = prevHeight/factor;
        int newWidth = prevWidth/factor;
        Color[][] newGrid = new Color[newHeight][newWidth]; 
        for (int i = 0; i < newHeight; i++) {
            for (int j = 0; j < newWidth; j++) {
                newGrid[i][j] = pixelGrid[i*factor][j*factor];
            }
        }

        return newGrid;
    }

    // [0 , 1] -> [0,
    //             1] 
    public static Color[][] RotateImageRight(Color[][] pixelGrid) {
        if (pixelGrid.length == 0 || pixelGrid[0].length == 0) {
            return new Color[0][0];
        }
    
        int prevHeight = pixelGrid.length;     
        int prevWidth = pixelGrid[0].length;    
        Color[][] newGrid = new Color[prevWidth][prevHeight]; 
    
        for (int i = 0; i < prevHeight; i++) {
            for (int j = 0; j < prevWidth; j++) {
                newGrid[j][prevHeight - 1 - i] = pixelGrid[i][j]; 
            }
        }
    
        return newGrid;
    }

    // [0 , 1] -> [1,
    //             0] 
    public static Color[][] RotateImageLeft(Color[][] pixelGrid) {
        if (pixelGrid.length == 0 || pixelGrid[0].length == 0) {
            return new Color[0][0];
        }
    
        int prevHeight = pixelGrid.length;     
        int prevWidth = pixelGrid[0].length;   
        Color[][] newGrid = new Color[prevWidth][prevHeight]; 
    
        for (int i = 0; i < prevHeight; i++) {
            for (int j = 0; j < prevWidth; j++) {
                newGrid[prevWidth - 1 - j][i] = pixelGrid[i][j]; 
            }
        }
    
        return newGrid;
    }

    // [0, 1] -> [1, 0]
    public static Color[][] FlipImageHorizontal(Color[][] pixelGrid) {
        if (pixelGrid.length == 0 || pixelGrid[0].length == 0) {
            return new Color[0][0];
        }
    
        int prevHeight = pixelGrid.length;     
        int prevWidth = pixelGrid[0].length;   
        Color[][] newGrid = new Color[prevHeight][prevWidth]; 
    
        for (int i = 0; i < prevHeight; i++) {
            for (int j = prevWidth - 1; j >= 0; j--) {
                newGrid[i][prevWidth-1-j] = pixelGrid[i][j]; 
            }
        }
    
        return newGrid;
    }

    // [0, -> [1,
    // 1]     0]
    public static Color[][] FlipImageVertical(Color[][] pixelGrid) {
        if (pixelGrid.length == 0 || pixelGrid[0].length == 0) {
            return new Color[0][0];
        }
    
        int prevHeight = pixelGrid.length;     
        int prevWidth = pixelGrid[0].length;   
        Color[][] newGrid = new Color[prevHeight][prevWidth]; 
    
        for (int i = prevHeight - 1; i >= 0; i--) {
            for (int j = 0; j < prevWidth; j++) {
                newGrid[prevHeight-1-i][j] = pixelGrid[i][j]; 
            }
        }
    
        return newGrid;
    }

    // expects coordinates of where the user first sets the crop and where they drag it to.
    public static Color[][] CropGrid(Color[][] grid, int x1, int y1, int x2, int y2) {
        int newWidth = Math.abs(x1 - x2) + 1;
        int newHeight = Math.abs(y1 - y2) + 1;
        Color[][] newGrid = new Color[newHeight][newWidth];
        if (x2 < x1) {
            int tmp = x1;
            x1 = x2;
            x2 = tmp;
        }

        if (y2 < y1) {
            int tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                newGrid[j-y1][i-x1] = grid[j][i];
            }
        }

        return newGrid;
    }

    // Gaussian Convolution Kernel
    static public double[][] gConvolution = {
        {1.0 / 16, 1.0 / 8, 1.0 / 16},
        {1.0 / 8,  1.0 / 4, 1.0 / 8},
        {1.0 / 16, 1.0 / 8, 1.0 / 16}
    };

    // Sharpen Convolution Kernel
    static public double[][] sConvolution = {
    {  -1, -1,  -1 },
    { -1,  9, -1 },
    {  -1, -1,  -1 }
    };

    public static Color[][] Blur(Color[][] pixelGrid) { 
        return KernalOperation(pixelGrid, gConvolution);
    }

    public static Color[][] Sharpen(Color[][] pixelGrid) { 
        return KernalOperation(pixelGrid, sConvolution);
    }

    // Naive implementation of Kernal Convolutions, could be improved by using Fast Fourier Transform (FFT)
    private static Color[][] KernalOperation(Color[][] pixelGrid, double[][] kernel) { 
        Color[][] newGrid = new Color[pixelGrid.length][pixelGrid[0].length];
        for (int i = 0; i < pixelGrid.length; i++) {
            for (int j = 0; j < pixelGrid[0].length; j++) {
                double redSum = 0, greenSum = 0, blueSum = 0, alphaSum = 0;
                 // track out of bound access in order to proportionally apply the kernal convolution so it always totals 1
                double outOfBounds = 0;
                for (int k = -1; k <= 1; k++) {
                    if ((i + k) < 0 || (i + k) >= pixelGrid.length) {
                        outOfBounds += kernel[k+1][0] + kernel[k+1][1] + kernel[k+1][2];
                        continue;
                    } 

                    for (int l = -1; l <= 1; l++) {
                        if ((j + l) < 0 || (j + l) >= pixelGrid.length) {
                            outOfBounds += kernel[k+1][l+1];
                            continue;
                        } 

                        alphaSum += pixelGrid[i+k][j+l].getAlpha() * kernel[k+1][l+1];
                        redSum += pixelGrid[i+k][j+l].getRed() * kernel[k+1][l+1];
                        greenSum += pixelGrid[i+k][j+l].getGreen() * kernel[k+1][l+1];
                        blueSum += pixelGrid[i+k][j+l].getBlue() * kernel[k+1][l+1];
                    }
                }

                // normalise
                alphaSum = alphaSum * (1/(1-outOfBounds));
                redSum = redSum * (1/(1-outOfBounds));  
                greenSum = greenSum * (1/(1-outOfBounds));
                blueSum = blueSum * (1/(1-outOfBounds));
                
                // bound
                alphaSum = boundColour(alphaSum);
                redSum = boundColour(redSum);
                greenSum = boundColour(greenSum);
                blueSum = boundColour(blueSum);
                newGrid[i][j] = new Color(Math.round(Math.round(redSum)), Math.round(Math.round(greenSum)), Math.round(Math.round(blueSum)), Math.round(Math.round(alphaSum)));
            } 
        }

        return newGrid;
    }

    private static double boundColour(double c) {
        if (c > 255)
            c = 255;

        if (c < 0)
            c = 0;

        return c;
    }

    // Nearest Neighbour Interpolation
    public static Color[][] NNIResize(Color[][] pixelGrid, int newWidth, int newHeight) throws Exception {
        if (newWidth > 800 || newHeight > 800) throw new Exception("Resize too large");
        int oldHeight = pixelGrid.length;
        int oldWidth = pixelGrid[0].length;
        double xRatio = (double) oldWidth / newWidth;
        double yRatio = (double) oldHeight / newHeight;
        Color[][] newGrid = new Color[newHeight][newWidth];

        for (int i = 0; i < newHeight; i++) {
            for (int j = 0; j < newWidth; j++) {
                int srcX = Math.min(Math.round(Math.round(j * xRatio)), oldWidth - 1);
                int srcY = Math.min(Math.round(Math.round(i * yRatio)), oldHeight - 1);
                newGrid[i][j] = pixelGrid[srcY][srcX];
            }
        }

        return newGrid;
    }

    public static void printGrid(Color[][] grid) { // for testing imageController please ignore
        for (Color[] row : grid) {
            for (Color pixel : row) {
                System.out.print((pixel == null ? "NULL" : pixel.getRGB()) + "  ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // To prevent overlapping memory references
    public static Color[][] ClonePixelGrid(Color[][] grid) {
        if (grid == null) return null;
        Color[][] newGrid = new Color[grid.length][];
        for (int i = 0; i < grid.length; i++) {
            newGrid[i] = grid[i].clone(); 
        }
        return newGrid;
    }
}
