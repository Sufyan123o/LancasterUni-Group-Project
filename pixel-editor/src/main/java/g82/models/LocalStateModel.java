package g82.models;

import java.awt.Color;

import g82.controllers.ImageController;

// Saves the state for just one layer, this makes it more efficient than using LayerStateModel
public class LocalStateModel implements IStateModel {
    private LayerModel lm;
    private Color[][] oldPixelGrid;
    private Color[][] newPixelGrid;
    public LocalStateModel(LayerModel lm, Color[][] oldPixelGrid, Color[][] newPixelGrid) {
        try {
            this.lm = lm.clone();
            this.newPixelGrid = ImageController.ClonePixelGrid(newPixelGrid);
            this.oldPixelGrid = ImageController.ClonePixelGrid(oldPixelGrid);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    @Override
    public Locality GetLocality() {
        return Locality.LOCAL;
    }

    public LayerModel GetLayer() {
        return this.lm;
    }
    
    public Color[][] GetNewPixelGrid() {
        return this.newPixelGrid;
    }
    public Color[][] GetOldPixelGrid() {
        return this.oldPixelGrid;
    }
}
