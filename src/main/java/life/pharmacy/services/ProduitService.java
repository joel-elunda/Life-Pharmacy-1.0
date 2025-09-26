package life.pharmacy.services;

import life.pharmacy.models.Produit;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProduitService {

    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(Produit p) throws SQLException {
        String sql = "INSERT INTO produits(nomCommercial, description, forme, dosage, conditionnement, " +
                "prixVente, prixAchat, statut, categorie, prescriptionRequise, dateExpiration, stock, seuilAlerte) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNomCommercial());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getForme());
            ps.setString(4, p.getDosage());
            ps.setString(5, p.getConditionnement());
            ps.setDouble(6, p.getPrixVente());
            ps.setDouble(7, p.getPrixAchat());
            ps.setString(8, p.getStatut());
            ps.setString(9, p.getCategorie());
            ps.setBoolean(10, p.isPrescriptionRequise());
            ps.setString(11,
                    p.getDateExpiration() != null ? p.getDateExpiration().toString() : null
            );
            ps.setInt(12, p.getStock());
            ps.setInt(13, p.getSeuilAlerte());

            ps.executeUpdate();
        }
    }

    public void update(Produit p) throws SQLException {
        String sql = "UPDATE produits SET nomCommercial=?, description=?, forme=?, dosage=?, conditionnement=?, prixVente=?, prixAchat=?, statut=?, categorie=?, prescriptionRequise=?, dateExpiration=?, stock=?, seuilAlerte=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNomCommercial());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getForme());
            ps.setString(4, p.getDosage());
            ps.setString(5, p.getConditionnement());
            ps.setDouble(6, p.getPrixVente());
            ps.setDouble(7, p.getPrixAchat());
            ps.setString(8, p.getStatut());
            ps.setString(9, p.getCategorie());
            ps.setBoolean(10, p.isPrescriptionRequise());
            ps.setString(11,
                    p.getDateExpiration() != null ? p.getDateExpiration().toString() : null
            );
            ps.setInt(12, p.getStock());
            ps.setInt(13, p.getSeuilAlerte());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM produits WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Produit> getAll() throws SQLException {
        List<Produit> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM produits")) {
            while (rs.next()) {
                String dateStr = rs.getString("dateExpiration"); // ← LIRE COMME STRING
                LocalDate dateExp = null;
                if (dateStr != null && !dateStr.isBlank()) {
                    // parse "YYYY-MM-DD"
                    dateExp = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                }
                list.add(new Produit(
                        rs.getInt("id"),
                        rs.getString("nomCommercial"),
                        rs.getString("description"),
                        rs.getString("forme"),
                        rs.getString("dosage"),
                        rs.getString("conditionnement"),
                        rs.getDouble("prixVente"),
                        rs.getDouble("prixAchat"),
                        rs.getString("statut"),
                        rs.getString("categorie"),
                        rs.getBoolean("prescriptionRequise"),
                        dateExp, // ← LocalDate
                        rs.getInt("stock"),
                        rs.getInt("seuilAlerte")
                ));
            }
        }
        return list;
    }

    public List<Produit> search(String query) throws SQLException {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produits WHERE nomCommercial LIKE ?  OR categorie LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String dateStr = rs.getString("dateExpiration"); // ← LIRE COMME STRING
                LocalDate dateExp = null;
                if (dateStr != null && !dateStr.isBlank()) {
                    // parse "YYYY-MM-DD"
                    dateExp = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                }
                list.add(new Produit(
                        rs.getInt("id"),
                        rs.getString("nomCommercial"),
                        rs.getString("description"),
                        rs.getString("forme"),
                        rs.getString("dosage"),
                        rs.getString("conditionnement"),
                        rs.getDouble("prixVente"),
                        rs.getDouble("prixAchat"),
                        rs.getString("statut"),
                        rs.getString("categorie"),
                        rs.getBoolean("prescriptionRequise"),
                        dateExp, // ← LocalDate
                        rs.getInt("stock"),
                        rs.getInt("seuilAlerte")
                ));
            }
        }
        return list;
    }

    public int getNextId() {
        try {
            List<Produit> p = getAll();
            return p.stream()
                    .mapToInt(Produit::getId)
                    .max()
                    .orElse(0) + 1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch products for ID generation", e);
        }
    }

    public boolean ifExists(Produit produit) {
        try{
            List<Produit> produits = getAll();
            for(Produit p : produits) {
                if(p.getNomCommercial().equals(produit.getNomCommercial()))
                    return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}