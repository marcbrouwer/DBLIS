package dblis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * Common Excel methods
 *
 * @author Brouwer M.R.
 */
public final class Excel {
    
    // Instance declaration 
    
    /** Excel instance */
    private static final Excel instance = new Excel();
    
    /** Don't let anyone else instantiate this class */
    private Excel() {}
    
    /**
     * Gets the Excel instance
     * 
     * @return Excel instance
     */
    public static final Excel getInstance() {
        return instance;
    }
    
    // Excel methods
    
    /**
     * Fills excel sheet with data
     * 
     * @param workbook excel workbook
     * @param data data to fill
     * @param sheetName excel sheet name
     * @return workbook with added sheet
     */
    private HSSFWorkbook addExcelSheet(HSSFWorkbook workbook, Object[][] data, String sheetName) {
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
    private void createExcelFile(File file) {
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
    private void outputExcel(File file, HSSFWorkbook workbook) {
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
    public final void writeToExcel(File file, Object[][] data) {
        createExcelFile(file);
        
        HSSFWorkbook workbook = new HSSFWorkbook();
        
        workbook = addExcelSheet(workbook, data, "data");

        outputExcel(file, workbook);
    }
    
}