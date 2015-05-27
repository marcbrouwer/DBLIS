package gui;

import dblis.SportData2;
import java.util.ArrayList;
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
        
        series.addAll(getSeries()); // implementation should be changed
        
        Scene scene = new Scene(lineChart, 800, 600);
        
        return scene;
    }
    
    private static Collection<XYChart.Series<Date, Number>> getSeries() {
        //Getting the selected information from the GUI
        final Date startdate = SportData2.getInstance().getStartDate();
        final Date enddate = SportData2.getInstance().getEndDate();
        final int interval = SportData2.getInstance().getInterval();
        final List<String> sports = SportData2.getInstance().getSelected();
        
        //Creating variables required
        final Map<String, Map<Date, Double>> sportpop = new HashMap();
        final Map<String, XYChart.Series<Date, Number>> sportseries = new HashMap();
        
        
        
        //Getting the data from the server
        sports.stream().forEach(s -> {
            //Get the data for a specific time frame
            sportpop.put(s, SportData2.getInstance()
                    .getSportsForDate(startdate, enddate, s, interval));
            //Putting it in the chart and setting the name of the sport
            sportseries.put(s, new XYChart.Series());
            sportseries.get(s).setName(s);
            
            //
            final List<Date> listofdates = new ArrayList(sportpop.get(s).keySet());
            Collections.sort(listofdates);
            
            //
            final Calendar calendar = Calendar.getInstance();
            
            //Adding the data to the line chart.
            listofdates.stream().forEach(d -> {
                calendar.setTimeInMillis(d.getTime());
                sportseries.get(s).getData().add(
                        new XYChart.Data(calendar.getTime(), 
                                sportpop.get(s).get(d)));
            });
        });
        
        return sportseries.values();
    }
    
}