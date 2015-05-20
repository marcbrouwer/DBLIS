package dblis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;

public class PlayOffsPieChart extends Application implements Runnable {
    
    private void init(Stage primaryStage) {
        SportData.getInstance().initPlayOff();
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        
        final Map<String, Double> sportPop = 
                SportData.getInstance().getSportPopMatch();
        
        final List<PieChart.Data> list = new ArrayList<>();
        
        sportPop.entrySet().stream().forEach(entry -> {
            list.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        });
        
        PieChart pie = new PieChart(
                FXCollections.observableArrayList(list));
        pie.setTitle("Popularity");
        
        root.getChildren().add(pie);
    }
    
    @Override 
    public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }
    
    @Override
    public void run() {
        launch();
    }
    
}
