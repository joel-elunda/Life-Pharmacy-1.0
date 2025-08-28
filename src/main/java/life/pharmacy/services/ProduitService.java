package life.pharmacy.services;

import life.pharmacy.models.Produit;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ProduitService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(Produit p) throws SQLException {
        String sql = "INSERT INTO produits(nom, description, prix, quantite_stock, date_expiration, fournisseur_id, categorie) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNomCommercial());
            ps.setString(2, p.getNomGenerique());
            ps.setString(3, p.getForme());
            ps.setString(4, p.getDosage());
            ps.setString(5, p.getConditionnement());
            ps.setString(6, p.getFabricant());
            ps.setString(7, p.getCodeBarres());
            ps.setDouble(8, p.getPrixVente());
            ps.setDouble(9, p.getPrixAchat());
            ps.setString(10, p.getStatut());
            ps.setString(11, p.getCategorie());
            ps.setBoolean(12, p.isPrescriptionRequise());
            ps.setString(13, p.getDateExpiration().toString());
            ps.setString(14, p.getNumeroLot());
            ps.setInt(15, p.getStock());
            ps.setInt(16, p.getSeuilAlerte());

            ps.executeUpdate();
        }
    }

    public void update(Produit p) throws SQLException {
        String sql = "UPDATE produits SET nom=?, description=?, prix=?, quantite_stock=?, date_expiration=?, fournisseur_id=?, categorie=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNomCommercial());
            ps.setString(2, p.getNomGenerique());
            ps.setString(3, p.getForme());
            ps.setString(4, p.getDosage());
            ps.setString(5, p.getConditionnement());
            ps.setString(6, p.getFabricant());
            ps.setString(7, p.getCodeBarres());
            ps.setDouble(8, p.getPrixVente());
            ps.setDouble(9, p.getPrixAchat());
            ps.setString(10, p.getStatut());
            ps.setString(11, p.getCategorie());
            ps.setBoolean(12, p.isPrescriptionRequise());
            ps.setString(13, p.getDateExpiration().toString());
            ps.setString(14, p.getNumeroLot());
            ps.setInt(15, p.getStock());
            ps.setInt(16, p.getSeuilAlerte());
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
                list.add(new Produit(
                        rs.getInt("id"),
                        rs.getString("nom_commercial"),
                        rs.getString("nom_generique"),
                        rs.getString("forme"),
                        rs.getString("dosage"),
                        rs.getString("conditionnement"),
                        rs.getString("fabricant"),
                        rs.getString("codeBarres"),
                        rs.getDouble("prixVente"),
                        rs.getDouble("prixAchat"),
                        rs.getString("statut"),
                        rs.getString("categorie"),
                        rs.getBoolean("prescriptionRequise"),
                        rs.getDate("dateExpiration").toLocalDate(),
                        rs.getString("numeroLot"),
                        rs.getInt("stock"),
                        rs.getInt("seuilAlerte")
                ));
            }
        }
        return list;
    }

    public List<Produit> search(String query) throws SQLException {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produits WHERE nom LIKE ? OR categorie LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Produit(
                        rs.getInt("id"),
                        rs.getString("nom_commercial"),
                        rs.getString("nom_generique"),
                        rs.getString("forme"),
                        rs.getString("dosage"),
                        rs.getString("conditionnement"),
                        rs.getString("fabricant"),
                        rs.getString("codeBarres"),
                        rs.getDouble("prixVente"),
                        rs.getDouble("prixAchat"),
                        rs.getString("statut"),
                        rs.getString("categorie"),
                        rs.getBoolean("prescriptionRequise"),
                        rs.getDate("dateExpiration").toLocalDate(),
                        rs.getString("numeroLot"),
                        rs.getInt("stock"),
                        rs.getInt("seuilAlerte")
                ));
            }
        }
        return list;
    }
}