package life.pharmacy.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import life.pharmacy.models.Client;
import life.pharmacy.services.ClientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import static life.pharmacy.controllers.DashboardController.exportImportService;
import static life.pharmacy.controllers.DashboardController.showError;

public class ClientController implements Initializable {

    @FXML private TextField fieldNom;
    @FXML private DatePicker dateDateNaissance;
    @FXML private TextField fieldAdresse;
    @FXML private TextField fieldTelephone;
    @FXML private TextField fieldEmail;
    @FXML private TextArea areaConditionsMedicales;
    @FXML private TextArea areaAllergies;
    @FXML private ComboBox<String> comboResearch;

    @FXML private TableView<Client> tableView;
    @FXML private TableColumn<Client, Number> colId;
    @FXML private TableColumn<Client, String> colNom;
    @FXML private TableColumn<Client, String> colTelephone;
    @FXML private TableColumn<Client, String> colEmail;
    @FXML private TableColumn<Client, String> colAdresse;
    @FXML private TableColumn<Client, String> colDateNaissance;
    @FXML private TableColumn<Client, String> colConditionsMedicales;
    @FXML private TableColumn<Client, String> colAllergies;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;

    private Client selected = null;
    private final ClientService service = new ClientService();
    private final ObservableList<Client> data = FXCollections.observableArrayList();

    @FXML
    private void handleRefresh() {
        reloadClients(null);
    }
    private void reloadClients(String q) {
        try {
            var list = (q == null || q.isBlank()) ? service.getAll() : service.search(q);
            tableView.getItems().setAll(list);
        } catch (Exception e) { showError(e); }
    }

    @FXML
    public void onAdd() {
        Client c = new Client(
                service.getNextId(),
                fieldNom.getText(),
                dateDateNaissance.getValue(),
                fieldAdresse.getText(),
                fieldTelephone.getText(),
                fieldEmail.getText(),
                areaConditionsMedicales.getText(),
                areaAllergies.getText()
        );
        try {
            service.add(c);
            data.setAll(service.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        clearFields();
    }

    @FXML
    public void onEdit() {
        Client selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setNomComplet(fieldNom.getText());
            selected.setDateNaissance(dateDateNaissance.getValue());
            selected.setAdresse(fieldAdresse.getText());
            selected.setTelephone(fieldTelephone.getText());
            selected.setEmail(fieldEmail.getText());
            selected.setConditionsMedicales(areaConditionsMedicales.getText());
            selected.setAllergies(areaAllergies.getText());

            try {
                service.update(selected);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            tableView.refresh();
            clearFields();
        } else {
            showAlert("Sélectionnez un client à modifier.");
        }
    }

    @FXML
    public void onDelete() {
        Client selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce client ?", ButtonType.YES, ButtonType.NO);
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
            showAlert("Sélectionnez un client à supprimer.");
        }
    }

    @FXML
    public void onExportExcel() {
        if(exportImportService.exportClients(new DashboardController().getStage()))
            showAlert("Exportation réussie !");
    }

    @FXML
    public void onImportExcel() {
        if(exportImportService.importClients(new DashboardController().getStage()))
            showAlert("Importation réussie !");
    }

    private void handleTableClick(MouseEvent event) {
        selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fieldNom.setText(selected.getNomComplet());
            dateDateNaissance.setValue(selected.getDateNaissance());
            fieldTelephone.setText(selected.getTelephone());
            fieldEmail.setText(selected.getEmail());
            fieldAdresse.setText(selected.getAdresse());
            areaConditionsMedicales.setText(selected.getConditionsMedicales());
            areaAllergies.setText(selected.getAllergies());
        }
    }

    private void clearFields() {
        fieldNom.clear();
        dateDateNaissance.setValue(null);
        fieldAdresse.clear();
        fieldTelephone.clear();
        fieldEmail.clear();
        areaConditionsMedicales.clear();
        areaAllergies.clear();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colNom.setCellValueFactory(cell -> cell.getValue().nomCompletProperty());
        colTelephone.setCellValueFactory(cell -> cell.getValue().telephoneProperty());
        colEmail.setCellValueFactory(cell -> cell.getValue().emailProperty());
        colAdresse.setCellValueFactory(cell -> cell.getValue().adresseProperty());
        colDateNaissance.setCellValueFactory(cell -> cell.getValue().dateNaissanceProperty().asString());
        colConditionsMedicales.setCellValueFactory(cell -> cell.getValue().conditionsMedicalesProperty());
        colAllergies.setCellValueFactory(cell -> cell.getValue().allergiesProperty());

        tableView.setOnMouseClicked(this::handleTableClick);

        try {
            comboResearch.setItems(FXCollections.observableArrayList(
                    service.getAll().stream()
                            .map(Client::getNomComplet)
                            .toList()
            ));

            // Quand on change la sélection → on affiche uniquement l’élément dans la table
            comboResearch.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
                if (selected == null) return;
                // Récupérer le nom (avant le pipe)
                String nom = selected.split("\\|")[0].trim();
                List<Client> found = null; // ta méthode retourne 0..n éléments
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

    private void populateFieldsFromSelection(Client c) {
        if (c == null) return;
        fieldNom.setText(c.getNomComplet());
        dateDateNaissance.setValue(c.getDateNaissance());
        fieldAdresse.setText(c.getAdresse());
        fieldTelephone.setText(c.getTelephone());
        fieldEmail.setText(c.getEmail());
        areaConditionsMedicales.setText(c.getConditionsMedicales());
        areaAllergies.setText(c.getAllergies());
    }

    @FXML
    private void onSearch(ActionEvent event) {
        String query = comboResearch.getValue().toLowerCase().trim();

        try {
            // Récupérer tous les clients
            List<Client> clients = service.getAll();

            // Filtrer selon le texte saisi
            List<Client> filtered = clients.stream()
                    .filter(c ->
                            c.getNomComplet().toLowerCase().contains(query) ||
                            c.getTelephone().toLowerCase().contains(query) ||
                            c.getAdresse().toLowerCase().contains(query) ||
                            c.getEmail().toLowerCase().contains(query)
                    )
                    .toList();

            // Afficher dans la TableView
            tableView.setItems(FXCollections.observableArrayList(filtered));

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

}


