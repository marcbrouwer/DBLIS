package gui;

import dblis.SportData2;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 *
 * @author Brouwer M.R.
 */
public class Marc {
    
    private Scene drawBarChartHashtags(FXPanel panel) {
        Runnable runner = () -> {
            final ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Hashtags");
            yAxis.setLabel("Popularity");

            final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis, series);

            final List<String> selected = SportData2.getInstance().getSelected();
            final long starttime = SportData2.getInstance().getStartDate().getTime();
            final long endtime = SportData2.getInstance().getEndDate().getTime();
            
            final Map<String, Double> pop = SportData2.getInstance().getMostCommonHashtags(selected, starttime, endtime);
            final XYChart.Series<String, Number> serie = new XYChart.Series();
            
            selected.stream().forEach(hashtag -> {
                serie.getData().add(new XYChart.Data("", pop.get(hashtag)));
                serie.setName(hashtag);
                series.add(serie);
            });
            
            Scene scene = new Scene(barChart, 800, 600);
            panel.setScene(scene);
        };
        
        Thread t = new Thread(runner);
        t.start();
        return null;
    }
    
}