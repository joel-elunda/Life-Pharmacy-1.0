package life.pharmacy.controllers;

import javafx.fxml.Initializable;
import life.pharmacy.models.Client;
import life.pharmacy.models.Facture;
import life.pharmacy.services.ClientService;
import life.pharmacy.services.FactureService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class FactureController implements Initializable {

    @FXML private ComboBox<String> comboClient;
    @FXML private ComboBox<String> comboEmploye;
    @FXML private DatePicker dateDate;
    @FXML private TextField fieldMontantTotal;
    @FXML private TextField fieldModePaiement;
    @FXML private TextField fieldRechercher;

    @FXML private TableView<Facture> tableView;
    @FXML private TableColumn<Facture, Number> colId;
    @FXML private TableColumn<Facture, String> colClient;
    @FXML private TableColumn<Facture, String> colEmploye;
    @FXML private TableColumn<Facture, Number> colMontantTotal;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;

    private final FactureService service = new FactureService();
    private final ObservableList<Facture> data = FXCollections.observableArrayList();

    private ClientService clientService = new ClientService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colClient.setCellValueFactory(cell -> cell.getValue().clientProperty().asString());
        colEmploye.setCellValueFactory(cell -> cell.getValue().employeProperty().asString());
        colMontantTotal.setCellValueFactory(cell -> cell.getValue().montantTotalProperty());

        try {
            comboClient.setItems(FXCollections.observableArrayList(
                    clientService.getAll().stream().map(Client::getNomComplet).toList()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            data.addAll(service.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        tableView.setItems(data);
    }

    @FXML
    public void onAdd() {
//        public Facture(int id, Client client, Employe employe, LocalDate date, double montantTotal, String modePaiement) {
        Facture f = new Facture(
                service.getNextId(),
                service.getClientByName(comboClient.getValue()),
                service.getEmployeByName(comboEmploye.getValue()),
                dateDate.getValue(),
                Double.parseDouble(fieldMontantTotal.getText()),
                fieldModePaiement.getText()
        );
        try {
            service.add(f);
            data.setAll(service.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        clearFields();
    }

    @FXML
    public void onEdit() {
        Facture selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setClient(service.getClientByName(comboClient.getValue()));
            selected.setEmploye(service.getEmployeByName(comboEmploye.getValue()));
            selected.setDate(dateDate.getValue());
            selected.setMontantTotal(Double.parseDouble(fieldMontantTotal.getText()));
            selected.setModePaiement(fieldModePaiement.getText());

            try {
                service.update(selected);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            tableView.refresh();
            clearFields();
        } else {
            showAlert("Sélectionnez une facture à modifier.");
        }
    }

    @FXML
    public void onDelete() {
        Facture selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette facture ?", ButtonType.YES, ButtonType.NO);
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
            showAlert("Sélectionnez une facture à supprimer.");
        }
    }

    @FXML
    public void onSearch() {
        String query = fieldRechercher.getText();
        List<Facture> f = null;
        try {
            f = service.search(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (f != null) {
            data.setAll(f);
        } else {
            showAlert("Aucune facture trouvée !");
        }
    }

    @FXML
    public void onExportExcel() {
        service.exportToFile("factures.xlsx");
        showAlert("Exportation réussie !");
    }

    @FXML
    public void onImportExcel() {
        service.importFromFile("factures.xlsx");
        try {
            data.setAll(service.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        showAlert("Importation réussie !");
    }

    private void clearFields() {
        comboClient.setValue(null);
        comboEmploye.setValue(null);
        dateDate.setValue(LocalDate.now());
        fieldMontantTotal.clear();
        fieldModePaiement.clear();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
