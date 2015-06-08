package gui;

import dblis.SportData2;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("The amount of sports selected is less then 2");

            alert.showAndWait();
        } else {
            data.clear();
            final Stage primaryStage = new Stage();
            Scene scene = new Scene(new Group());
            primaryStage.setTitle("Number of users tweeting");
            primaryStage.setWidth(500);
            primaryStage.setHeight(500);

            //final Map<String, Integer> numberUsers = new HashMap();
            int total = 0;
            final List<String> selected = SportData2.getInstance().getSelected();


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
                data.add(new TableData("number of users tweeting about all", SportData2.getInstance()
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

            data.add(new TableData("number of users tweeting", total, 100.0));

            for(int i=0; i<data.size()-1; i++){
                if(total<1){
                    data.get(i).setPercentage(100.0);
                } else {
                    int j = data.get(i).getAmount()*10000/total;
                    j += 5;
                    System.out.println(j + " j");
                    double k = j/100.0;
                    System.out.println(k + " k");
                    data.get(i).setPercentage(k);
                }
            }



            final Label label = new Label("Number of users tweeting");
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
        private final DoubleProperty percentage;

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
            this.percentage = new SimpleDoubleProperty(100);
            System.out.println(sport + amount);
        }
        
        public TableData(String sport, Integer amount, Double percentage) {
            this.sport = new SimpleStringProperty(sport);
            this.amount = new SimpleIntegerProperty(amount);
            this.percentage = new SimpleDoubleProperty(percentage);
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
}