package life.pharmacy.services;

import life.pharmacy.models.Client;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientService {

    private final List<Client> clients = new ArrayList<>();


    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(Client c) throws SQLException {
        String sql = "INSERT INTO clients (nom_complet, date_naissance, adresse, telephone, email, conditions_medicales, allergies) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, c.getNomComplet());
            pstmt.setString(2, c.getDateNaissance().toString());
            pstmt.setString(3, c.getAdresse());
            pstmt.setString(4, c.getTelephone());
            pstmt.setString(5, c.getEmail());
            pstmt.setString(6, c.getConditionsMedicales());
            pstmt.setString(7, c.getAllergies());
            pstmt.executeUpdate();
        }
    }

    public void update(Client c) throws SQLException {
        String sql = "UPDATE clients SET nom_complet=?, date_naissance=?, adresse=?, telephone=?, email=?, conditions_medicales=?, allergies=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getNomComplet());
            pstmt.setString(2, c.getDateNaissance().toString());
            pstmt.setString(3, c.getAdresse());
            pstmt.setString(4, c.getTelephone());
            pstmt.setString(5, c.getEmail());
            pstmt.setString(6, c.getConditionsMedicales());
            pstmt.setString(7, c.getAllergies());
            pstmt.setInt(8, c.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM clients WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Client> getAll() throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Client c = new Client(
                        rs.getInt("id"),
                        rs.getString("nom_complet"),
                        LocalDate.parse(rs.getString("date_naissance")),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("conditions_medicales"),
                        rs.getString("allergies")
                );
                list.add(c);
            }
        }
        return list;
    }

    public List<Client> search(String query) throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE nom_complet LIKE ? OR telephone LIKE ? OR email LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            pstmt.setString(3, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Client c = new Client(
                        rs.getInt("id"),
                        rs.getString("nom_complet"),
                        LocalDate.parse(rs.getString("date_naissance")),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("conditions_medicales"),
                        rs.getString("allergies")
                );
                list.add(c);
            }
        }
        return list;
    }

    // === EXPORT EXCEL ===
    public void exportToFile(String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clients");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nom Complet");
            header.createCell(2).setCellValue("Téléphone");
            header.createCell(3).setCellValue("Email");

            int rowNum = 1;
            for (Client c : clients) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getNomComplet());
                row.createCell(2).setCellValue(c.getTelephone());
                row.createCell(3).setCellValue(c.getEmail());
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
            clients.clear();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
//                    public Client(int id, String nomComplet, LocalDate dateNaissance,
//                    String adresse, String telephone, String email, String conditionsMedicales, String allergies) {
                    Client c = new Client(
                            (int) row.getCell(0).getNumericCellValue(),
                            row.getCell(1).getStringCellValue(),
                            LocalDate.parse(row.getCell(2).getStringCellValue()),
                            row.getCell(3).getStringCellValue(),
                            row.getCell(4).getStringCellValue(),
                            row.getCell(5).getStringCellValue(),
                            row.getCell(6).getStringCellValue(),
                            row.getCell(7).getStringCellValue()
                    );
                    clients.add(c);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
