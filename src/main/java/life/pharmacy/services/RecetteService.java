package life.pharmacy.services;

import life.pharmacy.config.DatabaseInitializer;
import life.pharmacy.models.Facture;
import life.pharmacy.models.Recette;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecetteService {

    private List<Recette> recettes = new ArrayList<>();

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

    public List<Recette> getAll() {
        List<Recette> list = new ArrayList<>();
        String sql = "SELECT * FROM recettes";
        try (Connection conn = DatabaseInitializer.connect();
             Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Recette(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getDouble("montant"),
                        rs.getString("periode")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Recette> getByPeriode(String periode) {
        List<Recette> recettes = new ArrayList<>();

        String query = switch (periode.toLowerCase()) {
            case "jour" -> "SELECT date, SUM(montant) as montant FROM recettes GROUP BY date";
            case "semaine" -> "SELECT strftime('%W', date) as semaine, SUM(montant) as montant FROM recettes GROUP BY semaine";
            case "mois" -> "SELECT strftime('%m-%Y', date) as mois, SUM(montant) as montant FROM recettes GROUP BY mois";
            case "annee" -> "SELECT strftime('%Y', date) as annee, SUM(montant) as montant FROM recettes GROUP BY annee";
            default -> "SELECT date, montant FROM recettes";
        };

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:pharmacy.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Recette r = new Recette();
                r.setDate(LocalDate.parse(rs.getString(1)));   // attention : pour semaine/mois/annee → String
                r.setMontant(rs.getDouble(2));
                recettes.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return recettes;
    }

}
