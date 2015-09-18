/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cgg.arielgeometryjfcviewer;

import java.awt.Color;
import java.awt.Paint;
import org.jfree.chart.renderer.PaintScale;

/**
 *
 * @author jgrimsdale
 */
public class InterpolatedPaintScale implements PaintScale {

    private float max;
    private float lower;
    private float upper;
    private static boolean debug = false;

    public InterpolatedPaintScale(float max) {
        this.max = 0.1f + max / 3;

        lower = 0;
        upper = 1;
    }

    public InterpolatedPaintScale(float max, float lower, float upper) {
        this.max = 0.1f + max / 3;
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public double getLowerBound() {
        if (debug) {
            System.out.println("InterpolatedPaintScale.getLowerBound returns: " + lower);
        }
        return lower;
    }

    @Override
    public double getUpperBound() {
        if (debug) {
            System.out.println("InterpolatedPaintScale.getUpperBound returns: " + upper);
        }
        return upper;
    }

    @Override
    public Paint getPaint(double d) {
        float a = 0;
        float b = 0;
        float c = 0;
        Paint paint = null;

        if (d < max) {
            a = (float) d / max;
        }
        if (d >= max && d < max * 2) {
            a = (float) 1.0;
            b = ((float) d - max) / max;
        }
        if (d >= max * 2) {
            a = (float) 1.0;
            b = (float) 1.0;
            c = ((float) d - 2 * max) / max;
        }

        try {
            paint = new Color(a, b, c);
        }
        catch (Exception e) {
            System.out.println("InterpolatedPaintScale.getPaint Exception: " + e);
            System.out.println("InterpolatedPaintScale.getPaint: A color component is out of bounds a b c d max: " + a + " " + b + " " + c + " " + d + " " + max);
            throw (e);
        }
        return paint;
    }
}
