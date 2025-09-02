package life.pharmacy.controllers;

import javafx.fxml.Initializable;
import life.pharmacy.models.Fournisseur;
import life.pharmacy.services.FournisseurService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class FournisseurController implements Initializable {

    @FXML private TextField fieldNom;
    @FXML private TextField fieldContact;
    @FXML private TextField fieldTelephone;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldAdresse;
    @FXML private TextArea areaConditionsPaiement;
    @FXML private TextField fieldRechercher;

    @FXML private TableView<Fournisseur> tableView;
    @FXML private TableColumn<Fournisseur, Number> colId;
    @FXML private TableColumn<Fournisseur, String> colNom;
    @FXML private TableColumn<Fournisseur, String> colContact;
    @FXML private TableColumn<Fournisseur, String> colTelephone;
    @FXML private TableColumn<Fournisseur, String> colEmail;
    @FXML private TableColumn<Fournisseur, String> colAdresse;
    @FXML private TableColumn<Fournisseur, String> colConditionsPaiement;

    private final FournisseurService service = new FournisseurService();
    private Fournisseur selected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colNom.setCellValueFactory(data -> data.getValue().nomProperty());
        colContact.setCellValueFactory(data -> data.getValue().contactProperty());
        colTelephone.setCellValueFactory(data -> data.getValue().telephoneProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());
        colAdresse.setCellValueFactory(data -> data.getValue().adresseProperty());
        colConditionsPaiement.setCellValueFactory(data -> data.getValue().conditionsPaiementProperty());

        refreshTable();

        tableView.setOnMouseClicked(this::handleTableClick);
    }

    private void refreshTable() {
        try {
            tableView.setItems(FXCollections.observableArrayList(service.getAll()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void clearFields() {
        fieldNom.clear();
        fieldContact.clear();
        fieldTelephone.clear();
        fieldEmail.clear();
        fieldAdresse.clear();
        areaConditionsPaiement.clear();
        fieldRechercher.clear();
        selected = null;
    }

    @FXML
    private void onAdd() {
        Fournisseur f = new Fournisseur(
                0,
                fieldNom.getText(),
                fieldContact.getText(),
                fieldTelephone.getText(),
                fieldEmail.getText(),
                fieldAdresse.getText(),
                areaConditionsPaiement.getText()
        );
        try {
            service.add(f);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        refreshTable();
        clearFields();
    }

    @FXML
    private void onEdit() {
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un fournisseur à modifier !").showAndWait();
            return;
        }

        selected.setNom(fieldNom.getText());
        selected.setContact(fieldContact.getText());
        selected.setTelephone(fieldTelephone.getText());
        selected.setEmail(fieldEmail.getText());
        selected.setAdresse(fieldAdresse.getText());
        selected.setConditionsPaiement(areaConditionsPaiement.getText());

        try {
            service.update(selected);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        refreshTable();
        clearFields();
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un fournisseur à supprimer !").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer ce fournisseur ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                service.delete(selected.getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            refreshTable();
            clearFields();
        }
    }

    @FXML
    private void handleRechercher() {
        String query = fieldRechercher.getText().trim();

        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        List<Fournisseur> resultats = null;
        try {
            resultats = service.search(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (resultats.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucun fournisseur trouvé pour : " + query).showAndWait();
        } else {
            tableView.setItems(FXCollections.observableArrayList(resultats));
        }
    }

    private void handleTableClick(MouseEvent event) {
        selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fieldNom.setText(selected.getNom());
            fieldContact.setText(selected.getContact());
            fieldTelephone.setText(selected.getTelephone());
            fieldEmail.setText(selected.getEmail());
            fieldAdresse.setText(selected.getAdresse());
            areaConditionsPaiement.setText(selected.getConditionsPaiement());
        }
    }

    // === EXPORT EXCEL ===
    @FXML
    private void handleExportExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fournisseurs");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nom");
            header.createCell(2).setCellValue("Contact");
            header.createCell(3).setCellValue("Téléphone");
            header.createCell(4).setCellValue("Email");
            header.createCell(5).setCellValue("Adresse");
            header.createCell(6).setCellValue("Conditions Paiement");

            int rowNum = 1;
            try {
                for (Fournisseur f : service.getAll()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(f.getId());
                    row.createCell(1).setCellValue(f.getNom());
                    row.createCell(2).setCellValue(f.getContact());
                    row.createCell(3).setCellValue(f.getTelephone());
                    row.createCell(4).setCellValue(f.getEmail());
                    row.createCell(5).setCellValue(f.getAdresse());
                    row.createCell(6).setCellValue(f.getConditionsPaiement());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            try (FileOutputStream fos = new FileOutputStream("fournisseurs.xlsx")) {
                workbook.write(fos);
            }

            new Alert(Alert.AlertType.INFORMATION, "Exportation réussie !").showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'exportation !").showAndWait();
        }
    }

    // === IMPORT EXCEL ===
    @FXML
    private void handleImportExcel() {
        try (FileInputStream fis = new FileInputStream("fournisseurs.xlsx");
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

//            service.clear();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Fournisseur f = new Fournisseur(
                            (int) row.getCell(0).getNumericCellValue(),
                            row.getCell(1).getStringCellValue(),
                            row.getCell(2).getStringCellValue(),
                            row.getCell(3).getStringCellValue(),
                            row.getCell(4).getStringCellValue(),
                            row.getCell(5).getStringCellValue(),
                            row.getCell(6).getStringCellValue()
                    );
                    try {
                        service.add(f);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            refreshTable();
            new Alert(Alert.AlertType.INFORMATION, "Importation réussie !").showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'importation !").showAndWait();
        }
    }
}
