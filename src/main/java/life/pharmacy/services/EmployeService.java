package life.pharmacy.services;


import life.pharmacy.models.Employe;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";
    private List<Employe> employes = new ArrayList<>();

    public void add(Employe e) throws SQLException {
        String sql = "INSERT INTO employes (nom_complet, role, login, mot_de_passe, permissions) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.getNomComplet());
            pstmt.setString(2, e.getRole());
            pstmt.setString(3, e.getLogin());
            pstmt.setString(4, e.getMotDePasseHash());
            pstmt.setString(5, e.getPermissions());
            pstmt.executeUpdate();
        }
    }

    public void update(Employe e) throws SQLException {
        String sql = "UPDATE employes SET nom_complet=?, role=?, login=?, mot_de_passe=?, permissions=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.getNomComplet());
            pstmt.setString(2, e.getRole());
            pstmt.setString(3, e.getLogin());
            pstmt.setString(4, e.getMotDePasseHash());
            pstmt.setString(5, e.getPermissions());
            pstmt.setInt(6, e.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM employes WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Employe> getAll() throws SQLException {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM employes";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Employe e = new Employe(
                        rs.getInt("id"),
                        rs.getString("nom_complet"),
                        rs.getString("role"),
                        rs.getString("login"),
                        rs.getString("mot_de_passe"),
                        rs.getString("permissions")
                );
                list.add(e);
            }
        }
        return list;
    }

    public List<Employe> search(String query) throws SQLException {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM employes WHERE nom_complet LIKE ? OR role LIKE ? OR login LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            pstmt.setString(3, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Employe e = new Employe(
                        rs.getInt("id"),
                        rs.getString("nom_complet"),
                        rs.getString("role"),
                        rs.getString("login"),
                        rs.getString("mot_de_passe"),
                        rs.getString("permissions")
                );
                list.add(e);
            }
        }
        return list;
    }

    // === EXPORT EXCEL ===
    public void exportToFile(String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employes");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nom Complet");
            header.createCell(2).setCellValue("Rôle");
            header.createCell(3).setCellValue("Login");
            header.createCell(4).setCellValue("Mot de Passe Hash");
            header.createCell(5).setCellValue("Permissions");

            int rowNum = 1;
            for (Employe e : employes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(e.getNomComplet());
                row.createCell(2).setCellValue(e.getRole());
                row.createCell(3).setCellValue(e.getLogin());
                row.createCell(4).setCellValue(e.getMotDePasseHash());
                row.createCell(5).setCellValue(e.getPermissions());
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
            employes.clear();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Employe e = new Employe(
                            (int) row.getCell(0).getNumericCellValue(),
                            row.getCell(1).getStringCellValue(),
                            row.getCell(2).getStringCellValue(),
                            row.getCell(3).getStringCellValue(),
                            row.getCell(4).getStringCellValue(),
                            row.getCell(5).getStringCellValue()
                    );
                    employes.add(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
