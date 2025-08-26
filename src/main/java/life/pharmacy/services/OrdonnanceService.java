package life.pharmacy.services;

import life.pharmacy.models.Ordonnance;
import java.sql.*;
import java.util.*;

public class OrdonnanceService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(Ordonnance o) throws SQLException {
        String sql = "INSERT INTO ordonnance(client_id, medecin, date_prescription, details) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, o.getId());
            ps.setInt(2, o.getPatientId());
            ps.setString(3, o.getMedecin());
            ps.setString(4, o.getDateEmission().toString());
            ps.setString(5, o.getProduitsPrescrits());
            ps.setString(6, o.getInstructionsDosage());
            ps.setString(7, o.getStatut());
            ps.setString(8, o.getNumeroUnique());
            ps.executeUpdate();
        }
    }
    /*
    *  public Ordonnance(int id, int patientId, String medecin, LocalDate dateEmission, LocalDate dateExpiration,
                      String produitsPrescrits, String instructionsDosage, String statut, String numeroUnique) {*/
    public void update(Ordonnance o) throws SQLException {
        String sql = "UPDATE ordonnance SET client_id=?, medecin=?, date_prescription=?, details=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, o.getId());
            ps.setInt(2, o.getPatientId());
            ps.setString(3, o.getMedecin());
            ps.setString(4, o.getDateEmission().toString());
            ps.setString(5, o.getProduitsPrescrits());
            ps.setString(6, o.getInstructionsDosage());
            ps.setString(7, o.getStatut());
            ps.setString(8, o.getNumeroUnique());

            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM ordonnance WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /*
        *  public Ordonnance(int id, int patientId, String medecin, LocalDate dateEmission, LocalDate dateExpiration,
                          String produitsPrescrits, String instructionsDosage, String statut, String numeroUnique) {*/
    public List<Ordonnance> getAll() throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM ordonnance")) {
            while (rs.next()) {
                list.add(new Ordonnance(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("medecin"),
                        rs.getDate("date_prescription").toLocalDate(),
                        rs.getDate("date_expiration") != null ? rs.getDate("date_expiration").toLocalDate() : null,
                        rs.getString("produits_prescrits"),
                        rs.getString("instructions_dosage"),
                        rs.getString("statut"),
                        rs.getString("numero_unique")
                ));
            }
        }
        return list;
    }
}