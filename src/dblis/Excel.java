package dblis;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;

/**
 * Common Excel methods
 *
 * @author Brouwer M.R.
 */
public final class Excel {
    
    // Excel methods
    
    /**
     * Fills excel sheet with data
     * 
     * @param workbook excel workbook
     * @param data data to fill
     * @param sheetName excel sheet name
     * @return workbook with added sheet
     */
    private static HSSFWorkbook addExcelSheet(HSSFWorkbook workbook, Object[][] data, String sheetName) {
        HSSFSheet sheet = workbook.createSheet(sheetName);
        int rownum = 0;
        for (Object[] data1 : data) {
            Row row = sheet.createRow(rownum++);
            Object[] objArr = data1;
            int cellnum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellnum++);
                if (obj instanceof Date) {
                    cell.setCellValue((Date) obj);
                } else if (obj instanceof Boolean) {
                    cell.setCellValue((Boolean) obj);
                } else if (obj instanceof String) {
                    cell.setCellValue((String) obj);
                } else if (obj instanceof Double) {
                    cell.setCellValue((Double) obj);
                } else if (obj instanceof Float) {
                    cell.setCellValue((Float) obj);
                } else if (obj instanceof Byte || obj instanceof Integer 
                        || obj instanceof Short) {
                    cell.setCellValue(Double.parseDouble(String.valueOf(obj)));
                }
            }
        }
        
        return workbook;
    }
    
    /**
     * Create file
     * 
     * @param file Excel file to create
     */
    private static void createExcelFile(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                System.out.println("Error creating file: " + file.getAbsolutePath());
            }
        }
    }
    
    /**
     * Writes workbook to Excel document
     * 
     * @param file excel file
     * @param workbook excel workbook
     */
    private static void outputExcel(File file, HSSFWorkbook workbook) {
        try {
            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
            System.out.println("Excel written successfully... (" + file.getAbsolutePath() + ")");
        } catch (IOException e) {
        }
    }
    
    /**
     * Writes data to Excel document
     * 
     * @param file file to write
     * @param data data to write
     */
    public static final void writeToExcel(File file, Object[][] data) {
        createExcelFile(file);
        
        HSSFWorkbook workbook = new HSSFWorkbook();
        
        workbook = addExcelSheet(workbook, data, "data");

        outputExcel(file, workbook);
    }
    
    public static final void pieChart(File file, Object[][] data) throws IOException {
        writeToExcel(file, data);
        
        /* Read Excel and the Chart Data */
        FileInputStream chart_file_input = new FileInputStream(file);
                
        /* Read data into HSSFWorkbook */
        HSSFWorkbook workbook = new HSSFWorkbook(chart_file_input);
        
        /* This worksheet contains the Pie Chart Data */
        HSSFSheet sheet = workbook.getSheetAt(0);
        
        /* Create JFreeChart object that will hold the Pie Chart Data */
        DefaultPieDataset chartData = new DefaultPieDataset();
        
        /* Create an Iterator object */
        Iterator<Row> rowIterator = sheet.iterator();
        
        /* Loop through worksheet data and populate Pie Chart Dataset */
        String chart_label = "a";
        Number chart_data = 1;
        while (rowIterator.hasNext()) {
            //Read Rows from Excel document
            Row row = rowIterator.next();
            //Read cells in Rows and get chart data
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_NUMERIC:
                        chart_data = cell.getNumericCellValue();
                        break;
                    case Cell.CELL_TYPE_STRING:
                        chart_label = cell.getStringCellValue();
                        break;
                }
            }
            /* Add data to the data set */
            chartData.setValue(chart_label, chart_data);
        }              
        
        /* Create a logical chart object with the chart data collected */
        JFreeChart chart = 
                ChartFactory.createPieChart3D("Excel Pie Chart Java Example",
                        chartData, true, true, false);
        
        final PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        plot.setNoDataMessage("No data to display");
        
        int width = 640; /* Width of the chart */
        int height = 480; /* Height of the chart */
        float quality = 1; /* Quality factor */
        
        /* Write chart as JPG to Output Stream */
        ByteArrayOutputStream chart_out = new ByteArrayOutputStream();
        ChartUtilities.writeChartAsJPEG(chart_out, quality, chart, width, height);
        
        /* We now read from the output stream and frame the input chart data */
        InputStream feed_chart_to_excel = new ByteArrayInputStream(chart_out.toByteArray());
        byte[] bytes = IOUtils.toByteArray(feed_chart_to_excel);
        
        /* Add picture to workbook */
        int picture_id = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
        
        /* We can close Piped Input Stream. We don't need this */
        feed_chart_to_excel.close();
        
        /* Close PipedOutputStream also */
        chart_out.close();
        
        /* Create the drawing container */
        HSSFPatriarch drawing = sheet.createDrawingPatriarch();
        
        /* Create an anchor point */
        ClientAnchor anchor = new HSSFClientAnchor();
        
        /* Define top left corner, and we can resize picture suitable from there */
        anchor.setCol1(4);
        anchor.setRow1(5);
        
        /* Invoke createPicture and pass the anchor point and ID */
        HSSFPicture picture = drawing.createPicture(anchor, picture_id);
        
        /* Call resize method, which resizes the image */
        picture.resize();
        
        /* Close the FileInputStream */
        chart_file_input.close();
        
        /* Write changes to the workbook */
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
    }
    
}