/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import dblis.SportData2;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author s124392
 */
public class FXPanel extends JFXPanel {
    private static final Dimension POPSIZE = new Dimension(780, 578);
    
    public FXPanel() {}
    
    public void drawScene(int index) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Scene scene = null;
                switch (index) {
                case 0:
                    scene = drawSceneWelcome();
                    break;
                case 1:
                    scene = drawSceneGoodbye();
                    break;
                case 2:
                    scene = drawLineChart();
                    break;
                case 3:
                    scene = drawPieChart();
                    break;
                }
                if (scene != null) {
                    setScene(scene);
                }
            }
        });
    }
    
    private Scene drawSceneWelcome() {
        Group root = new Group();
        Scene scene = new Scene(root);
        Text  text  =  new  Text();
        text.setX(50);
        text.setY(50);
        text.setFont(new Font(15));
        text.setText("Welcome JavaFX!");
        root.getChildren().add(text);
        return scene;
    }
    
    private Scene drawSceneGoodbye() {
        Group root = new Group();
        Scene scene = new Scene(root);
        Text  text  =  new  Text();
        text.setX(50);
        text.setY(50);
        text.setFont(new Font(15));
        text.setText("Goodbye JavaFX!");
        root.getChildren().add(text);
        return scene;
    } 
    
    private Scene drawLineChart(){
        Group root = new Group();
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Number of Month");
        //creating the chart
        //final LineChart<Number,Number> lineChart = 
          //      new LineChart<Number,Number>(xAxis,yAxis);
                
        //lineChart.setTitle("Popularity of Sports");
        //defining a series
        //XYChart.Series series = new XYChart.Series();
        //series.setName("Popularity of Sports");
        SimpleDateFormat sf = new SimpleDateFormat("dd-mm-yyyy");
        //populating the series with datas
        /*
        series.getData().add(new XYChart.Data(1, 23));
        series.getData().add(new XYChart.Data(2, 14));
        series.getData().add(new XYChart.Data(3, 15));
        series.getData().add(new XYChart.Data(4, 24));
        series.getData().add(new XYChart.Data(5, 34));
        series.getData().add(new XYChart.Data(6, 36));
        series.getData().add(new XYChart.Data(7, 22));
        series.getData().add(new XYChart.Data(8, 45));
        series.getData().add(new XYChart.Data(9, 43));
        series.getData().add(new XYChart.Data(10, 17));
        series.getData().add(new XYChart.Data(11, 29));
        series.getData().add(new XYChart.Data(12, 25));
        */
        ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();
        ObservableList<XYChart.Data<Date, Number>> series1Data = FXCollections.observableArrayList();
        //series1Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2012, 11, 15).getTime(), 2));
        //series1Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2014, 5, 3).getTime(), 4));
        ObservableList<XYChart.Data<Date, Number>> series2Data = FXCollections.observableArrayList();
        //series2Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2014, 0, 13).getTime(), 8));
        //series2Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2014, 7, 27).getTime(), 4));
        //series.add(new XYChart.Series<>("Series1", series1Data));
        //series.add(new XYChart.Series<>("Series2", series2Data));
        NumberAxis numberAxis = new NumberAxis();
        DateAxis dateAxis = new DateAxis();
        LineChart<Date, Number> lineChart = new LineChart<Date, Number>(dateAxis, numberAxis, series);
        Date startdate = SportData2.getInstance().getStartDate();
        Date enddate = SportData2.getInstance().getEndDate();
        int interval = SportData2.getInstance().getInterval();
        final Map<Date, Double> count = SportData2.getInstance().getSportsForDate(startdate, enddate,
                "football", interval);
        List<Date> listofdates = new ArrayList(count.keySet());
        Collections.sort(listofdates);
        Calendar calendar = Calendar.getInstance();
        listofdates.stream().forEach(d -> {
            calendar.setTimeInMillis(d.getTime());
            series.getData().add(new XYChart.Data(calendar.getTime(),count.get(d)));
        });
        System.out.println(listofdates);
        Scene scene  = new Scene(lineChart,1600,900);
        //lineChart.getData().add(series);
        return scene;
    }
    
    private Scene drawPieChart() {
        Group root = new Group();
        Scene scene = new Scene(root);
        final Map<String, Double> sportPop =
                SportData2.getInstance().getPopularityAllSportsAsPercentage();
        
        final List<PieChart.Data> list = new ArrayList<>();
        
        sportPop.entrySet().stream().forEach(entry -> {
            list.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        });
        
        PieChart pie = new PieChart(
                FXCollections.observableArrayList(list));
        pie.setTitle("Popularity");
        pie.setPrefSize(POPSIZE.width, POPSIZE.height);
        
        list.stream().forEach(data -> {
            setDrilldownData(pie, data, data.getName());
        });
        
        root.getChildren().add(pie);
        return scene;
    }
    
    private void setDrilldownData(final PieChart pie, PieChart.Data data, final String sport) {
        data.getNode().setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent t) {
                final List<PieChart.Data> list = new ArrayList<>();
                final Map<String, Double> cPop =
                        SportData2.getInstance().getPopularityKeywordsAsPercentage(sport);
                cPop.entrySet().stream().forEach(keyword -> {
                    list.add(new PieChart.Data(keyword.getKey(), keyword.getValue()));
                });
                pie.setData(FXCollections.observableArrayList(list));
            }
        });
    }

}
