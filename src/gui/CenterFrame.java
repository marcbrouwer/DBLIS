/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 *
 * @author s124392
 */
public abstract class CenterFrame extends javax.swing.JFrame {
    
    public CenterFrame() {
        super();
    }
    
    /**
     * Centers the frame.
     */
    protected void centerize() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2- (int) getContentPane().getWidth() / 2, dim.height / 
                2 - (int) getContentPane().getHeight() / 2);
    }
}
