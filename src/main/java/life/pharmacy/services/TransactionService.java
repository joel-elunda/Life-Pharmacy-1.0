package life.pharmacy.services;

import life.pharmacy.models.Transaction;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import java.sql.*;
import java.util.*;

public class TransactionService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    //int id, LocalDateTime dateHeure, double total, String statutPaiement, String methodePaiement, int clientId, int employeId)
    public void add(Transaction t) throws SQLException {
        String sql = "INSERT INTO table_transactions(client_id, employe_id, date_transaction, montant_total, methode_paiement) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getId());
            ps.setString(2, String.valueOf(t.getDateHeure()));
            ps.setDouble(3, t.getTotal());
            ps.setString(4, t.getStatutPaiement());
            ps.setString(5, t.getMethodePaiement());
            ps.setInt(6, t.getClientId());
            ps.setInt(7, t.getEmployeId());

            ps.executeUpdate();
        }
    }

    public void update(Transaction t) throws SQLException {
        String sql = "UPDATE table_transactions SET client_id=?, employe_id=?, date_transaction=?, montant_total=?, methode_paiement=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getId());
            ps.setString(2, String.valueOf(t.getDateHeure()));
            ps.setDouble(3, t.getTotal());
            ps.setString(4, t.getStatutPaiement());
            ps.setString(5, t.getMethodePaiement());
            ps.setInt(6, t.getClientId());
            ps.setInt(7, t.getEmployeId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM table_transactions WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
    //public Transaction(int id, LocalDateTime dateHeure, double total, String statutPaiement, String methodePaiement, int clientId, int employeId)
    public List<Transaction> getAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM table_transactions")) {
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        LocalDateTime.parse(rs.getString("date_transaction")),
                        rs.getDouble("montant_total"),
                        rs.getString("statut_paiement"),
                        rs.getString("methode_paiement"),
                        rs.getInt("client_id"),
                        rs.getInt("employe_id")
                ));
            }
        }
        return list;
    }

    public int getLastInsertId() throws SQLException {
        String sql = "SELECT IFNULL(MAX(id),0) AS id FROM table_transactions";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt("id") : 0;
        }
    }
}