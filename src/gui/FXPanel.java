/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javax.swing.JFrame;

/**
 *
 * @author s124392
 */
public abstract class FXPanel extends JFXPanel {
    
    /**
     * Constructor.
     */
    public FXPanel () { }
    
    public void draw() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createScene();
            }
       });
    }
    
    private void createScene() {
        Group root = new Group();
        Scene scene = new Scene(root);
        getElements().stream().forEach(node -> root.getChildren().add(node));
        this.setScene(scene);
    }
    
    protected abstract List<Node> getElements();
}
