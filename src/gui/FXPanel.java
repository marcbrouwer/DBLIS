package gui;

import dblis.SportData2;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

    public FXPanel() {
    }

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
        Text text = new Text();
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
        Text text = new Text();
        text.setX(50);
        text.setY(50);
        text.setFont(new Font(15));
        text.setText("Goodbye JavaFX!");
        root.getChildren().add(text);
        return scene;
    }

    private Scene drawLineChart() {
        ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();

        NumberAxis numberAxis = new NumberAxis();

        DateAxis dateAxis = new DateAxis();

        Date startdate = SportData2.getInstance().getStartDate();
        Date enddate = SportData2.getInstance().getEndDate();

        LineChart<Date, Number> lineChart = new LineChart<>(dateAxis, numberAxis, series);

        int interval = SportData2.getInstance().getInterval();
        List<String> sports = SportData2.getInstance().getSelected();
        Map<String, Map<Date, Double>> count2 = new HashMap();
        Map<String, XYChart.Series> sportseries = new HashMap();
        sports.stream().forEach(s -> {
            count2.put(s, SportData2.getInstance().getSportsForDate(startdate, enddate, s, interval));
            sportseries.put(s, new XYChart.Series());
            sportseries.get(s).setName(s);
            final List<Date> listofdates = new ArrayList(count2.get(s).keySet());
            Collections.sort(listofdates);
            Calendar calendar = Calendar.getInstance();
            listofdates.stream().forEach(d -> {
                calendar.setTimeInMillis(d.getTime());
                sportseries.get(s).getData().add(new XYChart.Data(calendar.getTime(), count2.get(s).get(d)));
            });
        });
        sportseries.entrySet().stream().forEach(serie -> {
            series.add(serie.getValue());
        });
        Scene scene = new Scene(lineChart, 1600, 900);
        //lineChart.getData().add(series);
        return scene;
    }

    private Scene drawPieChart() {
        Group root = new Group();
        Scene scene = new Scene(root);
        final Map<String, Double> sportPop
                = SportData2.getInstance().getPopularityAllSportsAsPercentage();

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
                final Map<String, Double> cPop
                        = SportData2.getInstance().getPopularityKeywordsAsPercentage(sport);
                cPop.entrySet().stream().forEach(keyword -> {
                    list.add(new PieChart.Data(keyword.getKey(), keyword.getValue()));
                });
                pie.setData(FXCollections.observableArrayList(list));
            }
        });
    }

}
