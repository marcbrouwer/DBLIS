/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import dblis.GraphInfo;
import dblis.SportData2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author s124392
 */
public class FXPanel extends JFXPanel {
    
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
        final LineChart<Number,Number> lineChart = 
                new LineChart<Number,Number>(xAxis,yAxis);
                
        lineChart.setTitle("Popularity of Sports");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("Popularity of Sports");
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
        Map<Date, Double> count = new HashMap();
        Date startdate = SportData2.getInstance().getStartDate();
        Date enddate = SportData2.getInstance().getEndDate();
        int interval = SportData2.getInstance().getInterval();
        count = SportData2.getInstance().getSportsForDate(startdate, enddate,
                "football", interval);
        List<Date> listofdates = new ArrayList(count.keySet());
        Collections.sort(listofdates, Collections.reverseOrder());
        System.out.println(listofdates);
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
        return scene;
    }
}
