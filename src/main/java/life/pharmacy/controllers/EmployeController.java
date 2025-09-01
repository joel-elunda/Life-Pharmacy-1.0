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
import life.pharmacy.controllers.dialogcontrollers.EmployeDialogController;
import life.pharmacy.models.Employe;
import life.pharmacy.services.EmployeService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EmployeController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<Employe> tableView;
    @FXML private TableColumn<Employe, Number> colId;
    @FXML private TableColumn<Employe, String> colNom;
    @FXML private TableColumn<Employe, String> colRole;
    @FXML private TableColumn<Employe, String> colLogin;
    @FXML private Button addButton, editButton, deleteButton;

    private final EmployeService service = new EmployeService();

    private void reload(String q){
        try {
            List<Employe> data = (q==null || q.isBlank()) ? service.getAll() : service.search(q);
            tableView.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) { err(e); }
    }

    @FXML private void onAdd(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/life/pharmacy/dialogs/employe-dialog-view.fxml"));
            Parent root = loader.load();

            EmployeDialogController controller = loader.getController();
            controller.setEmploye(new Employe());

            Stage dialog = new Stage();
            dialog.setTitle("Ajouter Employé");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Employe nouveau = controller.getEmploye();
            if (nouveau != null) {
                service.add(nouveau);
                tableView.getItems().setAll(service.getAll());
            }
        } catch (IOException | SQLException e) { err(e); }
    }

    @FXML private void onEdit(){
        Employe selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/life/pharmacy/dialogs/employe-dialog-view.fxml"));
            Parent root = loader.load();

            EmployeDialogController controller = loader.getController();
            controller.setEmploye(selected);

            Stage dialog = new Stage();
            dialog.setTitle("Modifier Employé");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Employe updated = controller.getEmploye();
            if (updated != null) {
                service.update(updated);
                tableView.refresh();
            }
        } catch (IOException | SQLException e) { err(e); }
    }

    @FXML private void onDelete(){
        Employe selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                service.delete(selected.getId());
                tableView.getItems().remove(selected);
            } catch (SQLException e) { err(e); }
        }
    }

    private void err(Throwable t){ new Alert(Alert.AlertType.ERROR, t.getMessage()).showAndWait(); }

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        System.out.println("Employe started...");

        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colNom.setCellValueFactory(c -> c.getValue().nomCompletProperty());
        colRole.setCellValueFactory(c -> c.getValue().roleProperty());
        colLogin.setCellValueFactory(c -> c.getValue().loginProperty());

        reload(null);
        searchField.textProperty().addListener((o,ov,nv)->reload(nv));
    }

    @FXML private void handleRechercher(KeyEvent event) {
        String query = searchField.getText();
        try {
            tableView.getItems().setAll(service.search(query));
        } catch (SQLException e) { err(e); }
    }

    @FXML private void handleExportExcel(ActionEvent event) {
        service.exportToFile("employes.xlsx");
    }

    @FXML private void handleImportExcel(ActionEvent event) {
        service.importFromFile("employes.xlsx");
        try {
            tableView.getItems().setAll(service.getAll());
        } catch (SQLException e) { err(e); }
    }


}
