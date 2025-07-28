package g82.managers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import g82.controllers.ImageController;
import g82.models.LayerModel;

public class LayerManager implements Cloneable { // all layers must be the same size
    private List<LayerModel> layers;
    private int height;
    private int width;
    private int nameCount;

    /**
     * Creates Layer Manager holding up to 10 LayerModel objects
     * @param baseLayer - Color[][] pixel grid for base layer
     */
    public LayerManager(Color[][] baseLayer) {
        this.layers = new LinkedList<>();
        try {
            LayerModel lm = new LayerModel(baseLayer, 1, "1");
            this.layers.add(lm);
            this.height = baseLayer.length;
            this.width = baseLayer[0].length;
            this.nameCount = 2;
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public LayerModel AddLayer(Color[][] newLayer, float opacity) throws Exception {
        if (this.layers.size() >= 10) 
            throw new Exception("Layers overflow");
        
        if (newLayer.length != this.height || newLayer[0].length != this.width) 
            throw new Exception("The size of the new layer does not match");
        
        LayerModel lm = new LayerModel(newLayer, opacity, (String.valueOf(nameCount)));
        this.nameCount++;
        this.layers.add(lm);
        return lm;
    }

    //Method overload for default parameter opacity (alpha) = 1
    public LayerModel AddLayer(Color[][] newLayer) throws Exception{
        return AddLayer(newLayer,1);
    }

    public void RemoveLayer(int pos) throws Exception {
        if (this.layers.isEmpty()) 
            throw new Exception("Layers underflow");
        

        this.layers.remove(pos);
    }

    // returns -1 if the layer cannot be found
    public int GetLayerPos(LayerModel layer) {
        for (int i = 0; i < this.layers.size(); i++) {
            LayerModel l = this.FindLayer(i);
            if (layer.equals(l))
                return i;
        }

        return -1;
    }

    public LayerModel FindLayer(int pos) {
        return this.layers.get(pos);
    }

    public void ChangeLayerPos(int pos1, int pos2) {
        LayerModel tmp = this.FindLayer(pos1);
        this.layers.set(pos1, this.FindLayer(pos2));
        this.layers.set(pos2, tmp);
    }

    public int GetLayersSize() {
        return this.layers.size();
    }

    public LayerModel[] GetLayerArray(){
        LayerModel[] layerArray = new LayerModel[this.layers.size()];
        return this.layers.toArray(layerArray);
    }

    // Alpha compositing method - https://ciechanow.ski/alpha-compositing/
    // assumes a background colour of white
    // Destination - Background; Source - Foreground; Result - Output of layering src on dest.
    // RRGB = SRGB × SA + DRGB × (1 − SA)
    public Color[][] CondenseLayersAC() { //untested
        Color[][] newGrid = new Color[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double r = 0; 
                double g = 0;
                double b = 0;
                double a = 0.0;
                for (int k = 0; k < this.layers.size(); k++) {
                    LayerModel layer = this.layers.get(k);
                    if (!layer.isVisible()) continue;
                    Color px = layer.getPixelGrid()[i][j];
                    double srcA = (px.getAlpha() / 255.0) * layer.getOpacity();
                    double srcR = px.getRed();
                    double srcG = px.getGreen();
                    double srcB = px.getBlue();
                    
                    double rA = srcA + a * (1.0 - srcA);
                    if (rA > 0) {
                        r = (srcR * srcA + r * a * (1.0 - srcA)) / rA;
                        g = (srcG * srcA + g * a * (1.0 - srcA)) / rA;
                        b = (srcB * srcA + b * a * (1.0 - srcA)) / rA;
                    }

                    a = rA;
                }
                
                a = a * 255;
                newGrid[i][j] = new Color((int)r, (int)g, (int)b, (int)a);
            }
        }

        return newGrid;
    }

    // Weighted average method - ignore the fact that there are 2 methods at the moment
    // I will test both and see which is better
    // also need to find a way to deal with transparent pixels if we plan on doing that
    public Color[][] CondenseLayersWA() { // untested
        Color[][] newGrid = new Color[this.height][this.width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                float r = 0;
                float g = 0;
                float b = 0;
                int opacityTotal = 0;
                for (LayerModel layer : this.layers) 
                    opacityTotal += layer.getOpacity();
                
                for (int k = 0; k < this.layers.size(); k++) {
                    LayerModel layer = this.layers.get(k);
                    Color px = layer.getPixelGrid()[i][j];
                    
                    r += px.getRed() * (layer.getOpacity()/opacityTotal);
                    g += px.getGreen() * (layer.getOpacity()/opacityTotal);
                    b += px.getBlue() * (layer.getOpacity()/opacityTotal);
                }

                newGrid[i][j] = new Color((int)r, (int)g, (int)b);
            }
        }

        return newGrid;
    }   

    public void SetHeight(int height) {
        this.height = height;
    }

    public void SetWidth(int width) {
        this.width = width;
    }

    public int GetHeight() {
        return this.height;
    }

    public int GetWidth() {
        return this.width;
    }

    public void CropLayers(LayerModel currentLayer) {
        for (LayerModel lm : layers) {
            Color[][] pg = lm.getPixelGrid();
            pg = ImageController.CropGrid(pg, CropManager.getFirstX(), 
                CropManager.getFirstY(), CropManager.getLastX(), CropManager.getLastY());
            lm.setPixelGrid(pg);
            if (lm.equals(currentLayer)) 
                currentLayer.setPixelGrid(pg);
        }
    }

    public void NNIResizeLayers(LayerModel currentLayer, int newWidth, int newHeight) throws Exception {
        for (LayerModel lm : layers) {
            Color[][] pg = lm.getPixelGrid();
            pg = ImageController.NNIResize(pg, newWidth, newHeight);
            lm.setPixelGrid(pg);
            if (lm.equals(currentLayer)) 
                currentLayer.setPixelGrid(pg);
        }
    }

    public void RotateLayersLeft(LayerModel currentLayer) {
        for (LayerModel lm : layers) {
            Color[][] pg = lm.getPixelGrid();
            pg = ImageController.RotateImageLeft(pg);
            lm.setPixelGrid(pg);
            if (lm.equals(currentLayer)) 
                currentLayer.setPixelGrid(pg);
        }
    }

    public void RotateLayersRight(LayerModel currentLayer) {
        for (LayerModel lm : layers) {
            Color[][] pg = lm.getPixelGrid();
            pg = ImageController.RotateImageRight(pg);
            lm.setPixelGrid(pg);
            if (lm.equals(currentLayer)) 
                currentLayer.setPixelGrid(pg);
        }
    }

    public void FlipLayersH(LayerModel currentLayer) {
        for (LayerModel lm : layers) {
            Color[][] pg = lm.getPixelGrid();
            pg = ImageController.FlipImageHorizontal(pg);
            lm.setPixelGrid(pg);
            if (lm.equals(currentLayer)) 
                currentLayer.setPixelGrid(pg);
        }
    }

    public void FlipLayersV(LayerModel currentLayer) {
        for (LayerModel lm : layers) {
            Color[][] pg = lm.getPixelGrid();
            pg = ImageController.FlipImageVertical(pg);
            lm.setPixelGrid(pg);
            if (lm.equals(currentLayer)) 
                currentLayer.setPixelGrid(pg);
        }
    }

    @Override
    public LayerManager clone() throws CloneNotSupportedException {
        try {
            LayerManager copy = (LayerManager) super.clone();
            copy.layers = new ArrayList<>();
            for (LayerModel layer : this.layers) {
                copy.layers.add(layer.clone());
            }

            copy.height = this.height;
            copy.width = this.width;
            copy.nameCount = this.nameCount;
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); 
        }
    }
}
