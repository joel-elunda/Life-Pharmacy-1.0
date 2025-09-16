package life.pharmacy.services;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.*;

public class ExportImportService {

    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    // ----------- OUTIL COMMUN -----------
    private File getSaveFile(Stage stage, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        return fileChooser.showSaveDialog(stage);
    }

    private File getOpenFile(Stage stage, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        return fileChooser.showOpenDialog(stage);
    }

    private void exportTable(Stage stage, String title, String sql, String sheetName, String[] cols) {
        File file = getSaveFile(stage, title);
        if (file == null) return;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);
             Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet(sheetName);
            Row header = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }

            int rowIndex = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < cols.length; i++) {
                    row.createCell(i).setCellValue(rs.getString(cols[i]) == null ? "" : rs.getString(cols[i]));
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            System.out.println(sheetName + " exportés !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importTable(Stage stage, String title, String insertSql, int colCount) {
        File file = getOpenFile(stage, title);
        if (file == null) return;
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis);
             Connection conn = DriverManager.getConnection(DB_URL)) {

            Sheet sheet = wb.getSheetAt(0);
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row r = sheet.getRow(i);
                    if (r == null) continue;
                    for (int j = 0; j < colCount; j++) {
                        Cell c = r.getCell(j + 1); // on saute l'id
                        if (c == null) {
                            ps.setObject(j + 1, null);
                        } else {
                            switch (c.getCellType()) {
                                case STRING -> ps.setString(j + 1, c.getStringCellValue());
                                case NUMERIC -> ps.setDouble(j + 1, c.getNumericCellValue());
                                case BOOLEAN -> ps.setBoolean(j + 1, c.getBooleanCellValue());
                                default -> ps.setObject(j + 1, null);
                            }
                        }
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            System.out.println(title + " importés !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------- PRODUITS -----------
    public void exportProduits(Stage stage) {
        exportTable(stage, "Exporter Produits",
                "SELECT * FROM produits",
                "Produits",
                new String[]{"id","nomCommercial","nomGenerique","forme","dosage","conditionnement","fabricant","codeBarres","prixVente","prixAchat","statut","categorie","prescriptionRequise","dateExpiration","numeroLot","stock","seuilAlerte"});
    }
    public void importProduits(Stage stage) {
        importTable(stage, "Importer Produits",
                "INSERT INTO produits(nomCommercial,nomGenerique,forme,dosage,conditionnement,fabricant,codeBarres,prixVente,prixAchat,statut,categorie,prescriptionRequise,dateExpiration,numeroLot,stock,seuilAlerte) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                16);
    }

    // ----------- EMPLOYES -----------
    public void exportEmployes(Stage stage) {
        exportTable(stage, "Exporter Employés",
                "SELECT * FROM employes",
                "Employes",
                new String[]{"id","nomComplet","role","login","motDePasseHash","permissions"});
    }
    public void importEmployes(Stage stage) {
        importTable(stage, "Importer Employés",
                "INSERT INTO employes(nomComplet,role,login,motDePasseHash,permissions) VALUES(?,?,?,?,?)",
                5);
    }

    // ----------- CLIENTS -----------
    public void exportClients(Stage stage) {
        exportTable(stage, "Exporter Clients",
                "SELECT * FROM clients",
                "Clients",
                new String[]{"id","nomComplet","dateNaissance","adresse","telephone","email","conditionsMedicales","allergies"});
    }
    public void importClients(Stage stage) {
        importTable(stage, "Importer Clients",
                "INSERT INTO clients(nomComplet,dateNaissance,adresse,telephone,email,conditionsMedicales,allergies) VALUES(?,?,?,?,?,?,?)",
                7);
    }

    // ----------- FOURNISSEURS -----------
    public void exportFournisseurs(Stage stage) {
        exportTable(stage, "Exporter Fournisseurs",
                "SELECT * FROM fournisseurs",
                "Fournisseurs",
                new String[]{"id","nom","contact","telephone","email","adresse","conditionsPaiement"});
    }
    public void importFournisseurs(Stage stage) {
        importTable(stage, "Importer Fournisseurs",
                "INSERT INTO fournisseurs(nom,contact,telephone,email,adresse,conditionsPaiement) VALUES(?,?,?,?,?,?)",
                6);
    }

    // ----------- FACTURES -----------
    public void exportFactures(Stage stage) {
        exportTable(stage, "Exporter Factures",
                "SELECT * FROM factures",
                "Factures",
                new String[]{"id","clientId","employeId","date","montantTotal","modePaiement"});
    }
    public void importFactures(Stage stage) {
        importTable(stage, "Importer Factures",
                "INSERT INTO factures(clientId,employeId,date,montantTotal,modePaiement) VALUES(?,?,?,?,?)",
                5);
    }

    // ----------- TRANSACTIONS -----------
    public void exportTransactions(Stage stage) {
        exportTable(stage, "Exporter Transactions",
                "SELECT * FROM table_transactions",
                "Transactions",
                new String[]{"id","dateHeure","total","statutPaiement","methodePaiement","clientId","employeId"});
    }
    public void importTransactions(Stage stage) {
        importTable(stage, "Importer Transactions",
                "INSERT INTO table_transactions(dateHeure,total,statutPaiement,methodePaiement,clientId,employeId) VALUES(?,?,?,?,?,?)",
                6);
    }

    // ----------- RECETTES -----------
    public void exportRecettes(Stage stage) {
        exportTable(stage, "Exporter Recettes",
                "SELECT * FROM recettes",
                "Recettes",
                new String[]{"id","date","montant","periode"});
    }
    public void importRecettes(Stage stage) {
        importTable(stage, "Importer Recettes",
                "INSERT INTO recettes(date,montant,periode) VALUES(?,?,?)",
                3);
    }
}






