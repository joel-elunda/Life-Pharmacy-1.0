package life.pharmacy.services;

import life.pharmacy.models.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class FactureService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";
    private List<Facture> factures = new ArrayList<>();

    // Ici, on mappe la table 'transaction' comme 'facture' (vente = facture)
    public List<Facture> getAll() throws SQLException {
        List<Facture> list = new ArrayList<>();
        String sql = """
            SELECT t.id, t.client_id, t.employe_id, t.date_transaction, t.montant_total, t.methode_paiement,
                   c.nom_complet AS client_nom, e.nom_complet AS employe_nom
            FROM table_transactions t
            LEFT JOIN clients c ON c.id=t.client_id
            LEFT JOIN employes e ON e.id=t.employe_id
            ORDER BY t.id DESC
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Client cli = new Client(); cli.setId(rs.getInt("client_id")); cli.setNomComplet(rs.getString("client_nom"));
                Employe emp = new Employe(); emp.setId(rs.getInt("employe_id")); emp.setNomComplet(rs.getString("employe_nom"));
                Facture f = new Facture(
                        rs.getInt("id"),
                        cli,
                        emp,
                        rs.getTimestamp("date_transaction").toLocalDateTime().toLocalDate(),
                        rs.getDouble("montant_total"),
                        rs.getString("methode_paiement")
                );
                list.add(f);
            }
        }
        return list;
    }

    public List<Facture> search(String q) throws SQLException {
        if (q == null || q.isBlank()) return getAll();
        List<Facture> list = new ArrayList<>();
        String sql = """
            SELECT t.id, t.client_id, t.employe_id, t.date_transaction, t.montant_total, t.methode_paiement,
                   c.nom_complet AS client_nom, e.nom_complet AS employe_nom
            FROM table_transactions t
            LEFT JOIN clients c ON c.id=t.client_id
            LEFT JOIN employes e ON e.id=t.employe_id
            WHERE CAST(t.id AS TEXT) LIKE ? OR c.nom_complet LIKE ?
            ORDER BY t.id DESC
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + q + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Client cli = new Client(); cli.setId(rs.getInt("client_id")); cli.setNomComplet(rs.getString("client_nom"));
                Employe emp = new Employe(); emp.setId(rs.getInt("employe_id")); emp.setNomComplet(rs.getString("employe_nom"));
                Facture f = new Facture(
                        rs.getInt("id"),
                        cli,
                        emp,
                        rs.getTimestamp("date_transaction").toLocalDateTime().toLocalDate(),
                        rs.getDouble("montant_total"),
                        rs.getString("methode_paiement")
                );
                list.add(f);
            }
        }
        return list;
    }

    // Enregistrement d'une facture (via table transaction)
    public int saveAsTransaction(Facture facture) throws SQLException {
        String sql = "INSERT INTO table_transactions(client_id, employe_id, date_transaction, montant_total, methode_paiement) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, facture.getClient() != null ? facture.getClient().getId() : 0);
            ps.setInt(2, facture.getEmploye() != null ? facture.getEmploye().getId() : 0);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setDouble(4, facture.getMontantTotal());
            ps.setString(5, facture.getModePaiement());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return 0;
    }

    // === EXPORT EXCEL ===
    public void exportToFile(String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Factures");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Client ID");
            header.createCell(2).setCellValue("Employé ID");
            header.createCell(3).setCellValue("Date");
            header.createCell(4).setCellValue("Montant Total");
            header.createCell(5).setCellValue("Mode Paiement");

            int rowNum = 1;
            for (Facture f : factures) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(f.getId());
                row.createCell(1).setCellValue(f.getClient().getId());
                row.createCell(2).setCellValue(f.getEmploye().getId());
                row.createCell(3).setCellValue(f.getDate().toString());
                row.createCell(4).setCellValue(f.getMontantTotal());
                row.createCell(5).setCellValue(f.getModePaiement());
            }

            try (FileOutputStream fos = new FileOutputStream(filename)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // === IMPORT EXCEL ===
    public void importFromFile(String filename) {
        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            factures.clear();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
//                    public Facture(int id, Client client, Employe employe, LocalDate date, double montantTotal, String modePaiement) {
                    Facture f = new Facture(
                            (int) row.getCell(0).getNumericCellValue(),
                            new Client((int) row.getCell(1).getNumericCellValue(), "", null, "", "", "", "", ""),
                            new Employe((int) row.getCell(2).getNumericCellValue(), "", "", "", "", ""),
                            LocalDate.parse(row.getCell(3).getStringCellValue()),
                            row.getCell(4).getNumericCellValue(),
                            row.getCell(5).getStringCellValue()
                    );
                    factures.add(f);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}