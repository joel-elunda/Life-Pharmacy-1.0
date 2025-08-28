package life.pharmacy.services;


import life.pharmacy.models.Employe;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

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
}
