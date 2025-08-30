package life.pharmacy.controllers;

import javafx.collections.FXCollections;
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
import life.pharmacy.controllers.dialogcontrollers.ClientDialogController;
import life.pharmacy.models.Client;
import life.pharmacy.services.ClientService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ClientController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, Number> colId;
    @FXML private TableColumn<Client, String> colNom;
    @FXML private TableColumn<Client, String> colTelephone;
    @FXML private TableColumn<Client, String> colEmail;
    @FXML private Button addButton, editButton, deleteButton;

    private final ClientService service = new ClientService();


    private void reload(String q){
        try {
            List<Client> data = (q==null || q.isBlank()) ? service.getAll() : service.search(q);
            clientTable.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) { err(e); }
    }

    @FXML private void onAdd(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/life/pharmacy/views/ClientDialog.fxml"));
            Parent root = loader.load();

            ClientDialogController controller = loader.getController();
            controller.setClient(new Client());

            Stage dialog = new Stage();
            dialog.setTitle("Ajouter Client");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Client nouveau = controller.getClient();
            if (nouveau != null) {
                try {
                    service.add(nouveau);
                    clientTable.getItems().setAll(service.getAll());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML private void onEdit(){
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/life/pharmacy/dialogs/client-dialog.fxml"));
            Parent root = loader.load();

            ClientDialogController controller = loader.getController();
            controller.setClient(selected);

            Stage dialog = new Stage();
            dialog.setTitle("Modifier Client");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Client updated = controller.getClient();
            if (updated != null) {
                try {
                    service.update(updated);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                clientTable.refresh();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML private void onDelete(){ var c = clientTable.getSelectionModel().getSelectedItem(); if(c!=null){ try{ service.delete(c.getId()); reload(null);}catch(Exception e){err(e);} } }

    private void err(Throwable t){ new Alert(Alert.AlertType.ERROR, t.getMessage()).showAndWait(); }


    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        // Initialization code here
        System.out.println("Client started...");

        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colNom.setCellValueFactory(c -> c.getValue().nomCompletProperty());
        colTelephone.setCellValueFactory(c -> c.getValue().telephoneProperty());
        colEmail.setCellValueFactory(c -> c.getValue().emailProperty());
        reload(null);
        searchField.textProperty().addListener((o,ov,nv)->reload(nv));

    }

    @FXML
    private void handleRechercher(KeyEvent event) {
        String query = searchField.getText();
        try {
            clientTable.getItems().setAll(service.search(query));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleExportExcel(ActionEvent event) {
        service.exportToFile("clients.xlsx");
    }

    @FXML
    private void handleImportExcel(ActionEvent event) {
        service.importFromFile("clients.xlsx");
        try {
            clientTable.getItems().setAll(service.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
