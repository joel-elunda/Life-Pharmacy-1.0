package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import life.pharmacy.models.Client;
import life.pharmacy.services.ClientService;

import java.util.List;

public class ClientController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML private TableView<Client> tableView;
    @FXML private TableColumn<Client, Number> colId;
    @FXML private TableColumn<Client, String> colNom;
    @FXML private TableColumn<Client, String> colTel;
    @FXML private TableColumn<Client, String> colEmail;
    @FXML private Button addButton, editButton, deleteButton;

    private final ClientService service = new ClientService();


    private void reload(String q){
        try {
            List<Client> data = (q==null || q.isBlank()) ? service.getAll() : service.search(q);
            tableView.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) { err(e); }
    }

    @FXML private void onAdd(){ /* ouvrir dialog, créer Client, service.add(...) puis reload */ }
    @FXML private void onEdit(){ /* idem update */ }
    @FXML private void onDelete(){ var c = tableView.getSelectionModel().getSelectedItem(); if(c!=null){ try{ service.delete(c.getId()); reload(null);}catch(Exception e){err(e);} } }

    private void err(Throwable t){ new Alert(Alert.AlertType.ERROR, t.getMessage()).showAndWait(); }


    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        // Initialization code here
        System.out.println("Client started...");

        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colNom.setCellValueFactory(c -> c.getValue().nomCompletProperty());
        colTel.setCellValueFactory(c -> c.getValue().telephoneProperty());
        colEmail.setCellValueFactory(c -> c.getValue().emailProperty());
        reload(null);
        searchField.textProperty().addListener((o,ov,nv)->reload(nv));

    }
}
