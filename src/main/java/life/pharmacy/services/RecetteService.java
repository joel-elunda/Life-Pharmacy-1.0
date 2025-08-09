package life.pharmacy.services;

import life.pharmacy.config.Database;
import life.pharmacy.models.Recette;
import life.pharmacy.utils.ExcelExporter;
import life.pharmacy.utils.ExcelImporter;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static void importCSV(File excelFile) {
        try {
            List<List<String>> rows = ExcelExporter.read(excelFile);
            if (rows == null || rows.size() <= 1) return;

            // En-tête présumé : Date | MontantHT | MontantTVA | MontantTTC | ModePaiement | Source
            for (int i = 1; i < rows.size(); i++) {
                List<String> r = rows.get(i);
                try {
                    String dateStr     = r.size() > 0 ? r.get(0).trim() : "";
                    String montantHTs  = r.size() > 1 ? r.get(1).trim() : "0";
                    String montantTVAs = r.size() > 2 ? r.get(2).trim() : "0";
                    String montantTTCs = r.size() > 3 ? r.get(3).trim() : "0";
                    String mode        = r.size() > 4 ? r.get(4).trim() : "";
                    String source      = r.size() > 5 ? r.get(5).trim() : "";

                    // Création de l'entité Recette (utilise setters pour compatibilité)
                    life.pharmacy.models.Recette rec = new life.pharmacy.models.Recette();
                    // Date : tenter LocalDate puis LocalDateTime puis substring fallback
                    if (!dateStr.isEmpty()) {
                        try { rec.setDate(LocalDate.parse(dateStr)); }
                        catch (Exception ex1) {
                            try { rec.setDate(LocalDate.from(LocalDateTime.parse(dateStr))); }
                            catch (Exception ex2) {
                                try { rec.setDate(LocalDate.parse(dateStr.substring(0, 10))); }
                                catch (Exception ignored) {}
                            }
                        }
                    }

                    // Montants (tolérance pour virgule)
                    double montantHT = 0, montantTVA = 0, montantTTC = 0;
                    try { montantHT = Double.parseDouble(montantHTs.replace(",", ".")); } catch (Exception ignored) {}
                    try { montantTVA = Double.parseDouble(montantTVAs.replace(",", ".")); } catch (Exception ignored) {}
                    try { montantTTC = Double.parseDouble(montantTTCs.replace(",", ".")); } catch (Exception ignored) {}

                    try { rec.setMontant(montantHT); } catch (Exception ignored) {}
//                    try { rec.setMontantTVA(montantTVA); } catch (Exception ignored) {}
//                    try { rec.setMontantTTC(montantTTC); } catch (Exception ignored) {}
//                    try { rec.setModePaiement(mode); } catch (Exception ignored) {}
//                    try { rec.setSource(source); } catch (Exception ignored) {}

                    // Persister (adapte si ta méthode s'appelle autrement : insert/save)
                    insert(rec);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportCSV(File excelFile) {
        try {
            List<List<String>> rows = new ArrayList<>();
            rows.add(Arrays.asList("Date", "MontantHT", "MontantTVA", "MontantTTC", "ModePaiement", "Source"));

            for (life.pharmacy.models.Recette r : getAll()) {
                String dateStr = "";
                try {
                    // getDate() peut renvoyer LocalDate / LocalDateTime / String selon ton modèle
                    Object d = r.getDate();
                    if (d != null) dateStr = d.toString();
                } catch (Exception ignored) {}

                String mht = "";
                String mtva = "";
                String mttc = "";
                try { mht = String.valueOf(r.getMontant()); } catch (Exception ignored) {}
//                try { mtva = String.valueOf(r.getMontantTVA()); } catch (Exception ignored) {}
//                try { mttc = String.valueOf(r.getMontantTTC()); } catch (Exception ignored) {}

                String mode = "";
                String source = "";
//                try { mode = r.getModePaiement(); } catch (Exception ignored) {}
//                try { source = r.getSource(); } catch (Exception ignored) {}

                rows.add(Arrays.asList(safe(dateStr), safe(mht), safe(mtva), safe(mttc), safe(mode), safe(source)));
            }

            ExcelExporter.write(rows, excelFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    // placeholder signatures — adapte si noms différents
//    private static void insert(life.pharmacy.models.Recette r) {
//        // appelle ta méthode réelle d'insertion (ex : DAO ou repository)
//        // ex : new RecetteDAO().insert(r);
//        throw new UnsupportedOperationException("Remplace RecetteService.insert(...) par ta méthode réelle");
//    }
//    private static java.util.List<life.pharmacy.models.Recette> getAll() {
//        // retourne toutes les recettes depuis la BDD
//        throw new UnsupportedOperationException("Remplace RecetteService.getAll() par ta méthode réelle");
//    }

}
