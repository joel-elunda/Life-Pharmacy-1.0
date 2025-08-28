package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import life.pharmacy.models.Facture;
import life.pharmacy.services.FactureService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class FactureController implements Initializable {
    @FXML
    private TextField searchFactureField;
    @FXML private TableView<Facture> factureTable;
    @FXML private TableColumn<Facture, Number> colId;
    @FXML private TableColumn<Facture, String> colClient;
    @FXML private TableColumn<Facture, String> colEmploye;
    @FXML private TableColumn<Facture, String> colDate;
    @FXML private TableColumn<Facture, Number> colMontant;
    @FXML private TableColumn<Facture, String> colModePaiement;
    @FXML private Button searchButton, refreshButton, newButton, editButton, deleteButton, exportButton;

    private final FactureService service = new FactureService();

    @FXML
    public void initialize() {

    }

    @FXML private void onSearch(){ reload(searchFactureField.getText()); }
    @FXML private void onRefresh(){ reload(null); }

    private void reload(String q){
        try {
            List<Facture> data = (q==null || q.isBlank()) ? service.getAll() : service.search(q);
            factureTable.setItems(FXCollections.observableArrayList(data));
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Facture started...");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colClient.setCellValueFactory(c -> c.getValue().clientProperty().get().nomCompletProperty());
        colEmploye.setCellValueFactory(c -> c.getValue().employeProperty().get().nomCompletProperty());
        colDate.setCellValueFactory(c -> c.getValue().dateProperty().asString());
        colMontant.setCellValueFactory(c -> c.getValue().montantTotalProperty());
        colModePaiement.setCellValueFactory(c -> c.getValue().modePaiementProperty());
        reload(null);
    }
}
