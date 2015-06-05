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
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
    
    private final static TableView table = new TableView();
    private final static ObservableList<TableData> data = FXCollections.observableArrayList();
    
    public static void createTheFuckingTable(){
        if (SportData2.getInstance().getSelected().size() < 2) {

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
                for (int i = 0; i < selected.size(); i++) {
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
                    int j = data.get(i).getAmount()*100/total;
                    data.get(i).setPercentage(j);
                }
            }

            System.out.println("3");



            final Label label = new Label("Amount of people talking");
            label.setFont(new Font("Arial", 20));

            table.setEditable(false);

            TableColumn sports = new TableColumn("Sport");
            sports.setMinWidth(200);
            sports.setCellValueFactory(
                new PropertyValueFactory<>("sport"));

            TableColumn totalAmount = new TableColumn("Total amount");
            totalAmount.setMinWidth(100);
            totalAmount.setCellValueFactory(
                new PropertyValueFactory<>("amount"));

            TableColumn percentage = new TableColumn("Percentage");
            percentage.setMinWidth(100);
            percentage.setCellValueFactory(
                new PropertyValueFactory<>("percentage"));

            table.setItems(data);
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
        private final IntegerProperty percentage;

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
            this.percentage = new SimpleIntegerProperty(100);
            System.out.println(sport + amount);
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