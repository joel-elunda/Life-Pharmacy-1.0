package life.pharmacy.services;

import life.pharmacy.models.LigneTransaction;
import java.sql.*;
import java.util.*;

public class LigneTransactionService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(LigneTransaction lt) throws SQLException {
        String sql = "INSERT INTO ligne_transactions(transactionId, produitId, produitNom, quantite, prixUnitaire, sousTotal, numeroOrdonnance) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lt.getTransactionId());
            ps.setInt(2, lt.getProduitId());
            ps.setString(3, lt.getProduitNom());
            ps.setInt(4, lt.getQuantite());
            ps.setDouble(5, lt.getPrixUnitaire());
            ps.setDouble(6, lt.getSousTotal());
            ps.setString(7, lt.getNumeroOrdonnance());
            ps.executeUpdate();
        }
    }

    public void update(LigneTransaction lt) throws SQLException {
        String sql = "UPDATE ligne_transactions SET transactionId=?, produitId=?, produitNom=?, quantite=?, prixUnitaire=?, sousTotal=?, numeroOrdonnance=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lt.getTransactionId());
            ps.setInt(2, lt.getProduitId());
            ps.setString(3, lt.getProduitNom());
            ps.setInt(4, lt.getQuantite());
            ps.setDouble(5, lt.getPrixUnitaire());
            ps.setDouble(6, lt.getSousTotal());
            ps.setString(7, lt.getNumeroOrdonnance());
            ps.setInt(8, lt.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM ligne_transactions WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<LigneTransaction> getAll() throws SQLException {
        List<LigneTransaction> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM ligne_transactions")) {
            while (rs.next()) {
                list.add(new LigneTransaction(
                        rs.getInt("id"),
                        rs.getInt("transactionId"),
                        rs.getInt("produitId"),
                        rs.getString("produitNom"),
                        rs.getInt("quantite"),
                        rs.getDouble("prixUnitaire"),
                        rs.getString("numeroOrdonnance")
                ));
            }
        }
        return list;
    }
}
