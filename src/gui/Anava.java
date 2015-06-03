package gui;

import dblis.SportData2;
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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author Brouwer M.R.
 */
public class Anava {
    
    public static Scene drawLineChart(FXPanel panel) {

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

        final boolean year = SportData2.getInstance().getYearSelected();
        
        Runnable runner = () -> {
            boolean showStage = false;
            if (SportData2.getInstance().footballSeperate() && selected.contains("football")) {
                
                    selected.remove("football");
                    seriesFootball.addAll(getSeries(Arrays.asList("football"), year));
                    seriesRest.addAll(getSeries(selected, year));

                    addShowPieOnClick(seriesFootball);
                    showStage = true;
            } else {
                seriesRest.addAll(getSeries(selected, year));
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
    
    /** COPIED NOT CHANGED */
    private static Collection<XYChart.Series<Date, Number>> getSeries(
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

    /** COPIED NOT CHANGED */
    private static void addShowPieOnClick(ObservableList<XYChart.Series<Date, Number>> series) {
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

    /** COPIED NOT CHANGED */
    private static void makePieChart(String sport, Date date, Number yValue) {
        if(yValue.intValue()==0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("There is no data to show for this sport at the given date.");

            alert.showAndWait();
        } else {
            final Stage primaryStage = new Stage();
            primaryStage.setTitle("Popularity for " + sport + ", " + date);
            
            //setting a scene
            Scene scene0 = null;
            /*try {
                HBox loader = FXMLLoader.load(getClass().getResource("loader.fxml"));
                scene0 = new Scene(loader, 800, 600, Color.WHITE);
                setScene(scene0);
                System.out.println((new Date()).getTime());
            }catch (IOException ex) {
                System.out.println(ex);
            }*/
            primaryStage.setScene(scene0);
            primaryStage.show();
            
            //Runnable runner = () -> {
                final List<PieChart.Data> list = new ArrayList<>();

                final long[] stamps = SportData2.getInstance().getDayTimestamps(date);

                final Map<String, Double> sportPop = SportData2.getInstance()
                        .getPopularityKeywordsAsPercentage(sport, stamps[0], stamps[1]);

                for (Map.Entry<String, Double> entry : sportPop.entrySet()) {
                    list.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
                PieChart pie = new PieChart(
                        FXCollections.observableArrayList(list));
                pie.setTitle("Popularity for " + sport + ", " + date);

                Scene scene = new Scene(pie, 800, 600);
                primaryStage.setScene(scene);
                primaryStage.show();
            /*};
            Thread t = new Thread(runner);
            t.start();*/
        }
    }
    
    private final static TableView table = new TableView();
    private final static ObservableList<TableData> data = FXCollections.observableArrayList();
    
    public static void createTheFuckingTable(){
        if(SportData2.getInstance().getSelected().size()<2){
            
            System.out.println("The amount of sports selected is less then 2");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("The amount of sports selected is less then 2");

            alert.showAndWait();
        } else {
            System.out.println("0");

            final Stage primaryStage = new Stage();
            Scene scene = new Scene(new Group());
            primaryStage.setTitle("Amount of users tweeting");
            primaryStage.setWidth(500);
            primaryStage.setHeight(500);

            //final Map<String, Integer> numberUsers = new HashMap();
            int total = 0;
            final List<String> selected = SportData2.getInstance().getSelected();

            System.out.println("1");

            if(SportData2.getInstance().getYearSelected()){

                final int year = SportData2.getInstance().getYear();

                //for(String sport : selected){
                for(int i=0; i<selected.size(); i++){
                    data.add(new TableData(selected.get(i), SportData2.getInstance()
                            .getNumberUsersInterestedIn(selected.get(i), year)));
                    total += data.get(i).getAmount();
                    /*numberUsers.put(sport, SportData2.getInstance()
                            .getNumberUsersInterestedIn(sport, year));
                    total += numberUsers.get(sport);*/
                }
                data.add(new TableData("Both", SportData2.getInstance()
                        .getNumberUsersInterestedIn(selected, year)));
                /*numberUsers.put("Both", SportData2.getInstance()
                        .getNumberUsersInterestedIn(selected, year)); */

            } else {
                final long startTime = SportData2.getInstance().getStartDate().getTime();
                final long endTime = SportData2.getInstance().getEndDate().getTime();

                for(int i=0; i<selected.size(); i++){
                    data.add(new TableData(selected.get(i), SportData2.getInstance()
                        .getNumberUsersInterestedIn(selected.get(i), startTime, endTime)));
                    total += data.get(i).getAmount();
                }

                data.add(new TableData("Both", SportData2.getInstance()
                        .getNumberUsersInterestedIn(selected, startTime, endTime)));
            }
            total -= data.get(data.size()-1).getAmount();


            System.out.println("2");

            for(int i=0; i<data.size(); i++){
                if(total<1){
                    data.get(i).setPercentage(100);
                } else {
                    int j = data.get(i).getAmount()/total;
                    data.get(i).setPercentage(j);
                }
            }

            System.out.println("3");



            final Label label = new Label("Amount of people talking");
            label.setFont(new Font("Arial", 20));

            table.setEditable(false);

            TableColumn sports = new TableColumn("Sport");
            sports.setMinWidth(200);
            sports.setCellValueFactory(new PropertyValueFactory<>("sport"));

            TableColumn totalAmount = new TableColumn("Total amount");
            totalAmount.setMinWidth(100);
            totalAmount.setCellFactory(new PropertyValueFactory<>("amount"));

            TableColumn percentage = new TableColumn("Percentage");
            percentage.setMinWidth(100);
            percentage.setCellFactory(new PropertyValueFactory<>("percentage"));

            table.getColumns().addAll(sports, totalAmount, percentage);

            final VBox vbox = new VBox();
            vbox.setSpacing(5);
            vbox.setPadding(new Insets(10, 0, 0, 10));
            vbox.getChildren().addAll(label, table);

            ((Group) scene.getRoot()).getChildren().addAll(vbox);

            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("4");
        
        }
    }
    
    public static class TableData {

        private final StringProperty sport;
        private final IntegerProperty amount;
        private IntegerProperty percentage;

        /**
         * Default constructor.
         */
        public TableData() {
            this(null, null);
        }

        /**
         * Constructor with some initial data.
         * 
         * @param firstName
         * @param lastName
         */
        public TableData(String sport, Integer amount) {
            this.sport = new SimpleStringProperty(sport);
            this.amount = new SimpleIntegerProperty(amount);
        }

        public String getSport() {
            return sport.get();
        }

        public void setSport(String sportt) {
            this.sport.set(sportt);
        }

        public StringProperty sportProperty() {
            return sport;
        }

        public Integer getAmount() {
            return amount.get();
        }

        public void setLastName(Integer amountt) {
            this.amount.set(amountt);
        }

        public IntegerProperty amountProperty() {
            return amount;
        }
        
        public Integer getPercentage() {
            return percentage.get();
        }
        
        public void setPercentage(Integer percent) {
            this.percentage.set(percent);
        }
        
        public IntegerProperty percentageProperty() {
            return percentage;
        }
    }
}