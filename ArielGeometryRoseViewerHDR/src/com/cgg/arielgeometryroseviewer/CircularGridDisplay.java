/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometryroseviewer;

import com.cgg.arielgeometry.model.types.XYLocation;
import com.cgg.arielgeometry.model.types.EndCoordinateList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author jgrimsdale
 */
public class CircularGridDisplay extends JComponent {

    private static final long serialVersionUID = 42L;
    private CircularGrid grid;
    private BufferedImage canvas;
    private boolean debug = false;
    private int canvaswidth;
    private int canvasheight;
    private float xscale;
    private float yscale;

    public CircularGridDisplay(CircularGrid grid) {
        this.grid = grid;
        setPreferredSize(new Dimension(grid.getDiameter(), grid.getDiameter()));
        canvas = new BufferedImage(grid.getDiameter(), grid.getDiameter(), BufferedImage.TYPE_INT_ARGB);

        if(!debug) fillCanvas();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        PaintScale scale = new InterpolatedPaintScale(10.0f, 0, 10.0f);
        NumberAxis scaleAxis = new NumberAxis("Scale");
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);
        scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 7));
        PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);
        legend.setStripOutlineVisible(false);
        legend.setSubdivisionCount(20);
        legend.setAxisOffset(5.0);
        legend.setMargin(new RectangleInsets(5, 5, 5, 5));
        legend.setFrame(new BlockBorder(Color.WHITE));
        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
        legend.setStripWidth(10);
        legend.setPosition(RectangleEdge.RIGHT);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);
//        legend.draw(g2, new Rectangle2D.Float(0, 0, 80, 300));
    }

    public final void fillCanvas() {
        InterpolatedPaintScale paintScale = new InterpolatedPaintScale((float) grid.getMaxCellWeight());
        
        canvaswidth = canvas.getWidth();
        canvasheight = canvas.getHeight();
        xscale = grid.getDiameter()/canvaswidth;
        yscale = grid.getDiameter()/canvasheight;
        
        if(debug) System.out.println("CircularGridDisplay.fillCanvas canvaswidth canvasheight "+canvaswidth+" "+canvasheight);

        //InterpolatedPaintScale
        for (int x = 0; x < canvaswidth; x++) {
            for (int y = 0; y < canvasheight; y++) {
                int id_cell = grid.findCell(scalex(x), scaley(y));

                if (id_cell != -1) {
                    if(debug) System.out.println("CircularGridDisplay.fillCanvas x y weight "+x+" "+y+" "+grid.getCellWeight(id_cell));
                    canvas.setRGB(x, y, ((Color) paintScale.getPaint((float) grid.getCellWeight(id_cell))).getRGB());
                }
                else {
                    if(debug) System.out.println("CircularGridDisplay.fillCanvas x y c "+x+" "+y+" "+"White");
                    canvas.setRGB(x, y, Color.BLUE.getRGB());
                }
            }
        }

        repaint();
    }
    
    private int scalex(int x){
        return (int)xscale*(x - (int) (canvaswidth / 2.));
    }
    
    private int scaley(int y) {
        return (int)yscale*(y - (int) (canvasheight / 2.));
    }
    
    
    public final void drawplot(EndCoordinateList endCoordinateList) {
        List<XYLocation> list = endCoordinateList.list;
        int imax = list.size();
        float data[][] = new float[2][imax];
        for(int i = 0; i < imax; i++) {
            data[0][i] = list.get(i).x;
            data[1][i] = list.get(i).y;
        }
        float radius = endCoordinateList.maxx > endCoordinateList.maxy ? endCoordinateList.maxx : endCoordinateList.maxy;
        grid = new CircularGrid(radius, data, 10000);
        if(debug) System.out.println("CircularGridDisplay.drawplot this.getHeight() "+this.getHeight());
        if(debug) System.out.println("CircularGridDisplay.drawplot this.getWidth() "+this.getWidth());
        if(debug) System.out.println("CircularGridDisplay.drawplot imax "+imax);
        if(debug) System.out.println("CircularGridDisplay.drawplot grid.getRadius "+grid.getRadius());        
        if(debug) System.out.println("CircularGridDisplay.drawplot grid.getDiameter "+grid.getDiameter());
        if(debug) System.out.println("CircularGridDisplay.drawplot grid.getMaxCellWeight "+grid.getMaxCellWeight());
        if(debug) System.out.println("CircularGridDisplay.drawplot grid.getTotalCells "+grid.getTotalCells());
        if(debug) System.out.println("CircularGridDisplay.drawplot grid.getTotalPoints "+grid.getTotalPoints());

        fillCanvas();
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
        CircularGridDisplay demo = new CircularGridDisplay(grid);
        demo.displayInFrame();
    }
}
