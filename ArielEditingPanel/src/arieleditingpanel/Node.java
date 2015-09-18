/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arieleditingpanel;

import java.awt.Point;

/**
 * Marina transitional point
 * @author hdrira
 */
public class Node {
    
    // The coordinates
    private Point coodinates;
    
    // The time from the simulation beginning
    private long time;

    public Node(Point coodinates, long time) {
        this.coodinates = coodinates;
        this.time = time;
    }

    public Point getCoodinates() {
        return coodinates;
    }

    public long getTime() {
        return time;
    }

    public void setCoodinates(Point coodinates) {
        this.coodinates = coodinates;
    }

    public void setTime(long time) {
        this.time = time;
    }
    
    
    
    
    
    
    
    
    
}
