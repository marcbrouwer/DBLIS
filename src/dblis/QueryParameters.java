/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dblis;

import javax.swing.JPanel;

/**
 *
 * @author Chen
 */
public class QueryParameters {
    
    private final JPanel thisPane ;
    private final boolean pieChartEnabled ;
    private final boolean lineGraphEnabled;
    private final boolean listGraphEnabled;
    private final boolean histogramEnabled;
    public QueryParameters(JPanel pane, boolean pieChartEnabled, boolean lineGraphEnabled, boolean listGraphEnabled, boolean histogramEnabled){
        thisPane = pane;
        this.pieChartEnabled = pieChartEnabled;
        this.lineGraphEnabled = lineGraphEnabled;
        this.listGraphEnabled = listGraphEnabled;
        this.histogramEnabled = histogramEnabled;
    }

    /**
     * @return the thisPane
     */
    public JPanel getThisPane() {
        return thisPane;
    }

    /**
     * @return the pieChartEnabled
     */
    public boolean isPieChartEnabled() {
        return pieChartEnabled;
    }

    /**
     * @return the lineGraphEnabled
     */
    public boolean isLineGraphEnabled() {
        return lineGraphEnabled;
    }

    /**
     * @return the listGraphEnabled
     */
    public boolean isListGraphEnabled() {
        return listGraphEnabled;
    }

    /**
     * @return the histogramEnabled
     */
    public boolean isHistogramEnabled() {
        return histogramEnabled;
    }
}
