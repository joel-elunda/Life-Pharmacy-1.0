package life.pharmacy.services;

import life.pharmacy.models.Employe;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;

public class AuthService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public Employe authenticate(String login, String password) throws Exception {
        if (login == null || password == null) return null;

        final String sql = "SELECT * FROM employes WHERE login = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String stored = rs.getString("motDePasseHash");
                if (verifyPassword(password, stored)) {
                    Employe e = new Employe();
                    e.setId(rs.getInt("id"));
                    e.setNomComplet(rs.getString("nomComplet"));
                    e.setRole(rs.getString("role"));
                    e.setLogin(rs.getString("login"));
                    e.setMotDePasseHash(stored);
                    e.setPermissions(rs.getString("permissions"));
                    return e;
                }
            }

            // fallback dev: admin/admin
            if ("admin".equals(login) && "@admin.2025".equals(password)) {
                Employe admin = new Employe();
                admin.setId(0);
                admin.setNomComplet("Administrateur");
                admin.setRole("Admin");
                admin.setLogin("admin");
                return admin;
            }
        }
        return null;
    }

    public static String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static boolean verifyPassword(String rawPassword, String stored) throws Exception {
        if (stored == null) return false;
        // Accepte soit mot de passe déjà hashé (SHA-256), soit (rare) en clair (ex: admin/@admin.2025)
        String hashed = hashPassword(rawPassword);
        return stored.equals(hashed) || stored.equals(rawPassword);
    }
}
