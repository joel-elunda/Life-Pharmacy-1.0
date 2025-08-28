package life.pharmacy.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.MessageDigest;

import static java.sql.DriverManager.getConnection;

public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(DB_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                // Employés
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS employes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nomComplet TEXT NOT NULL,
                    role TEXT NOT NULL,
                    login TEXT UNIQUE NOT NULL,
                    motDePasseHash TEXT NOT NULL,
                    permissions TEXT
                )
            """);

                // Clients
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS clients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nomComplet TEXT NOT NULL,
                    dateNaissance TEXT,
                    adresse TEXT,
                    telephone TEXT,
                    email TEXT,
                    conditionsMedicales TEXT,
                    allergies TEXT
                )
            """);

                // Fournisseurs
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS fournisseurs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom TEXT NOT NULL,
                    contact TEXT,
                    telephone TEXT,
                    email TEXT,
                    adresse TEXT,
                    conditionsPaiement TEXT
                )
            """);

                // Produits
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS produits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nomCommercial TEXT NOT NULL,
                    nomGenerique TEXT,
                    forme TEXT,
                    dosage TEXT,
                    conditionnement TEXT,
                    fabricant TEXT,
                    codeBarres TEXT UNIQUE,
                    prixVente REAL NOT NULL,
                    prixAchat REAL NOT NULL,
                    statut TEXT,
                    categorie TEXT,
                    prescriptionRequise INTEGER DEFAULT 0,
                    dateExpiration TEXT,
                    numeroLot TEXT,
                    stock INTEGER DEFAULT 0,
                    seuilAlerte INTEGER DEFAULT 0
                )
            """);

                // Transactions
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS table_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    dateHeure TEXT NOT NULL,
                    total REAL NOT NULL,
                    statutPaiement TEXT,
                    methodePaiement TEXT,
                    clientId INTEGER,
                    employeId INTEGER,
                    FOREIGN KEY(clientId) REFERENCES clients(id),
                    FOREIGN KEY(employeId) REFERENCES employes(id)
                )
            """);

                // LigneTransaction
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS ligne_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transactionId INTEGER NOT NULL,
                    produitId INTEGER NOT NULL,
                    produitNom TEXT,
                    quantite INTEGER NOT NULL,
                    prixUnitaire REAL NOT NULL,
                    sousTotal REAL NOT NULL,
                    numeroOrdonnance TEXT,
                    FOREIGN KEY(transactionId) REFERENCES transactions(id),
                    FOREIGN KEY(produitId) REFERENCES produits(id)
                )
            """);

                // Factures
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS factures (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    clientId INTEGER,
                    employeId INTEGER,
                    date TEXT NOT NULL,
                    montantTotal REAL NOT NULL,
                    modePaiement TEXT,
                    FOREIGN KEY(clientId) REFERENCES clients(id),
                    FOREIGN KEY(employeId) REFERENCES employes(id)
                )
            """);

                // Ordonnances
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS ordonnances (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patientId INTEGER NOT NULL,
                    medecin TEXT NOT NULL,
                    dateEmission TEXT NOT NULL,
                    dateExpiration TEXT,
                    produitsPrescrits TEXT,
                    instructionsDosage TEXT,
                    statut TEXT,
                    numeroUnique TEXT UNIQUE,
                    FOREIGN KEY(patientId) REFERENCES clients(id)
                )
            """);

                // Recettes
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS recettes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL,
                    montant REAL NOT NULL,
                    periode TEXT NOT NULL
                )
            """);

                System.out.println("✅ Base de données initialisée avec succès.");

                // Ajout d’un admin par défaut si inexistant
                stmt.execute("""
                INSERT INTO employes (nomComplet, role, login, motDePasseHash, permissions)
                SELECT 'Administrateur', 'Admin', 'admin', '@admin.2025', 'ALL'
                WHERE NOT EXISTS (SELECT 1 FROM employes WHERE login = 'admin')
            """);
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

}
