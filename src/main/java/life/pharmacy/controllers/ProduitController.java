package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import life.pharmacy.controllers.dialogcontrollers.ProduitDialogController;
import life.pharmacy.models.Produit;
import life.pharmacy.services.ProduitService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ProduitController implements Initializable {

    @FXML private TextField fieldNomCommercial;
    @FXML private TextField fieldNomGenerique;
    @FXML private ComboBox<String> comboForme;
    @FXML private TextField fieldDosage;
    @FXML private TextField fieldConditionnement;
    @FXML private TextField fieldFabricant;
    @FXML private TextField fieldCodeBarres;
    @FXML private TextField fieldPrixVente;
    @FXML private TextField fieldPrixAchat;
    @FXML private TextField fieldStatut;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private CheckBox checkPrescriptionRequise;
    @FXML private DatePicker dataDateExpiration;
    @FXML private TextField fieldNumeroLot;
    @FXML private TextField fieldStock;
    @FXML private TextField fieldSeuilAlerte;
    @FXML private TextField fieldRechercher;

    @FXML private TableView<Produit> tableView;
    @FXML private TableColumn<Produit, Number> colId;
    @FXML private TableColumn<Produit, String> colNomCommercial;
    @FXML private TableColumn<Produit, String> colNomGenerique;
    @FXML private TableColumn<Produit, String> colForme;
    @FXML private TableColumn<Produit, String> colDosage;
    @FXML private TableColumn<Produit, Number> colPrixVente;
    @FXML private TableColumn<Produit, Number> colStock;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;

    private final ProduitService service = new ProduitService();
    private final ObservableList<Produit> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colNomCommercial.setCellValueFactory(cell -> cell.getValue().nomCommercialProperty());
        colNomGenerique.setCellValueFactory(cell -> cell.getValue().nomGeneriqueProperty());
        colForme.setCellValueFactory(cell -> cell.getValue().formeProperty());
        colDosage.setCellValueFactory(cell -> cell.getValue().dosageProperty());
        colPrixVente.setCellValueFactory(cell -> cell.getValue().prixVenteProperty());
        colStock.setCellValueFactory(cell -> cell.getValue().stockProperty());

        data.addAll(service.getProduits());
        tableView.setItems(data);
    }

    @FXML
    public void onAdd() {
        Produit p = new Produit(
                service.getNextId(),
                fieldNomCommercial.getText(),
                fieldNomGenerique.getText(),
                comboForme.getValue(),
                fieldDosage.getText(),
                fieldConditionnement.getText(),
                fieldFabricant.getText(),
                fieldCodeBarres.getText(),
                Double.parseDouble(fieldPrixVente.getText()),
                Double.parseDouble(fieldPrixAchat.getText()),
                fieldStatut.getText(),
                comboCategorie.getValue(),
                checkPrescriptionRequise.isSelected(),
                dataDateExpiration.getValue(),
                fieldNumeroLot.getText(),
                Integer.parseInt(fieldStock.getText()),
                Integer.parseInt(fieldSeuilAlerte.getText())
        );
        service.addProduit(p);
        data.setAll(service.getProduits());
        clearFields();
    }

    @FXML
    public void onEdit() {
        Produit selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setNomCommercial(fieldNomCommercial.getText());
            selected.setNomGenerique(fieldNomGenerique.getText());
            selected.setForme(comboForme.getValue());
            selected.setDosage(fieldDosage.getText());
            selected.setConditionnement(fieldConditionnement.getText());
            selected.setFabricant(fieldFabricant.getText());
            selected.setCodeBarres(fieldCodeBarres.getText());
            selected.setPrixVente(Double.parseDouble(fieldPrixVente.getText()));
            selected.setPrixAchat(Double.parseDouble(fieldPrixAchat.getText()));
            selected.setStatut(fieldStatut.getText());
            selected.setCategorie(comboCategorie.getValue());
            selected.setPrescriptionRequise(checkPrescriptionRequise.isSelected());
            selected.setDateExpiration(dataDateExpiration.getValue());
            selected.setNumeroLot(fieldNumeroLot.getText());
            selected.setStock(Integer.parseInt(fieldStock.getText()));
            selected.setSeuilAlerte(Integer.parseInt(fieldSeuilAlerte.getText()));

            service.updateProduit(selected);
            tableView.refresh();
            clearFields();
        } else {
            showAlert("Sélectionnez un produit à modifier.");
        }
    }

    @FXML
    public void onDelete() {
        Produit selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce produit ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                service.deleteProduit(selected.getId());
                data.setAll(service.getProduits());
            }
        } else {
            showAlert("Sélectionnez un produit à supprimer.");
        }
    }

    @FXML
    public void onSearch() {
        String query = fieldRechercher.getText();
        Produit p = service.search(query);
        if (p != null) {
            data.setAll(p);
        } else {
            showAlert("Aucun produit trouvé !");
        }
    }

    @FXML
    public void onExportExcel() {
        service.exportToFile("produits.xlsx");
        showAlert("Exportation réussie !");
    }

    @FXML
    public void onImportExcel() {
        service.importFromFile("produits.xlsx");
        data.setAll(service.getProduits());
        showAlert("Importation réussie !");
    }

    private void clearFields() {
        fieldNomCommercial.clear();
        fieldNomGenerique.clear();
        comboForme.setValue(null);
        fieldDosage.clear();
        fieldConditionnement.clear();
        fieldFabricant.clear();
        fieldCodeBarres.clear();
        fieldPrixVente.clear();
        fieldPrixAchat.clear();
        fieldStatut.clear();
        comboCategorie.setValue(null);
        checkPrescriptionRequise.setSelected(false);
        dataDateExpiration.setValue(null);
        fieldNumeroLot.clear();
        fieldStock.clear();
        fieldSeuilAlerte.clear();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        colNomCommercial.setCellValueFactory(cell -> cell.getValue().nomCommercialProperty());
        colNomGenerique.setCellValueFactory(cell -> cell.getValue().nomGeneriqueProperty());
        colCategorie.setCellValueFactory(cell -> cell.getValue().categorieProperty());
        colPrix.setCellValueFactory(cell -> cell.getValue().prixVenteProperty().asObject());
        colStock.setCellValueFactory(cell -> cell.getValue().stockProperty().asObject());

        try {
            produits.addAll(produitService.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        produitTable.setItems(produits);
    }
}
