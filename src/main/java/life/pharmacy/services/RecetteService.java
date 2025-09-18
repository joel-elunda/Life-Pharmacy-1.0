package life.pharmacy.services;

import life.pharmacy.config.DatabaseInitializer;
import life.pharmacy.models.Recette;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RecetteService {

    private List<Recette> recettes = new ArrayList<>();
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(Recette r) {
        String sql = "INSERT INTO recettes(date, montant, periode) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getDate().toString());
            ps.setDouble(2, r.getMontant());
            ps.setString(3, r.getPeriode());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Retourne toutes les recettes (utilisé pour export)
    public List<Recette> getAll() throws SQLException {
        List<Recette> list = new ArrayList<>();
        String sql = "SELECT id, date, montant, periode FROM recettes ORDER BY date DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                double montant = rs.getDouble("montant");
                String periode = rs.getString("periode");
                Recette r = new Recette(id, date, montant, periode);
                list.add(r);
            }
        }
        return list;
    }

    // Export simple de la table recettes vers un fichier XLSX
    public void exportToFile(String filename) throws SQLException, IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Recettes");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Date");
            header.createCell(2).setCellValue("Montant");
            header.createCell(3).setCellValue("Periode");

            List<Recette> recettes = getAll();
            int rowIndex = 1;
            for (Recette r : recettes) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getDate().toString());
                row.createCell(2).setCellValue(r.getMontant());
                row.createCell(3).setCellValue(r.getPeriode());
            }

            for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(filename)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Génère un map label -> montant selon la période choisie.
     * periodAccepted: "Jour", "Semaine", "Mois", "Trimestre", "Semestre", "Année"
     *
     * Remarque : on lit depuis table_transactions.total (les ventes). On agrège selon la période.
     */
    public Map<String, Double> getRevenueSeries(String period) throws SQLException {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement()) {

            switch (period.toLowerCase()) {
                case "jour": // par heure pour aujourd'hui (00..23)
                    sql = "SELECT strftime('%H', dateHeure) as label, IFNULL(SUM(total),0) as sum " +
                            "FROM table_transactions " +
                            "WHERE date(dateHeure) = date('now','localtime') " +
                            "GROUP BY label ORDER BY label";
                    try (ResultSet rs = st.executeQuery(sql)) {
                        // init labels 00..23 pour garantir présence
                        for (int h = 0; h < 24; h++) map.put(String.format("%02d:00", h), 0.0);
                        while (rs.next()) {
                            String label = rs.getString("label");
                            double v = rs.getDouble("sum");
                            map.put(label + ":00", v);
                        }
                    }
                    break;

                case "semaine": // derniers 7 jours (date)
                    sql = "SELECT date(dateHeure) as label, IFNULL(SUM(total),0) as sum " +
                            "FROM table_transactions " +
                            "WHERE date(dateHeure) BETWEEN date('now','-6 days','localtime') AND date('now','localtime') " +
                            "GROUP BY label ORDER BY label";
                    try (ResultSet rs = st.executeQuery(sql)) {
                        // init last 7 days
                        for (int i = 6; i >= 0; i--) {
                            String d = String.format("%s", java.time.LocalDate.now().minusDays(i));
                            map.put(d, 0.0);
                        }
                        while (rs.next()) {
                            String label = rs.getString("label");
                            double v = rs.getDouble("sum");
                            map.put(label, v);
                        }
                    }
                    break;

                case "mois": // jours du mois courant
                    sql = "SELECT strftime('%Y-%m-%d', dateHeure) as label, IFNULL(SUM(total),0) as sum " +
                            "FROM table_transactions " +
                            "WHERE strftime('%Y-%m', dateHeure) = strftime('%Y-%m','now','localtime') " +
                            "GROUP BY label ORDER BY label";
                    try (ResultSet rs = st.executeQuery(sql)) {
                        // initialize days of current month
                        java.time.LocalDate now = java.time.LocalDate.now();
                        int days = now.lengthOfMonth();
                        java.time.YearMonth ym = java.time.YearMonth.of(now.getYear(), now.getMonth());
                        for (int d = 1; d <= days; d++) {
                            String label = ym.atDay(d).toString();
                            map.put(label, 0.0);
                        }
                        while (rs.next()) {
                            String label = rs.getString("label");
                            double v = rs.getDouble("sum");
                            map.put(label, v);
                        }
                    }
                    break;

                case "trimestre": // group by month for current quarter
                    // compute quarter start month
                    java.time.LocalDate today = java.time.LocalDate.now();
                    int q = (today.getMonthValue() - 1) / 3; // 0..3
                    int startMonth = q * 3 + 1;
                    String qStart = String.format("%04d-%02d-01", today.getYear(), startMonth);
                    String qEndMonth = String.format("%02d", startMonth + 2);
                    sql = "SELECT strftime('%Y-%m', dateHeure) as label, IFNULL(SUM(total),0) as sum " +
                            "FROM table_transactions " +
                            "WHERE date(dateHeure) BETWEEN date('" + qStart + "') AND date('" + today.getYear() + "-" + qEndMonth + "-31') " +
                            "GROUP BY label ORDER BY label";
                    try (ResultSet rs = st.executeQuery(sql)) {
                        // init months in quarter
                        for (int m = startMonth; m < startMonth + 3; m++) {
                            String label = String.format("%04d-%02d", today.getYear(), m);
                            map.put(label, 0.0);
                        }
                        while (rs.next()) {
                            String label = rs.getString("label");
                            double v = rs.getDouble("sum");
                            map.put(label, v);
                        }
                    }
                    break;

                case "semestre": // group by month for current half-year
                    java.time.LocalDate t2 = java.time.LocalDate.now();
                    int half = (t2.getMonthValue() - 1) / 6; // 0 or 1
                    int startM = half * 6 + 1;
                    int endM = startM + 5;
                    sql = "SELECT strftime('%Y-%m', dateHeure) as label, IFNULL(SUM(total),0) as sum " +
                            "FROM table_transactions " +
                            "WHERE strftime('%Y', dateHeure) = strftime('%Y','now','localtime') " +
                            "AND CAST(strftime('%m', dateHeure) AS INTEGER) BETWEEN " + startM + " AND " + endM + " " +
                            "GROUP BY label ORDER BY label";
                    try (ResultSet rs = st.executeQuery(sql)) {
                        for (int m = startM; m <= endM; m++) {
                            String label = String.format("%04d-%02d", t2.getYear(), m);
                            map.put(label, 0.0);
                        }
                        while (rs.next()) {
                            String label = rs.getString("label");
                            double v = rs.getDouble("sum");
                            map.put(label, v);
                        }
                    }
                    break;

                case "année":
                default: // année courante par mois
                    sql = "SELECT strftime('%m', dateHeure) as label, IFNULL(SUM(total),0) as sum " +
                            "FROM table_transactions " +
                            "WHERE strftime('%Y', dateHeure) = strftime('%Y','now','localtime') " +
                            "GROUP BY label ORDER BY label";
                    try (ResultSet rs = st.executeQuery(sql)) {
                        // init 01..12
                        java.time.LocalDate now = java.time.LocalDate.now();
                        for (int m = 1; m <= 12; m++) {
                            String label = String.format("%02d", m);
                            map.put(label, 0.0);
                        }
                        while (rs.next()) {
                            String label = rs.getString("label"); // "01".."12"
                            double v = rs.getDouble("sum");
                            map.put(label, v);
                        }
                    }
                    break;
            }
        }

        return map;
    }

    // Supprime toutes les recettes d'une année donnée dans la table "recettes"
    public void resetRecettesYear(int year) throws SQLException {
        String sql = "DELETE FROM recettes WHERE strftime('%Y', date) = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(year));
            ps.executeUpdate();
        }
    }

//    public List<Recette> getByPeriode(String periode) {
//        List<Recette> recettes = new ArrayList<>();
//
//        String query = switch (periode.toLowerCase()) {
//            case "jour" -> "SELECT date, SUM(montant) as montant FROM recettes GROUP BY date";
//            case "semaine" -> "SELECT strftime('%W', date) as semaine, SUM(montant) as montant FROM recettes GROUP BY semaine";
//            case "mois" -> "SELECT strftime('%m-%Y', date) as mois, SUM(montant) as montant FROM recettes GROUP BY mois";
//            case "annee" -> "SELECT strftime('%Y', date) as annee, SUM(montant) as montant FROM recettes GROUP BY annee";
//            default -> "SELECT date, montant FROM recettes";
//        };
//
//        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:pharmacy.db");
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//
//            while (rs.next()) {
//                Recette r = new Recette();
//                r.setDate(LocalDate.parse(rs.getString(1)));   // attention : pour semaine/mois/annee → String
//                r.setMontant(rs.getDouble(2));
//                recettes.add(r);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return recettes;
//    }

    public List<Recette> getByPeriode(String periode) throws SQLException {
        List<Recette> list = new ArrayList<>();
        String sql = "";

        switch (periode.toLowerCase()) {
            case "jour":
                sql = """
                SELECT strftime('%Y-%m-%d', dateHeure) as d, SUM(total) as montant
                FROM table_transactions
                GROUP BY d
                ORDER BY d
            """;
                break;
            case "semaine":
                sql = """
                SELECT strftime('%Y-%W', dateHeure) as d, SUM(total) as montant
                FROM table_transactions
                GROUP BY d
                ORDER BY d
            """;
                break;
            case "mois":
                sql = """
                SELECT strftime('%m', dateHeure) as d, SUM(total) as montant
                FROM table_transactions
                GROUP BY d
                ORDER BY d
            """;
                break;
            case "annee":
                sql = """
                SELECT strftime('%Y', dateHeure) as d, SUM(total) as montant
                FROM table_transactions
                GROUP BY d
                ORDER BY d
            """;
                break;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Recette r = new Recette(
                        0,
                        LocalDate.parse(rs.getString("d")),
                        rs.getDouble("montant"),
                        periode
                );
                list.add(r);

                // Insertion automatique dans table recettes
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO recettes(date, montant, periode) VALUES(?,?,?)")) {
                    ps.setString(1, rs.getString("d"));
                    ps.setDouble(2, rs.getDouble("montant"));
                    ps.setString(3, periode);
                    ps.executeUpdate();
                } catch (Exception ignore) {}
            }
        }
        return list;
    }


}
