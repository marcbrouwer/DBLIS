package gui;

import dblis.SportData2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author Brouwer M.R.
 */
public class Marc {
    
    public static Scene drawBarChart(FXPanel panel) {
        Runnable runner = () -> {
            final ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Event");
            yAxis.setLabel("Popularity");

            final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis, series);

            final List<String> events = SportData2.getInstance().getSelected();
            events.stream().forEach(e -> series.addAll(getSerie(e, true)));
            
            addShowHashtagsOnClick(series);

            Scene scene = new Scene(barChart, 800, 600);
            panel.setScene(scene);
        };
        
        Thread t = new Thread(runner);
        t.start();
        return null;
    }
    
    public static Scene drawBarChartHashtags() {
        Runnable task = () -> {
            final Stage primaryStage = new Stage();
            primaryStage.setTitle("Hashtags");

            final ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Hashtags");
            yAxis.setLabel("Number of hashtags");

            final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis, series);

            final List<String> selected = SportData2.getInstance().getSelected();
            final List<String> copy = new ArrayList<>(selected);
            copy.stream().forEach(keyword -> {
                if (keyword.contains(" - ")) {
                    selected.remove(keyword);
                    final String[] splits = keyword.split(" - ");
                    selected.add(splits[0]);
                    selected.add(splits[1]);
                }
            });

            final Map<String, Double> pop;

            if (SportData2.getInstance().getYearSelected()) {
                final int year = SportData2.getInstance().getYear();
                pop = SportData2.getInstance().getMostCommonHashtags(selected, year);
            } else {
                final long starttime = SportData2.getInstance().getStartDate().getTime();
                final long endtime = SportData2.getInstance().getEndDate().getTime();
                pop = SportData2.getInstance().getMostCommonHashtags(selected, starttime, endtime);
            }

            final List<Double> values = new ArrayList<>(pop.values());
            Collections.sort(values, Collections.reverseOrder());
            double min = 1.0;
            if (values.size() >= 10) {
                min = values.get(9);
            } else if (!values.isEmpty()) {
                min = values.get(values.size() - 1);
            }
            final double atleast = min;

            pop.entrySet().stream()
                    .filter(entry -> entry.getValue() >= atleast)
                    .forEach(hashtag -> {
                        final XYChart.Series<String, Number> serie = new XYChart.Series();
                        serie.getData().add(new XYChart.Data("", hashtag.getValue()));
                        serie.setName(hashtag.getKey());
                        series.add(serie);
                    });

            Scene scene = new Scene(barChart, 800, 600);
            Platform.runLater(() -> {
                primaryStage.setScene(scene);
                primaryStage.show();

            });

        };

        Thread t = new Thread(task);
        t.start();
        return null;
    }
    
    private static void addShowHashtagsOnClick(ObservableList<XYChart.Series<String, Number>> series) {
        series.stream().forEach(serie -> {
            serie.getData().stream().forEach(data -> {
                try {
                    data.getNode().setOnMousePressed((MouseEvent mouseEvent) -> {
                        SportData2.getInstance().setSelected(Arrays.asList(serie.getName()));
                        drawBarChartHashtags();
                    });
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            });
        });
    }
    
    /** COPIED NOT CHANGED */
    private static XYChart.Series<String, Number> getSerie(String event, boolean pop_user) {
        //Getting the selected information from the GUI
        final Date startdate = SportData2.getInstance().getStartDate();
        final Date enddate = SportData2.getInstance().getEndDate();
        
        //Creating variables required
        final XYChart.Series<String, Number> serie = new XYChart.Series();
        
        final String sep = ";&;";
        int pop = 0;
        
        if (pop_user) {
            
            if (event.contains(sep)) {
                
                String[] teams = event.split(sep);
                if (SportData2.getInstance().getYearSelected()) {
                    final int year = SportData2.getInstance().getYear();
                    pop += SportData2.getInstance().getPopularity(teams[0], year);
                    pop += SportData2.getInstance().getPopularity(teams[1], year);
                } else {
                    pop += SportData2.getInstance().getPopularity(
                                teams[0], startdate.getTime(), enddate.getTime());
                    pop += SportData2.getInstance().getPopularity(
                                teams[1], startdate.getTime(), enddate.getTime());
                }
                serie.setName(teams[0] + " - " + teams[1]);
                
            } else { 
                if (SportData2.getInstance().getYearSelected()) {
                    final int year = SportData2.getInstance().getYear();
                    pop = SportData2.getInstance().getPopularity(event, year);
                } else {
                    pop = SportData2.getInstance().getPopularity(
                                event, startdate.getTime(), enddate.getTime());
                }
                serie.setName(event);
            }
            
        } else {
            
            if (event.contains(sep)) {
                
                String[] teams = event.split(sep);
                if (SportData2.getInstance().getYearSelected()) {
                    final int year = SportData2.getInstance().getYear();
                    pop += SportData2.getInstance().getNumberUsers(teams[0], year);
                    pop += SportData2.getInstance().getNumberUsers(teams[1], year);
                } else {
                    pop += SportData2.getInstance().getNumberUsers(
                                teams[0], startdate.getTime(), enddate.getTime());
                    pop += SportData2.getInstance().getNumberUsers(
                                teams[1], startdate.getTime(), enddate.getTime());
                }
                serie.setName(teams[0] + " - " + teams[1]);
                
            } else { 
                if (SportData2.getInstance().getYearSelected()) {
                    final int year = SportData2.getInstance().getYear();
                    pop = SportData2.getInstance().getNumberUsers(event, year);
                } else {
                    pop = SportData2.getInstance().getNumberUsers(
                                event, startdate.getTime(), enddate.getTime());
                }
                serie.setName(event);
            }
            
        }
        
        serie.getData().add(new XYChart.Data("", pop));
        
        return serie;
    }
    
}