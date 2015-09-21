/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometryroseviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author jgrimsdale
 */
public class CircularGridDemo extends JComponent {

    private static final long serialVersionUID = 42L;
    private CircularGrid grid;
    private BufferedImage canvas;

    public CircularGridDemo(CircularGrid grid) {
        this.grid = grid;
        setPreferredSize(new Dimension(grid.getDiameter(), grid.getDiameter()));
        canvas = new BufferedImage(grid.getDiameter(), grid.getDiameter(), BufferedImage.TYPE_INT_ARGB);

        fillCanvas();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);
    }

    public final void fillCanvas() {
        InterpolatedPaintScale paintScale = new InterpolatedPaintScale((float) grid.getMaxCellWeight());

        //InterpolatedPaintScale
        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                int id_cell = grid.findCell(x - (int) (canvas.getWidth() / 2.), y - (int) (canvas.getHeight() / 2.));

                if (id_cell != -1) {
                    canvas.setRGB(x, y, ((Color) paintScale.getPaint((float) grid.getCellWeight(id_cell))).getRGB());
                }
                else {
                    canvas.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        repaint();
    }

    public void displayInFrame() {
        JFrame frame = new JFrame("CircularGridDemo");
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        CircularGrid grid = new CircularGrid(1000, 200.0f);
        CircularGridDemo demo = new CircularGridDemo(grid);
        demo.displayInFrame();
    }
}
