package g82;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CanvasZoom extends JScrollPane {
    private CanvasPanel canvas;
    private double scale = 1.0;
    private final double ZOOM_FACTOR = 0.1;

    public CanvasZoom(CanvasPanel canvas) {
        super(canvas);
        this.canvas = canvas;

        setPreferredSize(new Dimension(1000, 800));
        setWheelScrollingEnabled(false);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        canvas.addMouseWheelListener(e -> {
            if (e.isControlDown()) { // hold Ctrl + scroll to zoom
                if (e.getWheelRotation() < 0) {
                    zoomIn();
                } 
                else {
                    zoomOut();
                }
                e.consume();
            } 
            else {
                // normal scrolling
                getHorizontalScrollBar().setValue(getHorizontalScrollBar().getValue() + e.getScrollAmount() * e.getWheelRotation());
            }
        });
    }

    public void zoomIn() {
        scale += ZOOM_FACTOR;
        applyZoom();
    }

    public void zoomOut() {
        scale = Math.max(0.1, scale - ZOOM_FACTOR);
        applyZoom();
    }

    private void applyZoom() {
        canvas.setScale(scale);
        canvas.updateCanvasSize();
        canvas.revalidate();
        canvas.repaint();
    }
}
