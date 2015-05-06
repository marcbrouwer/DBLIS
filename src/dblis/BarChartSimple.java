package dblis;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class BarChartSimple extends Application {
    
    final static String[] colours = new String[]{
        "DarkBlue", "DarkCyan", "DarkGray", "DarkGreen", "DarkMagenta", 
        "DarkOrange", "DarkRed", "DarkSlateGray", "Green", "Gold", "LightSeaGreen",
        "Magenta", "Olive", "Red", "Sienna", "Wheat"
    };
    
    @Override
    public void start(Stage stage) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = 
                new BarChart<String, Number>(xAxis, yAxis);
        bc.setTitle("Summary");
        xAxis.setLabel("Sport");
        xAxis.setTickLabelRotation(0);
        yAxis.setLabel("Popularity");

        XYChart.Series series1 = new XYChart.Series();
        
        series1.setName("NL");
        series1.getData().add(new XYChart.Data("football", 2));
        series1.getData().add(new XYChart.Data("tennis", 20));
        series1.getData().add(new XYChart.Data("formula 1", 10));

        /*XYChart.Series series2 = new XYChart.Series();
        series2.setName("2004");
        series2.getData().add(new XYChart.Data(50, itemA));
        series2.getData().add(new XYChart.Data(41, itemB));
        series2.getData().add(new XYChart.Data(45, itemC));

        XYChart.Series series3 = new XYChart.Series();
        series3.setName("2005");
        series3.getData().add(new XYChart.Data(45, itemA));
        series3.getData().add(new XYChart.Data(44, itemB));
        series3.getData().add(new XYChart.Data(18, itemC));*/

        Scene scene = new Scene(bc, 800, 600);
        bc.getData().addAll(series1);
        stage.setScene(scene);
        
        XYChart.Data sd;
        for (int i = 0; i < series1.getData().size(); i++) {
            sd = (XYChart.Data) series1.getData().get(i);
            sd.getNode().setStyle("-fx-bar-fill: " + colours[i % colours.length]);
        }
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}