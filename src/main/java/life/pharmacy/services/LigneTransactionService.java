package life.pharmacy.services;

import life.pharmacy.models.LigneTransaction;
import java.sql.*;
import java.util.*;

public class LigneTransactionService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(LigneTransaction lt) throws SQLException {
        String sql = "INSERT INTO ligne_transaction(transaction_id, produit_id, quantite, prix_unitaire) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lt.getTransactionId());
            ps.setInt(2, lt.getProduitId());
            ps.setInt(3, lt.getQuantite());
            ps.setDouble(4, lt.getPrixUnitaire());
            ps.executeUpdate();
        }
    }

    public void update(LigneTransaction lt) throws SQLException {
        String sql = "UPDATE ligne_transaction SET transaction_id=?, produit_id=?, quantite=?, prix_unitaire=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lt.getTransactionId());
            ps.setInt(2, lt.getProduitId());
            ps.setInt(3, lt.getQuantite());
            ps.setDouble(4, lt.getPrixUnitaire());
            ps.setInt(5, lt.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM ligne_transaction WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<LigneTransaction> getAll() throws SQLException {
        List<LigneTransaction> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM ligne_transaction")) {
            while (rs.next()) {
                list.add(new LigneTransaction(
                        rs.getInt("id"),
                        rs.getInt("transaction_id"),
                        rs.getInt("produit_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire")
                ));
            }
        }
        return list;
    }
}
