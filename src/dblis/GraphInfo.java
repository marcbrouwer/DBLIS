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
    
    public GraphInfo(Date startDate, Date endDate, boolean football, 
            boolean hockey, boolean cycling, boolean tennis, boolean skating, 
            int timeinterval){
        this.startDate = startDate;
        this.endDate = endDate;
        this.selectedSports = new boolean[5];
        this.selectedSports[0] = football;
        this.selectedSports[1] = hockey;
        this.selectedSports[2] = cycling;
        this.selectedSports[3] = tennis;
        this.selectedSports[4] = skating;
        this.timeInterval = timeinterval;
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
    public static boolean[] selectedSports(){
        return selectedSports;
    }
    
    public static int timeinterval(){
        return timeInterval;
    }
}