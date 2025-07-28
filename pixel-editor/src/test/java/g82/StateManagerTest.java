package g82;

import g82.controllers.ImageController;
import g82.managers.LayerManager;
import g82.managers.StateManager;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.Before;

import java.awt.Color;

import g82.models.GlobalStateModel;
import g82.models.IStateModel;
import g82.models.LayerModel;
import g82.models.LocalStateModel;
import g82.models.StateResult;

/**
 * Unit tests for StateManager class.
 * Stores pixel grids for version control - undo/redo tool.
 * Testing initialisation, saving new versions onto manager, undo/redo (+able-to-check)
 * get current version, and reset states
 */
public class StateManagerTest {
    private static final int MAX_STATES = 10;
    private StateManager stateMan;
    private int h;
    private int w;
    private int [] rgb = {255,255,255};

    /**
     * Create state manager object prepared for use in each test
     */
    @Before
    public void setUp() throws Exception {
        h = 10;
        w = 16;
        LayerModel lm = new LayerModel(this.createPixelGrid(h, w, rgb), 0, "1");
        stateMan = new StateManager(lm);
    }

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

    /**
     * Create array of size n, each element is a pixel grid
     * @param h - Height of pixel grid
     * @param w - Width of pixel grid
     * @param rgb - Array of rgb values [red,blue,green]
     * @param n - Number of pixel grids in array
     * @return - Array of size n, h x w solid colour pixel grids 
     */
    public Color[][][] createNGrids(int h, int w, int[] rgb, int n){
        Color[][][] pixelGrids = new Color[n][h][w];        
        for (int i = 0; i < n; i++){
            pixelGrids[i] = createPixelGrid(h, w, rgb);
        }
        return pixelGrids;
    }

    //Compares two pixel grids to check RGB values match at each pixel
    public boolean arePixelGridsSame(Color[][] A, Color[][] B){
        //If dimensions do not match, then not same and return false
        if(A.length != B.length || A[0].length != B[0].length){
            return(false);
        }

        for(int i = 0; i < A.length; i++){
            for(int j = 0; j < A[0].length; j++){
                if(A[i][j] == null || B[i][j] == null){
                    return(false);
                } else if(
                    A[i][j].getRGB() != B[i][j].getRGB()){
                    return(false);
                }
            }
        }

        return(true);
    }

    /**
     * Tests if object instance of StateManager class initialises
     * Fails if object does not initialise
     */
    @Test
    public void InitialisationTest() {
        assertFalse("State Manager object cannot initialise", (stateMan == null));
    }

    @Test
    public void SaveLocalStateTest(){
        Color[][][] pixelGrids = createNGrids(h, w, rgb, MAX_STATES+1);

        try {
            LayerModel layer = new LayerModel(pixelGrids[0], 1, "LocalSaveTest");

            try {
                for (int i = 0; i < MAX_STATES-1; i++){
                    stateMan.SaveLocalState(layer, pixelGrids[i], pixelGrids[i+1]);
                }
            } catch (Exception a) {
                fail("SaveLocalStateTest failed due to: " + a);
            }

        } catch (Exception e){
            fail("SaveLocalStateTest failed as Layer Model cannot be initialised: " + e);
        }
    }

    @Test
    public void SaveGlobalStateTest(){
        GlobalStateModel.Op[] operations = {GlobalStateModel.Op.RESIZE,GlobalStateModel.Op.ROTATE_RIGHT,GlobalStateModel.Op.ROTATE_LEFT,GlobalStateModel.Op.CROP,GlobalStateModel.Op.FLIPH,GlobalStateModel.Op.FLIPV};
        int [][] dimensions = {{h,h/2,w/2,h/2,(h/2)-1,(h/2)-1,(h/2)-1},{w,w/2,h/2,w/2,(w/2)-1,(w/2)-1,(w/2)-1}};

        try {
            for (int i = 0; i < operations.length; i++){
                int oldH = dimensions[0][i];
                int oldW = dimensions[1][i];
                int newH = dimensions[0][i+1];
                int newW = dimensions[1][i+1];
                stateMan.SaveGlobalState(operations[i], newW, newH, oldW, oldH);
            }
        } catch (Exception e) {
            fail("SaveGlobalStateTest failed due to: " + e);
        }
    }

    @Test
    public void SaveLayerStateTest(){
        LayerManager [] managers = new LayerManager[MAX_STATES];
        for (int i = 0; i < MAX_STATES; i++){
            int [] newRGB = {100+i,100+i,100+i};
            managers[i] = new LayerManager(createPixelGrid(h, w, newRGB));
        }
        
        try {
            for (int i = 0; i < MAX_STATES-1; i++){
                stateMan.SaveLayerState(managers[i],managers[i+1],i,i+1);
            }
        } catch (Exception e) {
            fail("SaveLayerStateTest failed due to: " + e);
        }
    }

    @Test
    public void GetCurStateTest(){
        int[] newRGB = {123,124,125};
        Color [][] originalGrid = createPixelGrid(h, w, rgb);
        Color [][] newGrid = createPixelGrid(h, w, newRGB);

        try{
            LayerModel layer = new LayerModel(originalGrid,1,"TargetLayer");
            stateMan.SaveLocalState(layer, originalGrid, newGrid);
            IStateModel currentState  = stateMan.GetCurState();

            try{
                assertSame(IStateModel.Locality.LOCAL, currentState.GetLocality());
                LocalStateModel localState = (LocalStateModel) currentState;
                
                assertTrue(arePixelGridsSame(originalGrid, localState.GetOldPixelGrid()));
                assertTrue(arePixelGridsSame(newGrid,localState.GetNewPixelGrid()));
            } catch (Exception a) {
                fail("GetCurStateTest failed as current state returned not same as expected: " + a);
            }

        } catch (Exception e) {
            fail("GetCurStateTest failed as layer model could not be created due to: " + e);
        }
    }

    @Test
    public void UndoStateTest(){
        int[] interRGB = {123,234,159};
        int[] lastRGB = {4,5,7};
        Color[][] interGrid = createPixelGrid(h, w, interRGB);
        Color[][] lastGrid = createPixelGrid(h, w, lastRGB);
        LayerManager firstMan = new LayerManager(createPixelGrid(h, w, rgb));
        LayerManager interMan = new LayerManager(interGrid);
        LayerManager lastMan = new LayerManager(lastGrid);

        stateMan.SaveLayerState(firstMan, interMan,-1,0);
        stateMan.SaveLayerState(interMan, lastMan,0,1);

        try{
            LayerModel currentLayer = new LayerModel(lastGrid, 1, "LastGrid");

            try{
                stateMan.UndoState(lastMan, currentLayer);
            } catch (Exception a) {
                fail("UndoStateTest failed as undo could not be actioned: " + a);
            }
            
        } catch (Exception e) {
            fail("UndoStateTest failed as LayerModel could not be produced: " + e);
        }
    }

    @Test
    public void RedoStateTest(){
        int[] interRGB = {123,234,159};
        int[] lastRGB = {4,5,7};
        Color[][] lastGrid = createPixelGrid(h, w, lastRGB);
        Color[][] interGrid = createPixelGrid(h, w, interRGB);
        LayerManager firstMan = new LayerManager(createPixelGrid(h, w, rgb));
        LayerManager interMan = new LayerManager(interGrid);
        LayerManager lastMan = new LayerManager(lastGrid);

        stateMan.SaveLayerState(firstMan, interMan,0,0);
        stateMan.SaveLayerState(interMan, lastMan,0,0);

        try{
            LayerModel interLayer = new LayerModel(interGrid, 1, "IntermediateGrid");
            LayerModel currentLayer = new LayerModel(lastGrid, 1, "LastGrid");
            StateResult undoManager = stateMan.UndoState(lastMan, interLayer);

            try{
                StateResult redoManager = stateMan.RedoState(undoManager.layerManager, undoManager.curLayer);
            } catch (Exception a) {
                fail("RedoStateTest failed as redo could not be actioned: " + a);
            }
            //StateResult redoManager = stateMan.RedoState(undoManager.layerManager, currentLayer);
            //assertSame(redoManager.curLayer,currentLayer);
        } catch (Exception e) {
            fail("RedoStateTest failed as LayerModel could not be produced: " + e);
        }
    }

    @Test
    public void canRedoTest(){
        assertFalse(stateMan.canRedo());

        Color[][] oldPixelGrid = createPixelGrid(h, w, rgb);
        Color[][] currentPixelGrid = createPixelGrid(h, w, rgb);
        LayerManager firstMan = new LayerManager(createPixelGrid(h, w, rgb));
        LayerManager nextMan = new LayerManager(currentPixelGrid);
        try{
            LayerModel oldLayer = new LayerModel(oldPixelGrid, 1, "Old Pixel Grid");
            LayerModel currentLayer = new LayerModel(currentPixelGrid, 1, "Current Pixel Grid");
            try{
                stateMan.SaveLayerState(firstMan, nextMan,0,0);
                assertFalse(stateMan.canRedo());
            } catch (Exception a) {
                fail("canRedoTest failed as returns true when unable to redo: " + a);
            }
            
            try{
                stateMan.UndoState(nextMan, oldLayer);
                assertTrue(stateMan.canRedo());
            } catch (Exception b) {
                fail("canRedoTest failed as returns false when able to redo");
            }
            
        } catch (Exception e) {
            fail("canRedoTest failed as layer model could not be created due to: " + e);
        }
    }

    @Test
    public void canUndoTest(){
        assertFalse(stateMan.canUndo());

        Color[][] oldPixelGrid = createPixelGrid(h, w, rgb);
        Color[][] currentPixelGrid = createPixelGrid(h, w, rgb);
        LayerManager firstMan = new LayerManager(oldPixelGrid);
        LayerManager nextMan = new LayerManager(currentPixelGrid);

        try{
            LayerModel oldLayer = new LayerModel(oldPixelGrid, 1, "Old Pixel Grid");
            
            try{
                stateMan.SaveLayerState(firstMan, nextMan,0,0);
                assertTrue(stateMan.canUndo());
            } catch (Exception a) {
                fail("canUndoTest failed as returns false when is able to undo: " + a);
            }
            
            try {
                stateMan.UndoState(nextMan, oldLayer);
                assertFalse(stateMan.canUndo());
            } catch (Exception b) {
                fail("canUndoTest failed as returns true when is unable to undo: " + b);
            }

        } catch (Exception e) {
            fail("canUndoTest failed as layer model could not be created due to: " + e);
        }
    }

    @Test
    public void ResetStatesTest(){
        LayerManager firstMan = new LayerManager(createPixelGrid(h, w, rgb));
        LayerManager nextMan = new LayerManager(createPixelGrid(h, w, rgb));
        stateMan.SaveLayerState(firstMan, nextMan,0,0);

        stateMan.ResetStates();
        assertTrue(stateMan.GetCurState()==null);
    }
}