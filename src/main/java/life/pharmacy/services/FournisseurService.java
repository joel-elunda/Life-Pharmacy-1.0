package life.pharmacy.services;

import life.pharmacy.models.Fournisseur;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class FournisseurService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";
    private final List<Fournisseur> fournisseurs = new ArrayList<>();

    public void add(Fournisseur f) throws SQLException {
        String sql = "INSERT INTO fournisseurs (nom, contact, adresse, email, conditionsPaiement) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getContact());
            ps.setString(3, f.getAdresse());
            ps.setString(4, f.getEmail());
            ps.executeUpdate();
        }
    }

    public void update(Fournisseur f) throws SQLException {
        String sql = "UPDATE fournisseurs SET nom=?, contact=?, adresse=?, email=?, conditionsPaiement=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getContact());
            ps.setString(3, f.getAdresse());
            ps.setString(4, f.getEmail());
            ps.setInt(5, f.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM fournisseurs WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    //int id, String nom, String contact, String telephone, String email, String adresse, String conditionsPaiement
    public List<Fournisseur> getAll() throws SQLException {
        List<Fournisseur> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM fournisseurs")) {
            while (rs.next()) {
                list.add(new Fournisseur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("contact"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("conditionsPaiement")
                ));
            }
        }
        return list;
    }

    public List<Fournisseur> search(String query) throws SQLException {
        List<Fournisseur> list = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs WHERE nom LIKE ? OR contact LIKE ? OR email LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Fournisseur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("contact"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("conditionsPaiement")
                ));
            }
        }
        return list;
    }

    public void exportToFile(String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fournisseurs");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nom");
            header.createCell(2).setCellValue("Téléphone");
            header.createCell(3).setCellValue("Email");
            header.createCell(4).setCellValue("Adresse");

            int rowNum = 1;
            for (Fournisseur f : fournisseurs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(f.getId());
                row.createCell(1).setCellValue(f.getNom());
                row.createCell(2).setCellValue(f.getTelephone());
                row.createCell(3).setCellValue(f.getEmail());
                row.createCell(4).setCellValue(f.getAdresse());
            }

            try (FileOutputStream fos = new FileOutputStream(filename)) {
                workbook.write(fos);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void importFromFile(String filename) {
        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            fournisseurs.clear();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    //public Fournisseur(int id, String nom, String contact, String telephone, String email, String adresse, String conditionsPaiement) {
                    Fournisseur f = new Fournisseur(
                            (int) row.getCell(0).getNumericCellValue(),
                            row.getCell(1).getStringCellValue(),
                            row.getCell(2).getStringCellValue(),
                            row.getCell(3).getStringCellValue(),
                            row.getCell(4).getStringCellValue(),
                            row.getCell(5).getStringCellValue(),
                            row.getCell(6).getStringCellValue()
                    );
                    fournisseurs.add(f);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}