package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import life.pharmacy.models.Produit;
import life.pharmacy.services.ProduitService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import static life.pharmacy.controllers.DashboardController.exportImportService;
import static life.pharmacy.controllers.DashboardController.showError;

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
    @FXML private ComboBox<String> comboResearch;

    @FXML private TableView<Produit> tableView;
    @FXML private TableColumn<Produit, Number> colId;
    @FXML private TableColumn<Produit, String> colNomCommercial;
    @FXML private TableColumn<Produit, String> colNomGenerique;
    @FXML private TableColumn<Produit, String> colForme;
    @FXML private TableColumn<Produit, String> colDosage;
    @FXML private TableColumn<Produit, Number> colPrixVente;
    @FXML private TableColumn<Produit, Number> colStock;
    @FXML private TableColumn<Produit, String> colCategorie;
    @FXML private TableColumn<Produit, Number> colPrixAchat;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;

    private final ProduitService service = new ProduitService();
    private final ObservableList<Produit> data = FXCollections.observableArrayList();

    private void populateFieldsFromSelection(Produit p) {
        if (p == null) return;
        fieldNomCommercial.setText(p.getNomCommercial());
        fieldNomGenerique.setText(p.getNomGenerique());
        comboForme.setValue(p.getForme());
        fieldDosage.setText(p.getDosage());
        fieldConditionnement.setText(p.getConditionnement());
        fieldFabricant.setText(p.getFabricant());
        fieldCodeBarres.setText(p.getCodeBarres());
        fieldPrixVente.setText(String.valueOf(p.getPrixVente()));
        fieldPrixAchat.setText(String.valueOf(p.getPrixAchat()));
        fieldStatut.setText(p.getStatut());
        comboCategorie.setValue(p.getCategorie());
        checkPrescriptionRequise.setSelected(p.isPrescriptionRequise());
        dataDateExpiration.setValue(p.getDateExpiration());
        fieldNumeroLot.setText(p.getNumeroLot());
        fieldStock.setText(String.valueOf(p.getStock()));
        fieldSeuilAlerte.setText(String.valueOf(p.getSeuilAlerte()));
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
        try {
            if(service.ifExists(p)) {
                new Alert(Alert.AlertType.ERROR, "Produit déjà enregistré", ButtonType.CANCEL).showAndWait();
            } else {
                service.add(p);
                data.setAll(service.getAll());
                tableView.refresh();
                clearFields();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

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

            try {
                service.update(selected);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
                try {
                    service.delete(selected.getId());
                    data.setAll(service.getAll());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            showAlert("Sélectionnez un produit à supprimer.");
        }
    }

    @FXML
    public void onExportExcel() {
         if (exportImportService.exportProduits(new DashboardController().getStage()))
             showAlert("Exportation réussie !");
    }

    @FXML
    public void onImportExcel() {
        if(exportImportService.importProduits(new DashboardController().getStage()))
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
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colNomCommercial.setCellValueFactory(cell -> cell.getValue().nomCommercialProperty());
        colNomGenerique.setCellValueFactory(cell -> cell.getValue().nomGeneriqueProperty());
        colForme.setCellValueFactory(cell -> cell.getValue().formeProperty());
        colDosage.setCellValueFactory(cell -> cell.getValue().dosageProperty());
        colStock.setCellValueFactory(cell -> cell.getValue().stockProperty());
        colPrixVente.setCellValueFactory(cell -> cell.getValue().prixVenteProperty());
        colCategorie.setCellValueFactory(cell -> cell.getValue().categorieProperty());
        colPrixAchat.setCellValueFactory(cell -> cell.getValue().prixAchatProperty());

        tableView.setOnMouseClicked(this::handleTableClick);

        try {
            comboResearch.setItems(FXCollections.observableArrayList(
                    service.getAll().stream()
                            .map(Produit::getNomCommercial)
                            .toList()
            ));

            // Quand on change la sélection → on affiche uniquement l’élément dans la table
            comboResearch.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
                if (selected == null) return;

                String nom = selected.split("\\|")[0].trim();  // Récupérer le nom (avant le pipe)
                List<Produit> found = null;                           // ta méthode retourne 0..n éléments
                try {
                    found = service.search(nom);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                data.setAll(found);
                tableView.setItems(FXCollections.observableArrayList(data));
                if (!found.isEmpty()) tableView.getSelectionModel().selectFirst();
            });

            // Remplir les champs quand on sélectionne une ligne (voir point 4, plus bas)
            tableView.getSelectionModel().selectedItemProperty().addListener((o,ov,nv)->populateFieldsFromSelection(nv));

            data.addAll(service.getAll());
            tableView.setItems(data);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        handleRefresh();
    }

    private void handleTableClick(MouseEvent event) {
        Produit selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fieldNomCommercial.setText(selected.getNomCommercial());
            fieldNomGenerique.setText(selected.getNomGenerique());
            comboForme.setValue(selected.getForme());
            fieldDosage.setText(selected.getDosage());
            fieldConditionnement.setText(selected.getConditionnement());
            fieldFabricant.setText(selected.getFabricant());
            fieldCodeBarres.setText(selected.getCodeBarres());
            fieldPrixVente.setText(String.valueOf(selected.getPrixVente()));
            fieldPrixAchat.setText(String.valueOf(selected.getPrixAchat()));
            fieldStatut.setText(selected.getStatut());
            comboCategorie.setValue(selected.getCategorie());
            checkPrescriptionRequise.setSelected(selected.isPrescriptionRequise());
            dataDateExpiration.setValue(selected.getDateExpiration());
            fieldNumeroLot.setText(selected.getNumeroLot());
            fieldStock.setText(String.valueOf(selected.getStock()));
            fieldSeuilAlerte.setText(String.valueOf(selected.getSeuilAlerte()));
        }
    }

    @FXML
    private void onSearch(ActionEvent event) {
        String query = comboResearch.getValue().toLowerCase().trim();

        try {
            // Récupérer tous les clients
            List<Produit> produits = service.getAll();

            // Filtrer selon le texte saisi
            List<Produit> filtered = produits.stream()
                    .filter(p ->
                            p.getNomCommercial().toLowerCase().contains(query) ||
                            p.getNomGenerique().toLowerCase().contains(query)
                    )
                    .toList();

            // Afficher dans la TableView
            tableView.setItems(FXCollections.observableArrayList(filtered));

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @FXML
    private void handleRefresh() {
        reloadProduits(null);
    }
    private void reloadProduits(String q) {
        try {
            var list = (q == null || q.isBlank()) ? service.getAll() : service.search(q);
            tableView.getItems().setAll(list);
        } catch (Exception e) { showError(e); }
    }
}
