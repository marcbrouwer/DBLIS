package gui;

import dblis.SportData2;
import java.util.Date;
import java.util.List;
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
    public static Scene drawRelativeChart(FXPanel panel) {
        Runnable runner = () -> {
        
            ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();

            final NumberAxis numberAxis = new NumberAxis();
            final DateAxis dateAxis = new DateAxis();
            final LineChart<Date, Number> lineChart = new LineChart<>(dateAxis, numberAxis, series);

            //series.addAll(getSeries()); // implementation should be changed

            Scene scene = new Scene(lineChart, 800, 600);

            panel.setScene(scene);
        };
        Thread t = new Thread(runner);
        t.start();
        return null;
    }
    
    public static Scene drawBarChart(FXPanel panel) {
        Runnable runner = () -> {
            final ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Event");
            yAxis.setLabel("Popularity");

            final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis, series);

            final List<String> events = SportData2.getInstance().getSelected();
            events.stream().forEach(e -> series.addAll(getSerie(e)));

            Scene scene = new Scene(barChart, 800, 600);
            panel.setScene(scene);
        };
        
        Thread t = new Thread(runner);
        t.start();
        return null;
    }
    
    
    private static XYChart.Series<String, Number> getSerie(String event) {
        //Getting the selected information from the GUI
        final Date startdate = SportData2.getInstance().getStartDate();
        final Date enddate = SportData2.getInstance().getEndDate();
        
        //Creating variables required
        final XYChart.Series<String, Number> serie = new XYChart.Series();
        
        final String sep = ";&;";
        int pop = 0;
        
        if (event.contains(sep)) {
            String[] teams = event.split(sep);
            pop += SportData2.getInstance().getPopularity(
                        teams[0], startdate.getTime(), enddate.getTime());
            pop += SportData2.getInstance().getPopularity(
                        teams[1], startdate.getTime(), enddate.getTime());
            serie.setName(teams[0] + " - " + teams[1]);
        } else { 
            pop = SportData2.getInstance().getPopularity(
                        event, startdate.getTime(), enddate.getTime());
            serie.setName(event);
        }
        
        serie.getData().add(new XYChart.Data("", pop));
        
        return serie;
    }
    
    public static Scene drawBarChartUsers() {
        final ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Event");
        yAxis.setLabel("Number of users");
        
        final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis, series);
        
        final List<String> events = SportData2.getInstance().getSelected();
        events.stream().forEach(e -> series.addAll(getSerieUsers(e)));
        
        Scene scene = new Scene(barChart, 800, 600);
        
        return scene;
    }
    
    private static XYChart.Series<String, Number> getSerieUsers(String event) {
        //Getting the selected information from the GUI
        final Date startdate = SportData2.getInstance().getStartDate();
        final Date enddate = SportData2.getInstance().getEndDate();
        
        //Creating variables required
        final XYChart.Series<String, Number> serie = new XYChart.Series();
        
        final String sep = ";&;";
        int pop = 0;
        
        if (event.contains(sep)) {
            String[] teams = event.split(sep);
            pop += SportData2.getInstance().getNumberUsers(
                        teams[0], startdate.getTime(), enddate.getTime());
            pop += SportData2.getInstance().getNumberUsers(
                        teams[1], startdate.getTime(), enddate.getTime());
            serie.setName(teams[0] + " - " + teams[1]);
        } else { 
            pop = SportData2.getInstance().getNumberUsers(
                        event, startdate.getTime(), enddate.getTime());
            serie.setName(event);
        }
        
        serie.getData().add(new XYChart.Data("", pop));
        
        return serie;
    }
    
}