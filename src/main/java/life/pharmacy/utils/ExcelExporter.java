package life.pharmacy.utils; // adapte le package si besoin

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcelExporter {

    /**
     * Ecrit un .xlsx (Excel) à partir d'une structure List<List<String>>.
     */
    public static void writeExcel(List<List<String>> rows, File file) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Export");
            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i);
                List<String> cols = rows.get(i);
                for (int j = 0; j < cols.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(cols.get(j) == null ? "" : cols.get(j));
                }
            }
            // Autosize (optionnel)
            if (!rows.isEmpty()) {
                int colCount = rows.get(0).size();
                for (int c = 0; c < colCount; c++) sheet.autoSizeColumn(c);
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }

    /**
     * Écrit un CSV (UTF-8) utilisant ';' comme séparateur.
     */
    public static void writeCSV(List<List<String>> rows, File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (List<String> row : rows) {
                // échappez les ; et les retours-lignes sommairement
                List<String> safe = new ArrayList<>();
                for (String cell : row) safe.add(cell == null ? "" : cell.replace("\n", " ").replace("\r", " ").replace(";", ","));
                bw.write(String.join(";", safe));
                bw.newLine();
            }
        }
    }

    /**
     * Lecture CSV simple (séparateur ';')
     */
    public static List<List<String>> readCSV(File file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                // split en gardant vides
                String[] cols = line.split(";", -1);
                rows.add(Arrays.asList(cols));
            }
        }
        return rows;
    }

    /**
     * Helper pour décider d'écrire CSV ou Excel selon l'extension :
     * - si file endsWith .csv -> writeCSV
     * - sinon -> writeExcel (.xlsx)
     */
    public static void write(List<List<String>> rows, File file) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".csv")) writeCSV(rows, file);
        else writeExcel(rows, file);
    }

    /**
     * Helper pour lire : si .csv -> readCSV else -> utilise ExcelImporter.readExcel (compatible .xlsx/.xls).
     * NOTE: ExcelImporter est la classe dont tu m'as donné le code. Ici on l'appelle.
     */
    public static List<List<String>> read(File file) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".csv")) return readCSV(file);
        else return life.pharmacy.utils.ExcelImporter.readExcel(file); // adapte le package si nécessaire
    }
}
