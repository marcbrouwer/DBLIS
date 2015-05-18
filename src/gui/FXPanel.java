/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;

/**
 *
 * @author s124392
 */
public class FXPanel extends JFXPanel {
    
    public FXPanel() {
        super();
        init();
    }
    
    private void init() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Group root = new Group();
                Scene scene = new Scene(root);
                setScene(scene);
            }
       });
    }
}
