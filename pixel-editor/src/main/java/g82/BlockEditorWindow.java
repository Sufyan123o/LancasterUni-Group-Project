package g82;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.*;

public class BlockEditorWindow {
    private MainWindow mainWindow;
    private CanvasPanel canvasPanel;
    //hold mapping of block name to PNG file so u can do loading/exporting
    private Map<String, File> blockMap = new TreeMap<>();
    private JList<String> textureList;
    private DefaultListModel<String> listModel;
    private File currentTextureDir;
    private File texturesRootDir;
    private JComboBox<String> categoryCombo;
    private Map<String, Color[][]> editedTextures = new HashMap<>(); // stores all edited textures in this session

    public BlockEditorWindow(MainWindow mainWindow, CanvasPanel canvas) {
        this.mainWindow = mainWindow;
        this.canvasPanel = canvas;

        // setup the texture list on the right
        listModel = new DefaultListModel<>();
        textureList = new JList<>(listModel);

        // add combo box for categories with default "no pack loaded" item
        categoryCombo = new JComboBox<>(new String[]{"No texture pack loaded"});
        categoryCombo.setEnabled(false); // disable until pack is loaded
        categoryCombo.addActionListener(e -> {
            if (texturesRootDir != null) {
                String selected = (String) categoryCombo.getSelectedItem();
                File categoryDir = new File(texturesRootDir, selected);
                if (categoryDir.exists()) {
                    loadTexturesFromDir(categoryDir);
                }
            }
        });
    }

    // saves the texture currently being edited to the hashmap
    // called when switching between textures in the list so nothing is lost
    private void saveCurrentEdit() {
        if (canvasPanel.getCurrentBlockName() != null) {
            editedTextures.put(canvasPanel.getCurrentBlockName(), canvasPanel.getPixelGridCopy());
        }
    }

    public JPanel createBlockEditorPanel() {
        // make it scroll nicely with mouse wheel
        JScrollPane listScroller = new JScrollPane(textureList);
        listScroller.setPreferredSize(new Dimension(200, 0));

        // when user clicks a texture, load it
        textureList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // saves current texture before switching to another one
                // so nothing is lost
                saveCurrentEdit();

                String selected = textureList.getSelectedValue();
                if (selected != null) {
                    File f = blockMap.get(selected);
                    if (f != null) { // if the user has already edited a texture, load it when they select it again
                        canvasPanel.loadBlockTexture(f);
                        // restore any previous edits
                        if (editedTextures.containsKey(selected)) {
                            canvasPanel.setImageGrid(editedTextures.get(selected));
                        }
                    }
                }
            }
        });

        // put category selector at top of panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Textures"));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Category: "), BorderLayout.WEST);
        topPanel.add(categoryCombo, BorderLayout.CENTER);
        listPanel.add(topPanel, BorderLayout.NORTH);
        listPanel.add(listScroller, BorderLayout.CENTER);

        return listPanel;
    }

    // helper method to scan for textures
    private void loadTexturesFromDir(File dir) {
        blockMap.clear();
        listModel.clear(); // clear the list

        File[] pngs = dir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".png"));
        if (pngs != null) {
            for (File f : pngs) {
                String name = f.getName().replaceAll("(?i)\\.png$", "");
                blockMap.put(name, f);
                listModel.addElement(name); // add to the visible list
            }
        }
        if (!blockMap.isEmpty()) {
            textureList.setSelectedIndex(0); // select the first item by default
        }
    }

    // this handles opening minecraft texture packs that are zipped
    public void handleZipTexturePack(File zipFile) throws Exception {
        // making a temp folder to unzip stuff into
        // using "mcpack" so we know what the temp folder is for
        Path tempDir = Files.createTempDirectory("mcpack");

        // unzip everything
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path path = tempDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    // make the folder if its a folder
                    Files.createDirectories(path);
                } else {
                    // make sure the parent folders exist before saving the file
                    Files.createDirectories(path.getParent());
                    Files.copy(zis, path);
                }
            }
        }

        // after unzipping look for the textures folder like normal
        File texturesDir = new File(tempDir.toFile(), "assets/minecraft/textures");
        if (texturesDir.isDirectory()) {
            texturesRootDir = texturesDir;
            String[] categories = texturesDir.list((dir, name) -> new File(dir, name).isDirectory());
            if (categories != null && categories.length > 0) {
                categoryCombo.setEnabled(true);
                categoryCombo.setModel(new DefaultComboBoxModel<>(categories));
                // Load first category
                File firstCat = new File(texturesDir, categories[0]);
                loadTexturesFromDir(firstCat);
            }
        }
    }

    // this saves our edited texture into a new minecraft resource pack
    public void exportToZip(String blockName, File zipFile) throws Exception {
        // save current texture before export to avoid losing edits
        saveCurrentEdit();

        try (InputStream defaultPack = MainWindow.class.getResourceAsStream("/default_pack_1.21.5.zip");
            ZipInputStream zis = new ZipInputStream(defaultPack);
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {

            // tracks which edited files have been written
            Set<String> writtenEdits = new java.util.HashSet<>();

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String currentPath = entry.getName().replace('\\', '/');
                zos.putNextEntry(new ZipEntry(currentPath));

                if (!entry.isDirectory()) {
                    // checks if this is a texture thats been edited
                    String textureName = getTextureNameFromPath(currentPath);
                    if (editedTextures.containsKey(textureName)) {
                        // write the edited texture
                        File tempFile = File.createTempFile("texture", ".png");
                        try {
                            g82.controllers.ImageController.ExportImage2PNG(
                                editedTextures.get(textureName),
                                tempFile.getAbsolutePath()
                            );
                            byte[] imageData = Files.readAllBytes(tempFile.toPath());
                            zos.write(imageData);
                            writtenEdits.add(textureName);
                        } finally {
                            tempFile.delete();
                        }
                    } else {
                        // copy original file
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }
                }
                zos.closeEntry();
            }
        }
    }

    // gets texture name from the full path of the texture
    // used for comparing
    private String getTextureNameFromPath(String path) {
        // extract texture name from full path
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.replaceAll("(?i)\\.png$", "");
    }

    // loads the default texture pack stored in resources folder
    public void loadDefaultTexturePack() {
        try {
            // finds the folder from resources
            InputStream is = MainWindow.class.getResourceAsStream("/default_pack_1.21.5.zip");
            if (is == null) {
                JOptionPane.showMessageDialog(mainWindow, "Default texture pack not found!");
                return;
            }

            // creates a temporary file to unzip into
            File tempFile = File.createTempFile("default_pack", ".zip");
            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            is.close();

            // use the existing code for handling the zip
            handleZipTexturePack(tempFile);

            // deletes the temporary file when done
            tempFile.deleteOnExit();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainWindow, "Failed to load default pack: " + ex.getMessage());
        }
    }

    public void handleBlockPackLoading() {
        JFileChooser chooser = new JFileChooser();
        // need to let users pick both folders and zip files bc minecraft texture packs can be either
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                // only show folders and zip files in the file picker
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".zip");
            }
            public String getDescription() {
                return "Folders and ZIP files";
            }
        });

        int res = chooser.showOpenDialog(mainWindow);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File base = chooser.getSelectedFile();

        if (base.getName().toLowerCase().endsWith(".zip")) {
            // handle zip file
            try {
                handleZipTexturePack(base);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainWindow, "Error loading ZIP: " + ex.getMessage());
                return;
            }
        } else {
            //find the textures directory in a resource pack structure
            File texturesDir = new File(base, "assets/minecraft/textures");
            if (texturesDir.isDirectory()) {
                texturesRootDir = texturesDir;
                String[] categories = texturesDir.list((dir, name) -> new File(dir, name).isDirectory());
                if (categories != null && categories.length > 0) {
                    categoryCombo.setEnabled(true);
                    categoryCombo.setModel(new DefaultComboBoxModel<>(categories));
                    // Load first category
                    File firstCat = new File(texturesDir, categories[0]);
                    loadTexturesFromDir(firstCat);
                }
            } else if (base.isDirectory()) {
                //fallback to chosen directory
                currentTextureDir = base;
            } else {
                JOptionPane.showMessageDialog(mainWindow, "Invalid folder selected.");
                return;
            }
            //gather all PNG textures in selected folder
            File[] pngs = currentTextureDir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".png"));
            blockMap.clear();
            listModel.clear(); // clear the list
            if (pngs != null) {
                for (File f : pngs) {
                    String name = f.getName().replaceAll("(?i)\\.png$", "");
                    blockMap.put(name, f);
                    listModel.addElement(name); // add to the visible list
                }
            }
            if (blockMap.isEmpty()) {
                JOptionPane.showMessageDialog(mainWindow, "No PNG textures found in: " + currentTextureDir.getAbsolutePath());
            }
        }
    }

    public void handleExport(CanvasPanel canvasPanel) {
        if (blockMap.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "Load a block pack first via File → Load Block Pack");
            return;
        }
        String blockName = canvasPanel.getCurrentBlockName();
        if (blockName == null) {
            JOptionPane.showMessageDialog(mainWindow, "No block loaded. Use Load Block Pack and select a block.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("my_texture_pack.zip"));
        if (chooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
            try {
                File zipFile = chooser.getSelectedFile();
                if (!zipFile.getName().toLowerCase().endsWith(".zip")) {
                    zipFile = new File(zipFile.getPath() + ".zip");
                }
                exportToZip(blockName, zipFile);
                JOptionPane.showMessageDialog(mainWindow, "Exported to ZIP: " + zipFile.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainWindow, "Export failed: " + ex.getMessage());
            }
        }
    }
}
