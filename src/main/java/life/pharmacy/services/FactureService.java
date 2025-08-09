package life.pharmacy.services;

import life.pharmacy.config.Database;
import life.pharmacy.models.Client;
import life.pharmacy.models.DetailFacture;
import life.pharmacy.models.Facture;
import life.pharmacy.models.Produit;
import life.pharmacy.utils.ExcelExporter;
import life.pharmacy.utils.ExcelImporter;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FactureService {

    static {
        // Création des tables Factures et Détails
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS factures (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            date TEXT NOT NULL,
                            client_id INTEGER,
                            montant_ht REAL NOT NULL,
                            montant_tva REAL NOT NULL,
                            montant_ttc REAL NOT NULL,
                            FOREIGN KEY(client_id) REFERENCES clients(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS details_facture (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            facture_id INTEGER NOT NULL,
                            produit_id INTEGER NOT NULL,
                            quantite INTEGER NOT NULL,
                            prix_unitaire REAL NOT NULL,
                            FOREIGN KEY(facture_id) REFERENCES factures(id),
                            FOREIGN KEY(produit_id) REFERENCES produits(id)
                        )
                    """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Facture> getAll() {
        List<Facture> factures = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM factures ORDER BY date DESC")) {
            while (rs.next()) {
                Client client = null;
                if (rs.getInt("client_id") != 0) {
                    client = ClientService.getAll().stream()
                            .filter(c -> {
                                try {
                                    return c.getId() == rs.getInt("client_id");
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .findFirst().orElse(null);
                }

                Facture f = new Facture(
                        rs.getInt("id"),
                        LocalDateTime.parse(rs.getString("date")),
                        client,
                        rs.getDouble("montant_ht"),
                        rs.getDouble("montant_tva"),
                        rs.getDouble("montant_ttc")
                );
                f.setDetails(getDetailsByFactureId(f.getId()));
                factures.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factures;
    }

    // life.pharmacy.services.FactureService (extrait)
    public static List<DetailFacture> getDetailsByFactureId(int factureId) {
        List<DetailFacture> list = new ArrayList<>();
        String sql = """
                    SELECT df.id, df.produit_id, p.nom AS produit_nom, df.quantite, df.prix_unitaire
                    FROM details_facture df
                    JOIN produits p ON df.produit_id = p.id
                    WHERE df.facture_id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, factureId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produit produit = new Produit(
                            rs.getInt("produit_id"),
                            rs.getString("produit_nom"),
                            null, // code_barre si pas dans ce SELECT
                            rs.getDouble("prix_unitaire"),
                            rs.getInt("quantite"),
                            false // TVA : à compléter si nécessaire
                    );
                    DetailFacture detail = new DetailFacture(
                            rs.getInt("id"),
                            factureId,
                            produit,
                            rs.getInt("quantite"),
                            rs.getDouble("prix_unitaire")
                    );
                    list.add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Récupère les détails d'une facture par son ID.
     *
     * @param factureId L'ID de la facture.
     * @return La liste des détails de la facture.
     * <p>
     * public static List<DetailFacture> getDetailsByFactureId(int factureId) {
     * List<DetailFacture> details = new ArrayList<>();
     * try (Connection conn = Database.getConnection();
     * PreparedStatement stmt = conn.prepareStatement("SELECT * FROM details_facture WHERE facture_id=?")) {
     * stmt.setInt(1, factureId);
     * ResultSet rs = stmt.executeQuery();
     * while (rs.next()) {
     * Produit produit = ProduitService.getAll().stream()
     * .filter(p -> {
     * try {
     * return p.getId() == rs.getInt("produit_id");
     * } catch (SQLException e) {
     * throw new RuntimeException(e);
     * }
     * })
     * .findFirst().orElse(null);
     * details.add(new DetailFacture(
     * rs.getInt("id"),
     * produit,
     * rs.getInt("quantite"),
     * rs.getDouble("prix_unitaire")
     * ));
     * }
     * } catch (SQLException e) {
     * e.printStackTrace();
     * }
     * return details;
     * }
     */

    //    public static void insert(Facture facture) {
    //        try (Connection conn = Database.getConnection()) {
    //            conn.setAutoCommit(false);
    //
    //            // Insertion facture
    //            PreparedStatement pstmt = conn.prepareStatement(
    //                    "INSERT INTO factures (date, client_id, montant_ht, montant_tva, montant_ttc) VALUES (?, ?, ?, ?, ?)",
    //                    Statement.RETURN_GENERATED_KEYS
    //            );
    //            pstmt.setString(1, facture.getDate().toString());
    //            if (facture.getClient() != null) {
    //                pstmt.setInt(2, facture.getClient().getId());
    //            } else {
    //                pstmt.setNull(2, Types.INTEGER);
    //            }
    //            pstmt.setDouble(3, facture.getMontantHT());
    //            pstmt.setDouble(4, facture.getMontantTVA());
    //            pstmt.setDouble(5, facture.getMontantTTC());
    //            pstmt.executeUpdate();
    //
    //            ResultSet rs = pstmt.getGeneratedKeys();
    //            if (rs.next()) {
    //                facture.setId(rs.getInt(1));
    //            }
    //
    //            // Insertion des détails + mise à jour du stock dans la même connexion
    //            PreparedStatement pstmtDetail = conn.prepareStatement(
    //                    "INSERT INTO details_facture (facture_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)"
    //            );
    //            PreparedStatement pstmtUpdateStock = conn.prepareStatement(
    //                    "UPDATE produits SET quantite = quantite - ? WHERE id = ?"
    //            );
    //
    //            for (DetailFacture d : facture.getDetails()) {
    //                // Détails facture
    //                pstmtDetail.setInt(1, facture.getId());
    //                pstmtDetail.setInt(2, d.getProduit().getId());
    //                pstmtDetail.setInt(3, d.getQuantite());
    //                pstmtDetail.setDouble(4, d.getPrixUnitaire());
    //                pstmtDetail.addBatch();
    //
    //                // Mise à jour stock
    //                pstmtUpdateStock.setInt(1, d.getQuantite());
    //                pstmtUpdateStock.setInt(2, d.getProduit().getId());
    //                pstmtUpdateStock.addBatch();
    //            }
    //            pstmtDetail.executeBatch();
    //            pstmtUpdateStock.executeBatch();
    //
    //            conn.commit();
    //        } catch (SQLException e) {
    //            e.printStackTrace();
    //        }
    //    }
    //

    // life.pharmacy.services.FactureService.insert (extrait modifié)
    public static void insert(Facture facture) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // Insertion facture
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO factures (date, client_id, montant_ht, montant_tva, montant_ttc) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            pstmt.setString(1, facture.getDate().toString());
            if (facture.getClient() != null) pstmt.setInt(2, facture.getClient().getId());
            else pstmt.setNull(2, Types.INTEGER);
            pstmt.setDouble(3, facture.getMontantHT());
            pstmt.setDouble(4, facture.getMontantTVA());
            pstmt.setDouble(5, facture.getMontantTTC());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) facture.setId(rs.getInt(1));

            // Insertion des détails (avec produit_nom) + mise à jour stock
            PreparedStatement pstmtDetail = conn.prepareStatement(
                    "INSERT INTO details_facture (facture_id, produit_id, produit_nom, quantite, prix_unitaire) VALUES (?, ?, ?, ?, ?)"
            );
            PreparedStatement pstmtUpdateStock = conn.prepareStatement(
                    "UPDATE produits SET quantite = quantite - ? WHERE id = ?"
            );

            for (DetailFacture d : facture.getDetails()) {
                Produit p = d.getProduit();
                String produitNomSnapshot = (p != null && p.getNom() != null) ? p.getNom() : d.getProduitNom();

                pstmtDetail.setInt(1, facture.getId());
                pstmtDetail.setInt(2, p != null ? p.getId() : 0);
                pstmtDetail.setString(3, produitNomSnapshot);
                pstmtDetail.setInt(4, d.getQuantite());
                pstmtDetail.setDouble(5, d.getPrixUnitaire());
                pstmtDetail.addBatch();

                // Mise à jour stock (si produit existant)
                if (p != null) {
                    pstmtUpdateStock.setInt(1, d.getQuantite());
                    pstmtUpdateStock.setInt(2, p.getId());
                    pstmtUpdateStock.addBatch();
                }
            }
            pstmtDetail.executeBatch();
            pstmtUpdateStock.executeBatch();

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // life.pharmacy.services.FactureService (extrait)
    public static boolean deleteByIdIfAllowed(int factureId, String role) {
        if (role == null || !role.equalsIgnoreCase("admin")) {
            // Seuls les admin peuvent supprimer
            return false;
        }

        String deleteDetails = "DELETE FROM details_facture WHERE facture_id = ?";
        String deleteFacture = "DELETE FROM factures WHERE id = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(deleteDetails);
                 PreparedStatement ps2 = conn.prepareStatement(deleteFacture)) {

                ps1.setInt(1, factureId);
                ps1.executeUpdate();

                ps2.setInt(1, factureId);
                ps2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ... dans FactureService

    public static void importCSV(File excelFile) {
        try {
            List<List<String>> rows = ExcelExporter.read(excelFile);
            if (rows == null || rows.size() <= 1) return;

            // En-tête présumé : Numéro | Date | ClientNom | MontantTTC
            for (int i = 1; i < rows.size(); i++) {
                List<String> r = rows.get(i);
                try {
                    String numero = r.size() > 0 ? r.get(0).trim() : "";
                    String dateStr = r.size() > 1 ? r.get(1).trim() : "";
                    String clientNom = r.size() > 2 ? r.get(2).trim() : "";
                    String montantStr = r.size() > 3 ? r.get(3).trim() : "0";

                    double montant = 0;
                    try { montant = Double.parseDouble(montantStr.replace(",", ".")); } catch (Exception ignored) {}

                    var f = new life.pharmacy.models.Facture();
                    try { f.setId(Integer.parseInt(numero)); } catch (Exception ignored) {}
                    try { /* parse dateStr si besoin et setDate */ } catch (Exception ignored) {}
                    try {
                        // try to find client by name
                        var clients = life.pharmacy.services.ClientService.getAll();
                        var client = clients.stream().filter(c -> clientNom.equalsIgnoreCase(c.getNom())).findFirst().orElse(null);
                        f.setClient(client);
                    } catch (Exception ignored) {}
                    try { f.setMontantTTC(montant); } catch (Exception ignored) {}

                    insert(f); // adapte selon ta méthode
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void exportCSV(File excelFile) {
        try {
            List<List<String>> rows = new ArrayList<>();
            rows.add(Arrays.asList("ID", "Date", "Client", "MontantTTC"));
            for (var f : getAll()) {
                rows.add(Arrays.asList(
                        String.valueOf(f.getId()),
                        f.getDate() != null ? f.getDate().toString() : "",
                        f.getClient() != null ? f.getClient().getNom() : "",
                        String.valueOf(f.getMontantTTC())
                ));
            }
            ExcelExporter.write(rows, excelFile);
        } catch (Exception e) { e.printStackTrace(); }
    }


}
