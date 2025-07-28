package g82.models;

import g82.managers.LayerManager;

// class for global state changes such as rotating, flipping, cropping and resizing
public class GlobalStateModel implements IStateModel {
    private Op op;
    private int newWidth;
    private int newHeight;
    private int oldWidth;
    private int oldHeight;
    private LayerStateModel lsm;
    
    // Resize, Crop
    public GlobalStateModel(LayerStateModel lsm, Op op, int newWidth, int newHeight, int oldWidth, int oldHeight) {
        try {
            this.lsm = lsm;
            this.op = op;
            this.newHeight = newHeight;
            this.newWidth = newWidth;
            this.oldHeight = oldHeight;
            this.oldWidth = oldWidth;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ROTATE_RIGHT, ROTATE_LEFT,FLIPH,FLIPV
    public GlobalStateModel(Op op, int newWidth, int newHeight, int oldWidth, int oldHeight) {
        this.op = op;
        this.newHeight = newHeight;
        this.newWidth = newWidth;
        this.oldHeight = oldHeight;
        this.oldWidth = oldWidth;
        this.lsm = null;
    }

    @Override
    public Locality GetLocality() {
        return Locality.GLOBAL;
    }

    public static enum Op {
        RESIZE,
        ROTATE_RIGHT,
        ROTATE_LEFT,
        CROP,
        FLIPH,
        FLIPV
    }

    public Op GetOp() {
        return this.op;
    }

    public int GetNewWidth() {
        return this.newWidth;
    }

    public int GetNewHeight() {
        return this.newHeight;
    }

    public int GetOldWidth() {
        return this.oldWidth;
    }

    public int GetOldHeight() {
        return this.oldHeight;
    }

    public LayerManager GetNewLayerManager() {
        return this.lsm.GetNewLayerManager();
    }

    public LayerManager GetOldLayerManager() {
        return this.lsm.GetOldLayerManager();
    }

    public int GetOldLayerNum() {
        return this.lsm.GetNewLayerNum();
    }
    public int GetNewLayerNum() {
        return this.lsm.GetOldLayerNum();
    }
}
