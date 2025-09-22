package life.pharmacy.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
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

import static life.pharmacy.controllers.DashboardController.exportImportService;

public class EmployeController implements Initializable {

    @FXML private TextField fiedlNomComplet;
    @FXML private ComboBox<String> comboRole;
    @FXML private TextField fieldLogin;
    @FXML private PasswordField fieldMotDePasseHash;
    @FXML private CheckBox checkPermissions;
    @FXML private ComboBox<String> comboResearch;

    @FXML public  TableView<Employe> tableView;
    @FXML private TableColumn<Employe, Number> colId;
    @FXML private TableColumn<Employe, String> colNom;
    @FXML private TableColumn<Employe, String> colRole;
    @FXML private TableColumn<Employe, String> colLogin;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;

    private Employe selected = null;
    public static final EmployeService service = new EmployeService();
    private final ObservableList<Employe> data = FXCollections.observableArrayList();

    private void handleTableClick(MouseEvent event) {
        selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fiedlNomComplet.setText(selected.getNomComplet());
            comboRole.setValue(selected.getRole());
            fieldLogin.setText(selected.getLogin());
            fieldMotDePasseHash.setText(selected.getMotDePasseHash());
            if (checkPermissions.isSelected()) checkPermissions.setSelected(true);

        }
    }

    private void populateFieldsFromSelection(Employe e) {
         if (e == null) return;
         fiedlNomComplet.setText(e.getNomComplet());
         comboRole.setValue(e.getRole());
         fieldLogin.setText(e.getLogin());
         fieldMotDePasseHash.setText(e.getMotDePasseHash());
        if (checkPermissions.isSelected()) checkPermissions.setSelected(true);

    }

    public void reloadEmploye() { reloadEmploye(null); }
    private void reloadEmploye( String q) {
        try {
            var list = (q == null || q.isBlank()) ? service.getAll() : service.search(q);
            tableView.getItems().setAll(list);
        } catch (Exception e) { DashboardController.showError(e); }
    }

    @FXML
    public void onAdd() {
        Employe e = new Employe(
                service.getNextId(),
                fiedlNomComplet.getText(),
                comboRole.getValue(),
                fieldLogin.getText(),
                fieldMotDePasseHash.getText(),
                checkPermissions.getText()
        );
        try {
            if(service.ifExists(e)) {
                new Alert(Alert.AlertType.ERROR, "Utilisateur déjà enregistré", ButtonType.CANCEL).showAndWait();
            } else {
                service.add(e);
                data.setAll(service.getAll());
                clearFields();
                tableView.refresh();
            }
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
            selected.setNomComplet(fiedlNomComplet.getText());
            selected.setRole(comboRole.getValue());
            selected.setLogin(fieldLogin.getText());
            selected.setMotDePasseHash(fieldMotDePasseHash.getText());
            selected.setPermissions(checkPermissions.getText());

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
    public void onExportExcel() {
        if(exportImportService.exportEmployes(new DashboardController().getStage()))
            showAlert("Exportation réussie !");
    }

    @FXML
    public void onImportExcel() {
        if(exportImportService.importEmployes(new DashboardController().getStage()))
            showAlert("Importation réussie !");
    }

    private void clearFields() {
        fiedlNomComplet.clear();
        comboRole.setValue(null);
        fieldLogin.clear();
        fieldMotDePasseHash.clear();
        if (checkPermissions.isSelected()) checkPermissions.setSelected(true);
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

        comboRole.setItems(FXCollections.observableArrayList(
                "Administrateur", "Pharmacien(ne)", "Gérant(e)", "Vendeur(se)"
        ));

        tableView.setOnMouseClicked(this::handleTableClick);

        try {
            comboResearch.setItems(FXCollections.observableArrayList(
                    service.getAll().stream()
                            .map(Employe::getNomComplet)
                            .toList()
            ));

            // Quand on change la sélection → on affiche uniquement l’élément dans la table
            comboResearch.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
                if (selected == null) return;
                // Récupérer le nom (avant le pipe)
                String nom = selected.split("\\|")[0].trim();
                List<Employe> found = null; // ta méthode retourne 0..n éléments
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

        reloadEmploye();
    }

    @FXML
    private void onSearch(ActionEvent event) {
        String query = comboResearch.getValue().toLowerCase().trim();

        try {
            // Récupérer tous les clients
            List<Employe> employes = service.getAll();

            // Filtrer selon le texte saisi
            List<Employe> filtered = employes.stream()
                    .filter(e ->
                            e.getNomComplet().toLowerCase().contains(query) ||
                            e.getRole().toLowerCase().contains(query)
                    )
                    .toList();

            // Afficher dans la TableView
            tableView.setItems(FXCollections.observableArrayList(filtered));

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {

        try {
            this.tableView.setItems(FXCollections.observableArrayList(service.getAll()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        reloadEmploye();

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Données mises à jour avec succès !");
        alert.showAndWait();
    }
}
