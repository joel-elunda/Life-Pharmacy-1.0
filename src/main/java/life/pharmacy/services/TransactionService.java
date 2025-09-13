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
        String sql = "INSERT INTO table_transactions(dateHeure, total, statutPaiement, methodePaiement, clientId, employeId ) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(t.getDateHeure()));
            ps.setDouble(2, t.getTotal());
            ps.setString(3, t.getStatutPaiement());
            ps.setString(4, t.getMethodePaiement());
            ps.setInt(5, t.getClientId());
            ps.setInt(6, t.getEmployeId());

            ps.executeUpdate();
        }
    }

    public void update(Transaction t) throws SQLException {
        String sql = "UPDATE table_transactions SET  dateHeure=?, total=?, statutPaiement=?, methodePaiement=?, clientId=?, employeId=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(t.getDateHeure()));
            ps.setDouble(2, t.getTotal());
            ps.setString(3, t.getStatutPaiement());
            ps.setString(4, t.getMethodePaiement());
            ps.setInt(5, t.getClientId());
            ps.setInt(6, t.getEmployeId());
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

    public List<Transaction> getAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM table_transactions")) {
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        LocalDateTime.parse(rs.getString("dateHeure")),
                        rs.getDouble("total"),
                        rs.getString("statutPaiement"),
                        rs.getString("methodePaiement"),
                        rs.getInt("clientId"),
                        rs.getInt("employeId")
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