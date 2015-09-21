/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometryjfcviewer;

import com.cgg.arielgeometry.model.AbstractGeometryModel;
import com.cgg.arielgeometry.model.types.XYLocation;
import com.cgg.arielgeometry.model.types.EndCoordinateList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYDataImageAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author jgrimsdale
 */
public class ArielGeometryJFCXYPlot extends JPanel {

    private InstanceContent content;
    ChartPanel[] chartPanels = null;
    AbstractGeometryModel agm = null;
    double xmax = Double.NEGATIVE_INFINITY;
    double ymax = Double.NEGATIVE_INFINITY;
    double xmin = Double.POSITIVE_INFINITY;
    double ymin = Double.POSITIVE_INFINITY;
    float maxoffset = 0;
    static private boolean debug = false;
    // Tunables
    int maxrecvr = 250000;   // approximate max number of receiver positions to plot

    public ArielGeometryJFCXYPlot(String title, InstanceContent content) {
        setToolTipText(title);
        this.content = content;
    }

    public void drawmap(AbstractGeometryModel agm) {
        if (chartPanels != null) {
            for (int i = 0; i < chartPanels.length; i++) {
                if (chartPanels[i] != null) {
                    remove(chartPanels[i]);
                }
            }
        }
        chartPanels = new ChartPanel[3];
        double xrange = agm.getBINSX() * agm.getBXS();
        double yrange = agm.getBINSY() * agm.getBYS();
        double maxrange = xrange > yrange ? xrange : yrange;
        xmax = agm.binoriginx + maxrange;
        ymax = agm.binoriginy + maxrange;
        xmin = agm.binoriginx - agm.getBXS();
        ymin = agm.binoriginy - agm.getBYS();

        this.maxoffset = agm.getMAXOFFSET();

        this.agm = agm;

        plotreceivers();
        if (!agm.asm.isEmpty()) {
            plotsources();
        }
        
        if (!agm.abm.isEmpty()) {
            plotbincoverage();
        }
        
        JPanel testpanel = new EditingPanel();
        add(testpanel);
        setVisible(true);
        
//        plotbinmetrics();
    }

    void plotsources() {
        float cmax = Float.NEGATIVE_INFINITY;
        ChartPanel chartPanel;
        // sources
        List<XYLocation> sourcelist = agm.getsources();
        int sources = sourcelist.size();
        double sourcedata[][] = new double[3][sources];
        for (int i = 0; i < sources; i++) {
            float c = i;
//            float c = i * agm.spti / (24 * 3600);
            if (c > cmax) {
                cmax = c;
            }
            sourcedata[0][i] = sourcelist.get(i).x;
            sourcedata[1][i] = sourcelist.get(i).y;
            sourcedata[2][i] = c;
        }
        chartPanel = displayPlot(sourcedata, "Source location map", "Sources",
                cmax, true, 0, agm.getBXS() / 4, agm.getBYS() / 4, "Shot Number");
        chartPanel.addChartMouseListener(new JFCXYSourcePlotMouseListener());
    }

    void plotreceivers() {
        float cmax = Float.NEGATIVE_INFINITY;
        ChartPanel chartPanel;
        // receivers
        List<List<XYLocation>> receiverlist = agm.getreceivers();
        int shots = receiverlist.size();
        int shotinc = 1;
        // how many are there ?
        int receivers = 0;
        for (List<XYLocation> l : receiverlist) {
            receivers += l.size();
        }
        // adjust if there are too many
        if (receivers > maxrecvr) {
            shotinc = receivers / maxrecvr;
            int s = 0;
            receivers = 0;
            for (List<XYLocation> l : receiverlist) {
                if (s % shotinc == 0) {
                    receivers += l.size();
                }
            }
        }
        double receiverdata[][] = new double[3][receivers];
        int rindex = 0;
        for (int i = 0; i < shots; i += shotinc) {
            List<XYLocation> rlist = receiverlist.get(i);
            for (int j = 0; j < rlist.size(); j++) {
                if (i > cmax) {
                    cmax = i;
                }
                XYLocation rloc = rlist.get(j);
                float x = rloc.x;
                float y = rloc.y;
                receiverdata[0][rindex] = x;
                receiverdata[1][rindex] = y;
                receiverdata[2][rindex] = (double) i;
                if (j == 0 && debug) {
                    System.out.println("ArielGeometryJFCXYPlot.plotreceivers j=0 x=" + x + " y=" + y);
                }
                rindex++;
            }
        }
        chartPanel = displayPlot(receiverdata, "Receiver location map", "Receivers",
                cmax, true, 1, agm.getBXS() / 4, agm.getBYS() / 4, "Shot Number");
        chartPanel.addChartMouseListener(new JFCXYReceiverPlotMouseListener());
    }

    void plotbincoverage() {
        ChartPanel chartPanel;
        int[][] coverage = agm.getbincoverage();
        int nonzerobins = 0;
        for (int x = 0; x < agm.getBINSX(); x++) {
            for (int y = 0; y < agm.getBINSY(); y++) {
                if (coverage[x][y] > 0) {
                    nonzerobins++;
                }
            }
        }
        double data[][] = new double[3][nonzerobins];
        double binxsize = agm.getBXS();
        double binysize = agm.getBYS();
        int idx = 0;
        float max = 0;
        for (int x = 0; x < agm.getBINSX(); x++) {
            double xcoord = agm.binoriginx + (x * binxsize);
            for (int y = 0; y < agm.getBINSY(); y++) {
                double ycoord = agm.binoriginy + (y * binysize);
                int c = coverage[x][y];
                if (c > 0) {
                    data[0][idx] = xcoord;
                    data[1][idx] = ycoord;
                    data[2][idx] = c;
                    if (Float.compare(c, max) > 0) {
                        max = c;
                    }
                    if (debug) {
                        System.out.println("ArielGeometryJFCXYPlot.plotcoverage Coverage x y c: " + data[0][idx] + " " + data[1][idx] + " " + data[2][idx]);
                    }
                    idx++;
                }
            }
        }
        chartPanel = displayPlot(data, "Coverage Map", "Bins", max, true, 2, agm.getBXS(), agm.getBYS(), "Coverage");
        chartPanel.addChartMouseListener(new JFCXYBinPlotMouseListener());
    }

    public ChartPanel displayPlot(double data[][], String pname, String sname, float max, boolean showscale, int i, float x, float y, String scaletext) {
        JFreeChart chart = createChart(createXYZDataset(data), pname, max, showscale, x, y, scaletext);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanels[i] = chartPanel;
        
        // Provide a scaled background image - can be commented out
        BufferedImage background;
        XYPlot plot = chart.getXYPlot();
        try {
            background = ImageIO.read(new File("C:\\Users\\hdrira\\Documents\\NetBeansProjects\\Images sources\\Moussafir A0 SURVEY map.png"));
        }
        catch (IOException e) {
            System.out.println("Cannot open file for background");
            background = null;
        }
        plot.getRenderer().addAnnotation(new XYDataImageAnnotation(background, 0, 0, 7000, 7000) {
    }, Layer.BACKGROUND);
        
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
//      Dell laptop screen
//        chartPanel.setPreferredSize(new Dimension(600, 300));
//      Dell large screen
        chartPanel.setPreferredSize(new Dimension(700, 500));
//      smart screen
//        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setMouseZoomable(true, false);
        chartPanel.setRangeZoomable(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setZoomAroundAnchor(true);
        if (i == 0) {
            setLayout(new GridLayout(2, 2));
        }
        if (i == 0) {
            add(chartPanel);
        }
        if (i == 1) {
            add(chartPanel);
        }
        if (i == 2) {
            add(chartPanel);
        }
        setVisible(true);
        return chartPanel;
    }

    private DefaultXYZDataset createXYZDataset(double data[][]) {
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        dataset.addSeries("series1", data);
        return (dataset);
    }

    private static void printDataset(DefaultXYZDataset dataset) {
        System.out.println("getSeriesCount = " + dataset.getSeriesCount());
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            for (int j = 0; j < dataset.getItemCount(i); j++) {
                System.out.println("getItemCount = " + dataset.getItemCount(i));
                System.out.println("series = " + i + " getX = " + dataset.getX(i, j));
                System.out.println("series = " + i + " getY = " + dataset.getY(i, j));
                System.out.println("series = " + i + " getXValue = " + dataset.getXValue(i, j));
                System.out.println("series = " + i + " getYValue = " + dataset.getYValue(i, j));
                System.out.println("series = " + i + " getZ = " + dataset.getZ(i, j));
                System.out.println("series = " + i + " getZValue = " + dataset.getZValue(i, j));
            }
        }
    }

    private JFreeChart createChart(XYDataset dataset,
                                   String pname,
                                   float max,
                                   boolean showscale,
                                   float xblksize,
                                   float yblksize,
                                   String scaletext) {

        JFreeChart chart = ChartFactory.createScatterPlot(
                pname, // chart title
                "X in metres", // domain axis label
                "Y in metres", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                false, // include legend
                true, // tooltips?
                false // URLs?
                );

        chart.setBackgroundPaint(Color.WHITE);
        chart.setAntiAlias(false);

        if (debug) {
            System.out.println("ArielGeometryJFCXYPlot.createChart Plot is of type: " + chart.getPlot());
        }

        XYPlot xyplot = (XYPlot) chart.getPlot();
        xyplot.setDomainPannable(true);
        xyplot.setRangePannable(true);

        ValueAxis rangeValueAxis = xyplot.getRangeAxis();
        rangeValueAxis.setLowerBound(ymin);
        rangeValueAxis.setUpperBound(ymax);

        ValueAxis domainValueAxis = xyplot.getDomainAxis();
        domainValueAxis.setLowerBound(xmin);
        domainValueAxis.setUpperBound(xmax);

        XYBlockRenderer renderer = new XYBlockRenderer();

        PaintScale scale = new InterpolatedPaintScale(max, 0, max);
        renderer.setPaintScale(scale);
        renderer.setBlockHeight(yblksize);
        renderer.setBlockWidth(xblksize);

        NumberAxis scaleAxis = new NumberAxis("Scale");
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);
        scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 7));

        PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);
        legend.setStripOutlineVisible(false);
        legend.setSubdivisionCount(20);
        legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        legend.setAxisOffset(5.0);
        legend.setMargin(new RectangleInsets(5, 5, 5, 5));
        legend.setFrame(new BlockBorder(Color.WHITE));
        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
        legend.setStripWidth(10);
        legend.setPosition(RectangleEdge.RIGHT);
        scaleAxis.setLabel(scaletext);
        //legend.setBackgroundPaint(new Color(120, 120, 180));
        if (showscale) {
            chart.addSubtitle(legend);
        }

        xyplot.setRenderer(renderer);
        if (debug) {
            System.out.println("ArielGeometryJFCXYPlot.createChart Renderer is of type: " + xyplot.getRenderer());
        }

        return chart;

    }

    public class JFCXYBinPlotMouseListener extends JFCXYPlotMouseListener {

        @Override
        public void chartMouseClicked(ChartMouseEvent cme) {
            super.chartMouseClicked(cme);
            EndCoordinateList endCoordinateList = agm.getxyoffsets(new XYLocation(x, y));
            endCoordinateList.maxx = agm.maxoffset;
            endCoordinateList.maxy = agm.maxoffset;
            System.out.printf("ArielGeometryJFCXYPlot.JFCXYBinPlotMouseListener.chartMouseClicked coverage=");
            System.out.printf("%d\n", agm.getbincoverage(x, y));
            content.add(endCoordinateList);
        }
    }

    public class JFCXYReceiverPlotMouseListener extends JFCXYPlotMouseListener {

        @Override
        public void chartMouseClicked(ChartMouseEvent cme) {
            super.chartMouseClicked(cme);
            System.out.printf("ArielGeometryJFCXYPlot.JFCXYReceiverPlotMouseListyener: x=%f y=%f z=%f\n", x, y, z);
        }
    }

    public class JFCXYSourcePlotMouseListener extends JFCXYPlotMouseListener {

        @Override
        public void chartMouseClicked(ChartMouseEvent cme) {
            super.chartMouseClicked(cme);
            System.out.printf("ArielGeometryJFCXYPlot.JFCXYSourcePlotMouseListyener: x=%f y=%f z=%f\n", x, y, z);
        }
    }

    public class JFCXYPlotMouseListener implements ChartMouseListener {

        float x = 0;
        float y = 0;
        float z = 0;

        @Override
        public void chartMouseClicked(ChartMouseEvent cme) {
            x = 0;
            y = 0;
            z = 0;
            ChartEntity e = cme.getEntity();
            XYItemEntity entity;
            if (e instanceof XYItemEntity) {
                entity = (XYItemEntity) e;
            }
            else {
                return;
            }
            XYDataset dataset = entity.getDataset();
            x = (float) dataset.getXValue(0, entity.getItem());
            y = (float) dataset.getYValue(0, entity.getItem());
            if (dataset instanceof XYZDataset) {
                XYZDataset xyzdataset = (XYZDataset) dataset;
                z = (float) xyzdataset.getZValue(0, entity.getItem());

            }
            if (debug) {
                System.out.printf("chartMouseClicked: x=%f y=%f\n", x, y);
            }

        }

        @Override
        public void chartMouseMoved(ChartMouseEvent cme) {
            //        System.out.println("MouseMoved "+cme);
        }
    }
}
