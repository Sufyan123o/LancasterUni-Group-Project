package g82;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainMenu {

    public MainMenu() {
        SwingUtilities.invokeLater(MainMenu::createAndShowGUI);
    }

    static void createAndShowGUI() {
        JFrame frame = new JFrame("Minedit!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        URL iconURL = MainMenu.class.getResource("/mineditLogo.png");
        Image appIcon = new ImageIcon(iconURL).getImage();
        frame.setIconImage(appIcon);
    
        JPanel panel = new JPanel() {
            private Image bgImage = new ImageIcon(MainMenu.class.getResource("/background.png")).getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
        try {
            URL imageURL = MainMenu.class.getResource("/placeholder.png");
            ImageIcon topIcon = new ImageIcon(imageURL); 
            Image scaledImage = topIcon.getImage().getScaledInstance(300, 150, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(Box.createVerticalStrut(20));
            panel.add(imageLabel);
        } catch (Exception e) {
            System.out.println("Failed to load top image.");
        }
    
        JLabel createLabel = new JLabel("CREATE NEW PROJECT", SwingConstants.CENTER);
        createLabel.setForeground(Color.WHITE);
        createLabel.setFont(new Font("Arial", Font.BOLD, 36));
        createLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        // NEW PROJECT ICONS
        JPanel iconsPanel = new JPanel();
        // allow the main background to show through
        iconsPanel.setOpaque(false);
        iconsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        
        URL steveURL = MainMenu.class.getResource("/steve_face.png");
        iconsPanel.add(createIconButton("SKIN", steveURL, e -> {
            frame.setVisible(false);
            MainWindow skinWindow = new MainWindow(16, 16, 60, "skin");
            skinWindow.setVisible(true);
        }));

        URL paintURL = MainMenu.class.getResource("/painting.png");
        iconsPanel.add(createIconButton("ART", paintURL, e -> {
            System.out.println("ART button clicked!");
            int[] values = showIntInputDialog(frame);
            if (values != null) {
                System.out.println("You entered: " + values[0] + " and " + values[1]);
                System.out.println("X = " + values[0] + " Y = " + values[1] + " Grid Size = " + values[2]);
                frame.setVisible(false);
                MainWindow mainWindow = new MainWindow(values[0], values[1], values[2], "art");
                mainWindow.setVisible(true);
            } else {
                System.out.println("User canceled or closed the dialog.");
            }
        }));
        
        URL blockURL = MainMenu.class.getResource("/grass_block.png");
        iconsPanel.add(createIconButton("BLOCK", blockURL, e -> {
            System.out.println("BLOCK button clicked!");
            // creates a 16×16 canvas for block editing
            frame.setVisible(false);
            MainWindow mainWindow = new MainWindow(16, 16, 60, "block");
            mainWindow.setVisible(true);
        }));
    
        // LOAD BUTTON
        JButton loadButton = new JButton("LOAD");
        loadButton.setFont(new Font("Arial", Font.BOLD, 24));
        loadButton.setBackground(Color.GREEN.darker());
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadButton.setMaximumSize(new Dimension(200, 50));

        loadButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loadButton.setBackground(Color.BLUE);
            }
        
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loadButton.setBackground(Color.GREEN.darker());
            }
        });

        loadButton.addActionListener(e -> {
            System.out.println("LOAD button clicked!");
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                try {
                    // Import the image into a Color[][] grid
                    Color[][] grid = g82.controllers.ImageController.ImportImage(file.getAbsolutePath());
                    int rows = grid.length;
                    int cols = grid[0].length;
        
                    // Estimate grid size (you can adjust this logic if needed)
                    int gridSize = (int) (1024.0 / (rows * 2));
                    if (gridSize < 1) gridSize = 1;
        
                    // Create main window with dimensions from the image
                    MainWindow mainWindow = new MainWindow(rows, cols, gridSize, "art"); // note: x=cols, y=rows
        
                    // Set the grid onto the canvas
                    mainWindow.setVisible(true);
                    mainWindow.requestFocus();
                    mainWindow.getCanvasPanel().setImageGrid(grid); // see next step
        
                    frame.dispose(); // close main menu
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to load image: " + ex.getMessage());
                }
            }
        });

        loadButton.setToolTipText("Load an image to edit or existing project");
    
        // NEW INFO BUTTON
        JButton infoButton = new JButton("INFO");
        infoButton.setFont(new Font("Arial", Font.BOLD, 24));
        infoButton.setBackground(Color.GREEN.darker());
        infoButton.setForeground(Color.WHITE);
        infoButton.setFocusPainted(false);
        infoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoButton.setMaximumSize(new Dimension(200, 50));

        infoButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                infoButton.setBackground(Color.BLUE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                infoButton.setBackground(Color.GREEN.darker());
            }
        });

        infoButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                frame,
                "Welcome to Minedit!\n\n" +
                "SKIN - Edit a Minecraft skin or create your own. Load Steve or Alex skins from the file menu in the top left corner.\n" +
                "ART - Create pixel art or edit existing images.\n" +
                "BLOCK - Edit a Minecraft texture pack or create your own.\n" +
                "Shortcuts: Ctrl+Scroll to Zoom In/Out, Alt+B: Brush, Alt+E: Eraser, Alt+F: Fill, Alt+P: Colour Picker,\nCtrl+Z: Undo, Ctrl+Y: Redo, Ctrl+O: Import Image, Ctrl+S: Save Image\n",
                "Information",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        

        // === ADD COMPONENTS TO PANEL ===
        panel.add(Box.createVerticalStrut(20));
        panel.add(createLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(iconsPanel);
        panel.add(loadButton);
        panel.add(Box.createVerticalStrut(10)); // Add some spacing between buttons
        panel.add(infoButton);
    
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
    

    private static JPanel createIconButton(String label, URL imagePath, ActionListener action) {
        JPanel panel = new JPanel(new BorderLayout());
        // make icon button panel transparent
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
    
        JButton iconButton; // assigns iimages to the buttons
        try {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaled = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            iconButton = new JButton(new ImageIcon(scaled));
        } catch (Exception e) {
            iconButton = new JButton("?"); // fallback in case the image is not found
        }
    
        iconButton.setFocusPainted(false); 
        iconButton.setContentAreaFilled(false); 
        iconButton.setBorderPainted(false); 
        iconButton.setOpaque(false);
    
        // if you leave the mouse still over the button it tells you what it will do, might be useful in the main drawing stuff to say what tools can do
        iconButton.setToolTipText("Create a new " + label.toLowerCase() + " project");
        iconButton.addActionListener(action);
    
        // highlights buttons in blue when hovered over
        iconButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            }
    
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
            }
        });
    
        JLabel textLabel = new JLabel(label, SwingConstants.CENTER);
        textLabel.setForeground(Color.WHITE);
    
        panel.add(iconButton, BorderLayout.CENTER);
        panel.add(textLabel, BorderLayout.SOUTH);
    
        return panel;
    }
    
    public static int[] showIntInputDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Enter Image Dimensions", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new GridLayout(4, 1, 10, 5));

        JTextField field1 = new JTextField();
        JTextField field2 = new JTextField();

        JLabel errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);

        JButton submitButton = new JButton("Submit");

        final int[] result = new int[3]; // to store returned values
        // x, y, gridsize
        final boolean[] submitted = {false}; // to know if the user pressed submit

        submitButton.addActionListener(e -> {
            try {
                int val1 = Integer.parseInt(field1.getText().trim());
                int val2 = Integer.parseInt(field2.getText().trim());
                if (val1 <= 0 || val2 <= 0) {
                    throw new NumberFormatException("Values must be positive integers.");
                }
                else if(val1 > 801 || val2 > 801) {
                    throw new NumberFormatException("Values must be 800 or less.");
                }
                result[0] = val1;
                result[1] = val2;
                float pixels = val1 * 2;
                if(val1 > 401 || val2 > 401) {
                    result[2] = 1; // here the canvas is big enough to be set to GRIDSIZE 1
                }
                else{
                    pixels = 1024 / pixels;
                    result[2] = (int) pixels; // calculate grid size based on the area of the image
                }
                
                submitted[0] = true;
                dialog.dispose();
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter valid integers (800 or under)!");
            }
        });

        dialog.add(new JLabel("Enter Rows (X):", SwingConstants.CENTER));
        dialog.add(field1);
        dialog.add(new JLabel("Enter Columns (Y):", SwingConstants.CENTER));
        dialog.add(field2);
        dialog.add(errorLabel);
        dialog.add(submitButton);

        dialog.setLayout(new GridLayout(6, 1));
        dialog.setVisible(true);

        return submitted[0] ? result : null; // null if user didn't submit
    }
}

