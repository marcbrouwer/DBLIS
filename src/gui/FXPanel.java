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
import javafx.application.Platform;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private final CustomTableView table = new CustomTableView();
    private final ObservableList<TableData> data = FXCollections.observableArrayList();
    
    public FXPanel() {}

    /** function that returns the loading screen */
    private Scene getLoadingScene(){
        Scene scene = null;
        
        try {
            HBox loader = FXMLLoader.load(getClass().getResource("loader.fxml"));
            scene = new Scene(loader, 800, 600, Color.WHITE);
        }catch (IOException ex) {
            System.out.println(ex);
        }
        
        return scene;
    }
    
    public void drawScene(int index) {
        final FXPanel thisPanel = this;
        
        //set the loading screen
        setScene(getLoadingScene());
        setVisible(true);
        
        Platform.runLater(() -> {
            Scene scene1 = null;
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
                    break;
                case 4:
                    scene1 = drawUserTable(thisPanel);
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
    
    //Functions that are called when we start loading
    public void startLoading(){
        MainFrame.enableComponents(false);
    }
    
    //Functions that are called when we finished loading
    public void finishedLoading(){
        MainFrame.enableComponents(true);
    }

    private Scene drawSceneWelcome() {
        Group root = new Group();
        Scene scene = new Scene(root);
        Text text = new Text();
        text.setX(50);
        text.setY(50);
        text.setFont(new Font(15));
        text.setText("Welcome To The Bird Is Fit");
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

        final boolean year = SportData2.getInstance().getYearSelected();
        
        Runnable task = () -> {
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

            final boolean showStage1 = showStage;
            Platform.runLater(() -> {
                Scene scene = new Scene(lineChart, 800, 600);
                Scene sceneFootball = new Scene(lineChartFootball, 800, 600);
                if (showStage1) {
                    primaryStage.setScene(sceneFootball);
                    primaryStage.show();
                }
                panel.setScene(scene);
            });
        };
        Thread t = new Thread(task);
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
            
            addShowHashtagsOnClick(series);

            Scene scene = new Scene(barChart, 800, 600);
            panel.setScene(scene);
        };
        
        Thread t = new Thread(runner);
        t.start();
        return null;
    }

    private Scene drawBarChartUsers(FXPanel panel) {
        startLoading();
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
            
            finishedLoading();
            panel.setScene(scene);
        };
        
        Thread t = new Thread(runner);
        t.start();
        return null;
    }

    private Scene drawPieChart(FXPanel panel) {
        startLoading();
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
                setDrilldownData(panel, scene, pie, data, data.getName());
            });

            root.getChildren().add(pie);
            
            finishedLoading();
            panel.setScene(scene);
        };
        Thread t = new Thread(runner);
        t.start();
        return null;
    }

    private Scene drawUserTable(FXPanel panel){
        if (SportData2.getInstance().getSelected().size() < 2) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("The number of sports selected is less than 2");

            alert.showAndWait();
            panel.setVisible(false);
            
        } else {
            final Runnable runner = () -> {
                data.clear();

                int total = 0;
                final List<String> selected = SportData2.getInstance().getSelected();

                if (SportData2.getInstance().getYearSelected()) {

                    final int year = SportData2.getInstance().getYear();

                    for (int i = 0; i < selected.size(); i++) {
                        data.add(new TableData(selected.get(i), SportData2.getInstance()
                                .getNumberUsersInterestedIn(selected.get(i), year)));
                        total += data.get(i).getAmount();
                    }
                    data.add(new TableData("number of users tweeting about all", SportData2.getInstance()
                            .getNumberUsersInterestedIn(selected, year)));

                } else {
                    final long startTime = SportData2.getInstance().getStartDate().getTime();
                    final long endTime = SportData2.getInstance().getEndDate().getTime();

                    for (int i = 0; i < selected.size(); i++) {
                        data.add(new TableData(selected.get(i), SportData2.getInstance()
                                .getNumberUsersInterestedIn(selected.get(i), startTime, endTime)));
                        total += data.get(i).getAmount();
                    }

                    data.add(new TableData("number of users tweeting about all", SportData2.getInstance()
                            .getNumberUsersInterestedIn(selected, startTime, endTime)));
                }
                total -= data.get(data.size() - 1).getAmount();

                data.add(new TableData("number of users tweeting", total, 100.0));

                for (int i = 0; i < data.size() - 1; i++) {
                    if (total < 1) {
                        data.get(i).setPercentage(100.0);
                    } else {
                        data.get(i).setPercentage(
                                (double) Math.round(data.get(i).getAmount() 
                                        * 100f / (double) total));
                    }
                }

                final Label label = new Label("Number of users tweeting");
                label.setFont(new Font("Arial", 20));

                table.table.setEditable(false);

                CustomTableColumn sports = new CustomTableColumn("Sport");
                //sports.setMinWidth(200);
                sports.setPercentWidth(50);
                sports.setCellValueFactory(new PropertyValueFactory<>("sport"));

                CustomTableColumn totalAmount = new CustomTableColumn("Total amount");
                //totalAmount.setMinWidth(100);
                totalAmount.setPercentWidth(25);
                totalAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

                CustomTableColumn percentage = new CustomTableColumn("Percentage");
                //percentage.setMinWidth(100);
                percentage.setPercentWidth(25);
                percentage.setCellValueFactory(new PropertyValueFactory<>("percentage"));

                table.table.setItems(data);
                table.table.getColumns().addAll(sports, totalAmount, percentage);
                table.table.setPrefHeight(600);
                
                final VBox vbox = new VBox();
                vbox.setPrefHeight(600);
                vbox.setSpacing(5);
                vbox.setPadding(new Insets(10, 0, 0, 10));
                vbox.getChildren().addAll(label, table);

                Platform.runLater(() -> {
                    final Scene scene = new Scene(vbox, 800, 600);
                    panel.setScene(scene);
                });
            };
            final Thread t = new Thread(runner);
            t.start();
        }
        return null;
    }
    
    private void setDrilldownData(FXPanel panel, Scene scene, 
            final PieChart pie, PieChart.Data data, final String sport) {
        data.getNode().setOnMouseClicked((MouseEvent t) -> {
            panel.setScene(getLoadingScene());
            final Runnable runnerd = () -> {
                final List<PieChart.Data> list = new ArrayList<>();
                final Map<String, Double> cPop
                        = SportData2.getInstance().getPopularityKeywordsAsPercentage(sport);
                cPop.entrySet().stream().forEach(keyword -> {
                    list.add(new PieChart.Data(keyword.getKey(), keyword.getValue()));
                });
                pie.setData(FXCollections.observableArrayList(list));
                panel.setScene(scene);
            };
            final Thread td = new Thread(runnerd);
            td.start();
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

    private void addShowHashtagsOnClick(ObservableList<XYChart.Series<String, Number>> series) {
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
    
    private Scene drawBarChartHashtags() {
        Runnable task = () -> {
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
                final Stage primaryStage = new Stage();
                primaryStage.setTitle("Hashtags");
                primaryStage.setScene(scene);
                primaryStage.show();

            });

        };

        Thread t = new Thread(task);
        t.start();
        return null;
    }

    private void makePieChart(String sport, Date date, Number yValue) {
        if (yValue.intValue() == 0) {
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
            try {
                HBox loader = FXMLLoader.load(getClass().getResource("loader.fxml"));
                scene0 = new Scene(loader, 800, 600, Color.WHITE);
            } catch (IOException ex) {
            }
            primaryStage.setScene(scene0);
            primaryStage.show();

            Runnable task = () -> {
                final List<PieChart.Data> list = new ArrayList<>();

                final long[] stamps = SportData2.getInstance().getLineToPieTimestamps(date);

                final Map<String, Double> sportPop = SportData2.getInstance()
                        .getPopularityKeywordsAsPercentage(sport, stamps[0], stamps[1]);

                for (Map.Entry<String, Double> entry : sportPop.entrySet()) {
                    list.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
                PieChart pie = new PieChart(FXCollections.observableArrayList(list));
                pie.setTitle("Popularity for " + sport + ", " + date);

                Platform.runLater(() -> {
                    Scene scene1 = new Scene(pie, 800, 600);
                    primaryStage.setScene(scene1);
                    primaryStage.show();
                });

            };
            Thread t = new Thread(task);
            t.start();
        }
    }

    public class TableData {

        private final StringProperty sport;
        private final IntegerProperty amount;
        private final DoubleProperty percentage;

        /**
         * Default constructor.
         */
        public TableData() {
            this(null, null);
        }

        public TableData(String sport, Integer amount) {
            this.sport = new SimpleStringProperty(sport);
            this.amount = new SimpleIntegerProperty(amount);
            this.percentage = new SimpleDoubleProperty(100);
            //System.out.println(sport + amount);
        }

        public TableData(String sport, Integer amount, Double percentage) {
            this.sport = new SimpleStringProperty(sport);
            this.amount = new SimpleIntegerProperty(amount);
            this.percentage = new SimpleDoubleProperty(percentage);
            //System.out.println(sport + amount);
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

        public Double getPercentage() {
            return percentage.get();
        }

        public void setPercentage(Double percent) {
            this.percentage.set(percent);
        }

        public DoubleProperty percentageProperty() {
            return percentage;
        }
    }

    /**
     * CustomTableView to hold the table and grid.
     */
    public class CustomTableView<s> extends StackPane {

        private TableView<s> table;

        @SuppressWarnings("rawtypes")
        public CustomTableView() {
            this.table = new TableView<s>();
            final GridPane grid = new GridPane();
            this.table.getColumns().addListener(new ListChangeListener<TableColumn>() {

                public void onChanged(javafx.collections.ListChangeListener.Change arg0) {
                    grid.getColumnConstraints().clear();
                    ColumnConstraints[] arr1 = new ColumnConstraints[CustomTableView.this.table.getColumns().size()];
                    StackPane[] arr2 = new StackPane[CustomTableView.this.table.getColumns().size()];
                    int i = 0;
                    for (TableColumn column : CustomTableView.this.table.getColumns()) {
                        CustomTableColumn col = (CustomTableColumn) column;
                        ColumnConstraints consta = new ColumnConstraints();
                        consta.setPercentWidth(col.getPercentWidth());
                        StackPane sp = new StackPane();
                        if (i == 0) { // Quick fix for not showing the horizantal scroll bar. 
                            NumberBinding diff = sp.widthProperty().subtract(3.75);
                            column.prefWidthProperty().bind(diff);
                        } else {
                            column.prefWidthProperty().bind(sp.widthProperty());
                        }
                        arr1[i] = consta;
                        arr2[i] = sp;
                        i++;
                    }
                    grid.getColumnConstraints().addAll(arr1);
                    grid.addRow(0, arr2);
                }
            }
            );
            getChildren()
                    .addAll(grid, table);
        }

        public TableView<s> getTableView() {
            return this.table;

        }
    }

    /**
     * CustomTableColumn to hold the custom percentWidth property.
     */
    public class CustomTableColumn<s> extends TableColumn {

        private SimpleDoubleProperty percentWidth = new SimpleDoubleProperty();

        public CustomTableColumn(String columnName) {
            super(columnName);
        }

        public SimpleDoubleProperty percentWidth() {
            return percentWidth;
        }

        public double getPercentWidth() {
            return percentWidth.get();
        }

        public void setPercentWidth(double percentWidth) {
            this.percentWidth.set(percentWidth);
        }
    }

    /**
     * * Domain Object.
     */
    public class MyDomain {

        private SimpleStringProperty name = new SimpleStringProperty();
        private SimpleStringProperty description = new SimpleStringProperty();
        private SimpleStringProperty color = new SimpleStringProperty();

        public MyDomain(String name, String desc, String color) {
            this.name.set(name);
            this.description.set(desc);
            this.color.set(color);
        }

        public String getDescription() {
            return description.get();
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public String getColor() {
            return color.get();
        }

        public SimpleStringProperty colorProperty() {
            return color;
        }
    }
}
