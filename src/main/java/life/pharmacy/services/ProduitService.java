package life.pharmacy.services;

import life.pharmacy.config.Database;
import life.pharmacy.models.Produit;
import life.pharmacy.utils.ExcelExporter;
import life.pharmacy.utils.ExcelImporter;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProduitService {

    private static final Object LOCK = new Object();

    static {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS produits (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nom TEXT NOT NULL,
                            prixUnitaire REAL NOT NULL,
                            quantite INTEGER NOT NULL
                        )
                    """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Dans life.pharmacy.services.ProduitService
    public static Produit getById(int id) {
        synchronized (LOCK) {
            String sql = "SELECT * FROM produits WHERE id = ?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Adapte les noms de colonnes si ta table utilise d'autres noms
                        int pid = rs.getInt("id");
                        String nom = rs.getString("nom");
                        String codeBarre = null;
                        double prixUnitaire = 0.0;
                        int quantite = 0;
                        boolean tva = false;

                        // lecture sécurisée selon colonnes possibles
                        try {
                            codeBarre = rs.getString("code_barre");
                        } catch (SQLException ignored) {
                        }
                        try {
                            prixUnitaire = rs.getDouble("prix_unitaire");
                        } catch (SQLException ignored) {
                        }
                        try {
                            prixUnitaire = rs.getDouble("prix");
                        } catch (SQLException ignored) {
                        }
                        try {
                            quantite = rs.getInt("quantite");
                        } catch (SQLException ignored) {
                        }
                        try {
                            tva = rs.getInt("tva") == 1;
                        } catch (SQLException ignored) {
                        }

                        return new Produit(pid, nom, codeBarre, prixUnitaire, quantite, tva);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static List<Produit> getAll() {
        synchronized (LOCK) {
            List<Produit> produits = new ArrayList<>();
            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM produits ORDER BY nom ASC")) {
                while (rs.next()) {
                    produits.add(new Produit(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getDouble("prixUnitaire"),
                            rs.getInt("quantite")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return produits;
        }
    }

    public static void insert(Produit produit) {
        synchronized (LOCK) {
            String sql = "INSERT INTO produits (nom, prixUnitaire, quantite) VALUES (?, ?, ?)";
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, produit.getNom());
                pstmt.setDouble(2, produit.getPrixUnitaire());
                pstmt.setInt(3, produit.getQuantite());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void update(Produit produit) {
        synchronized (LOCK) {
            String sql = "UPDATE produits SET nom=?, prixUnitaire=?, quantite=? WHERE id=?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, produit.getNom());
                pstmt.setDouble(2, produit.getPrixUnitaire());
                pstmt.setInt(3, produit.getQuantite());
                pstmt.setInt(4, produit.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void importFromExcel(File excelFile) {
        List<List<String>> rows = ExcelImporter.readExcel(excelFile);
        if (rows.size() <= 1) return; // pas de données

        // En-tête présumé : Nom | CodeBarre | Prix | Quantite | TVA (optionnel: "1" ou "0" / "Oui"/"Non")
        for (int i = 1; i < rows.size(); i++) {
            List<String> r = rows.get(i);
            if (r.isEmpty()) continue;
            try {
                String nom = r.size() > 0 ? r.get(0) : "";
                String codeBarre = r.size() > 1 ? r.get(1) : "";
                double prix = r.size() > 2 && !r.get(2).isEmpty() ? Double.parseDouble(r.get(2)) : 0.0;
                int quantite = r.size() > 3 && !r.get(3).isEmpty() ? Integer.parseInt(r.get(3)) : 0;
                boolean tva = false;
                if (r.size() > 4) {
                    String t = r.get(4).toLowerCase();
                    tva = t.equals("1") || t.equals("oui") || t.equals("true");
                }
                Produit p = new Produit(0, nom, codeBarre, prix, quantite, tva);
                insert(p);
            } catch (Exception ex) {
                ex.printStackTrace(); // ignorer ligne et continuer
            }
        }
    }

    // life.pharmacy.services.ProduitService (extraits)
    public static boolean isReferencedInDetails(int produitId) {
        String sql = "SELECT COUNT(*) as total FROM details_facture WHERE produit_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produitId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("total") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean delete(int id) {
        synchronized (LOCK) {
            if (isReferencedInDetails(id)) {
                // Ne pas supprimer — produit référencé par au moins une facture
                return false;
            }
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM produits WHERE id=?")) {
                pstmt.setInt(1, id);
                int affected = pstmt.executeUpdate();
                return affected > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    // ... dans ProduitService

    public static void importCSV(File excelFile) {
        try {
            List<List<String>> rows = ExcelExporter.read(excelFile);
            if (rows == null || rows.size() <= 1) return;

            // En-tête présumé : Nom | Prix | CodeBarre | Stock
            for (int i = 1; i < rows.size(); i++) {
                List<String> r = rows.get(i);
                try {
                    String nom = r.size() > 0 ? r.get(0).trim() : "";
                    String prixS = r.size() > 1 ? r.get(1).trim() : "0";
                    String codeBarre = r.size() > 2 ? r.get(2).trim() : "";
                    String stockS = r.size() > 3 ? r.get(3).trim() : "0";

                    double prix = 0;
                    int stock = 0;
                    try { prix = Double.parseDouble(prixS.replace(",", ".")); } catch (Exception ignored) {}
                    try { stock = Integer.parseInt(stockS); } catch (Exception ignored) {}

                    var p = new life.pharmacy.models.Produit();
                    try { p.setNom(nom); } catch (Exception ignored) {}
                    try { p.getClass().getMethod("setPrix", double.class).invoke(p, prix); } catch (Exception ignored) {}
                    try { p.getClass().getMethod("setCodeBarre", String.class).invoke(p, codeBarre); } catch (Exception ignored) {}
                    try { p.getClass().getMethod("setStock", int.class).invoke(p, stock); } catch (Exception ignored) {}

                    insert(p);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void exportCSV(File excelFile) {
        try {
            List<List<String>> rows = new ArrayList<>();
            rows.add(Arrays.asList("Nom", "Prix", "CodeBarre", "Stock"));
            for (life.pharmacy.models.Produit p : getAll()) {
                String prixS = "0";
                String stockS = "";
                try { prixS = String.valueOf(p.getClass().getMethod("getPrix").invoke(p)); } catch (Exception ignored) {}
                try { stockS = String.valueOf(p.getClass().getMethod("getStock").invoke(p)); } catch (Exception ignored) {}
                rows.add(Arrays.asList(
                        safe(p.getNom()),
                        prixS,
                        safe(getCodeBarreSafe(p)),
                        stockS
                ));
            }
            ExcelExporter.write(rows, excelFile);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String getCodeBarreSafe(life.pharmacy.models.Produit p) {
        try { var m = p.getClass().getMethod("getCodeBarre"); Object v = m.invoke(p); return v == null ? "" : String.valueOf(v); } catch (Exception ex) { return ""; }
    }


}
