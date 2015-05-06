package dblis;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.*;
import javafx.scene.Group;
 
public class PieChartSimple extends Application {
 
    @Override 
    public void start(Stage stage) {
        Scene scene = new Scene(new Group());
        stage.setTitle("Imported Fruits");
        stage.setWidth(500);
        stage.setHeight(500);
 
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        
        ObservableList<BarChart.Data> pieChartData =
                FXCollections.observableArrayList(
                new BarChart.Data("Grapefruit", 13),
                new BarChart.Data("Oranges", 25),
                new BarChart.Data("Plums", 10),
                new BarChart.Data("Pears", 22),
                new BarChart.Data("Apples", 30));
        final BarChart chart = new BarChart(xAxis, yAxis, pieChartData);
        chart.setTitle("Imported Fruits");

        ((Group) scene.getRoot()).getChildren().add(chart);
        stage.setScene(scene);
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}