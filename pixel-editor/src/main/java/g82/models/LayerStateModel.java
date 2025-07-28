package g82.models;

import g82.managers.LayerManager;

// Class for saving edits relating to layers e.g. add layer, remove layer, reorder layer, etc...
public class LayerStateModel implements IStateModel {
    private LayerManager prevLayerManager;
    private LayerManager newLayerManager;
    private int oldLayerNum;
    private int newLayerNum;
    public LayerStateModel(LayerManager prevLayerManager, LayerManager newLayerManager, int oldLayerNum, int newLayerNum) {
        try {
            this.prevLayerManager = prevLayerManager.clone();
            this.newLayerManager = newLayerManager.clone();
            this.oldLayerNum = oldLayerNum;
            this.newLayerNum = newLayerNum;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public Locality GetLocality() {
        return Locality.LAYER;
    }

    public LayerManager GetNewLayerManager() {
        return this.newLayerManager;
    }

    public LayerManager GetOldLayerManager() {
        return this.prevLayerManager;
    }

    public int GetOldLayerNum() {
        return this.oldLayerNum;
    }
    public int GetNewLayerNum() {
        return this.newLayerNum;
    }
}
