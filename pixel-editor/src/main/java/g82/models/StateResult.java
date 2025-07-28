package g82.models;

import g82.managers.LayerManager;

// Return object for StateManager Undo and Redo methods
public class StateResult {
    public final LayerManager layerManager;
    public final LayerModel curLayer;

    public StateResult(LayerManager lm, LayerModel cl) {
        this.layerManager = lm;
        this.curLayer = cl;
    }
}
