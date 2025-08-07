package life.pharmacy.services;

import life.pharmacy.config.Database;
import life.pharmacy.models.Recette;
import life.pharmacy.utils.ExcelImporter;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class RecetteService {

    static {
        // création table si inexistante
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS recettes (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            date TEXT NOT NULL, -- stocke ISO_LOCAL_DATE
                            montant REAL NOT NULL,
                            type TEXT
                        )
                    """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insert(Recette r) {
        String sql = "INSERT INTO recettes(date, montant, type) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getDate().toString());
            ps.setDouble(2, r.getMontant());
            ps.setString(3, r.getType());
            ps.executeUpdate();
            try (ResultSet g = ps.getGeneratedKeys()) {
                if (g.next()) r.setId(g.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Recette> getAll() {
        List<Recette> list = new ArrayList<>();
        String sql = "SELECT * FROM recettes ORDER BY date DESC";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Recette r = new Recette();
                r.setId(rs.getInt("id"));
                r.setDate(LocalDate.parse(rs.getString("date")));
                r.setMontant(rs.getDouble("montant"));
                r.setType(rs.getString("type"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Recette> getByRange(LocalDate start, LocalDate end) {
        List<Recette> list = new ArrayList<>();
        String sql = "SELECT * FROM recettes WHERE date BETWEEN ? AND ? ORDER BY date ASC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start.toString());
            ps.setString(2, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Recette r = new Recette();
                    r.setId(rs.getInt("id"));
                    r.setDate(LocalDate.parse(rs.getString("date")));
                    r.setMontant(rs.getDouble("montant"));
                    r.setType(rs.getString("type"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Aggregation par période.
     * granularity: "DAY", "MONTH", "YEAR"
     * renvoie une liste de PeriodTotal (period string, total)
     */
    public static List<PeriodTotal> getAggregated(LocalDate start, LocalDate end, String granularity) {
        List<PeriodTotal> out = new ArrayList<>();
        String periodExpr;
        String orderExpr;

        switch (granularity.toUpperCase()) {
            case "MONTH":
                // YYYY-MM
                periodExpr = "substr(date,1,7)";
                orderExpr = "period";
                break;
            case "YEAR":
                // YYYY
                periodExpr = "substr(date,1,4)";
                orderExpr = "period";
                break;
            case "DAY":
            default:
                periodExpr = "date";
                orderExpr = "period";
                break;
        }

        String sql = "SELECT " + periodExpr + " AS period, SUM(montant) AS total " +
                "FROM recettes WHERE date BETWEEN ? AND ? " +
                "GROUP BY period ORDER BY " + orderExpr + " ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start.toString());
            ps.setString(2, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String period = rs.getString("period");
                    double total = rs.getDouble("total");
                    out.add(new PeriodTotal(period, total));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    // Petite classe utilitaire renvoyée par getAggregated
    public static class PeriodTotal {
        private final String period;
        private final double total;

        public PeriodTotal(String period, double total) {
            this.period = period;
            this.total = total;
        }

        public String getPeriod() {
            return period;
        }

        public double getTotal() {
            return total;
        }
    }

    public static void importFromExcel(File excelFile) {
        //TODO: Implémenter l'importation des recettes depuis un fichier Excel
        System.out.println("Importation des recettes depuis le fichier : " + excelFile.getAbsolutePath());

        List<List<String>> rows = ExcelImporter.readExcel(excelFile);
        if (rows.size() <= 1) return;

        // En-tête présumé : Date(yyyy-MM-dd) | Montant | Type
        for (int i = 1; i < rows.size(); i++) {
            List<String> r = rows.get(i);
            try {
                LocalDate date = r.size() > 0 && !r.get(0).isEmpty() ? LocalDate.parse(r.get(0)) : LocalDate.now();
                double montant = r.size() > 1 && !r.get(1).isEmpty() ? Double.parseDouble(r.get(1)) : 0.0;
                String type = r.size() > 2 ? r.get(2) : "jour";
                Recette rec = new Recette(0, date, montant, type);
                insert(rec);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ------------- A) Agrégation directe depuis la table factures ----------------

    /**
     * Agrège les montants des factures entre start (incl) et end (incl) selon la granularité.
     * granularity: "DAY", "MONTH", "YEAR"
     * Utilise la colonne factures.date (format ISO_LOCAL_DATE_TIME ou ISO_LOCAL_DATE)
     * et factures.montant_ttc (ou montant_ttc selon ta colonne).
     */
    public static List<PeriodTotal> getAggregatedFromFactures(LocalDate start, LocalDate end, String granularity) {
        List<PeriodTotal> out = new ArrayList<>();
        String periodExpr;

        // On suppose la date stockée en ISO : "YYYY-MM-DD" ou "YYYY-MM-DDTHH:MM:SS"
        switch (granularity.toUpperCase()) {
            case "MONTH":
                // YYYY-MM
                periodExpr = "substr(date,1,7)";
                break;
            case "YEAR":
                periodExpr = "substr(date,1,4)";
                break;
            case "DAY":
            default:
                periodExpr = "substr(date,1,10)"; // YYYY-MM-DD
                break;
        }

        String sql = "SELECT " + periodExpr + " AS period, SUM(montant_ttc) AS total " +
                "FROM factures " +
                "WHERE substr(date,1,10) BETWEEN ? AND ? " +
                "GROUP BY period ORDER BY period ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start.toString());
            ps.setString(2, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new PeriodTotal(rs.getString("period"), rs.getDouble("total")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

// ------------- B) Optionnel : synchroniser la table recettes depuis factures -------------

    /**
     * Synchronise (rebuild) la table recettes à partir des factures existantes.
     * - Supprime les lignes existantes (ou on peut choisir UPDATE/UPSERT)
     * - Recréé des recettes par jour (ou par granularité choisie) en sommant montant_ttc
     * <p>
     * Attention : c'est destructif pour la table recettes actuelle.
     */
    public static void syncRecettesFromFactures(LocalDate start, LocalDate end, String granularity) {
        // On calcule d'abord l'agrégation depuis les factures
        List<PeriodTotal> data = getAggregatedFromFactures(start, end, granularity);

        // Ensuite on insère en base (table recettes) : une ligne par period
        // La table recettes attend date (ISO_LOCAL_DATE), montant, type
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // Option A: supprimer les recettes dans la plage => puis insert
            try (PreparedStatement deleteP = conn.prepareStatement("DELETE FROM recettes WHERE date BETWEEN ? AND ?")) {
                deleteP.setString(1, start.toString());
                deleteP.setString(2, end.toString());
                deleteP.executeUpdate();
            }

            try (PreparedStatement insertP = conn.prepareStatement("INSERT INTO recettes(date, montant, type) VALUES (?, ?, ?)")) {
                for (PeriodTotal p : data) {
                    String period = p.getPeriod();
                    // Convertir period en date ISO (on stocke la première date du period)
                    String dateForRow = period;
                    if ("MONTH".equalsIgnoreCase(granularity)) {
                        // period is YYYY-MM -> put first day YYYY-MM-01
                        dateForRow = period + "-01";
                    } else if ("YEAR".equalsIgnoreCase(granularity)) {
                        dateForRow = period + "-01-01";
                    } // else DAY is fine (YYYY-MM-DD)

                    insertP.setString(1, dateForRow);
                    insertP.setDouble(2, p.getTotal());
                    insertP.setString(3, granularity.toLowerCase());
                    insertP.addBatch();
                }
                insertP.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
