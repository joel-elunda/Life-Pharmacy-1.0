package life.pharmacy.services;



import life.pharmacy.models.Employe;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";
    private List<Employe> employes = new ArrayList<>();

    public void add(Employe e) throws SQLException {
        String sql = "INSERT INTO employes (nomComplet, role, login, motDePasseHash, permissions) VALUES(?,?,?,?,?)";
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
        String sql = "UPDATE employes SET nomComplet=?, role=?, login=?, motDePasseHash=?, permissions=? WHERE id=?";
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
                        rs.getString("nomComplet"),
                        rs.getString("role"),
                        rs.getString("login"),
                        rs.getString("motDePasseHash"),
                        rs.getString("permissions")
                );
                list.add(e);
            }
        }
        return list;
    }

    public Employe getByLogin(String query) throws SQLException {
        Employe employe = new Employe();
        String sql = "SELECT id, nomComplet, role, login, motDePasseHash, permissions FROM employes WHERE login=?";
        try(Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, query);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                employe.setId(rs.getInt("id"));
                employe.setNomComplet(rs.getString("nomComplet"));
                employe.setRole(rs.getString("role"));
                employe.setLogin(rs.getString("login"));
                employe.setMotDePasseHash(rs.getString("motDePasseHash"));
                employe.setPermissions(rs.getString("permissions"));
            }
        }
        return employe;
    }

    public List<Employe> search(String query) throws SQLException {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM employes WHERE nomComplet LIKE ? OR role LIKE ? OR login LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            pstmt.setString(3, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Employe e = new Employe(
                        rs.getInt("id"),
                        rs.getString("nomComplet"),
                        rs.getString("role"),
                        rs.getString("login"),
                        rs.getString("motDePasseHash"),
                        rs.getString("permissions")
                );
                list.add(e);
            }
        }
        return list;
    }

    public int getNextId() {
        try {
            List<Employe> employes = getAll();
            return employes.stream()
                    .mapToInt(Employe::getId)
                    .max()
                    .orElse(0) + 1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch clients for ID generation", e);
        }
    }

    public boolean ifExists(Employe employe) {
        try {
            List<Employe> employes = getAll();
            for(Employe e : employes) {
                if (e.getNomComplet().equals(employe.getNomComplet()) || e.getLogin().equals(employe.getLogin()))
                    return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
