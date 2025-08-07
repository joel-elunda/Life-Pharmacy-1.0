package life.pharmacy.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelImporter {

    public static List<List<String>> readExcel(File file) {
        List<List<String>> rows = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // première feuille

            for (Row row : sheet) {
                List<String> cellValues = new ArrayList<>();
                for (Cell cell : row) {
                    cellValues.add(getCellValueAsString(cell));
                }
                rows.add(cellValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private static String cellToString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return String.valueOf(cell.getLocalDateTimeCellValue());
                } else {
                    double d = cell.getNumericCellValue();
                    if (d == (long) d) {
                        return String.valueOf((long) d);
                    } else {
                        return String.valueOf(d);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception ex) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
            default:
                return "";
        }
    }
}
