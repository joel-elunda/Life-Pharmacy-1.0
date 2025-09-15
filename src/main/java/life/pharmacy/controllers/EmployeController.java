package life.pharmacy.controllers;

import javafx.fxml.Initializable;
import life.pharmacy.models.Employe;
import life.pharmacy.services.EmployeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EmployeController implements Initializable {

    @FXML private TextField fiedlNomComplet;
    @FXML private ComboBox<String> comboRole;
    @FXML private TextField fieldLogin;
    @FXML private PasswordField fieldMotDePasseHash;
    @FXML private ListView<CheckBox> Permissions;
    @FXML private TextField fieldRechercher;

    @FXML public static TableView<Employe> tableView;
    @FXML private TableColumn<Employe, Number> colId;
    @FXML private TableColumn<Employe, String> colNom;
    @FXML private TableColumn<Employe, String> colRole;
    @FXML private TableColumn<Employe, String> colLogin;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;

    public static final EmployeService service = new EmployeService();
    private final ObservableList<Employe> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

    }

    public static void reloadEmploye() { reloadEmploye(null); }
    private static void reloadEmploye( String q) {
        try {
            var list = (q == null || q.isBlank()) ? service.getAll() : service.search(q);
            tableView.getItems().setAll(list);
        } catch (Exception e) { DashboardController.showError(e); }
    }

    @FXML
    public void onAdd() {
        StringBuilder perms = new StringBuilder();
        for (CheckBox cb : Permissions.getItems()) {
            if (cb.isSelected()) perms.append(cb.getText()).append(",");
        }
        Employe e = new Employe(
                service.getNextId(),
                fiedlNomComplet.getText(),
                comboRole.getValue(),
                fieldLogin.getText(),
                fieldMotDePasseHash.getText(),
                perms.toString()
        );
        try {
            service.add(e);
            data.setAll(service.getAll());
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        clearFields();
    }

    @FXML
    public void onEdit() {
        Employe selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            StringBuilder perms = new StringBuilder();
            for (CheckBox cb : Permissions.getItems()) {
                if (cb.isSelected()) perms.append(cb.getText()).append(",");
            }
            selected.setNomComplet(fiedlNomComplet.getText());
            selected.setRole(comboRole.getValue());
            selected.setLogin(fieldLogin.getText());
            selected.setMotDePasseHash(fieldMotDePasseHash.getText());
            selected.setPermissions(perms.toString());

            try {
                service.update(selected);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            tableView.refresh();
            clearFields();
        } else {
            showAlert("Sélectionnez un employé à modifier.");
        }
    }

    @FXML
    public void onDelete() {
        Employe selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet employé ?", ButtonType.YES, ButtonType.NO);
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
            showAlert("Sélectionnez un employé à supprimer.");
        }
    }

    @FXML
    public void onSearch() {
        String query = fieldRechercher.getText();
        List<Employe> e = null;
        try {
            e = service.search(query);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        if (e != null) {
            data.setAll(e);
        } else {
            showAlert("Aucun employé trouvé !");
        }
    }

    @FXML
    public void onExportExcel() {
        service.exportToFile("employes.xlsx");
        showAlert("Exportation réussie !");
    }

    @FXML
    public void onImportExcel() {
        service.importFromFile("employes.xlsx");
        try {
            data.setAll(service.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        showAlert("Importation réussie !");
    }

    private void clearFields() {
        fiedlNomComplet.clear();
        comboRole.setValue(null);
        fieldLogin.clear();
        fieldMotDePasseHash.clear();
        for (CheckBox cb : Permissions.getItems()) cb.setSelected(false);
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colNom.setCellValueFactory(cell -> cell.getValue().nomCompletProperty());
        colRole.setCellValueFactory(cell -> cell.getValue().roleProperty());
        colLogin.setCellValueFactory(cell -> cell.getValue().loginProperty());


        try {
            data.addAll(service.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        tableView.setItems(data);
    }
}
