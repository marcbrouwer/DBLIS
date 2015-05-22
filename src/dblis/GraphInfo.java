/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dblis;
import java.util.Date;

/**
 *
 * @author s130031
 */
public class GraphInfo {
    
    private static Date startDate;
    private static Date endDate;
    private static boolean[] selectedSports;
    private static int timeInterval;
    /* 
    @Param startDate, the starting date of the graph
    @Param endDate, the end date of the graph.
    @Param football, whether football should be shown
    @Param hockey, whether hockey should be shown
    @Param cycling, whether cycling should be shown
    @Param tennis, whether tennis should be shown
    @Param Skarting, whether skating should be shown
    @Param timeinterval, the timerinterval in days
    */
    
    public GraphInfo(Date startdate, Date enddate, boolean football, 
            boolean hockey, boolean cycling, boolean tennis, boolean skating, 
            int timeinterval){
        startDate = startdate;
        endDate = enddate;
        setSelectedSports(football, hockey, cycling, tennis, skating);
        timeInterval = timeinterval;
    }
    
    public static Date getStartDate(){
        return startDate;
    }
    
    public static Date getEndDate(){
        return endDate;
    }
    
    //0=football;
    //1=hockey;
    //2=cycling
    //3=tennis;
    //4=skating;
    public static boolean[] getSelectedSports(){
        return selectedSports;
    }
    
    public static int getTimeinterval(){
        return timeInterval;
    }
    
    public static void setStartDate(Date startdate){
        startDate = startdate;
    }
    
    public static void setEndDate(Date enddate){
        endDate = enddate;
    }
    
    public static void setSelectedSports(boolean football, 
            boolean hockey, boolean cycling, boolean tennis, boolean skating){
        selectedSports = new boolean[5];
        selectedSports[0] = football;
        selectedSports[1] = hockey;
        selectedSports[2] = cycling;
        selectedSports[3] = tennis;
        selectedSports[4] = skating;
    }
    
    public static void setTimeinterval(int timeinterval){
        timeInterval = timeinterval;
    }
}
            
   