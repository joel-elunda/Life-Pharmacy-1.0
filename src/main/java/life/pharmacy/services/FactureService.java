package life.pharmacy.services;

import life.pharmacy.models.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class FactureService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    // Ici, on mappe la table 'transaction' comme 'facture' (vente = facture)
    public List<Facture> getAll() throws SQLException {
        List<Facture> list = new ArrayList<>();
        String sql = """
            SELECT t.id, t.client_id, t.employe_id, t.date_transaction, t.montant_total, t.methode_paiement,
                   c.nom_complet AS client_nom, e.nom_complet AS employe_nom
            FROM transaction t
            LEFT JOIN client c ON c.id=t.client_id
            LEFT JOIN employe e ON e.id=t.employe_id
            ORDER BY t.id DESC
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Client cli = new Client(); cli.setId(rs.getInt("client_id")); cli.setNomComplet(rs.getString("client_nom"));
                Employe emp = new Employe(); emp.setId(rs.getInt("employe_id")); emp.setNomComplet(rs.getString("employe_nom"));
                Facture f = new Facture(
                        rs.getInt("id"),
                        cli,
                        emp,
                        rs.getTimestamp("date_transaction").toLocalDateTime().toLocalDate(),
                        rs.getDouble("montant_total"),
                        rs.getString("methode_paiement")
                );
                list.add(f);
            }
        }
        return list;
    }

    public List<Facture> search(String q) throws SQLException {
        if (q == null || q.isBlank()) return getAll();
        List<Facture> list = new ArrayList<>();
        String sql = """
            SELECT t.id, t.client_id, t.employe_id, t.date_transaction, t.montant_total, t.methode_paiement,
                   c.nom_complet AS client_nom, e.nom_complet AS employe_nom
            FROM transaction t
            LEFT JOIN client c ON c.id=t.client_id
            LEFT JOIN employe e ON e.id=t.employe_id
            WHERE CAST(t.id AS TEXT) LIKE ? OR c.nom_complet LIKE ?
            ORDER BY t.id DESC
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + q + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Client cli = new Client(); cli.setId(rs.getInt("client_id")); cli.setNomComplet(rs.getString("client_nom"));
                Employe emp = new Employe(); emp.setId(rs.getInt("employe_id")); emp.setNomComplet(rs.getString("employe_nom"));
                Facture f = new Facture(
                        rs.getInt("id"),
                        cli,
                        emp,
                        rs.getTimestamp("date_transaction").toLocalDateTime().toLocalDate(),
                        rs.getDouble("montant_total"),
                        rs.getString("methode_paiement")
                );
                list.add(f);
            }
        }
        return list;
    }

    // Enregistrement d'une facture (via table transaction)
    public int saveAsTransaction(Facture facture) throws SQLException {
        String sql = "INSERT INTO transaction(client_id, employe_id, date_transaction, montant_total, methode_paiement) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, facture.getClient() != null ? facture.getClient().getId() : 0);
            ps.setInt(2, facture.getEmploye() != null ? facture.getEmploye().getId() : 0);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setDouble(4, facture.getMontantTotal());
            ps.setString(5, facture.getModePaiement());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return 0;
    }
}