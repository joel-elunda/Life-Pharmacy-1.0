package life.pharmacy.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;

public class AuthService {
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public boolean authenticate(String login, String password) throws Exception {
        if (login == null || password == null) return false;

        final String sql = "SELECT mot_de_passe_hache FROM employe WHERE login = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String stored = rs.getString("mot_de_passe_hache");
                return verifyPassword(password, stored);
            } else {
                // fallback dev: si aucun employé trouvé, on accepte admin/admin
                return "admin".equals(login) && "admin".equals(password);
            }
        }
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
        // Accepte soit mot de passe déjà hashé (SHA-256), soit (rare) en clair (ex: admin/admin)
        String hashed = hashPassword(rawPassword);
        return stored.equals(hashed) || stored.equals(rawPassword);
    }
}
