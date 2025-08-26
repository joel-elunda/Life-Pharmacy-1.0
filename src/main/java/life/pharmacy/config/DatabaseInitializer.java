package life.pharmacy.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.security.MessageDigest;

public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                // Table Produit
                stmt.execute(""" 
                        CREATE TABLE IF NOT EXISTS produit (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom_commercial TEXT NOT NULL,
                        nom_generique TEXT,
                        forme TEXT,
                        dosage TEXT,
                        conditionnement TEXT,
                        fabricant TEXT,
                        code_barres TEXT,
                        prix_vente REAL NOT NULL,
                        prix_achat REAL NOT NULL,
                        statut TEXT,
                        categorie TEXT,
                        prescription_requise INTEGER,
                        date_expiration TEXT,
                        numero_lot TEXT,
                        stock INTEGER,
                        seuil_alerte INTEGER
                    ); """);

                // Table Client
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS client (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom_complet TEXT NOT NULL,
                        date_naissance TEXT,
                        adresse TEXT,
                        telephone TEXT,
                        email TEXT,
                        conditions_medicales TEXT,
                        allergies TEXT
                    );
                """);

                // Table Employé
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS employe (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom_complet TEXT NOT NULL,
                        role TEXT NOT NULL,
                        login TEXT UNIQUE NOT NULL,
                        mot_de_passe TEXT NOT NULL,
                        permissions TEXT
                    );
                """);

                // Table Transaction
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS transaction (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        date_heure TEXT NOT NULL,
                        total REAL NOT NULL,
                        statut_paiement TEXT NOT NULL,
                        methode_paiement TEXT NOT NULL,
                        client_id INTEGER,
                        caissier_id INTEGER,
                        FOREIGN KEY(client_id) REFERENCES client(id),
                        FOREIGN KEY(caissier_id) REFERENCES employe(id)
                    );
                """);

                // Table LigneTransaction
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS ligne_transaction (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transaction_id INTEGER,
                        produit_id INTEGER,
                        quantite INTEGER,
                        prix_unitaire REAL,
                        sous_total REAL,
                        numero_ordonnance TEXT,
                        FOREIGN KEY(transaction_id) REFERENCES transaction(id),
                        FOREIGN KEY(produit_id) REFERENCES produit(id)
                    );
                """);

                // Table Fournisseur
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS fournisseur (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nom TEXT NOT NULL,
                        contact TEXT,
                        telephone TEXT,
                        email TEXT,
                        adresse TEXT,
                        conditions_paiement TEXT
                    );
                """);

                // Table Ordonnance
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS ordonnance (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        patient_id INTEGER,
                        medecin TEXT,
                        date_emission TEXT,
                        date_expiration TEXT,
                        instructions_dosage TEXT,
                        statut TEXT,
                        numero_ordonnance TEXT UNIQUE,
                        FOREIGN KEY(patient_id) REFERENCES client(id)
                    );
                """);

                // Table Ordonnance_Produit (relation n-n)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS ordonnance_produit (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        ordonnance_id INTEGER,
                        produit_id INTEGER,
                        FOREIGN KEY(ordonnance_id) REFERENCES ordonnance(id),
                        FOREIGN KEY(produit_id) REFERENCES produit(id)
                    );
                """);

                // Insertion Admin par défaut
                String hashedPassword = hashPassword("admin");
                stmt.execute("""
                    INSERT OR IGNORE INTO employe (id, nom_complet, role, login, mot_de_passe, permissions)
                    VALUES (1, 'Administrateur', 'admin', 'admin', ?, 'ALL');
                """);

                System.out.println("✅ Base de données initialisée avec succès !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}
