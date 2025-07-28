package g82;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class MainWindow extends JFrame {
    private CanvasPanel canvasPanel;
    private ToolBarPanel toolBarPanel;
    private BlockEditorWindow blockEditor;
    private String mode; // can be "block", "skin" or "art"

    // Default constructor (block editor mode)
    public MainWindow(int x, int y, int gridSize) {
        this(x, y, gridSize, "block");
    }

    // Main constructor with mode flag
    public MainWindow(int x, int y, int gridSize, String mode) {
        this.mode = mode;
        setTitle("Minedit!");
        URL iconURL = MainMenu.class.getResource("/mineditLogo.png");
        Image appIcon = new ImageIcon(iconURL).getImage();
        setIconImage(appIcon);
        
        // sets editor window to full screen by getting the screen size and setting the size of the window to that
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //panel to hold everything
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Setting up the CanvasPanel (the drawing area)
        canvasPanel = new CanvasPanel(x, y, gridSize);
        CanvasZoom canvasZoom = new CanvasZoom(canvasPanel); // now is wrapped in the canvas zoom class 
        JPanel canvasWrapper = new JPanel(new BorderLayout());
        canvasWrapper.add(canvasZoom, BorderLayout.CENTER);
        canvasWrapper.setBorder(BorderFactory.createEmptyBorder()); // no extra padding
        mainPanel.add(canvasWrapper, BorderLayout.CENTER);

        
        // Left side components (ColorSelector and LayerPane)
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        leftPanel.add(new ColorSelector(canvasPanel), BorderLayout.CENTER);
        
        LayerPane layersPanel = new LayerPane(canvasPanel);
        // Setting up the ToolBarPanel (selecting the tool you're using) at the top
        toolBarPanel = new ToolBarPanel(canvasPanel, layersPanel);
        mainPanel.add(toolBarPanel, BorderLayout.NORTH);
        
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize(); 
        
        int height = (int)size.getHeight();
        System.out.println(height);
        Dimension layerPanelSize;
        if (height >= 1220) {
            layerPanelSize = new Dimension(300,550);
        } else {
            layerPanelSize = new Dimension(300,250);
        }
        layersPanel.setPreferredSize(layerPanelSize);

        leftPanel.add(layersPanel, BorderLayout.SOUTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        
        // only show texture list for block editor
        if (mode.equals("block")) {
            blockEditor = new BlockEditorWindow(this, canvasPanel);
            mainPanel.add(blockEditor.createBlockEditorPanel(), BorderLayout.EAST);
        }

        add(mainPanel);

        // Setting up Menu Bar (file operations)
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(66, 66, 66));
        
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(Color.WHITE);
        fileMenu.setBackground(new Color(66, 66, 66));
        
        JMenuItem openItem = new JMenuItem("Import Image");
        KeyStroke openKey = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        openItem.setAccelerator(openKey);

        JMenuItem saveItem = new JMenuItem("Save Current Image");
        KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        saveItem.setAccelerator(saveKey);

        fileMenu.add(openItem);
        fileMenu.add(saveItem);

        // import menu items are different for block and skin editor modes
        if (mode.equals("block")) {
            JMenuItem loadBlocksItem = new JMenuItem("Load Block Pack");
            fileMenu.add(loadBlocksItem);
            loadBlocksItem.addActionListener(e -> blockEditor.handleBlockPackLoading());
        } else if (mode.equals("skin")) {
            JMenuItem loadSkinItem = new JMenuItem("Load Skin");
            fileMenu.add(loadSkinItem);
            loadSkinItem.addActionListener(e -> canvasPanel.openImage());
        }

        // quick-load items for default skins in skin editor mode
        if (mode == "skin") {
            JMenuItem steveItem = new JMenuItem("Load Steve");
            steveItem.addActionListener(e -> loadSkinFromResource("/steve.png"));
            fileMenu.add(steveItem);
            JMenuItem alexItem = new JMenuItem("Load Alex");
            alexItem.addActionListener(e -> loadSkinFromResource("/alex.png"));
            fileMenu.add(alexItem);
        }
        if (mode == "block") {
            JMenuItem exportMCItem = new JMenuItem("Export Minecraft Pack");
            fileMenu.add(exportMCItem);
            // modify export menu item
            exportMCItem.addActionListener(e -> blockEditor.handleExport(canvasPanel));
        }
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        openItem.addActionListener(e -> canvasPanel.openImage());
        saveItem.addActionListener(e -> canvasPanel.saveImage());

        // Add keyboard shortcuts
        KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);

        getRootPane().registerKeyboardAction(
                e -> canvasPanel.undo(),
                "Undo",
                undoKeyStroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(
                e -> canvasPanel.redo(),
                "Redo",
                redoKeyStroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // for block mode: shows prompt to allow user to pick what texture pack to load
        if (mode.equals("block")) {
            SwingUtilities.invokeLater(() -> {
                String[] options = {"Import Texture Pack", "Use Default 1.21.7 Pack"};
                int choice = JOptionPane.showOptionDialog(this,
                    "Would you like to import a texture pack or use the default pack?",
                    "Select Texture Pack",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);

                if (choice == 0) {
                    // loadBlocksItem.doClick(); // No longer needed as we handle it directly above
                } else {
                    blockEditor.loadDefaultTexturePack();
                }
            });
        }
        
        // for skin mode, auto load the Steve skin
        if (mode.equals("skin")) {
            SwingUtilities.invokeLater(() -> {
                loadSkinFromResource("/steve.png");
            });
        }
    }

    public CanvasPanel getCanvasPanel() {
        return canvasPanel;
    }

    /**
     * Helper: load a skin PNG from classpath into the canvas
     */
    private void loadSkinFromResource(String resourcePath) {
        try (InputStream is = MainWindow.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                JOptionPane.showMessageDialog(this, "Resource not found: " + resourcePath);
                return;
            }
            BufferedImage img = ImageIO.read(is);
            File tempFile = File.createTempFile("skin", ".png");
            ImageIO.write(img, "png", tempFile);
            canvasPanel.loadBlockTexture(tempFile);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load skin: " + ex.getMessage());
        }
    }
}