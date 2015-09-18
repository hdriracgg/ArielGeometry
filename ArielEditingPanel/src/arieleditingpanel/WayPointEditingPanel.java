/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arieleditingpanel;

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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author jgrimsdale
 */
public class WayPointEditingPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    BufferedImage background;
    boolean firstTime = true;
    boolean debug = true;
    int itlx = 0; // Image top left x
    int itly = 0; // Image top left y
    List<Point> pointList;
    List<Float> speedList;   // speed in m/s
    Point dragstartpoint;
    double scale = 1.0;
    double zoomfactor = 1.05;
    int xoffset;    // screen x coordinate of left edge of map 
    int yoffset;    // screen y coordinate of top edge of map
    int xorigin;    // real x coordinate of left edge of map
    int yorigin;    // real y coordinate of top edge of map
    double xscale;  // scale factor between screen and real in x direction
    double yscale;  // scale factor between screen and real in y direction
    boolean calibrating = false;
    boolean defaultcalibration = false;
    List<Point> calibrationpointlist;
    JDialog calibrationdialog;
    int speed = 25;
    // Marina swarm paramters
    int nbNodePerGroup = 4;
    int nbNodeperLine = 4;
    int stepDuration = 10;
    int marinasDistance = 125;

    public WayPointEditingPanel() {
        setBackground(Color.white);
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);

        try {
            background = ImageIO.read(new File("C:\\Users\\jgrimsdale\\Desktop\\Moussafir A0 SURVEY map.png"));
        }
        catch (IOException e) {
            System.out.println("Cannot open file for background");
        }

        JButton jb1 = new JButton("Clear");
        jb1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearpoints();
                repaint();
            }
        });
        add(jb1);

        JButton jb2 = new JButton("Print");
        jb2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printpoints();
                repaint();
            }
        });
        add(jb2);

        JButton jb3 = new JButton("Calibrate");
        jb3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calibrate();
            }
        });
        add(jb3);

        JButton jb4 = new JButton("Marinas");
        jb4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writemarinatrajectories();
            }
        });
        add(jb4);

        JButton jb5 = new JButton("Sources");
        jb5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writesourcetrajectories();
            }
        });
        add(jb5);

        JLabel jl1 = new JLabel("Speed in cm/s = ");
        jl1.setOpaque(true);
        jl1.setBackground(Color.LIGHT_GRAY);
        add(jl1);

        final JTextField jtf1 = new JTextField();
        jtf1.setText(Integer.toString(speed));
        jtf1.setToolTipText("Speed in cm/s");
        jtf1.setPreferredSize(new Dimension(50, 27));
        jtf1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speed = Integer.parseInt(jtf1.getText());
                System.out.println("Speed changed to " + speed + " cm/s");
            }
        });
        add(jtf1);

        pointList = new ArrayList<>();
        speedList = new ArrayList<>();
        calibrationpointlist = new ArrayList<>();
        setdefaultcalibration();
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
        pointList.add(screen2transformed(x, y));
        speedList.add((float) speed / 100);
    }

    private void removepoint(int x, int y) {
        Point tmppoint = screen2transformed(x, y);
        Rectangle r = new Rectangle(tmppoint.x - 5, tmppoint.y - 5, 10, 10);
        Point pointtoremove = null;
        for (Point p : pointList) {
            if (r.contains(p)) {
                pointtoremove = p;
            }
        }
        int index = pointList.indexOf(pointtoremove);
        pointList.remove(index);
        speedList.remove(index);
    }

    private void movepoint(int x, int y, int tox, int toy) {
        Point tmporigin = screen2transformed(x, y);
        Point tmpdestination = screen2transformed(tox, toy);
        Rectangle r = new Rectangle(tmporigin.x - 5, tmporigin.y - 5, 10, 10);
        Point newpoint = new Point(tmpdestination.x, tmpdestination.y);
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
        g.fillOval(x - 2, y - 2, 5, 5);
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
    }

    private void moveimage(MouseEvent e) {
        int x = dragstartpoint.x;
        int y = dragstartpoint.y;
        int newx = e.getX();
        int newy = e.getY();
        itlx += (newx - x);
        itly += (newy - y);
        dragstartpoint = new Point(newx, newy);
    }

    private void zoom(int direction) {
        if (direction < 0) {
            scale *= zoomfactor;
        }
        else {
            scale *= (1 / zoomfactor);
        }
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
        repaint();
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
        if (calibrating) {
            setcalibrationpoint(e.getX(), e.getY());
        }
        else {
            if (e.getButton() == MouseEvent.BUTTON1) {
                addpoint(e.getX(), e.getY());
            }
            if (e.getButton() == MouseEvent.BUTTON3) {
                removepoint(e.getX(), e.getY());
            }
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
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoom(e.getWheelRotation());
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform at = g2.getTransform();
        g2.scale(scale, scale);
        g2.translate(itlx, itly);
        g2.drawImage(background, 0, 0, this);
        Point previouspoint = null;
        for (Point p : pointList) {
            drawpoint(g2, p.x, p.y);
            if (previouspoint != null) {
                drawline(g2, p, previouspoint);
            }
            previouspoint = p;
        }
        if (!calibrationpointlist.isEmpty()) {
            for (Point p : calibrationpointlist) {
                drawpoint(g2, p.x, p.y);
            }
        }
        g2.setTransform(at);
    }

    private Point screen2transformed(int x, int y) {
        return new Point((int) ((x / scale) - itlx), (int) ((y / scale) - itly));
    }

    private Point screen2real(Point p) {
        int x = xorigin + (int) (xscale * (p.x - xoffset));
        int y = yorigin - (int) (yscale * (p.y - yoffset));
        return new Point(x, y);
    }

    private List<Point> printpoints() {
        if (pointList.isEmpty()) {
            System.out.println("No points clicked yet");
        }
        for (Point p : pointList) {
            System.out.println("Point = " + p);
        }
        if (!calibrationpointlist.isEmpty()) {
            for (Point p : pointList) {
                System.out.println("Real point = " + screen2real(p));
            }
        }
        return pointList;
    }

    private void writewaypoints() {
        PrintStream ps;
        JFileChooser jfc = new JFileChooser("C:\\Users\\jgrimsdale\\Desktop");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("csv Files", "csv");
        jfc.setFileFilter(filter);
        jfc.showSaveDialog(this);
        File of = jfc.getSelectedFile();
        String outputfilename = of.getAbsolutePath();
        System.out.println("Writing to file " + outputfilename);
        try {
            ps = new PrintStream(of);
            writewaypoints(ps);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found " + outputfilename);
        }
    }

    private void writewaypoints(PrintStream ps) {
        for (int i = 0; i < pointList.size(); i++) {
            Point realpoint = screen2real(pointList.get(i));
            float s = speedList.get(i);
            ps.printf("%d, %d, %05.3f\n", realpoint.x, realpoint.y, s);
        }
        ps.close();
    }

    private void writemarinatrajectories() {
        // Choose and create directory if needed
        JFileChooser jfc = new JFileChooser("C:\\Users\\jgrimsdale\\Desktop");
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File od = null;
        while (od == null) {
            jfc.showSaveDialog(this);
            od = jfc.getSelectedFile();
            if (od == null) {
                System.out.println("Ordinary file exists with this name");
            }
        }
        if (!od.isDirectory()) {
            System.out.println("Create directory");
            try {
                Files.createDirectory(od.toPath());
            }
            catch (IOException ex) {
                Logger.getLogger(WayPointEditingPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String outputdirectory = od.getAbsolutePath();
        System.out.println("Writing to directory " + outputdirectory);

        // Print waypoints to stdout
        if (debug) {
            printpoints();
        }

        // Prepare list of real points
        List<Point> realpointList = new ArrayList<>();
        for (Point p : pointList) {
            realpointList.add(screen2real(p));
        }

        // Test interpolation
        if (debug) {
            printmarinatrajectories();
        }

        // Run the interpolation
        MarinasPositionsCalculator mpc = new MarinasPositionsCalculator(realpointList,
                speedList, nbNodePerGroup, nbNodeperLine, stepDuration, marinasDistance);
        List<Map<Integer, Node>> lmin = mpc.getMarinasPoints();

        // Output each Marina to a separate file in chosen directory
        for (int i = 0; i < mpc.getNbMarinas(); i++) {
            PrintStream ps = getps("Marina", i, od);
            writetrajectories(ps, lmin, i);
        }
        System.out.println("Writing Marinas complete");
    }
    
       private void writesourcetrajectories() {
        // Choose and create directory if needed
        JFileChooser jfc = new JFileChooser("C:\\Users\\jgrimsdale\\Desktop");
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File od = null;
        while (od == null) {
            jfc.showSaveDialog(this);
            od = jfc.getSelectedFile();
            if (od == null) {
                System.out.println("Ordinary file exists with this name");
            }
        }
        if (!od.isDirectory()) {
            System.out.println("Create directory");
            try {
                Files.createDirectory(od.toPath());
            }
            catch (IOException ex) {
                Logger.getLogger(WayPointEditingPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String outputdirectory = od.getAbsolutePath();
        System.out.println("Writing to directory " + outputdirectory);

        // Print waypoints to stdout
        if (debug) {
            printpoints();
        }

        // Prepare list of real points
        List<Point> realpointList = new ArrayList<>();
        for (Point p : pointList) {
            realpointList.add(screen2real(p));
        }

        // Run the interpolation
        MarinasPositionsCalculator mpc = new MarinasPositionsCalculator(realpointList,
                speedList, 1, 1, stepDuration, marinasDistance);
        List<Map<Integer, Node>> lmin = mpc.getMarinasPoints();

        // Output each shot to a separate file in chosen directory
        for (int i = 0; i < mpc.getNbMarinas(); i++) {
            PrintStream ps = getps("Source", i, od);
            writetrajectories(ps, lmin, i);
        }
        System.out.println("Writing sources complete");
    }

    private void writetrajectories(PrintStream ps, List<Map<Integer, Node>> lmin, int i) {
        ps.println("time,x,y,depth,nominal_WH,propulsion_WH,communication_WH,total_WH,duration_estimation_DAYS");
        for (Map m : lmin) {
            Node n = (Node) m.get(i);
            ps.printf("%.1f, %.1f, %.1f,,,,,,\n", (double) n.getTime(), (float) n.getCoodinates().x, (float) n.getCoodinates().y);
        }
        ps.close();
    }

    // Prints the marina trajectories for debug purposes
    private void printmarinatrajectories() {
        // Prepare list of real points
        List<Point> realpointList = new ArrayList<>();
        for (Point p : pointList) {
            realpointList.add(screen2real(p));
        }

        // Run the interpolation
        MarinasPositionsCalculator mpc = new MarinasPositionsCalculator(realpointList,
                speedList, nbNodePerGroup, nbNodeperLine, stepDuration, marinasDistance);
        List<Map<Integer, Node>> lmin = mpc.getMarinasPoints();

        // Print results
        for (Map m : lmin) {
            for (int i = 0; i < mpc.getNbMarinas(); i++) {
                Node n = (Node) m.get(i);
                System.out.println("Node = " + i + " t = " + n.getTime() + " x= " + n.getCoodinates().x + " y= " + n.getCoodinates().y);
            }
        }
    }

    private PrintStream getps(String name, int index, File directory) {
        String filename = name + index + ".csv";
        File f = new File(directory, filename);
        PrintStream ps;
        try {
            ps = new PrintStream(f);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(WayPointEditingPanel.class.getName()).log(Level.SEVERE, null, ex);
            ps = null;
        }
        return ps;
    }

    private void calibrate() {
        calibrating = true;
        calibrationpointlist = new ArrayList<>();
    }

    private void setcalibrationpoint(int x, int y) {
        calibrationpointlist.add(screen2transformed(x, y));
        repaint();
        if (calibrationpointlist.size() == 2) {
            finishcalibrating();
            calibrating = false;
        }
    }

    private void finishcalibrating() {
        if (!defaultcalibration) {
            JDialog jd = new JDialog((JDialog) null, "Calibration", true);
            calibrationdialog = jd;
            jd.setSize(200, 300);
            jd.add(new CalibrateForm(this));
            jd.setVisible(true);
        }
    }

    void setcalibrationpoints(int tlx, int tly, int brx, int bry) {
        System.out.println(" calib points " + tlx + " " + tly + " " + brx + " " + bry);
        System.out.println(" points selected " + calibrationpointlist);
        Point tl = calibrationpointlist.get(0);
        Point br = calibrationpointlist.get(1);
        xscale = (float) (brx - tlx) / (float) (br.x - tl.x);
        yscale = (float) (tly - bry) / (float) (br.y - tl.y);
        xoffset = tl.x;
        yoffset = tl.y;
        xorigin = tlx;
        yorigin = tly;
    }

    private void setdefaultcalibration() {
        defaultcalibration = true;
        calibrate();
        setcalibrationpoint(58, 45);
        setcalibrationpoint(3890, 3248);
        setcalibrationpoints(122864, 3083951, 142331, 3067684);
        calibrationpointlist.add(new Point(58, 45));
        calibrationpointlist.add(new Point(3890, 3248));
        defaultcalibration = false;
    }
}
