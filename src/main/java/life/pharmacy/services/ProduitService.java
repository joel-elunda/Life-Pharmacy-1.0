package life.pharmacy.services;

import life.pharmacy.models.Produit;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ProduitService {

    private final List<Produit> produits = new ArrayList<>();

    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    public void add(Produit p) throws SQLException {
        String sql = "INSERT INTO produits(nom, description, prix, quantite_stock, date_expiration, fournisseur_id, categorie) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNomCommercial());
            ps.setString(2, p.getNomGenerique());
            ps.setString(3, p.getForme());
            ps.setString(4, p.getDosage());
            ps.setString(5, p.getConditionnement());
            ps.setString(6, p.getFabricant());
            ps.setString(7, p.getCodeBarres());
            ps.setDouble(8, p.getPrixVente());
            ps.setDouble(9, p.getPrixAchat());
            ps.setString(10, p.getStatut());
            ps.setString(11, p.getCategorie());
            ps.setBoolean(12, p.isPrescriptionRequise());
            ps.setString(13, p.getDateExpiration().toString());
            ps.setString(14, p.getNumeroLot());
            ps.setInt(15, p.getStock());
            ps.setInt(16, p.getSeuilAlerte());

            ps.executeUpdate();
        }
    }

    public void update(Produit p) throws SQLException {
        String sql = "UPDATE produits SET nom=?, description=?, prix=?, quantite_stock=?, date_expiration=?, fournisseur_id=?, categorie=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNomCommercial());
            ps.setString(2, p.getNomGenerique());
            ps.setString(3, p.getForme());
            ps.setString(4, p.getDosage());
            ps.setString(5, p.getConditionnement());
            ps.setString(6, p.getFabricant());
            ps.setString(7, p.getCodeBarres());
            ps.setDouble(8, p.getPrixVente());
            ps.setDouble(9, p.getPrixAchat());
            ps.setString(10, p.getStatut());
            ps.setString(11, p.getCategorie());
            ps.setBoolean(12, p.isPrescriptionRequise());
            ps.setString(13, p.getDateExpiration().toString());
            ps.setString(14, p.getNumeroLot());
            ps.setInt(15, p.getStock());
            ps.setInt(16, p.getSeuilAlerte());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM produits WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Produit> getAll() throws SQLException {
        List<Produit> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM produits")) {
            while (rs.next()) {
                list.add(new Produit(
                        rs.getInt("id"),
                        rs.getString("nom_commercial"),
                        rs.getString("nom_generique"),
                        rs.getString("forme"),
                        rs.getString("dosage"),
                        rs.getString("conditionnement"),
                        rs.getString("fabricant"),
                        rs.getString("codeBarres"),
                        rs.getDouble("prixVente"),
                        rs.getDouble("prixAchat"),
                        rs.getString("statut"),
                        rs.getString("categorie"),
                        rs.getBoolean("prescriptionRequise"),
                        rs.getDate("dateExpiration").toLocalDate(),
                        rs.getString("numeroLot"),
                        rs.getInt("stock"),
                        rs.getInt("seuilAlerte")
                ));
            }
        }
        return list;
    }

    public List<Produit> search(String query) throws SQLException {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produits WHERE nom LIKE ? OR categorie LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Produit(
                        rs.getInt("id"),
                        rs.getString("nom_commercial"),
                        rs.getString("nom_generique"),
                        rs.getString("forme"),
                        rs.getString("dosage"),
                        rs.getString("conditionnement"),
                        rs.getString("fabricant"),
                        rs.getString("codeBarres"),
                        rs.getDouble("prixVente"),
                        rs.getDouble("prixAchat"),
                        rs.getString("statut"),
                        rs.getString("categorie"),
                        rs.getBoolean("prescriptionRequise"),
                        rs.getDate("dateExpiration").toLocalDate(),
                        rs.getString("numeroLot"),
                        rs.getInt("stock"),
                        rs.getInt("seuilAlerte")
                ));
            }
        }
        return list;
    }

    public void exportToFile(String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Produits");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nom");
            header.createCell(2).setCellValue("Nom Générique");
            header.createCell(3).setCellValue("Prix Achat");
            header.createCell(4).setCellValue("Prix Vente");
            header.createCell(5).setCellValue("Stock");

            int rowNum = 1;
            for (Produit p : produits) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getNomCommercial());
                row.createCell(2).setCellValue(p.getNomGenerique());
                row.createCell(3).setCellValue(p.getPrixAchat());
                row.createCell(4).setCellValue(p.getPrixVente());
                row.createCell(5).setCellValue(p.getStock());
            }

            try (FileOutputStream fos = new FileOutputStream(filename)) {
                workbook.write(fos);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void importFromFile(String filename) {
        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            produits.clear();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    /*
                    * public Produit(
            int id,
            String nomCommercial,
            String nomGenerique,
            String forme,
            String dosage,
            String conditionnement,
            String fabricant,
            String codeBarres,
            double prixVente,
            double prixAchat,
            String statut,
            String categorie,
            boolean prescriptionRequise,
            LocalDate dateExpiration,
            String numeroLot,
            int stock,
            int seuilAlerte) {*/
                    Produit p = new Produit(
                            (int) row.getCell(0).getNumericCellValue(),
                            row.getCell(1).getStringCellValue(),
                            row.getCell(2).getStringCellValue(),
                            row.getCell(3).getStringCellValue(),
                            row.getCell(4).getStringCellValue(),
                            row.getCell(5).getStringCellValue(),
                            row.getCell(6).getStringCellValue(),
                            row.getCell(7).getStringCellValue(),
                            row.getCell(8).getNumericCellValue(),
                            row.getCell(9).getNumericCellValue(),
                            row.getCell(10).getStringCellValue(),
                            row.getCell(11).getStringCellValue(),
                            row.getCell(12).getBooleanCellValue(),
                            row.getCell(13).getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                            row.getCell(14).getStringCellValue(),
                            (int) row.getCell(15).getNumericCellValue(),
                            (int) row.getCell(5).getNumericCellValue()
                    );
                    produits.add(p);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}