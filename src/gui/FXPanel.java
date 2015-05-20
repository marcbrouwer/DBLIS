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
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author s124392
 */
public class FXPanel extends JFXPanel {
    
    public FXPanel() {}
    
    public void drawScene(int index) {
        System.out.println("Draw");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Scene scene = null;
                switch (index) {
                case 0:
                    scene = drawSceneWelcome();
                    break;
                case 1:
                    scene = drawSceneGoodbye();
                    break;
                }
                if (scene != null) {
                    setScene(scene);
                    System.out.println("Set");
                }
            }
        });
    }
    
    private Scene drawSceneWelcome() {
        Group root = new Group();
        Scene scene = new Scene(root);
        Text  text  =  new  Text();
        text.setX(50);
        text.setY(50);
        text.setFont(new Font(15));
        text.setText("Welcome JavaFX!");
        root.getChildren().add(text);
        System.out.println("Welcome");
        return scene;
    }
    
    private Scene drawSceneGoodbye() {
        Group root = new Group();
        Scene scene = new Scene(root);
        Text  text  =  new  Text();
        text.setX(50);
        text.setY(50);
        text.setFont(new Font(15));
        text.setText("Goodbye JavaFX!");
        root.getChildren().add(text);
        return scene;
    } 
}
