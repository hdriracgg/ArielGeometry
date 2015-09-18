/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometryroseviewer;

import com.cgg.arielgeometry.model.AbstractGeometryModel;
import com.cgg.arielgeometry.model.types.XYLocation;
import com.cgg.arielgeometry.model.types.EndCoordinateList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author jgrimsdale
 */
public class ArielGeometryBinXYPlot extends JPanel {

    ChartPanel[] chartPanels = null;
    AbstractGeometryModel agm = null;
    EndCoordinateList endCoordinateList = null;
    double xmax;
    double ymax;
    double xmin;
    double ymin;
    float maxoffset = 10000.0f;
    static boolean debug = false;
    
    public ArielGeometryBinXYPlot(String title) {
        setToolTipText(title);
    }

    public void drawmap(EndCoordinateList endCoordinateList) {
        if (debug) {
            System.out.println("ArielGeometryBinXYPlot.drawmap called");
        }
        if (chartPanels != null) {
            for (int i = 0; i < 1; i++) {
                remove(chartPanels[i]);
            }
        }
        chartPanels = new ChartPanel[1];
        xmax = endCoordinateList.maxx;
        ymax = endCoordinateList.maxy;
        xmin = -endCoordinateList.maxx;
        ymin = -endCoordinateList.maxy;
        this.endCoordinateList = endCoordinateList;

        plotxyoffsets();
    }

    void plotendpoints() {
        int i = 0;
        List<XYLocation> list = endCoordinateList.list;
        int imax = list.size();
        double data[][] = new double[3][imax + 1];
//        

        for (XYLocation endCoordinate : list) {
            data[0][i] = endCoordinate.x;
            data[1][i] = endCoordinate.y;
            data[2][i] = 1.0f;
            if (debug) {
                System.out.println("ArielGeometryBinXYPlot.plotendpoints x y : " + data[0][i] + " " + data[1][i]);
            }
            i++;
        }
        // force scale
        data[0][imax] = 0;
        data[1][imax] = 0;
        data[2][imax] = 0;
//        sourcedata[0][sources+1] = xmax;
//        sourcedata[1][sources+1] = ymax;
//        sourcedata[2][sources+1] = 0;
        displayPlot(data, "Endpoint location map", "Endpoints", 1, false, 0, 4.0f, 4.0f);
    }

    void plotoffsets() {
        int i = 0;
        List<XYLocation> list = endCoordinateList.list;
        int imax = list.size();
        double data[][] = new double[3][imax + 1];
//        

        for (XYLocation endCoordinate : list) {
            data[0][i] = endCoordinate.x;
            data[1][i] = endCoordinate.y;
            data[2][i] = 1.0f;
            if (debug) {
                System.out.println("ArielGeometryBinXYPlot.plotendpoints x y : " + data[0][i] + " " + data[1][i]);
            }
            i++;
        }
        // force scale
        data[0][imax] = 0;
        data[1][imax] = 0;
        data[2][imax] = 0;
//        sourcedata[0][sources+1] = xmax;
//        sourcedata[1][sources+1] = ymax;
//        sourcedata[2][sources+1] = 0;
        displayPlot(data, "Endpoint location map", "Endpoints", 1, false, 0, 4.0f, 4.0f);
    }

    void plotxyoffsets() {
        int i = 0;
        List<XYLocation> list = endCoordinateList.list;
        int imax = list.size();
        double data[][] = new double[3][imax*2];

        for (XYLocation endCoordinate : list) {
            double x = endCoordinate.x;
            double y = endCoordinate.y;
            double offsetcolourbase = getoffsetcolour(x, y);
            data[0][i] = x;
            data[1][i] = y;
            data[2][i] = offsetcolourbase;            
            data[0][i+1] = -1.0f * x;
            data[1][i+1] = -1.0f * y;
            data[2][i+1] = offsetcolourbase;
            if (debug) {
                System.out.println("ArielGeometryBinXYPlot.plotxyoffsets x y : " + data[0][i] + " " + data[1][i]);
            }
            i+=2;
        }
        displayPlot(data, "XY offset map", "Offsets", maxoffset, true, 0, (float)xmax/40, (float)ymax/40);
    }
    
    private double getoffsetcolour(double x, double y) {
        double offset = Math.sqrt(2*x*x + 2*y*y);
        return offset;
    }

    public ChartPanel displayPlot(double data[][], String pname, String sname, float max, boolean showscale, int i, float x, float y) {
        JFreeChart chart = createChart(createXYZDataset(data), pname, max, showscale, x, y);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanels[i] = chartPanel;

        chartPanel.setPreferredSize(new Dimension(700, 500));
        
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setMouseZoomable(true, false);
        chartPanel.setRangeZoomable(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setZoomAroundAnchor(true);
        add(chartPanel);
        setVisible(true);
        return chartPanel;
    }

    /**
     * Creates a sample chart.
     *
     * @param dataset the dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(XYDataset dataset,
                                          String pname,
                                          float max,
                                          boolean showscale,
                                          float xblksize,
                                          float yblksize) {

        // create the chart...
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

        //  NOW DO SOME CUSTOMISATION OF THE CHART...

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.WHITE);

        chart.setAntiAlias(false);

        if (debug) {
            System.out.println("ArielGeometryBinXYPlot.createChart chart is of type: " + chart.getPlot());
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

//        PaintScale scale = new GrayPaintScale(0.0, 10.0); 
//        LookupPaintScale scale = new LookupPaintScale(0.0, 10.0, java.awt.Color.PINK);
//        scale.add(0.0, java.awt.Color.WHITE);
//        scale.add(1.5, java.awt.Color.RED);
//        
//        Paint paint = new Color(0.0f, 1.0f, 0.0f);
//        scale.add(10.0, paint);
        PaintScale scale = new InterpolatedPaintScale(max, 0, max);
        renderer.setPaintScale(scale);
        renderer.setBlockHeight(yblksize);
        renderer.setBlockWidth(xblksize);

        NumberAxis scaleAxis = new NumberAxis("Offset");
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
        //legend.setBackgroundPaint(new Color(120, 120, 180));
        if (showscale) {
            chart.addSubtitle(legend);
        }

        xyplot.setRenderer(renderer);
        if (debug) {
            System.out.println("ArielGeometryBinXYPlot.createChart Renderer is of type: " + xyplot.getRenderer());
        }

        return chart;

    }

    private DefaultXYZDataset createXYZDataset(double data[][]) {
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        dataset.addSeries("series1", data);
        return (dataset);
    }
}
