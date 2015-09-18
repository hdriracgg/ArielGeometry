/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometryroseviewer;

/**
 *
 * @author jgrimsdale
 */
public final class CircularGrid {

    private int total_layers;   // layers are concentric circles
    private int total_cells;    // total number of cells in the rose plot
    private int total_points;   // total number of input coordinates
    private double grid_radius; // points outside a circle of this radius are discarded
    private int nbpoints;       // number of points used to define grid 
    private int[] cells;
    private boolean debug = false;

    // constructor to create a demo grid
    public CircularGrid(int total_points, float gr) {
        nbpoints = total_points;
        setTotalPoints(nbpoints);
        setNbLayers(calculateLayers(nbpoints));
        setTotalCells(calculateTotalCells(getNbLayers()));
        setGridRadius(gr);
        cells = createcells(getTotalCells());

        // fill with demo data by default
        for (int i = 0; i < getTotalCells(); i++) {
            cells[i] = i;
        }

        if(debug) System.out.println("CircularGrid.constructor int float "+total_points+" "+gr);
        if(debug) printInfo();
    }

    // constructor to create a grid based on number of input coordinates
    public CircularGrid(float gr, float[][] coords) {
        nbpoints = coords[0].length;
        setTotalPoints(nbpoints);
        setNbLayers(calculateLayers(total_points));
        setTotalCells(calculateTotalCells(getNbLayers()));
        setGridRadius(gr);
        cells = createcells(getTotalCells());       
        calculatehitspercell(coords);
        
        if(debug) System.out.println("CircularGrid.constructor float float[][] "+gr+" "+nbpoints);
        if(debug) printInfo();
    }
    
    // constructor to create a grid with given number of points
    public CircularGrid(float gr, float[][] coords, int nbpoints) {
        this.nbpoints = nbpoints;
        setTotalPoints(coords[0].length);
        setNbLayers(calculateLayers(nbpoints));
        setTotalCells(calculateTotalCells(getNbLayers()));
        setGridRadius(gr);
        cells = createcells(getTotalCells());   
        calculatehitspercell(coords);
        
        if(debug) System.out.println("CircularGrid.constructor float float[][] int "+gr+" "+coords[0].length+" "+nbpoints);
        if(debug) printInfo();
    }
    
    private int calculateLayers(int nbp) {
        return (int) Math.floor((1 + Math.sqrt(1 + nbp)) / 2);
    }
    
    private int calculateTotalCells(int layers) {
        return 4 * (int) Math.pow(layers, 2) - 4 * layers;
    }
    
    private void calculatehitspercell(float[][] coords) {
        // clear all cells
        for (int i = 0; i < getTotalCells(); i++) {
            cells[i] = 0;
        }

        // count number of hits per cell
        for (int i = 0; i < getTotalPoints(); i++) {
            float x = coords[0][i];
            float y = coords[1][i];
            int id_cell = findCell(x, -y);

            if (id_cell != -1) {
                cells[id_cell]++;
            }
        }
    }
    
    private int[] createcells(int nbc) {
        return new int[nbc];
    }
    
    public double getMaxCellWeight() {
        double max = 0.0;

        for (int i = 0; i < getTotalCells(); i++) {
            int w = getCellWeight(i);
            if(max < w) max = w;
        }

        return max;
    }

    public int getCellWeight(int id_cell) {
        if(id_cell < 0) {
            return(0);
        }
        else {
            return cells[id_cell];
        }
    }

    public int getCellWeight(float x, float y) {
        return getCellWeight(findCell(x, y));
    }

    public int findCell(float x, float y) {
        double radius = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

        if (radius >= grid_radius) {
            return -1;
        }

        // angle = [0 ; 2*PI[
        double angle = Math.atan2(y, x) + Math.PI;

        int id_layer;
        double dR = grid_radius / (total_layers - 0.5);

        if (radius <= dR * 0.5) {
            id_layer = 1;
        }
        else {
            id_layer = (int) Math.floor((radius - dR * 0.50) / dR) + 2;
        }

        int total_cells_in_layer = 8 * id_layer - 8;
        int id_cell_in_layer = (int) Math.floor((modulo2PI(angle) / (2. * Math.PI)) * total_cells_in_layer) + 1;
        int id_cell = 4 * (int) Math.pow(id_layer - 1, 2) - 4 * (id_layer - 1) + id_cell_in_layer;

        if (id_cell == getTotalCells() + 1) {
            id_cell--;
        }
        
        id_cell--; // [0 ; total_cells-1]
        if (debug) System.out.println("findCell grid_radius x y id_cell "+grid_radius+" "+x+" "+y+" "+id_cell);

        return id_cell; 
    }

    public double modulo2PI(double angle) {
        if (Math.abs(angle) >= 2. * Math.PI) {
            angle -= (Math.abs(angle) / angle) * 2. * Math.PI;
            return modulo2PI(angle);
        }
        else {
            return angle;
        }
    }
    
    public final void setTotalPoints(int tp) {
        total_points = tp;
    }
    
    private void setNbLayers(int nbl) {
        total_layers = nbl;
    }
    
    private void setTotalCells(int tc) {
        total_cells = tc;
    }
    
    public final void setNbPoints(int nbp) {
        
    }
    
    private void setGridRadius(float gr) {
        grid_radius = gr;
    }

    public final int getTotalCells() {
        return total_cells;
    }

    public final int getNbLayers() {
        return total_layers;
    }
    
    public final int getTotalPoints() {
        return total_points;
    }

    public double getRadius() {
        return grid_radius;
    }

    public int getDiameter() {
        return (int) grid_radius * 2;
    }
    
    public int getNbPoints() {
        return nbpoints;
    }
    
    private void printInfo() {
        System.out.println("CircularGrid.printInfo Total points: " + getTotalPoints());
        System.out.println("CircularGrid.printInfo Total cells: " + getTotalCells());
        System.out.println("CircularGrid.printInfo NbPoints: " + getNbPoints());
        System.out.println("CircularGrid.printInfo NbLayers: " + getNbLayers());
    }
}
