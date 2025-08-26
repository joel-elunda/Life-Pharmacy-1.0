package life.pharmacy.services;

import life.pharmacy.models.Client;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(Client c) throws SQLException {
        String sql = "INSERT INTO client(nom_complet, date_naissance, adresse, telephone, email, conditions_medicales, allergies) VALUES(?,?,?,?,?,?,?)";
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
        String sql = "UPDATE client SET nom_complet=?, date_naissance=?, adresse=?, telephone=?, email=?, conditions_medicales=?, allergies=? WHERE id=?";
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
        String sql = "DELETE FROM client WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Client> getAll() throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM client";
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
        String sql = "SELECT * FROM client WHERE nom_complet LIKE ? OR telephone LIKE ? OR email LIKE ?";
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
}
