package g82;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import g82.controllers.ImageController;
import g82.managers.CropManager;
import g82.managers.LayerManager;
import g82.managers.StateManager;
import g82.models.GlobalStateModel;
import g82.models.LayerModel;
import g82.models.LayerStateModel;
import g82.models.StateResult;

public class CanvasPanel extends JPanel {
    private static final int MAX_ZOOM = 200; // maximum zoom factor
    private static final int MIN_ZOOM = 100; // minimum zoom factor
    private int zoomFactor = 100; // default zoom factor
    private static int ROWS;
    private static int COLS;
    private int GRID_SIZE;
    private Color[][] pixelGrid;
    private LayerManager layerManager;
    private LayerModel currentLayer;
    private Color[][] displayGrid;
    private Color currentColour = Color.BLACK; // default drawing colour is black
    private int hoverRow = -1, hoverCol = -1; // rouge values for the variables used in previewing the colour selected
                                              // on the selected pixel
    public String currentTool = "Brush"; // used for checking what tool is selected so the it knows what to do when
                                         // clicking, ie it stops it drawing if you selected the colour picker
    private StateManager stateManager;
    private Color[][] strokeStartState; // stores the state at the start of a stroke
    private LayerManager layerStartState;
    private int lastX = -1;
    private int lastY = -1;
    private String currentBlockName;
    private int baselineGridSize;
    private double scale = 1.0; // for the scroll to zoom functionality

    public CanvasPanel(int x, int y, int gridSize) {
        ROWS = x; // set dimensions
        COLS = y;
        this.GRID_SIZE = gridSize;
        this.baselineGridSize = gridSize;
        
        
        this.pixelGrid = new Color[ROWS][COLS];
        this.displayGrid = new Color[ROWS][COLS];
        
        this.layerManager = new LayerManager(pixelGrid);
        this.currentLayer = layerManager.FindLayer(0);
        stateManager = new StateManager(currentLayer);

        int width = (int) (COLS * GRID_SIZE * scale);
        int height = (int) (ROWS * GRID_SIZE * scale);
        setPreferredSize(new Dimension(width, height));

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                pixelGrid[i][j] = new Color(255, 255, 255, 0); // initalises the grids pixels to be all white
            }
        }

        currentLayer.setPixelGrid(pixelGrid);
        displayGrid = layerManager.CondenseLayersAC();

        // save initial blank state
        // saveLayerState(layerManager, layerManager);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = (int) (e.getX() / (GRID_SIZE * scale));
                int row = (int) (e.getY() / (GRID_SIZE * scale));

                lastX = col;  //reset last clicked point
                lastY = row;

                if (row < 0 || row >= ROWS || col < 0 || col >= COLS)
                    return;
                    
                if (currentTool.equals("Brush") || currentTool.equals("Erase")) {
                    // store initial state when starting a new stroke
                    strokeStartState = new Color[ROWS][COLS];
                    for (int i = 0; i < ROWS; i++) {
                        for (int j = 0; j < COLS; j++) {
                            strokeStartState[i][j] = new Color(
                                    pixelGrid[i][j].getRed(),
                                    pixelGrid[i][j].getGreen(),
                                    pixelGrid[i][j].getBlue(),
                                    pixelGrid[i][j].getAlpha());
                        }
                    }
                    try {
                        layerStartState = layerManager.clone();
                        
                    } catch (Exception err) {
                        System.out.println(err);
                    }
                    drawPixel(e, false);
                } 
                else if (currentTool.equals("Fill")) {
                    Color targetColor = pixelGrid[row][col];
                    Color replacementColor = currentColour;
                    // Only fill if target and replacement differ.
                    if (!targetColor.equals(replacementColor)) {
                        fillArea(row, col, targetColor, replacementColor);
                    }
                }
                else if (currentTool.equals("Picker")) {
                    getPixelColour(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // save state when completing a stroke
                if ((currentTool.equals("Brush") || currentTool.equals("Erase")) && strokeStartState != null) {
                    // check if any pixels changed
                    boolean changed = false;
                    for (int i = 0; i < ROWS && !changed; i++) {
                        for (int j = 0; j < COLS && !changed; j++) {
                            if (!pixelGrid[i][j].equals(strokeStartState[i][j])) {
                                changed = true;
                            }
                        }
                    }
                    if (changed) {
                        saveLocalState(strokeStartState, pixelGrid);
                    }
                    strokeStartState = null;
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentTool.equals("Brush") || currentTool.equals("Erase"))
                    drawPixel(e, true); // when moved whilst holding down the LMB
                if (currentTool.equals("Crop")) {
                    updateCrop(e);
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // when the mouse moves without the LMB being held
                int col = (int) (e.getX() / (GRID_SIZE * scale));
                int row = (int) (e.getY() / (GRID_SIZE * scale));
                if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                    int prevRow = hoverRow;
                    int prevCol = hoverCol;
                    hoverRow = row;
                    hoverCol = col;
                    int repaintCol = (int)(col * GRID_SIZE * scale)- brushSize/2;
                    int repaintRow = (int)(row * GRID_SIZE * scale)- brushSize/2;
                    int repaintSize = (int)(GRID_SIZE * scale * brushSize) + 1;
                    repaint(repaintCol, repaintRow, repaintSize * brushSize, repaintSize);
                    int repaintPrevCol = (int)(prevCol * GRID_SIZE * scale) - brushSize/2;
                    int repaintPrevRow = (int)(prevRow * GRID_SIZE * scale) - brushSize/2;
                    repaint(repaintPrevCol, repaintPrevRow, repaintSize, repaintSize);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.scale(scale, scale);

        drawCheckeredBackground(g2d);
        drawGrid(g2d);

        g2d.setColor(Color.BLACK);
        int borderRows = displayGrid.length;
        int borderCols = (borderRows > 0 ? displayGrid[0].length : 0);
        g2d.drawRect(0, 0, borderCols * GRID_SIZE - 1, borderRows * GRID_SIZE - 1);

        g2d.dispose();
    }

    private void drawGrid(Graphics g) {
        currentLayer.setPixelGrid(pixelGrid);
        // Recompute displayGrid by compacting all semi transparent pixels
        displayGrid = layerManager.CondenseLayersAC();
        //use displayGrid dimensions for safe indexing
        int displayRows = displayGrid.length;
        int displayCols = (displayRows > 0 ? displayGrid[0].length : 0);
        for (int row = 0; row < displayRows; row++) {
            int col = 0;
            while (col < displayCols) {
                Color startColor = displayGrid[row][col];
                int runStart = col;
                while (col < displayCols && displayGrid[row][col].equals(startColor)) {
                    col++;
                }
                int runWidth = col - runStart;
        
                g.setColor(startColor);
                g.fillRect(runStart * GRID_SIZE, row * GRID_SIZE, runWidth * GRID_SIZE, GRID_SIZE);
            }
        }

        if (hoverRow >= 0 && hoverCol >= 0) {
            g.setColor(new Color(currentColour.getRed(), currentColour.getGreen(), currentColour.getBlue(), 100));
            // clamp brush preview to displayGrid bounds
            int halfBrushSize = brushSize / 2;
            int startRow = Math.max(hoverRow - halfBrushSize, 0);
            int startCol = Math.max(hoverCol - halfBrushSize, 0);
            int endRow = Math.min(hoverRow + halfBrushSize - (brushSize % 2 == 0 ? 1 : 0), displayRows - 1);
            int endCol = Math.min(hoverCol + halfBrushSize - (brushSize % 2 == 0 ? 1 : 0), displayCols - 1);

            for (int r = startRow; r <= endRow; r++) {
                for (int c = startCol; c <= endCol; c++) {
                    g.fillRect(c * GRID_SIZE, r * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                }
            }
        }

        if (currentTool.equals("Crop") && CropManager.getFirstX() != -1 && CropManager.getFirstY() != -1) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(255, 0, 0, 100));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            int x = Math.min(CropManager.getFirstX(), CropManager.getLastX()) * GRID_SIZE;
            int y = Math.min(CropManager.getFirstY(), CropManager.getLastY()) * GRID_SIZE;
            int width = (Math.abs(CropManager.getLastX() - CropManager.getFirstX()) + 1) * GRID_SIZE;
            int height = (Math.abs(CropManager.getLastY() - CropManager.getFirstY()) + 1) * GRID_SIZE;
            g2d.fillRect(x, y, width, height);
            g2d.dispose();
        }
    }

    private void drawCheckeredBackground(Graphics2D g2d) {
        int checkerSize = 20; // unscaled checker size
        int canvasWidth = COLS * GRID_SIZE;
        int canvasHeight = ROWS * GRID_SIZE;
    
        g2d.setClip(0, 0, canvasWidth, canvasHeight);
    
        for (int y = 0; y < canvasHeight; y += checkerSize) {
            for (int x = 0; x < canvasWidth; x += checkerSize) {
                if (((x / checkerSize) + (y / checkerSize)) % 2 == 0)
                    g2d.setColor(new Color(240, 240, 240));
                else
                    g2d.setColor(new Color(210, 210, 210));
    
                g2d.fillRect(x, y, checkerSize, checkerSize);
            }
        }
    
        g2d.setClip(null);
    }
    
    private void drawPixel(MouseEvent e, boolean isStroke) {
        int col = (int) (e.getX() / (GRID_SIZE * scale));
        int row = (int) (e.getY() / (GRID_SIZE * scale));
        // Calculate half size to center the brush stroke.
        int half = brushSize / 2;
        if (isStroke) { 
            drawLine(lastX, col, lastY, row, half);
        }

        for (int r = row - half; r < row - half + brushSize; r++) {
            for (int c = col - half; c < col - half + brushSize; c++) {
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                    pixelGrid[r][c] = currentColour;
                }
            }
        }

        repaint();
        lastX = col;
        lastY = row;
    }

    // Bresenham's Line Algorithm: avoids floating point calculations
    // https://www.geeksforgeeks.org/bresenhams-line-generation-algorithm/
    private void drawLine(int x0, int x1, int y0, int y1, int half) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int xStep = (x1 > x0) ? 1 : -1;
        int yStep = (y1 > y0) ? 1 : -1;
        int x = x0;
        int y = y0;
    
        if (dx >= dy) { // Horizontal dominant line
            int D = 2 * dy - dx;
            for (int i = 0; i <= dx; i++) {
                for (int r = y - half; r < y - half + brushSize; r++) {
                    for (int c = x - half; c < x - half + brushSize; c++) {
                        if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                            pixelGrid[r][c] = currentColour;
                        }
                    }
                }

                if (D > 0) {
                    y += yStep;
                    D -= 2 * dx;
                }

                D += 2 * dy;
                x += xStep;
            }
        } else { // Vertically dominant line
            int D = 2 * dx - dy;
            for (int i = 0; i <= dy; i++) {
                for (int r = y - half; r < y - half + brushSize; r++) {
                    for (int c = x - half; c < x - half + brushSize; c++) {
                        if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                            pixelGrid[r][c] = currentColour;
                        }
                    }
                }

                if (D > 0) {
                    x += xStep;
                    D -= 2 * dy;
                }

                D += 2 * dx;
                y += yStep;
            }
        }
    }

    // For tracking the current pixel selections
    private void updateCrop(MouseEvent e) {
        int col = (int) (e.getX() / (GRID_SIZE * scale));
        int row = (int) (e.getY() / (GRID_SIZE * scale));
        CropManager.Update(col, row);
    }

    private void fillArea(int row, int col, Color targetColor, Color replacementColor) {
        try {
            LayerManager prev = layerManager.clone();
            if (targetColor.equals(replacementColor))
                return;
            Color[][] pg = ImageController.ClonePixelGrid(pixelGrid);
            java.util.Stack<Point> stack = new java.util.Stack<>();
            stack.push(new Point(col, row)); // Point(x, y) x is column and y is row
    
            while (!stack.isEmpty()) {
                Point p = stack.pop();
                int r = p.y;
                int c = p.x;
    
                // skip if out-of-bounds locations
                if (r < 0 || r >= ROWS || c < 0 || c >= COLS)
                    continue;
    
                // only fill if the current cell matches the target color.
                if (!pixelGrid[r][c].equals(targetColor))
                    continue;
    
                // replace the color
                pixelGrid[r][c] = replacementColor;
    
                // add neighbouring cells to stack
                stack.push(new Point(c + 1, r));
                stack.push(new Point(c - 1, r));
                stack.push(new Point(c, r + 1));
                stack.push(new Point(c, r - 1));
            }
            saveLocalState(pg, pixelGrid);
            repaint();
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void getPixelColour(MouseEvent e) { // this is setting?
        int col = (int) (e.getX() / (GRID_SIZE * scale));
        int row = (int) (e.getY() / (GRID_SIZE * scale));
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS)
            setCurrentColour(pixelGrid[row][col]);
    }

    public void setCurrentColour(Color colour) {
        this.currentColour = colour;
    }

    public Color getCurrentColour() {
        return this.currentColour;
    }

    public String getCurrentTool() {
        return this.currentTool;
    }

    public void setCurrentTool(String tool) {
        this.currentTool = tool;
    }

    private int brushSize = 1;

    public int getBrushSize() {
        return brushSize;
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
    }

    public void zoomIn() {
        scale *= 1.1;
        revalidate();
        repaint();
    }
    
    public void zoomOut() {
        scale /= 1.1;
        revalidate();
        repaint();
    }

    // function that updates the grid size based on the zoom factor (when zooming
    // in/out)
    private void updateGridSize() {
        GRID_SIZE = Math.max(1, baselineGridSize * zoomFactor / 100);
        updateCanvasSize();
    }

    // function that updates the canvas size based on the grid size (when zooming
    // in/out)
    public void updateCanvasSize() {
        int width = (int) (COLS * GRID_SIZE * scale);
        int height = (int) (ROWS * GRID_SIZE * scale);
        setPreferredSize(new Dimension(width, height));
        revalidate(); // makes sure the canvas is redrawn with the new size
        repaint(); // repaints the canvas with the new size
    }

    public void openImage() {
        // allows user to open file explorer to choose an image to open
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) { // if the user selects a file
            File file = fileChooser.getSelectedFile();
            try {
                Color[][] importedGrid = ImageController.ImportImage(file.getAbsolutePath());
                pixelGrid = importedGrid;
                stateManager.ResetStates();
                layerManager = new LayerManager(pixelGrid);
                // saveLocalState(layerManager, layerManager);
                currentLayer = layerManager.FindLayer(0);
                fitToWindow();
                stateManager = new StateManager(currentLayer);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to open image: " + ex.getMessage());
            }
        }
    }

    public void saveImage() {
        // allows user to open file explorer to choose where to save the image
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) { // once the user selects a folder
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.endsWith(".png")) { // makes sure saved as png
                path += ".png";
            }
            try {
                currentLayer.setPixelGrid(pixelGrid);
                displayGrid = layerManager.CondenseLayersAC();
                ImageController.ExportImage2PNG(displayGrid, path);
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Failed to save image: " + ex.getMessage());
            }
        }
    }

    // Resize canvas keeping art proportional: applies to all layers
    public void NNIResize(int newWidth, int newHeight) throws Exception {
        if (newWidth < 1 || newHeight < 1) {
            JOptionPane.showMessageDialog(
                this,
                "Invalid Input",
                null,
                JOptionPane.PLAIN_MESSAGE);
        } else {
            LayerManager prev = layerManager.clone();
            layerManager.NNIResizeLayers(currentLayer, newWidth, newHeight);
            pixelGrid = currentLayer.getPixelGrid();
            if (this.pixelGrid.length < 1 || this.pixelGrid[0].length < 1)
                throw new Exception("Invalid PixelGrid");
            int pos = layerManager.GetLayerPos(currentLayer);
            LayerStateModel lsm = new LayerStateModel(prev, prev, pos, pos);
            saveGlobalState(lsm, GlobalStateModel.Op.RESIZE, pixelGrid[0].length, pixelGrid.length, COLS, ROWS); 
            ROWS = pixelGrid.length;
            COLS = pixelGrid[0].length;
            layerManager.SetHeight(ROWS);
            layerManager.SetWidth(COLS);
            repaint();
        }
        
    }

    // Apply crop to all layers
    public void confirmCrop() throws Exception {
        if (CropManager.IsReset()) {
            return;
        }

        LayerManager prev = layerManager.clone();
        layerManager.CropLayers(currentLayer);
        pixelGrid = currentLayer.getPixelGrid();
        CropManager.Reset();
        if (this.pixelGrid.length < 1 || this.pixelGrid[0].length < 1)
            throw new Exception("Invalid PixelGrid");
        LayerStateModel lsm = new LayerStateModel(prev, prev, prev.GetLayersSize(), layerManager.GetLayersSize());
        saveGlobalState(lsm, GlobalStateModel.Op.CROP, pixelGrid[0].length, pixelGrid.length, COLS, ROWS); 
        ROWS = pixelGrid.length;
        COLS = pixelGrid[0].length;
        layerManager.SetHeight(ROWS);
        layerManager.SetWidth(COLS);
        repaint();
    }

    // Blur current layer
    public void blurImage() {
        try {
            LayerManager prev = layerManager.clone();
            Color[][] pg = ImageController.ClonePixelGrid(currentLayer.getPixelGrid());
            currentLayer.setPixelGrid(ImageController.Blur(pg));
            pixelGrid = currentLayer.getPixelGrid();
            saveLocalState(pg, pixelGrid);
            repaint();
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Sharpen current layer
    public void sharpenImage() {
        try {
            LayerManager prev = layerManager.clone();
            Color[][] pg = ImageController.ClonePixelGrid(currentLayer.getPixelGrid());
            currentLayer.setPixelGrid(ImageController.Sharpen(pg));
            pixelGrid = currentLayer.getPixelGrid();
            saveLocalState(pg, pixelGrid);
            repaint();
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Rotate all layers right
    public void rotateRight() {
        layerManager.RotateLayersRight(currentLayer);
        pixelGrid = currentLayer.getPixelGrid();
        saveGlobalState(GlobalStateModel.Op.ROTATE_RIGHT, ROWS, COLS, COLS, ROWS); 
        ROWS = pixelGrid.length;
        COLS = pixelGrid[0].length;
        layerManager.SetHeight(ROWS);
        layerManager.SetWidth(COLS);
        repaint();
    }

    // Rotate all layers left
    public void rotateLeft() {
        layerManager.RotateLayersLeft(currentLayer);
        pixelGrid = currentLayer.getPixelGrid();
        saveGlobalState(GlobalStateModel.Op.ROTATE_LEFT, ROWS, COLS, COLS, ROWS); 
        ROWS = pixelGrid.length;
        COLS = pixelGrid[0].length;
        layerManager.SetHeight(ROWS);
        layerManager.SetWidth(COLS);
        repaint();
    }

    // Flip all layers y-axis
    public void flipH() {
        layerManager.FlipLayersH(currentLayer);
        pixelGrid = currentLayer.getPixelGrid();
        saveGlobalState(GlobalStateModel.Op.FLIPH, ROWS, COLS, ROWS, COLS); 
        repaint();
    }

    // Flip all layers x-axis
    public void flipV() {
        layerManager.FlipLayersV(currentLayer);
        pixelGrid = currentLayer.getPixelGrid();
        saveGlobalState(GlobalStateModel.Op.FLIPV, ROWS, COLS, ROWS, COLS); 
        repaint();
    }

    /**
     * Returns array of LayerModel objects in order
     */
    public LayerModel[] getLayerOrder() {
        return layerManager.GetLayerArray();
    }

    /**
     * Returns current layer model object
     */
    public LayerModel getCurrentLayer() {
        return currentLayer;
    }

    /**
     * Sets current layer to given LayerModel m
     * 
     * @param m - LayerModel object to be set as current layer
     */
    public void setCurrentLayer(LayerModel m) {
        // Saves current pixel grid to the layer and updates display
        currentLayer.setPixelGrid(pixelGrid);
        displayGrid = layerManager.CondenseLayersAC();

        currentLayer = m;
        // Updates pixelGrid to pull from new layer
        pixelGrid = currentLayer.getPixelGrid();
    }

    /**
     * Creates new empty layer and adds on top;
     * Set as current layer
     */
    public void addNewLayer() {
        try {
            int pos = layerManager.GetLayerPos(currentLayer);
            currentLayer.setPixelGrid(pixelGrid);
            displayGrid = layerManager.CondenseLayersAC();
            LayerManager prev = layerManager.clone();
            Color[][] newGrid = new Color[ROWS][COLS];
            // Set all cells to transparent
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    newGrid[i][j] = new Color(255, 255, 255, 0);
                }
            }

            layerManager.AddLayer(newGrid);
            int index = layerManager.GetLayersSize() - 1;
            currentLayer = layerManager.FindLayer(index);
            pixelGrid = currentLayer.getPixelGrid();
            saveLayerState(prev, layerManager, pos, index);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to add new layer due to: " + e.getMessage());
        }
    }

    /**
     * Deletes current selected layer;
     * If there is at least one layer, set current layer to the one at position 0
     * (bottom)
     */
    public void deleteCurrentLayer() {
        int pos = layerManager.GetLayerPos(currentLayer);
        try {
            LayerManager prev = layerManager.clone();
            layerManager.RemoveLayer(pos);
            if (layerManager.GetLayersSize() != 0) {
                currentLayer = layerManager.FindLayer(0);
                pixelGrid = currentLayer.getPixelGrid();
            }
            saveLayerState(prev, layerManager, pos, 0);
            repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to delete layer due to: " + e.getMessage());
        }
    }

    /**
     * Moves current layer up by one (swaps with layer above),
     * if it is not already on top
     */
    public void moveCurrentLayerUp() {
        int size = layerManager.GetLayersSize();
        int pos = layerManager.GetLayerPos(currentLayer);
        // If layer is NOT at the top then
        if (!(pos == (size - 1))) {
            try {
            LayerManager prev = layerManager.clone();
            layerManager.ChangeLayerPos(pos, pos + 1);
            saveLayerState(prev, layerManager, pos, pos+1);
            repaint();
            } catch (Exception e) {
                System.out.println(e);
            }
        } 
    }

    /**
     * Moves current layer down by one (swaps with layer below),
     * if it is not already at the bottom
     */
    public void moveCurrentLayerDown() {
        int pos = layerManager.GetLayerPos(currentLayer);
        // If layer is NOT at the bottom then
        if (!(pos == 0)) {
            try {
                LayerManager prev = layerManager.clone();
                layerManager.ChangeLayerPos(pos, pos - 1);
                saveLayerState(prev, layerManager, pos, pos-1);
                repaint();
                } catch (Exception e) {
                    System.out.println(e);
                }
        } // Otherwise do nothing
    }

    /**
     * Sets current layer opacity to a value between 0 and 1;
     * If value provided is not valid, an error message is displayed
     * 
     * @param alpha - opacity value between 0 and 1
     */
    public void setCurrentLayerOpacity(float alpha) {
        try {
            int pos = layerManager.GetLayerPos(currentLayer);
            LayerManager prev = layerManager.clone();
            currentLayer.setOpacity(alpha);
            saveLayerState(prev, layerManager, pos, pos);
            repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to change layer opacity due to: " + e.getMessage());
        }
    }

    /**
     * Toggles current layer visibility;
     * If the layer is visible, set to not visible, and vice versa
     */
    public void toggleCurrentLayerVisibility() {
        try {
            int pos = layerManager.GetLayerPos(currentLayer);
            LayerManager prev = layerManager.clone();
            currentLayer.toggleVisibility();
            pixelGrid = currentLayer.getPixelGrid();
            saveLayerState(prev, layerManager, pos, pos);
            repaint();
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // just used for loading an image from the main menu
    public void setImageGrid(Color[][] grid) {
        this.pixelGrid = grid;
        ROWS = grid.length;
        COLS = grid[0].length;
        //fit the imported texture in the viewport
        fitToWindow();
    }

    public void undo() {
        if (stateManager.canUndo()) { 
            try {
                StateResult sr = stateManager.UndoState(layerManager, currentLayer);
                layerManager = sr.layerManager;
                currentLayer = sr.curLayer;
                pixelGrid = currentLayer.getPixelGrid();
                displayGrid = layerManager.CondenseLayersAC();
                repaint();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Unexpected undo error: " + e.getMessage());
            }
        } else { // if there's nothing left to undo then let user know
        JOptionPane.showMessageDialog(
            this,
            "Nothing left to undo",
            null,
            JOptionPane.PLAIN_MESSAGE);
        }
    }
    
    public void redo() {
        if (stateManager.canRedo()) { 
            try {
                StateResult sr = stateManager.RedoState(layerManager, currentLayer);
                layerManager = sr.layerManager;
                currentLayer = sr.curLayer;
                pixelGrid = currentLayer.getPixelGrid();
                displayGrid = layerManager.CondenseLayersAC();
                repaint();
            } catch (Exception e) {
                System.err.println("Unexpected redo error: " + e.getMessage());
            }
        } else { // if there's nothing left to redo then let user know
            JOptionPane.showMessageDialog(
                    this,
                    "Nothing left to redo",
                    null,
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    // function that saves the current state of the pixel grid to the state manager
    // (for undo/redo)
    // called after every action that changes the pixel grid (drawing, filling...)
    private void saveLocalState(Color[][] oldPixelGrid, Color[][] newPixelGrid) {
        stateManager.SaveLocalState(currentLayer, oldPixelGrid, newPixelGrid);
    }

    private void saveGlobalState(GlobalStateModel.Op op, int newWidth, int newHeight, int oldWidth, int oldHeight) {
        stateManager.SaveGlobalState(op, newWidth, newHeight, oldWidth, oldHeight);
    }

    private void saveGlobalState(LayerStateModel lsm, GlobalStateModel.Op op, int newWidth, int newHeight, int oldWidth, int oldHeight) {
        stateManager.SaveGlobalState(lsm, op, newWidth, newHeight, oldWidth, oldHeight);
    }

    private void saveLayerState(LayerManager prevLayerManager, LayerManager newLayerManager, int oldLayerNum, int newLayerNum) {        
        stateManager.SaveLayerState(prevLayerManager, newLayerManager, oldLayerNum, newLayerNum);
    }

    // Copy pixel grid - canvas version of ImageController.ClonePixelGrid
    public Color[][] getPixelGridCopy() {
        Color[][] copy = new Color[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Color c = pixelGrid[i][j];
                copy[i][j] = new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
            }
        }
        return copy;
    }

    //Loads a block texture from a PNG file, resets history, and tracks its name.
    public void loadBlockTexture(File file) {
        try {
            //import pixel grid from PNG
            Color[][] importedGrid = ImageController.ImportImage(file.getAbsolutePath());
            // Update canvas dimensions and grid
            setImageGrid(importedGrid);
            //reinitialise layer manager to match new grid
            this.layerManager = new LayerManager(importedGrid);
            this.currentLayer = layerManager.FindLayer(0);
            //reset undo/redo history for the new grid size
            stateManager = new StateManager(currentLayer);
            //save initial loaded state
            // saveLayerState(layerManager, layerManager);
            //record block name (without .png)
            String name = file.getName();
            currentBlockName = name.replaceAll("(?i)\\.png$", "");
            repaint();
        } catch (Exception ex) {
            // Inform user if loading fails
            JOptionPane.showMessageDialog(this, "Failed to load block texture: " + ex.getMessage());
        }
    }

    //Returns the name of the currently loaded block texture (no extension).
    public String getCurrentBlockName() {
        return currentBlockName;
    }

    //fits the canvas so that the entire image is visible within the parent container.
    public void fitToWindow() {
        java.awt.Component parentComp = getParent();
        if (parentComp != null) {
            int availableWidth = parentComp.getWidth();
            int availableHeight = parentComp.getHeight();
            int newGridSize = Math.max(1, Math.min(availableWidth / COLS, availableHeight / ROWS));
            GRID_SIZE = newGridSize;
            // Store this as the new baseline for zooming
            baselineGridSize = newGridSize;
            // Reset zoom factor to 100% since we're at baseline
            zoomFactor = 100;
            updateCanvasSize();
        }
    }

    // for the scroll to zoom functionality
    public void setScale(double scale) {
        this.scale = scale;
        updateCanvasSize(); // reuse your existing resize logic
    }    

    public static void setRows(int rows) {
        ROWS = rows;
    }

    public static void setCols(int cols) {
        COLS = cols;
    }
}