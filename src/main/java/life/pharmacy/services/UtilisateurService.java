package life.pharmacy.services;

import life.pharmacy.config.Database;
import life.pharmacy.models.Utilisateur;
import life.pharmacy.utils.ExcelImporter;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {
    private static final Object LOCK = new Object();

    static {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS utilisateurs (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nom TEXT NOT NULL,
                            email TEXT UNIQUE NOT NULL,
                            mot_de_passe TEXT NOT NULL,
                            role TEXT NOT NULL
                        )
                    """);

            // Si aucun utilisateur n'existe, on insère un admin par défaut
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM utilisateurs");
            if (rs.next() && rs.getInt("total") == 0) {
                stmt.executeUpdate("""
                            INSERT INTO utilisateurs (nom, email, mot_de_passe, role)
                            VALUES ('Administrateur', 'admin@lifepharma.com', 'admin123', 'admin')
                        """);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Utilisateur> getAll() {
        synchronized (LOCK) {
            List<Utilisateur> utilisateurs = new ArrayList<>();
            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM utilisateurs")) {
                while (rs.next()) {
                    utilisateurs.add(new Utilisateur(

                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe"),
                            rs.getString("role")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return utilisateurs;
        }
    }

    public static Utilisateur login(String email, String motDePasse) {
        synchronized (LOCK) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM utilisateurs WHERE email=? AND mot_de_passe=?")) {
                stmt.setString(1, email);
                stmt.setString(2, motDePasse);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new Utilisateur(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe"),
                            rs.getString("role")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void createDefaultAdmin() {
        synchronized (LOCK) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO utilisateurs (nom, email, mot_de_passe, role) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, "Administrateur");
                pstmt.setString(2, "admin@lifepharma.com");
                pstmt.setString(3, "admin123");
                pstmt.setString(4, "admin");
                pstmt.executeUpdate();
            } catch (SQLException e) {
                // Si l'admin existe déjà (email unique), on ignore l'erreur
                if (!e.getMessage().contains("UNIQUE constraint failed")) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void insert(Utilisateur utilisateur) {
        synchronized (LOCK) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO utilisateurs (nom, email, motDePasse, role) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, utilisateur.getNom());
                pstmt.setString(2, utilisateur.getEmail());
                pstmt.setString(3, utilisateur.getMotDePasse());
                pstmt.setString(4, utilisateur.getRole());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void importFromExcel(File excelFile) {
        List<List<String>> rows = ExcelImporter.readExcel(excelFile);
        if (rows.size() <= 1) return;

        // En-tête présumé : Nom | Email | Role | MotDePasse
        for (int i = 1; i < rows.size(); i++) {
            List<String> r = rows.get(i);
            try {
                String nom = r.size() > 0 ? r.get(0) : "";
                String email = r.size() > 1 ? r.get(1) : "";
                String role = r.size() > 2 ? r.get(2) : "caissier";
                String motDePasse = r.size() > 3 ? r.get(3) : "changeme";

                // Utiliser insert existant (ou méthode create)
                Utilisateur u = new Utilisateur(0, nom, email, motDePasse, role);
                insert(u); // assure-toi d'avoir une méthode insert qui gère doublons
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void save(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateur(nom, mot_de_passe, role) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getMotDePasse());
            stmt.setString(3, utilisateur.getRole());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateur SET nom = ?, mot_de_passe = ?, role = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getMotDePasse());
            stmt.setString(3, utilisateur.getRole());
            stmt.setInt(4, utilisateur.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Utilisateur getById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
