/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometryjfcviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author jgrimsdale
 */
public class EditingPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    BufferedImage background;
    boolean firstTime = true;
    int itlx = 0; // Image top left x
    int itly = 0; // Image top left y
    List<Point> pointList;
    Point dragstartpoint;
    double scale = 1.0;
    double zoomfactor = 1.05;
    int maptoplx = 130000;
    int maptoply = 3080000;
    int mapwidth = 20000;
    int mapheight = 20000;
    int imgwidth;
    int imgheight;
    double xscale;
    double yscale;

    public EditingPanel() {
        setBackground(Color.white);
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);

        try {
            background = ImageIO.read(new File("C:\\Users\\hdrira\\Documents\\NetBeansProjects\\Images sources\\Moussafir A0 SURVEY map.png"));
        }
        catch (IOException e) {
            System.out.println("Cannot open file for background");
        }

        JButton jb = new JButton("Clear");
        jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearpoints();
                repaint();
            }
        });
        add(jb);

        imgwidth = background.getWidth();
        imgheight = background.getHeight();
        xscale = mapwidth / imgwidth;
        yscale = mapheight / imgheight;

        pointList = new ArrayList<>();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 600);
    }

    // Handles the event of the user pressing down the mouse button.
    @Override
    public void mousePressed(MouseEvent e) {
        dragstartpoint = new Point(e.getX(), e.getY());
    }

    private void addpoint(int x, int y) {
        pointList.add(new Point(x, y));
    }

    private void removepoint(int x, int y) {
        Rectangle r = new Rectangle(x - 5, y - 5, 10, 10);
        Point pointtoremove = null;
        for (Point p : pointList) {
            if (r.contains(p)) {
                pointtoremove = p;
            }
        }
        pointList.remove(pointtoremove);
    }

    private void movepoint(int x, int y, int tox, int toy) {
        Rectangle r = new Rectangle(x - 5, y - 5, 10, 10);
        Point newpoint = new Point(tox, toy);
        for (Point p : pointList) {
            if (r.contains(p)) {
                int index = pointList.indexOf(p);
                pointList.set(index, newpoint);
            }
        }
    }

    private void clearpoints() {
        pointList.clear();
    }

    private void drawpoint(Graphics2D g, int x, int y) {
        g.fillOval((int) scale(x - 2), (int) scale(y - 2), 5, 5);
    }

    private void drawline(Graphics2D g, Point p1, Point p2) {
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void movepoint(MouseEvent e) {
        int x = dragstartpoint.x;
        int y = dragstartpoint.y;
        int newx = e.getX();
        int newy = e.getY();
        movepoint(x, y, newx, newy);
        dragstartpoint = new Point(newx, newy);
        repaint();
    }

    private void moveimage(MouseEvent e) {
        int x = dragstartpoint.x;
        int y = dragstartpoint.y;
        int newx = e.getX();
        int newy = e.getY();
        itlx += (newx - x);
        itly += (newy - y);
        dragstartpoint = new Point(newx, newy);
        repaint();
    }

    private void zoom(int direction) {
        if (direction < 0) {
            System.out.println("Zoom in");
            scale *= zoomfactor;
        }
        else {
            System.out.println("Zoom out");
            scale *= (1 / zoomfactor);
        }
        repaint();
    }

    private double scale(float v) {
        return v * scale;
    }

    // Handles the event of a user dragging the mouse while holding
    // down the mouse button.
    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.isControlDown()) {
            moveimage(e);
        }
        else {
            movepoint(e);
        }
    }

    // Handles the event of a user releasing the mouse button.
    @Override
    public void mouseReleased(MouseEvent e) {
//        System.out.println("mouseReleased");
    }

    // This method is required by MouseListener.
    @Override
    public void mouseMoved(MouseEvent e) {
//        System.out.println("mouseMoved");
    }

    // These methods are required by MouseMotionListener.
    @Override
    public void mouseClicked(MouseEvent e) {
//        System.out.println("mouseClicked");
        if (e.getButton() == MouseEvent.BUTTON1) {
//            System.out.println("mouseB1Clicked");
            addpoint(e.getX(), e.getY());
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
//            System.out.println("mouseB3Clicked");
            removepoint(e.getX(), e.getY());
        }
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
//        System.out.println("mouseExited");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
//        System.out.println("mouseEntered");
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform at = g2.getTransform();
        g2.scale(scale, scale);
        g2.drawImage(background, itlx, itly, this);
        Point previouspoint = null;
        for (Point p : pointList) {
            drawpoint(g2, p.x, p.y);
            if (previouspoint != null) {
                drawline(g2, p, previouspoint);
            }
            previouspoint = p;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoom(e.getWheelRotation());
    }

    private Point screen2real(Point p) {
        int x = maptoplx + (int)(p.x * xscale);
        int y = maptoply + (int)(p.y * yscale);
        return new Point(x, y);
    }

    private Point real2screen(Point p) {
        int x = (int)((p.x-maptoplx)/xscale);
        int y = (int)((p.y-maptoply)/yscale);
        return new Point(x, y);
    }
}
