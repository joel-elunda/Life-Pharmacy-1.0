package life.pharmacy.services;

import life.pharmacy.models.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class FactureService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(Facture f) throws SQLException {

        String sql = "INSERT INTO factures (clientId, employedId, date, montantTotal, modePaiement) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, String.valueOf(f.getClient().getId()));
            pstmt.setString(2, String.valueOf(f.getEmploye().getId()));
            pstmt.setString(3, String.valueOf(f.getDate()));
            pstmt.setString(4, String.valueOf(f.getMontantTotal()));
            pstmt.setString(5, f.getModePaiement());
            pstmt.executeUpdate();
        }
    }

    // Ici, on mappe la table 'transaction' comme 'facture' (vente = facture)
    public List<Facture> getAll() throws SQLException {
        List<Facture> list = new ArrayList<>();
        String sql = """
            SELECT t.id, t.clientId, t.employeId, t.dateHeure, t.total, t.statutPaiement, t.methodePaiement,
                   c.nomComplet AS clientNom, e.nomComplet AS employeNom
            FROM table_transactions t
            LEFT JOIN clients c ON c.id=t.clientId
            LEFT JOIN employes e ON e.id=t.employeId
            ORDER BY t.id DESC
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Client cli = new Client(); cli.setId(rs.getInt("clientId")); cli.setNomComplet(rs.getString("clientNom"));
                Employe emp = new Employe(); emp.setId(rs.getInt("employeId")); emp.setNomComplet(rs.getString("employeNom"));
                LocalDateTime dateTime = LocalDateTime.parse(rs.getString("dateHeure"));
                LocalDate date = dateTime.toLocalDate();
                Facture f = new Facture(
                        rs.getInt("id"),
                        cli,
                        emp,
                        date,
                        rs.getDouble("total"),
                        rs.getString("methodePaiement")
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
            SELECT t.id, t.clientId, t.employeId, t.dateHeure, t.total, t.statutPaiement,
                   c.nomComplet AS clientNom, e.nomComplet AS employeNom
            FROM table_transactions t
            LEFT JOIN clients c ON c.id=t.clientId
            LEFT JOIN employes e ON e.id=t.employeId
            WHERE CAST(t.id AS TEXT) LIKE ? OR c.nomComplet LIKE ?
            ORDER BY t.id DESC
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + q + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Client cli = new Client(); cli.setId(rs.getInt("clientId")); cli.setNomComplet(rs.getString("client_nom"));
                Employe emp = new Employe(); emp.setId(rs.getInt("employeId")); emp.setNomComplet(rs.getString("employe_nom"));
                Facture f = new Facture(
                        rs.getInt("id"),
                        cli,
                        emp,
                        rs.getTimestamp("date").toLocalDateTime().toLocalDate(),
                        rs.getDouble("montantTotal"),
                        rs.getString("methodePaiement")
                );
                list.add(f);
            }
        }
        return list;
    }

    // Enregistrement d'une facture (via table transaction)
    public int saveAsTransaction(Facture facture) throws SQLException {
        String sql = "INSERT INTO table_transactions(clientId, employeId, dateHeure, total, statutPaiement) VALUES(?,?,?,?,?)";
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

    public void update(Facture f) throws SQLException {
        String sql = "UPDATE factures SET clientId=?, employedId=?, date=?, montantTotal=?, modePaiement=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, f.getClient().getId() + "");
            pstmt.setString(2, f.getEmploye().getId()  + "");
            pstmt.setString(3, String.valueOf(f.getDate()));
            pstmt.setString(4, String.valueOf(f.getMontantTotal()));
            pstmt.setString(5, f.getModePaiement());
            pstmt.setInt(8, f.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM factures WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public int getNextId() {
        try {
            List<Facture> factures = getAll();
            return factures.stream()
                    .mapToInt(Facture::getId)
                    .max()
                    .orElse(0) + 1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch invoices for ID generation", e);
        }
    }

    private List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT id, nomComplet FROM clients ORDER BY nomComplet";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Client c = new Client();
                c.setId(rs.getInt("id"));
                c.setNomComplet(rs.getString("nomComplet"));
                clients.add(c);
            }
        }
        return clients;
    }

    private List<Employe> getAllEmployes() throws SQLException {
        List<Employe> employes = new ArrayList<>();
        String sql = "SELECT id, nomComplet FROM employes ORDER BY nomComplet";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Employe e = new Employe();
                e.setId(rs.getInt("id"));
                e.setNomComplet(rs.getString("nomComplet"));
                employes.add(e);
            }
        }
        return employes;
    }

    public Client getClientByName(String name) {
        try {
            for (Client c : getAllClients()) {
                if (c.getNomComplet().equals(name)) {
                    return c;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch client by name", e);
        }
        return null;
    }

    public Employe getEmployeByName(String name) {
        try {
            for (Employe e : getAllEmployes()) {
                if (e.getNomComplet().equals(name)) {
                    return e;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch employe by name", ex);
        }
        return null;
    }
}