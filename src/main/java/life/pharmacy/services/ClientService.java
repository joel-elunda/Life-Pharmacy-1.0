package life.pharmacy.services;

import life.pharmacy.models.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientService {

    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(Client c) throws SQLException {
        String sql = "INSERT INTO clients (nomComplet, adresse, telephone, email, conditionsMedicales, allergies) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, c.getNomComplet());
            pstmt.setString(2, c.getAdresse());
            pstmt.setString(3, c.getTelephone());
            pstmt.setString(4, c.getEmail());
            pstmt.setString(5, c.getConditionsMedicales());
            pstmt.setString(6, c.getAllergies());
            pstmt.executeUpdate();
        }
    }

    public void update(Client c) throws SQLException {
        String sql = "UPDATE clients SET nomComplet=?, adresse=?, telephone=?, email=?, conditionsMedicales=?, allergies=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getNomComplet());
            pstmt.setString(2, c.getAdresse());
            pstmt.setString(3, c.getTelephone());
            pstmt.setString(4, c.getEmail());
            pstmt.setString(5, c.getConditionsMedicales());
            pstmt.setString(6, c.getAllergies());
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
                        rs.getString("nomComplet"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("conditionsMedicales"),
                        rs.getString("allergies")
                );
                list.add(c);
            }
        }
        return list;
    }

    public List<Client> search(String query) throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE nomComplet LIKE ? OR telephone LIKE ? OR email LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            pstmt.setString(3, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Client c = new Client(
                        rs.getInt("id"),
                        rs.getString("nomComplet"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("conditionsMedicales"),
                        rs.getString("allergies")
                );
                list.add(c);
            }
        }
        return list;
    }

    public int getNextId() {
        try {
            List<Client> clients = getAll();
            return clients.stream()
                    .mapToInt(Client::getId)
                    .max()
                    .orElse(0) + 1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch clients for ID generation", e);
        }
    }

    public boolean ifExists(Client client) {
        try {
            List<Client> clients = getAll();
            for (Client c : clients) {
                if(c.getNomComplet().equals(client.getNomComplet()) ||  c.getEmail().equals(client.getEmail())  ||   c.getTelephone().equals(client.getTelephone()) ) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
