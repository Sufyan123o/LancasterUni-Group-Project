package g82.managers;

// some parts will probably need to be extracted into other classes in line with class diagram

import java.awt.Color;

import g82.CanvasPanel;
import g82.controllers.ImageController;
import g82.models.GlobalStateModel;
import g82.models.IStateModel;
import g82.models.LayerModel;
import g82.models.LayerStateModel;
import g82.models.LocalStateModel;
import g82.models.StateResult;

// uses an array to manage the undo/redo of different buffered images that are chosen to be saved as states
// saving state should be after each action is performed i.e. filling, drawing a line, rotating image etc.
// 3 types of states for different actions: Global, Single Layer (Local) and LayerManager modifictions.
public class StateManager {
    private int curIndex;
    private int redoLim;
    private int undoLim;
    private IStateModel[] states;
    private static final int MAX_STATES = 10;

    public StateManager(LayerModel lm) {
        this.curIndex = 0;
        this.redoLim = 0; 
        this.undoLim = 0; 
        this.states = new IStateModel[MAX_STATES];
        for (int i = 0; i < MAX_STATES; i++) {
            this.states[i] = new LocalStateModel(lm, ImageController.ClonePixelGrid(lm.getPixelGrid()), ImageController.ClonePixelGrid(lm.getPixelGrid()));
        }
    }

    public void SaveLocalState(LayerModel lm, Color[][] oldPixelGrid, Color[][] newPixelGrid) {
        curIndex = (curIndex + 1) % MAX_STATES;
        redoLim = curIndex;

        if (undoLim == -1) { 
            undoLim = 0;
        } else if (curIndex == undoLim) {
            undoLim = (undoLim + 1) % MAX_STATES;
        }

        LocalStateModel sm = new LocalStateModel(lm, oldPixelGrid, newPixelGrid);
        states[curIndex] = sm;
    }

    public void SaveGlobalState(GlobalStateModel.Op op, int newWidth, int newHeight, int oldWidth, int oldHeight) {
        curIndex = (curIndex + 1) % MAX_STATES;
        redoLim = curIndex;

        if (undoLim == -1) {
            undoLim = 0;
        } else if (curIndex == undoLim) {
            undoLim = (undoLim + 1) % MAX_STATES;
        }
        
        GlobalStateModel sm = new GlobalStateModel(op, newWidth, newHeight, oldWidth, oldHeight);
        states[curIndex] = sm;
    }

    public void SaveGlobalState(LayerStateModel lsm, GlobalStateModel.Op op, int newWidth, int newHeight, int oldWidth, int oldHeight) {
        curIndex = (curIndex + 1) % MAX_STATES;
        redoLim = curIndex;

        if (undoLim == -1) {
            undoLim = 0;
        } else if (curIndex == undoLim) {
            undoLim = (undoLim + 1) % MAX_STATES;
        }
        
        GlobalStateModel sm = new GlobalStateModel(lsm, op, newWidth, newHeight, oldWidth, oldHeight);
        states[curIndex] = sm;
    }
    
    public void SaveLayerState(LayerManager prevLayerManager, LayerManager newLayerManager, int oldLayerNum, int newLayerNum) {
        curIndex = (curIndex + 1) % MAX_STATES;
        redoLim = curIndex;
        
        if (undoLim == -1) {
            undoLim = 0;
        } else if (curIndex == undoLim) {
            undoLim = (undoLim + 1) % MAX_STATES;
        }
        
        LayerStateModel sm = new LayerStateModel(prevLayerManager, newLayerManager, oldLayerNum, newLayerNum);
        states[curIndex] = sm;
    }

    public IStateModel GetCurState() {
        return states[Math.max(0,curIndex)]; //If states have just been reset (curIndex=-1) and this is called, returns state 0 avoiding errors
    }

    public StateResult UndoState(LayerManager layerManager, LayerModel curLayer) throws Exception {
        if (undoLim == curIndex || curIndex == -1) {
            throw new Exception("Undo limit hit");
        }

        IStateModel sm = states[curIndex];
        if (sm.GetLocality().equals(IStateModel.Locality.LOCAL)) {
            LocalStateModel lsm = (LocalStateModel)sm;
            curLayer.setPixelGrid(ImageController.ClonePixelGrid(lsm.GetOldPixelGrid()));
            curIndex = (curIndex - 1 + MAX_STATES) % MAX_STATES;
            return new StateResult(layerManager, curLayer);
        } else if (sm.GetLocality().equals(IStateModel.Locality.LAYER)) {
            LayerStateModel lsm = (LayerStateModel)sm;
            layerManager = lsm.GetOldLayerManager().clone();
            curLayer = layerManager.FindLayer(lsm.GetOldLayerNum()); // Prevents a non existent layer from being selected
            curIndex = (curIndex - 1 + MAX_STATES) % MAX_STATES;
            return new StateResult(layerManager, curLayer);
        } else{
            GlobalStateModel gsm = (GlobalStateModel)sm;
            // Replay the opposite global state change.
            switch (gsm.GetOp()) {
                case RESIZE:
                layerManager = gsm.GetOldLayerManager().clone();
                curLayer = layerManager.FindLayer(gsm.GetOldLayerNum());
                break;
                case CROP:
                layerManager = gsm.GetOldLayerManager().clone();
                curLayer = layerManager.FindLayer(gsm.GetOldLayerNum());
                break;
                case ROTATE_LEFT:
                layerManager.RotateLayersRight(curLayer);
                break;
                case ROTATE_RIGHT:
                layerManager.RotateLayersLeft(curLayer);
                break;
                case FLIPH:
                layerManager.FlipLayersH(curLayer);
                break;
                case FLIPV:
                layerManager.FlipLayersV(curLayer);
                break;
            }
            
            layerManager.SetHeight(gsm.GetOldHeight());
            layerManager.SetWidth(gsm.GetOldWidth());
            CanvasPanel.setCols(gsm.GetOldWidth());
            CanvasPanel.setRows(gsm.GetOldHeight());
            curIndex = (curIndex - 1 + MAX_STATES) % MAX_STATES;
            return new StateResult(layerManager, curLayer);
        }
    }

    public StateResult RedoState(LayerManager layerManager, LayerModel curLayer) throws Exception {
        if (redoLim == curIndex) {
            throw new Exception("Redo limit hit");
        }

        curIndex = (curIndex + 1) % MAX_STATES;
        IStateModel sm = states[curIndex];
        if (sm.GetLocality().equals(IStateModel.Locality.LOCAL)) {
            LocalStateModel lsm = (LocalStateModel)sm;
            curLayer.setPixelGrid(ImageController.ClonePixelGrid(lsm.GetNewPixelGrid()));
            return new StateResult(layerManager, curLayer);
        } else if (sm.GetLocality().equals(IStateModel.Locality.LAYER)) {
            LayerStateModel lsm = (LayerStateModel)sm;
            layerManager = lsm.GetNewLayerManager().clone();
            curLayer = layerManager.FindLayer(lsm.GetNewLayerNum());  // Prevents a non existent layer from being selected
            return new StateResult(layerManager, curLayer);
        } else{
            GlobalStateModel gsm = (GlobalStateModel)sm;
            // Replay the global state change.
            switch (gsm.GetOp()) {
                case RESIZE:
                layerManager = gsm.GetNewLayerManager().clone();
                curLayer = layerManager.FindLayer(gsm.GetNewLayerNum());
                break;
                case CROP:
                layerManager = gsm.GetNewLayerManager().clone();
                curLayer = layerManager.FindLayer(gsm.GetNewLayerNum());
                break;
                case ROTATE_LEFT:
                layerManager.RotateLayersLeft(curLayer);
                break;
                case ROTATE_RIGHT:
                layerManager.RotateLayersRight(curLayer);
                break;
                case FLIPH:
                layerManager.FlipLayersH(curLayer);
                break;
                case FLIPV:
                layerManager.FlipLayersV(curLayer);
                break;
            }
            
            layerManager.SetHeight(gsm.GetNewHeight());
            layerManager.SetWidth(gsm.GetNewWidth());
            CanvasPanel.setCols(gsm.GetNewWidth());
            CanvasPanel.setRows(gsm.GetNewHeight());
            return new StateResult(layerManager, curLayer);
        }
    }

    public void ResetStates() {
        states = new IStateModel[MAX_STATES];
        curIndex = -1;
        redoLim = -1;
        undoLim = -1;
    }

    public boolean canRedo() {
        return redoLim != curIndex;
    }

    public boolean canUndo() {
        return undoLim != curIndex && curIndex != -1;
    }

    public void PrintStates() { // function for debug
        for (int i = 0; i < redoLim; i++) {
            IStateModel sm = states[i];
            if (sm.GetLocality().equals(IStateModel.Locality.LOCAL)) {
                LocalStateModel lsm = (LocalStateModel)sm;
                System.out.println("----------");
                ImageController.printGrid(lsm.GetOldPixelGrid());
                ImageController.printGrid(lsm.GetNewPixelGrid());
                System.out.println("----------");
            }
        }
    }
}