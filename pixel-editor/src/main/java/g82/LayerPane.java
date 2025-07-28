package g82;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import g82.models.LayerModel;

public class LayerPane extends JScrollPane{
    private CanvasPanel canvas;
    private JPanel layerList;                   //Create JPanel layerList to hold all layer buttons to go in viewport
    private Dimension preferredButtonSize;
    private Color headerColour;
    private Color accentColour;

    public LayerPane(CanvasPanel c){
        this.canvas = c;
        this.preferredButtonSize = new Dimension(100,60);
        this.headerColour = new Color(66,66,66);
        this.accentColour = new Color(190,190,190);
        //Set vertical scroll bar only
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //Populate panel with current layers
        this.refreshLayers();


        //Create header and colour formatting for scroll pane
        JLabel header = new JLabel("Layer Selector");
        header.setBackground(headerColour);     //Set header to opaque and grey
        header.setOpaque(true);
        header.setForeground(Color.WHITE);      //Set text colour to white
        this.setColumnHeaderView(header);
        this.setBackground(headerColour);


        //Create side toolbar
        JToolBar sideToolBar = new JToolBar(1);
        //sideToolBar.setPreferredSize(new Dimension(150,150));


        //Create toolbar buttons
        URL imageURL = this.getClass().getResource("/add.png");
        JButton addLayerButton = new JButton(new ImageIcon(imageURL));
        addLayerButton.setToolTipText("Create New Layer");
        addLayerButton.addActionListener(e -> {
            canvas.addNewLayer();
            this.refreshLayers();
        });

        imageURL = this.getClass().getResource("/bin.png");
        JButton removeLayerButton = new JButton(new ImageIcon(imageURL));
        removeLayerButton.setToolTipText("Delete Current Layer");
        removeLayerButton.addActionListener(e -> {
            canvas.deleteCurrentLayer();
            this.refreshLayers();
        });
        

        imageURL = this.getClass().getResource("/up.png");
        JButton moveUpButton = new JButton(new ImageIcon(imageURL));
        moveUpButton.setToolTipText("Move Up");
        moveUpButton.addActionListener(e -> {
            canvas.moveCurrentLayerUp();
            this.refreshLayers();
        });
        

        imageURL = this.getClass().getResource("/down.png");
        JButton moveDownButton = new JButton(new ImageIcon(imageURL));
        moveDownButton.setToolTipText("Move Down");
        moveDownButton.addActionListener(e -> {
            canvas.moveCurrentLayerDown();
            this.refreshLayers();
        });

        imageURL = this.getClass().getResource("/opacity.png");
        JButton adjustOpacityButton = new JButton(new ImageIcon(imageURL));
        adjustOpacityButton.setToolTipText("Adjust Opacity");
        adjustOpacityButton.addActionListener(e -> {
            //On click triggers new option window with text field for user to enter a value
            String m = JOptionPane.showInputDialog("Enter opacity value (0 transparent - 1 opaque):");
            if (m != null && !m.isEmpty()) {
                float alpha = Float.parseFloat(m);
                canvas.setCurrentLayerOpacity(alpha);
            }
        });

        imageURL = this.getClass().getResource("/visible.png");
        JButton toggleVisibilityButton = new JButton(new ImageIcon(imageURL));
        toggleVisibilityButton.setToolTipText("Toggle Visibility");
        toggleVisibilityButton.addActionListener(e -> {
            canvas.toggleCurrentLayerVisibility();
            this.refreshLayers();
        });


        //Populate toolbar with buttons and add to scroll pane
        sideToolBar.add(addLayerButton);
        sideToolBar.add(removeLayerButton);
        sideToolBar.add(moveUpButton);
        sideToolBar.add(moveDownButton);
        sideToolBar.add(adjustOpacityButton);
        sideToolBar.add(toggleVisibilityButton);
        this.setRowHeaderView(sideToolBar);
    }

    //Update the layers in the scroll pane
    //Shall be called after every change in order, addition or deletion of layers
    public void refreshLayers(){
        //Pull current order of layers and get current layer
        LayerModel[] layers = this.canvas.getLayerOrder();
        LayerModel currentLayer = canvas.getCurrentLayer();

        //Panel of layers to go in viewport
        layerList = new JPanel(new GridLayout(10,1));
        for (int i = (layers.length-1); i > -1; i--){
            LayerModel layer = layers[i];
            String layerName = "Layer " + layer.getName();

            //If layer is set to invisible, strikeout layer name
            if(!layer.isVisible()){
                layerName = "<html><strike>" + layerName + "</strike></html>";
            }
            JButton layerButton = new JButton(layerName);
            layerButton.addActionListener(e -> {
                canvas.setCurrentLayer(layer);
                refreshLayers();
            });

            //Make button for current layer grey to indicate selection
            if (layer == currentLayer){
                layerButton.setBackground(accentColour);
            }
            layerButton.setPreferredSize(preferredButtonSize);
            layerList.add(layerButton);
        }

        this.setViewportView(layerList);
    }
}
