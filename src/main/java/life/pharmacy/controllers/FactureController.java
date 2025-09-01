package life.pharmacy.controllers;

import life.pharmacy.models.Facture;
import life.pharmacy.services.FactureService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class FactureController {

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

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colClient.setCellValueFactory(cell -> cell.getValue().clientProperty());
        colEmploye.setCellValueFactory(cell -> cell.getValue().employeProperty());
        colMontantTotal.setCellValueFactory(cell -> cell.getValue().montantTotalProperty());

        data.addAll(service.getFactures());
        tableView.setItems(data);
    }

    @FXML
    public void onAdd() {
        Facture f = new Facture(
                service.getNextId(),
                comboClient.getValue(),
                comboEmploye.getValue(),
                dateDate.getValue(),
                Double.parseDouble(fieldMontantTotal.getText()),
                fieldModePaiement.getText()
        );
        service.addFacture(f);
        data.setAll(service.getFactures());
        clearFields();
    }

    @FXML
    public void onEdit() {
        Facture selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setClient(comboClient.getValue());
            selected.setEmploye(comboEmploye.getValue());
            selected.setDate(dateDate.getValue());
            selected.setMontantTotal(Double.parseDouble(fieldMontantTotal.getText()));
            selected.setModePaiement(fieldModePaiement.getText());

            service.updateFacture(selected);
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
                service.deleteFacture(selected.getId());
                data.setAll(service.getFactures());
            }
        } else {
            showAlert("Sélectionnez une facture à supprimer.");
        }
    }

    @FXML
    public void onSearch() {
        String query = fieldRechercher.getText();
        Facture f = service.search(query);
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
        data.setAll(service.getFactures());
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
