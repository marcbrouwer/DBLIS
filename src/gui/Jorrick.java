package gui;

import dblis.SportData2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 *
 * @author Brouwer M.R.
 */
public class Jorrick {
    
    public static Scene drawRelativeChart() {
        ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();

        final NumberAxis numberAxis = new NumberAxis();
        final DateAxis dateAxis = new DateAxis();
        final LineChart<Date, Number> lineChart = new LineChart<>(dateAxis, numberAxis, series);
        
        //series.addAll(getSeries()); // implementation should be changed
        
        Scene scene = new Scene(lineChart, 800, 600);
        
        return scene;
    }
    
    
    
    
    
    public static Scene drawBarChart() {
        final ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Event");
        yAxis.setLabel("Popularity");
        
        final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis, series);
        
        final List<String> events = SportData2.getInstance().getSelected();
        events.stream().forEach(e -> series.addAll(getSerie(e)));
        
        Scene scene = new Scene(barChart, 800, 600);
        
        return scene;
    }
    
    private static XYChart.Series<String, Number> getSerie(String event) {
        //Getting the selected information from the GUI
        final Date startdate = SportData2.getInstance().getStartDate();
        final Date enddate = SportData2.getInstance().getEndDate();
        
        //Creating variables required
        final int pop = SportData2.getInstance().getPopularity(
                        event, startdate.getTime(), enddate.getTime());
        final XYChart.Series<String, Number> serie = new XYChart.Series();
        
        serie.getData().add(new XYChart.Data(event, pop));
        serie.setName(event);
        
        return serie;
    }
    
}