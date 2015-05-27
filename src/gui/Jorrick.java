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
        ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Event");
        yAxis.setLabel("Popularity");
        
        final BarChart<String, Number> bareChart = new BarChart<String, Number>(xAxis, yAxis);
        
        series.addAll(getSerie()); // implementation should be changed
        
        Scene scene = new Scene(bareChart, 800, 600);
        
        return scene;
    }
    
    private static XYChart.Series<String, Number> getSerie() {
        //Getting the selected information from the GUI
        final Date startdate = SportData2.getInstance().getStartDate();
        final Date enddate = SportData2.getInstance().getEndDate();
        final List<String> events = Arrays.asList("Roland Garros", "Champions League");//SportData2.getInstance().getSelected();
        
        //Creating variables required
        final Map<String, Map<String, Double>> sportpop = new HashMap();
        final XYChart.Series<String, Number> serie = new XYChart.Series();
        
        
        //Getting the data from the server
        events.stream().forEach(e -> {
            //Get the data for a specific time frame
            sportpop.put(e, SportData2.getInstance()
                    .getPopularityKeywords(e, startdate.getTime(), enddate.getTime() ));
            //Putting it in the chart and setting the name of the sport
            
            serie.getData().add(
                    new XYChart.Data(e, sportpop.get(e)));
            
            
            
            //listofdates.stream().forEach()
            //
            //final List<Date> listofdates = new ArrayList(sportpop.get(e).keySet());
            //Collections.sort(listofdates);
            
            //
            //final Calendar calendar = Calendar.getInstance();
            
            //Adding the data to the line chart.
            //listofdates.stream().forEach(d -> {
            //    calendar.setTimeInMillis(d.getTime());
            //    sportseries.get(e).getData().add(
            //            new XYChart.Data(calendar.getTime(), 
             //                   sportpop.get(e).get(d)));
            //});
            
        });        
        return serie;
    }
}