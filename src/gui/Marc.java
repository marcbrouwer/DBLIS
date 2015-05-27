package gui;

import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Brouwer M.R.
 */
public class Marc {
    
    public static Scene drawLineChart() {
        ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();

        final NumberAxis numberAxis = new NumberAxis();
        final DateAxis dateAxis = new DateAxis();
        final LineChart<Date, Number> lineChart = new LineChart<>(dateAxis, numberAxis, series);
        
        series.addAll(FXPanel.getSeries());
        
        series.stream().forEach(serie -> {
            serie.getData().stream().forEach(data -> {
                data.getNode().setOnMousePressed((MouseEvent mouseEvent) -> {
                    System.out.println(serie.getName() + ", "
                            + data.getXValue() + ", " + data.getYValue());
                });
            });
        });

        Scene scene = new Scene(lineChart, 800, 600);
        
        return scene;
    }
    
    private static void makePieChart() {
        
    }
    
}