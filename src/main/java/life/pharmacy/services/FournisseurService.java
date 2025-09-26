package life.pharmacy.services;

import life.pharmacy.models.Fournisseur;
import java.sql.*;
import java.util.*;

public class FournisseurService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

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

    public boolean ifExists(Fournisseur fournisseur) {
        try{
            List<Fournisseur> fournisseurs = getAll();
            for(Fournisseur f : fournisseurs) {
                if (f.getNom().equals(fournisseur.getNom()) || f.getTelephone().equals(fournisseur.getTelephone()))
                    return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}