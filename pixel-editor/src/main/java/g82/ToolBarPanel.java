package g82;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

public class ToolBarPanel extends JToolBar {
    Color prevColour = Color.BLACK;
    private final Dimension TOOL_BUTTON_SIZE = new Dimension(32, 32);
    private JButton selectedToolButton = null; // Keeps track of the selected tool button
    private LayerPane layerPane;
    public ToolBarPanel(CanvasPanel canvas, LayerPane layerPane) {
        setFloatable(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS)); // Changed to horizontal layout

        // undo/redo group
        add(createHistoryGroup(canvas));
        add(new JSeparator(SwingConstants.VERTICAL));

        // main tools group (brush, fill, eraser, picker) 
        add(createToolsGroup(canvas));
        add(new JSeparator(SwingConstants.VERTICAL));

        // brush size group (the slider)
        add(createBrushGroup(canvas));
        add(new JSeparator(SwingConstants.VERTICAL));

        // filters group (blur, sharpen)
        add(createFiltersGroup(canvas));
        add(new JSeparator(SwingConstants.VERTICAL));

        // transform group (flip, rotate)
        add(createTransformGroup(canvas));
        add(new JSeparator(SwingConstants.VERTICAL));

        // zoom group (zoom in, zoom out)
        add(createZoomGroup(canvas));
        add(new JSeparator(SwingConstants.VERTICAL));
        this.layerPane = layerPane;
    }

    // creates the button for toolbar tools (sizes them nicely and adds tooltip)
    private JButton createToolButton(String iconPath, String tooltip) {
        URL imageURL = this.getClass().getResource(iconPath);
        ImageIcon icon = new ImageIcon(imageURL);
        icon = new ImageIcon(icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton button = new JButton(icon);
        button.setPreferredSize(TOOL_BUTTON_SIZE);
        button.setToolTipText(tooltip);
        
        // removes the blue button stuff so it looks cleaner
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBackground(null);
        button.setOpaque(false);

        // hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button != selectedToolButton) {
                    button.setContentAreaFilled(true);
                    button.setBackground(new Color(230, 230, 230));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button != selectedToolButton) {
                    button.setContentAreaFilled(false);
                    button.setBackground(null);
                }
            }
        });

        return button;
    }

    // method to highlight/shade in the selected tool's button
    private void highlightSelectedButton(JButton button) {
        if (selectedToolButton != null) {
            // reset the appearance of the previously selected button
            selectedToolButton.setContentAreaFilled(false);
            selectedToolButton.setBackground(null);
        }
        // highlight the new selected button
        selectedToolButton = button;
        button.setContentAreaFilled(true);
        button.setBackground(new Color(200, 200, 200)); // selected tool's colour
    }

    private JPanel createHistoryGroup(CanvasPanel canvas) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        
        JButton undoButton = createToolButton("/editor-icons/undo.png", "Undo (Ctrl+Z)");
        undoButton.setMnemonic(KeyEvent.VK_Z);
        undoButton.addActionListener(e -> {
            canvas.undo(); 
            layerPane.refreshLayers();       
    });

        JButton redoButton = createToolButton("/editor-icons/redo.png", "Redo (Ctrl+Y)");
        redoButton.setMnemonic(KeyEvent.VK_Y);
        redoButton.addActionListener(e -> {
            canvas.redo(); 
            layerPane.refreshLayers();       
    });

        panel.add(undoButton);
        panel.add(redoButton);
        return panel;
    }

    private JPanel createToolsGroup(CanvasPanel canvas) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        // Create the brush button
        JButton brushButton = createToolButton("/editor-icons/brush.png", "Brush (B)");
        brushButton.setMnemonic(KeyEvent.VK_B);
        brushButton.addActionListener(e -> {
            if (canvas.getCurrentTool().equals("Erase")) {
                canvas.setCurrentColour(prevColour);
            }
            canvas.setCurrentTool("Brush");
            highlightSelectedButton(brushButton);
        });

        // highlight the brush button by default and set the tool to "Brush"
        highlightSelectedButton(brushButton);
        canvas.setCurrentTool("Brush");

        // create the fill button
        JButton fillButton = createToolButton("/editor-icons/fill.png", "Fill (F)");
        fillButton.setMnemonic(KeyEvent.VK_F);
        fillButton.addActionListener(e -> {
            if (canvas.getCurrentTool().equals("Erase")) {
                canvas.setCurrentColour(prevColour);
            }
            canvas.setCurrentTool("Fill");
            highlightSelectedButton(fillButton);
        });

        // create the erase button
        JButton eraseButton = createToolButton("/editor-icons/eraser.png", "Eraser (E)");
        eraseButton.setMnemonic(KeyEvent.VK_E);
        eraseButton.addActionListener(e -> {
            if (!canvas.getCurrentTool().equals("Erase")) {
                prevColour = canvas.getCurrentColour();
            }
            canvas.setCurrentTool("Erase");
            canvas.setCurrentColour(new Color(255, 255, 255, 0));
            highlightSelectedButton(eraseButton);
        });

        // create the picker button
        JButton pickerButton = createToolButton("/editor-icons/picker.png", "Color Picker (P)");
        pickerButton.setMnemonic(KeyEvent.VK_P);
        pickerButton.addActionListener(e -> {
            canvas.setCurrentTool("Picker");
            highlightSelectedButton(pickerButton);
        });

        panel.add(brushButton);
        panel.add(fillButton);
        panel.add(eraseButton);
        panel.add(pickerButton);
        return panel;
    }

    private JPanel createBrushGroup(CanvasPanel canvas) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        JLabel brushLabel = new JLabel("Brush Size:");
        brushLabel.setForeground(Color.BLACK); // Set label text color to black

        JSlider brushSlider = new JSlider(1, 9, canvas.getBrushSize());
        brushSlider.setOpaque(false);
        brushSlider.setBackground(Color.BLACK);
        brushSlider.setPreferredSize(new Dimension(150, 30));
        brushSlider.setPaintTicks(true);
        brushSlider.setPaintLabels(true);
        brushSlider.setMinorTickSpacing(1);
        brushSlider.addChangeListener(e -> canvas.setBrushSize(brushSlider.getValue()));

        panel.add(brushLabel);
        panel.add(brushSlider);
        return panel;
    }

    private JPanel createFiltersGroup(CanvasPanel canvas) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        
        JButton blurButton = createToolButton("/editor-icons/blur.png", "Blur");
        JButton sharpenButton = createToolButton("/editor-icons/sharpen.png", "Sharpen");
        
        blurButton.addActionListener(e -> canvas.blurImage());
        sharpenButton.addActionListener(e -> canvas.sharpenImage());
        
        panel.add(blurButton);
        panel.add(sharpenButton);
        return panel;
    }

    private JPanel createTransformGroup(CanvasPanel canvas) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        
        JButton resizeButton = createToolButton("/editor-icons/resize.png", "Resize");
        JTextField widthField = new JTextField(3); 
        JTextField heightField = new JTextField(3);
        JButton confirmResizeButton = createToolButton("/editor-icons/check.png", "Confirm Resize");
        JButton cropButton = createToolButton("/editor-icons/crop.png", "Crop");
        JButton confirmCropButton = createToolButton("/editor-icons/check.png", "Confirm Crop");
        JButton flipHButton = createToolButton("/editor-icons/flip-h.png", "Flip Horizontal");
        JButton flipVButton = createToolButton("/editor-icons/flip-v.png", "Flip Vertical");
        JButton rotateLeftButton = createToolButton("/editor-icons/rotate-left.png", "Rotate Left");
        JButton rotateRightButton = createToolButton("/editor-icons/rotate-right.png", "Rotate Right");
        
        // resize 
        confirmResizeButton.setVisible(false);
        widthField.setVisible(false);
        heightField.setVisible(false);

        resizeButton.addActionListener(e -> {
            canvas.setCurrentTool("Resize");
            confirmResizeButton.setVisible(true);
            widthField.setVisible(true);
            heightField.setVisible(true);
            resizeButton.setVisible(false);
        });

        confirmResizeButton.addActionListener(e -> {
            try {
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());
                canvas.NNIResize(width, height);
                canvas.setCurrentTool("Brush");
                widthField.setVisible(false);
                heightField.setVisible(false);
                confirmResizeButton.setVisible(false);
                resizeButton.setVisible(true);   
            } catch (Exception err) {
                System.out.println(err);
                canvas.setCurrentTool("Brush");
                widthField.setVisible(false);
                heightField.setVisible(false);
                confirmResizeButton.setVisible(false);
                resizeButton.setVisible(true);   
            }
        });
        // crop
        confirmCropButton.setVisible(false); // hides the confirm crop button until crop is selected
        cropButton.addActionListener(e -> {
            canvas.setCurrentTool("Crop");
            confirmCropButton.setVisible(true);
            cropButton.setVisible(false);
        });

        confirmCropButton.addActionListener(e -> {
            try {
                canvas.confirmCrop();
                canvas.setCurrentTool("Brush"); // after the crop set tool back to brush
                confirmCropButton.setVisible(false); // hides the confirm crop button after cropping
                cropButton.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Failed to crop: " + ex.getMessage());
            }
        });

        flipHButton.addActionListener(e -> canvas.flipH());
        flipVButton.addActionListener(e -> canvas.flipV());
        rotateRightButton.addActionListener(e -> canvas.rotateRight());
        rotateLeftButton.addActionListener(e -> canvas.rotateLeft());
        
        panel.add(resizeButton);
        panel.add(widthField);
        panel.add(heightField);
        panel.add(confirmResizeButton);
        panel.add(cropButton);
        panel.add(confirmCropButton);
        panel.add(flipHButton);
        panel.add(flipVButton);
        panel.add(rotateLeftButton);
        panel.add(rotateRightButton);
        return panel;
    }

    private JPanel createZoomGroup(CanvasPanel canvas) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        
        JButton zoomInButton = createToolButton("/editor-icons/zoom-in.png", "Zoom In (Ctrl+Plus)");
        JButton zoomOutButton = createToolButton("/editor-icons/zoom-out.png", "Zoom Out (Ctrl+Minus)");
        
        zoomInButton.addActionListener(e -> canvas.zoomIn());
        zoomOutButton.addActionListener(e -> canvas.zoomOut());
        
        panel.add(zoomInButton);
        panel.add(zoomOutButton);
        return panel;
    }
}