package dblis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class PieChartFX extends Application implements Runnable {
    
    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        //String drilldownCss = PieChart.class.getResource("DrilldownChart.css").toExternalForm();
 
        final Map<String, Double> sportPop = 
                SportData2.getInstance().getPopularityAllSportsAsPercentage();
        
        final List<PieChart.Data> list = new ArrayList<>();
        
        sportPop.entrySet().stream().forEach(entry -> {
            list.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        });
        
        PieChart pie = new PieChart(
                FXCollections.observableArrayList(list));
        pie.setTitle("Popularity");
        //((Parent) pie).getStylesheets().add(drilldownCss);
        
        list.stream().forEach(data -> {
            setDrilldownData(pie, data, data.getName());
        });
        
        root.getChildren().add(pie);
    }
 
    private void setDrilldownData(final PieChart pie, PieChart.Data data, final String sport) {
        data.getNode().setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent t) {
                final List<PieChart.Data> list = new ArrayList<>();
                final Map<String, Double> cPop = 
                        SportData2.getInstance().getPopularityKeywordsAsPercentage(sport);
                cPop.entrySet().stream().forEach(keyword -> {
                    list.add(new PieChart.Data(keyword.getKey(), keyword.getValue()));
                });
                pie.setData(FXCollections.observableArrayList(list));
            }
        });
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
