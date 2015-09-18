/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package arieleditingpanel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

/**
 *
 * @author jgrimsdale
 */
public class ArielEditingPanel extends JFrame {
    
    public ArielEditingPanel() {
        WindowListener l = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
        addWindowListener(l);
        setContentPane(new WayPointEditingPanel());
        setSize(1500, 1000);
        setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ArielEditingPanel();
    }
}