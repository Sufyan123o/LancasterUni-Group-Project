package g82;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class ColorSelector extends JPanel {
    private CanvasPanel canvas;
    private static final int maxRecentColors = 8;
    private static final int colorSize = 30; //size of color boxes -> recent colors + preset palettes
    private Color selectedColor = Color.BLACK;
    private LinkedList<Color> recentColors = new LinkedList<>();
    
    private JSlider redSlider, greenSlider, blueSlider, brightnessSlider;
    private JLabel previewBox;
    private JPanel colorGradientPanel;
    private JPanel saturationGradientPanel;
    private JPanel recentColorsPanel;

    public ColorSelector(CanvasPanel canvas) {
        this.canvas = canvas;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 500)); // adjust size to fit left panel

        colorGradientPanel = createColorGradient();
        saturationGradientPanel = createSaturationGradient();
        JPanel gradientPanel = new JPanel(new BorderLayout(2,2));
        gradientPanel.add(saturationGradientPanel,BorderLayout.WEST);
        gradientPanel.add(colorGradientPanel,BorderLayout.CENTER);


        JPanel slidersPanel = createColorSliders();
        slidersPanel.setOpaque(false);
        recentColorsPanel = createRecentColorsPanel(); // store panel reference
        JTabbedPane presetsPane = createPresetsPanel();//
        presetsPane.setPreferredSize(new Dimension(250,50));
        presetsPane.setPreferredSize(new Dimension(200,70));//

        JPanel controlsPanel = new JPanel(new BorderLayout());
        //controlsPanel.setPreferredSize(new Dimension(300,400));
        controlsPanel.add(slidersPanel, BorderLayout.NORTH);
        controlsPanel.add(recentColorsPanel, BorderLayout.SOUTH);
        controlsPanel.add(presetsPane, BorderLayout.CENTER);//
        add(gradientPanel, BorderLayout.NORTH);
        add(controlsPanel, BorderLayout.CENTER);
        //add(presetsPanel,BorderLayout.SOUTH);
    }

    private JPanel createColorGradient() {
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int width = getWidth();
                int height = 100; // shorter gradient so it fits in the left panel better
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        float hue = (float) x / width;
                        float brightness = brightnessSlider.getValue() / 255f;
                        g.setColor(Color.getHSBColor(hue, 1f, brightness));
                        g.fillRect(x, y, 1, 1);
                    }
                }
            }
        };

        panel.setPreferredSize(new Dimension(250, 100)); // smaller size
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectColorFromGrid(e.getX(), e.getY());
            }
        });
        return panel;
    }
    
    private JPanel createSaturationGradient(){
        int width = 20;
        int height = 100;
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        float[] hsb = Color.RGBtoHSB(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue(), null);
                        float hue = hsb[0];
                        float satRange = (float) y/height;
                        //float brightness = brightnessSlider.getValue() / 255f;
                        g.setColor(Color.getHSBColor(hue, satRange, 1f));
                        g.fillRect(x, y, 1, 1);
                    }
                }
            }
        };

        panel.setPreferredSize(new Dimension(width, height)); // smaller size
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectSaturationFromGrid(e.getX(), e.getY());
            }
        });
        return panel;
    }

    private void selectColorFromGrid(int x, int y) {
        float hue = (float) x/colorGradientPanel.getWidth();
        float brightness = brightnessSlider.getValue() / 255f;
        selectedColor = Color.getHSBColor(hue, 1f, brightness);
        selectColor(selectedColor);
        updateUIFromSelectedColor();
    }

    private void selectSaturationFromGrid(int x, int y) {
        float[] hsb = Color.RGBtoHSB(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue(), null);
        float saturation = (float) y/saturationGradientPanel.getHeight();
        selectedColor = Color.getHSBColor(hsb[0], saturation, hsb[2]);
        selectColor(selectedColor);
        updateUIFromSelectedColor();
    }

    private JSlider createSlider(String name, JPanel panel) {
        JLabel label = new JLabel(name,SwingConstants.CENTER);
        JSlider slider = new JSlider(0, 255, 255);
        JLabel valueLabel = new JLabel("255",SwingConstants.CENTER);

        slider.addChangeListener(e -> {
            valueLabel.setText(String.valueOf(slider.getValue()));
            updatePreviewBox();
        });

        //Helps spacing of sliders when they are added to GridLayout
        JPanel sliderPanel = new JPanel(new BorderLayout());
        JPanel margin = new JPanel();
        margin.setPreferredSize(new Dimension(0,5));

        JPanel north = new JPanel();
        north.add(label);
        north.add(margin);

        JPanel south = new JPanel();
        south.add(valueLabel);
        south.add(margin);

        sliderPanel.add(north,BorderLayout.NORTH);
        sliderPanel.add(slider,BorderLayout.CENTER);        
        sliderPanel.add(south,BorderLayout.SOUTH);
        
        panel.add(sliderPanel);
        return slider;
    }

    private JPanel createColorSliders() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        redSlider = createSlider("Red", panel);
        greenSlider = createSlider("Green", panel);
        blueSlider = createSlider("Blue", panel);
        brightnessSlider = createSlider("Brightness", panel);

        brightnessSlider.addChangeListener(e -> {
            updatePreviewBox();
            colorGradientPanel.repaint(); // change gradient when brightness changes
        });

        previewBox = new JLabel();
        previewBox.setOpaque(true);
        previewBox.setPreferredSize(new Dimension(50, 50));
        updatePreviewBox();

        panel.add(new JLabel("Current colour:", SwingConstants.CENTER));
        panel.add(previewBox);
        return panel;
    }

    private JPanel createRecentColorsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        for (int i = 0; i < maxRecentColors; i++) {
            JPanel colorPanel = new JPanel();
            colorPanel.setPreferredSize(new Dimension(colorSize, colorSize));
            colorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            colorPanel.setBackground(Color.WHITE);

            final int index = i;
            colorPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (index < recentColors.size()) {
                        selectColor(recentColors.get(index));
                    }
                }
            });

            panel.add(colorPanel);
        }

        return panel;
    }

    private void selectColor(Color color) {
        if((canvas.getCurrentTool().equals("Erase"))==false) {
            canvas.setCurrentColour(color);
            selectedColor = color;
            updateRecentColors(color);
            updateUIFromSelectedColor();
        }
    }

    private void updateRecentColors(Color color) {
        if (recentColors.contains(color)) {
            recentColors.remove(color);
        }
        recentColors.addFirst(color);
        while (recentColors.size() > maxRecentColors) {
            recentColors.removeLast();
        }
        refreshRecentColorsUI();
    }

    private void refreshRecentColorsUI() {

        for (int i = 0; i < recentColorsPanel.getComponentCount(); i++) {
            JPanel colorPanel = (JPanel) recentColorsPanel.getComponent(i);
            if (i < recentColors.size()) {
                colorPanel.setBackground(recentColors.get(i));
            } else {
                colorPanel.setBackground(Color.WHITE);
            }
        }
        recentColorsPanel.repaint();
    }

    private Color getSelectedColor() {
        return new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue(), 255);
    }

    private void updateUIFromSelectedColor() {
        redSlider.setValue(selectedColor.getRed());
        greenSlider.setValue(selectedColor.getGreen());
        blueSlider.setValue(selectedColor.getBlue());
        saturationGradientPanel.repaint();
        updatePreviewBox();
    }

    private void updatePreviewBox() {
        Color currentColor = getSelectedColor(); 
        previewBox.setBackground(currentColor);
        previewBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        previewBox.setOpaque(true); // makes sure it's fully opaque bc it hates me
        previewBox.repaint(); 
    }

    private JTabbedPane createPresetsPanel() {
        JTabbedPane tabbedColours = new JTabbedPane();

        JPanel pinkPanel = new JPanel();
    
        pinkPanel.add(createColorGroup("Pinks", new Color[]{
            new Color(255, 182, 193, 255), new Color(255, 105, 180, 255), 
            new Color(255, 20, 147, 255), new Color(219, 112, 147, 255),
            new Color(255, 192, 203, 255), new Color(255, 174, 185, 255),
            new Color(255, 240, 245, 255), new Color(220, 20, 60, 255)
        }));

        tabbedColours.addTab("Pinks",pinkPanel);
    
        JPanel skinPanel = new JPanel();
        skinPanel.add(createColorGroup("Skintones", new Color[]{
            new Color(255, 224, 189, 255), new Color(255, 205, 148, 255),
            new Color(237, 194, 171, 255), new Color(210, 180, 140, 255),
            new Color(224, 172, 105, 255), new Color(193, 154, 107, 255),
            new Color(150, 111, 51, 255), new Color(101, 67, 33, 255)
        }));

        tabbedColours.addTab("Skin Tones",skinPanel);
    
        JPanel greenPanel = new JPanel();
        greenPanel.add(createColorGroup("Greens", new Color[]{
            new Color(144, 238, 144, 255), new Color(60, 179, 113, 255),
            new Color(46, 139, 87, 255), new Color(34, 139, 34, 255),
            new Color(0, 128, 0, 255), new Color(0, 100, 0, 255),
            new Color(154, 205, 50, 255), new Color(107, 142, 35, 255)
        }));

        tabbedColours.addTab("Greens", greenPanel);

        JPanel monoPanel = new JPanel();
        monoPanel.add(createColorGroup("Mono", new Color[]{
            new Color(0, 0, 0, 255), new Color(62, 62, 62, 255),
            new Color(93, 93, 93, 255), new Color(124, 124, 124, 255),
            new Color(155, 155, 155, 255), new Color(186, 186, 186, 255),
            new Color(210, 210, 210, 255), new Color(255, 255, 255, 255)
        }));

        tabbedColours.addTab("Mono", monoPanel);
    
        return tabbedColours;
    }
    
    private JPanel createColorGroup(String title, Color[] colors) {
        JPanel groupPanel = new JPanel(new BorderLayout());
    
/*         JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        groupPanel.add(titleLabel, BorderLayout.NORTH); */
    
        JPanel colorGrid = new JPanel(new GridLayout(1, 10, 5, 5)); // 2x4 grid with spacing - Changed to 10x1
        for (Color color : colors) {
            JPanel colorPanel = new JPanel();
            colorPanel.setBackground(color);
            colorPanel.setPreferredSize(new Dimension(colorSize, colorSize));
            colorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
            colorPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectColor(color); // apply color directly
                }
            });
    
            colorGrid.add(colorPanel);
        }
    
        groupPanel.add(colorGrid, BorderLayout.CENTER);
        return groupPanel;
    }
}