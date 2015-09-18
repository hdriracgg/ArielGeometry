/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometryroseviewer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author jgrimsdale
 */
public class BinMetric {

    static boolean debug = false;
    
    public static void main(String[] args) {
        String img_name;

        for (int i = 6; i <= 6; i++) {
            img_name = "img/" + i + ".png";
            System.out.println("Img \"" + img_name + "\": \t" + BinMetric.getbinmetric_img(img_name, true) + "\n");
        }
    }

    public static float getbinmetric_img(String img_name, boolean display_demo) {
        float coeff = 0;

        try {
            BufferedImage img_data = ImageIO.read(new File(img_name));

            // Counting points
            int total_points = 0;
            for (int i = 0; i < img_data.getWidth(); i++) {
                for (int j = 0; j < img_data.getHeight(); j++) {
                    if (img_data.getRGB(i, j) != Color.WHITE.getRGB()
                            && img_data.getRGB(i, j) != Color.LIGHT_GRAY.getRGB()) // if there is a point
                    {
                        total_points++;
                    }
                }
            }

            // Loading data
            int k = 0;
            float[][] coords = new float[2][total_points];
            for (int i = 0; i < img_data.getWidth(); i++) {
                for (int j = 0; j < img_data.getHeight(); j++) {
                    if (img_data.getRGB(i, j) != Color.WHITE.getRGB()
                            && img_data.getRGB(i, j) != Color.LIGHT_GRAY.getRGB()) // if there is a point
                    {
                        coords[0][k] = i - (int) Math.floor(img_data.getWidth() / 2.);
                        coords[1][k] = j - (int) Math.floor(img_data.getWidth() / 2.);
                        k++;
                    }
                }
            }

            coeff = BinMetric.getbinmetric(coords, img_data.getWidth() / 2.0f, display_demo);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return coeff;
    }

    public static float getbinmetric(float[][] coords, float maxoffset, boolean display_demo) {
        return Math.abs(distance(coords, maxoffset, display_demo));
//        return 1.0f / Math.abs(1.0f - distance(coords, maxoffset, display_demo));
    }

    public static float distance(float[][] coords, float maxoffset, boolean display_demo) {
        CircularGrid grid = new CircularGrid(maxoffset, coords);
        float distance = 0.0f;
        float theoretical_distribution = grid.getTotalPoints() * 1.0f / grid.getTotalCells();
        if(debug) System.out.println("BinMetric.distance Max offset: " + maxoffset);
        if(debug) System.out.println("BinMetric.distance Theoretical distribution: " + theoretical_distribution);
        int[] cells = new int[grid.getTotalCells()];

        for (int i = 0; i < grid.getTotalPoints(); i++) {
            float x = coords[0][i];
            float y = coords[1][i];
            int id_cell = grid.findCell(x, y);

            if (id_cell != -1) {
                cells[id_cell]++;
            }
        }

        for (int i = 0; i < grid.getTotalCells(); i++) {
            distance += Math.pow(cells[i] - theoretical_distribution, 2) / Math.pow(theoretical_distribution, 0.5);
        }

        if(debug) System.out.println("BinMetric.distance \tDistance: \t" + distance);

        if (display_demo) {
            CircularGridDemo demo = new CircularGridDemo(grid);
            demo.displayInFrame();
        }

        return distance;
    }
}
