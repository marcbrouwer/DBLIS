package gui;

import dblis.SportData2;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author s124392
 */
public class FXPanel extends JFXPanel {

    private static final Dimension POPSIZE = new Dimension(780, 578);

    public FXPanel() {
    }

    public void drawScene(int index) {
        final FXPanel thisPanel = this;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Scene scene = null;
                try {
                    HBox loader = FXMLLoader.load(getClass().getResource("loader.fxml"));
                    scene = new Scene(loader);
                    setScene(scene);
                    System.out.println((new Date()).getTime());
                } catch (IOException ex) {
                    System.out.println(ex);
                }
                switch (index) {
                    case 0:
                        scene = drawSceneWelcome();
                        break;
                    case 1:
                        scene = drawSceneGoodbye();
                        break;
                    case 2:
                        scene = drawLineChart(thisPanel);
                        break;
                    case 3:
                        scene = drawPieChart(thisPanel);
                        System.out.println((new Date()).getTime());
                        break;
                    case 4:
                        scene = drawRelativeChart(thisPanel);
                        break;
                    case 5:
                        scene = drawBarChart(thisPanel);
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

    private Scene drawLineChart(FXPanel panel) {
        Runnable runner = () -> {

            ObservableList<XYChart.Series<Date, Number>> seriesFootball = FXCollections.observableArrayList();
            ObservableList<XYChart.Series<Date, Number>> seriesRest = FXCollections.observableArrayList();

            final Stage primaryStage = new Stage();
            primaryStage.setTitle("Football Popup");

            final NumberAxis numberAxis = new NumberAxis();
            final DateAxis dateAxis = new DateAxis();

            final NumberAxis numberAxis2 = new NumberAxis();
            final DateAxis dateAxis2 = new DateAxis();

            final List<String> selected = SportData2.getInstance().getSelected();

            final LineChart<Date, Number> lineChart = new LineChart<>(dateAxis, numberAxis, seriesRest);
            final LineChart<Date, Number> lineChartFootball = new LineChart<>(dateAxis2, numberAxis2, seriesFootball);

            boolean showStage = false;
            if (SportData2.getInstance().footballSeperate() && selected.contains("football")) {
                //if (selected.contains("football")) {

                    selected.remove("football");
                    seriesFootball.addAll(getSeries(Arrays.asList("football")));
                    seriesRest.addAll(getSeries(selected));

                    addShowPieOnClick(seriesFootball);
                    showStage = true;
                //}

            } else {
                seriesRest.addAll(getSeries(selected));
            }

            addShowPieOnClick(seriesRest);

            Scene scene = new Scene(lineChart, 800, 600);
            Scene sceneFootball = new Scene(lineChartFootball, 800, 600);
            if (showStage) {
                primaryStage.setScene(sceneFootball);
                primaryStage.show();
            }
            panel.setScene(scene);
        };
        Thread t = new Thread(runner);
        t.start();
        return null;
    }

    private Scene drawRelativeChart(FXPanel panel) {
        return Jorrick.drawRelativeChart(panel);
    }

    private Scene drawBarChart(FXPanel panel) {
        return Jorrick.drawBarChart(panel);
    }

    private Scene drawPieChart(FXPanel panel) {
        Runnable runner = () -> {
            final Group root = new Group();
            final Scene scene = new Scene(root);
            final List<PieChart.Data> list = new ArrayList<>();

            final Date startdate = SportData2.getInstance().getStartDate();
            final Date enddate = SportData2.getInstance().getEndDate();
            final List<String> sports = SportData2.getInstance().getSelected();

            final Map<String, Double> sportPop = SportData2.getInstance()
                    .getPopularitySportsAsPercentage(sports, startdate.getTime(),
                            enddate.getTime());

            sportPop.entrySet().stream().forEach(entry -> {
                list.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            });

            PieChart pie = new PieChart(FXCollections.observableArrayList(list));
            pie.setTitle("Popularity");
            pie.setPrefSize(POPSIZE.width, POPSIZE.height);

            list.stream().forEach(data -> {
                setDrilldownData(pie, data, data.getName());
            });

            root.getChildren().add(pie);
            
            panel.setScene(scene);
        };
        Thread t = new Thread(runner);
        t.start();
        return null;
    }

    private void setDrilldownData(final PieChart pie, PieChart.Data data, final String sport) {
        data.getNode().setOnMouseClicked((MouseEvent t) -> {
            final List<PieChart.Data> list = new ArrayList<>();
            final Map<String, Double> cPop
                    = SportData2.getInstance().getPopularityKeywordsAsPercentage(sport);
            cPop.entrySet().stream().forEach(keyword -> {
                list.add(new PieChart.Data(keyword.getKey(), keyword.getValue()));
            });
            pie.setData(FXCollections.observableArrayList(list));
        });
    }

    public static Collection<XYChart.Series<Date, Number>> getSeries(List<String> sports) {
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

    private void addShowPieOnClick(ObservableList<XYChart.Series<Date, Number>> series) {
        series.stream().forEach(serie -> {
            serie.getData().stream().forEach(data -> {
                try {
                    data.getNode().setOnMousePressed((MouseEvent mouseEvent) -> {
                        makePieChart(serie.getName(), data.getXValue());
                    });
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            });
        });
    }

    private void makePieChart(String sport, Date date) {
           
        final Stage primaryStage = new Stage();
        primaryStage.setTitle("Popularity for " + sport + ", " + date);

        final List<PieChart.Data> list = new ArrayList<>();

        final long[] stamps = SportData2.getInstance().getDayTimestamps(date);

        final Map<String, Double> sportPop = SportData2.getInstance()
                .getPopularityKeywordsAsPercentage(sport, stamps[0], stamps[1]);

        boolean isData0 = true; //If there is no data to view -> true, if there is ->false
            
        for (Entry<String, Double> entry : sportPop.entrySet()) {
            list.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            if(entry.getValue()!=0 && !entry.getValue().isNaN()){
                isData0 = false;
            }
            System.out.println(";pievalue " + entry.getValue());
        }
        
        if(isData0){ // This is if there is no data to view.
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("There is no data to show for this sport at the given date.");

            alert.showAndWait();
        } else { // If there is data to view

            PieChart pie = new PieChart(
                    FXCollections.observableArrayList(list));
            pie.setTitle("Popularity for " + sport + ", " + date);

            Scene scene = new Scene(pie, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

}