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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
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
        Platform.runLater(() -> {
            Scene scene0 = null;
            try {
                HBox loader = FXMLLoader.load(getClass().getResource("loader.fxml"));
                scene0 = new Scene(loader, 800, 600, Color.WHITE);
                setScene(scene0);
                System.out.println((new Date()).getTime());
            }catch (IOException ex) {
                System.out.println(ex);
            }
            Scene scene1  = null;
            switch (index) {
                case 0:
                    scene1 = drawSceneWelcome();
                    break;
                case 1:
                    scene1 = drawSceneGoodbye();
                    break;
                case 2:
                    scene1 = drawLineChart(thisPanel);
                    break;
                case 3:
                    scene1 = drawPieChart(thisPanel);
                    System.out.println((new Date()).getTime());
                    break;
                case 4:
                    scene1 = null;
                    break;
                case 5:
                    scene1 = drawBarChart(thisPanel);
                    break;
                case 6:
                    scene1 = drawBarChartUsers(thisPanel);
                    break;
            }
            if (scene1 != null) {
                setScene(scene1);
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

        Runnable runner = () -> {
            boolean showStage = false;
            if (SportData2.getInstance().footballSeperate() && selected.contains("football")) {
                //if (selected.contains("football")) {

                    selected.remove("football");
                    seriesFootball.addAll(getSeries(Arrays.asList("football"), false));
                    seriesRest.addAll(getSeries(selected, false));

                    addShowPieOnClick(seriesFootball);
                    showStage = true;
                //}

            } else {
                seriesRest.addAll(getSeries(selected, false));
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

    private Scene drawBarChart(FXPanel panel) {
        Runnable runner = () -> {
            final ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Event");
            yAxis.setLabel("Popularity");

            final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis, series);

            final List<String> events = SportData2.getInstance().getSelected();
            events.stream().forEach(e -> series.addAll(getSerie(e, true)));

            Scene scene = new Scene(barChart, 800, 600);
            panel.setScene(scene);
        };
        
        Thread t = new Thread(runner);
        t.start();
        return null;
    }

    private Scene drawBarChartUsers(FXPanel panel) {
        Runnable runner = () -> {
            final ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();

            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Event");
            yAxis.setLabel("Number of users");

            final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis, series);

            final List<String> events = SportData2.getInstance().getSelected();
            events.stream().forEach(e -> series.addAll(getSerie(e, false)));

            Scene scene = new Scene(barChart, 800, 600);
            panel.setScene(scene);
        };
        
        Thread t = new Thread(runner);
        t.start();
        return null;
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

    private XYChart.Series<String, Number> getSerie(String event, boolean pop_user) {
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
        } else {
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
        }
        
        serie.getData().add(new XYChart.Data("", pop));
        
        return serie;
    }
    
    private Collection<XYChart.Series<Date, Number>> getSeries(
            List<String> sports, boolean year) {
        final Date startdate = SportData2.getInstance().getStartDate();
        final Date enddate = SportData2.getInstance().getEndDate();
        final int interval = SportData2.getInstance().getInterval();

        final Map<String, Map<Date, Double>> sportpop = new HashMap();
        final Map<String, XYChart.Series<Date, Number>> sportseries = new HashMap();
        
        sports.stream().forEach(s -> {
            if (!year) {
                sportpop.put(s, SportData2.getInstance()
                        .getSportsForDate(startdate, enddate, s, interval));
            } else {
                sportpop.put(s, SportData2.getInstance().getSportsForYear(
                        SportData2.getInstance().getYear(), s, interval));
            }
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
                        makePieChart(serie.getName(), data.getXValue(), data.getYValue());
                    });
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            });
        });
    }

    private void makePieChart(String sport, Date date, Number yValue) {
        if(yValue.intValue()==0){
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("There is no data to show for this sport at the given date.");

            alert.showAndWait();
        } else {
            final Stage primaryStage = new Stage();
            primaryStage.setTitle("Popularity for " + sport + ", " + date);
            
            //setting a scene
            Scene scene0 = null;
            try {
                HBox loader = FXMLLoader.load(getClass().getResource("loader.fxml"));
                scene0 = new Scene(loader, 800, 600, Color.WHITE);
                setScene(scene0);
                System.out.println((new Date()).getTime());
            }catch (IOException ex) {
                System.out.println(ex);
            }
            primaryStage.setScene(scene0);
            primaryStage.show();
            
            Runnable runner = () -> {
                final List<PieChart.Data> list = new ArrayList<>();

                final long[] stamps = SportData2.getInstance().getDayTimestamps(date);

                final Map<String, Double> sportPop = SportData2.getInstance()
                        .getPopularityKeywordsAsPercentage(sport, stamps[0], stamps[1]);

                for (Entry<String, Double> entry : sportPop.entrySet()) {
                    list.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
                PieChart pie = new PieChart(
                        FXCollections.observableArrayList(list));
                pie.setTitle("Popularity for " + sport + ", " + date);

                Scene scene = new Scene(pie, 800, 600);
                primaryStage.setScene(scene);
                primaryStage.show();
            };
            Thread t = new Thread(runner);
            t.start();
        }
    }

}