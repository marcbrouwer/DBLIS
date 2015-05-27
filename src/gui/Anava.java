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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;
import javax.swing.Popup;

/**
 *
 * @author Brouwer M.R.
 */
public class Anava {
    
    public static Scene drawLineChart() {
        ObservableList<XYChart.Series<Date, Number>> seriesFootball = FXCollections.observableArrayList();
        ObservableList<XYChart.Series<Date, Number>> seriesRest = FXCollections.observableArrayList();
        
        final Stage primaryStage = new Stage();
        primaryStage.setTitle("Football Popup");
        
        final NumberAxis numberAxis = new NumberAxis();
        final DateAxis dateAxis = new DateAxis();
        
        final NumberAxis numberAxis2 = new NumberAxis();
        final DateAxis dateAxis2 = new DateAxis();
        
        final List<String> selected = SportData2.getInstance().getSelected();
        if (selected.contains("football")) {
            selected.remove("football");
            System.out.println(selected);
            seriesFootball.addAll(getSeries(Arrays.asList("football")));
            seriesRest.addAll(getSeries(selected));
        } else { 
            System.out.println("else" + selected);
        }
        
        final LineChart<Date, Number> lineChart = new LineChart<>(dateAxis, numberAxis, seriesRest);
        final LineChart<Date, Number> lineChartFootball = new LineChart<>(dateAxis2, numberAxis2, seriesFootball);
        
        Scene scene = new Scene(lineChart, 800, 600);
        Scene sceneFootball = new Scene(lineChartFootball, 800, 600); 
        
        primaryStage.setScene(sceneFootball);
        primaryStage.show();
        
        return scene;
    }
    
    private static Collection<XYChart.Series<Date, Number>> getSeries(List<String> sports) {
        final Date startdate = SportData2.getInstance().getStartDate();
        final Date enddate = SportData2.getInstance().getEndDate();
        final int interval = SportData2.getInstance().getInterval();
        
        final Map<String, Map<Date, Double>> sportpop = new HashMap();
        final Map<String, XYChart.Series<Date, Number>> sportseries = new HashMap();
        
        sports.stream().forEach(s -> {
            sportpop.put(s, SportData2.getInstance()
                    .getSportsForDate(startdate, enddate, s, interval));
            sportseries.put(s, new XYChart.Series());
            sportseries.get(s).setName(s);
            
            final List<Date> listofdates = new ArrayList(sportpop.get(s).keySet());
            Collections.sort(listofdates);
            
            final Calendar calendar = Calendar.getInstance();
            
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