package g82.models;
import java.awt.Color;

import g82.controllers.ImageController;
public class LayerModel implements Cloneable {
    private float opacity; // between 0 and 1 and technically the alpha value, but coverage is assumed as 100%
    private Color[][] pixelGrid;
    private String name;
    private boolean isVisible;

    public LayerModel(Color[][] pixelGrid, float opacity, String name) throws Exception {
        if (opacity > 1 || opacity < 0) {
            throw new Exception("Invalid opactity value");
        }
    
        this.opacity = opacity;
        this.pixelGrid = pixelGrid;
        this.name = name;
        this.isVisible = true;
    }
    public Color[][] getPixelGrid() {
        if (this.isVisible) {
            return this.pixelGrid;
        } else {
            Color[][] emptyGrid = new Color[pixelGrid.length][pixelGrid[0].length];
            for (int i = 0; i < pixelGrid.length; i++){
                for (int j = 0; j < pixelGrid[0].length; j++){
                    emptyGrid[i][j] = new Color(255,255,255,0);
                }
            }
            return emptyGrid;
        }
    }

    public float getOpacity() {
        return this.opacity;
    }

    public void setOpacity(float opacity) throws Exception {
        if (opacity > 1 || opacity < 0) {
            throw new Exception("Invalid opacity value (0 - 1)");
        }

        this.opacity = opacity;
    }

    public void setPixelGrid(Color[][] pixelGrid) {
        if (this.isVisible) {
            this.pixelGrid = pixelGrid;
        }
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void toggleVisibility(){
        this.isVisible = !this.isVisible;
    }

    public boolean isVisible(){
        return this.isVisible;
    }

    @Override
    public LayerModel clone() throws CloneNotSupportedException {
        try {
            LayerModel copy = (LayerModel) super.clone();
            copy.pixelGrid = ImageController.ClonePixelGrid(this.pixelGrid);
            copy.opacity = this.opacity;
            copy.isVisible = true;
            copy.name = name;
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // shouldn't happen if Cloneable is implemented
        }
    }
}
