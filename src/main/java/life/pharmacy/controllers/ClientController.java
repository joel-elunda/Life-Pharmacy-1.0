package life.pharmacy.controllers;


import javafx.fxml.Initializable;
import life.pharmacy.models.Client;
import life.pharmacy.services.ClientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML private TextField fieldNom;
    @FXML private DatePicker dateDateNaissance;
    @FXML private TextField fieldAdresse;
    @FXML private TextField fieldTelephone;
    @FXML private TextField fieldEmail;
    @FXML private TextArea areaConditionsMedicales;
    @FXML private TextArea areaAllergies;
    @FXML private TextField fieldRechercher;

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

    private final ClientService service = new ClientService();
    private final ObservableList<Client> data = FXCollections.observableArrayList();


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
    public void onSearch() {
        String query = fieldRechercher.getText();
        List<Client> c = null;
        try {
            c = service.search(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (c != null) {
            data.setAll(c);
        } else {
            showAlert("Aucun client trouvé !");
        }
    }

    @FXML
    public void onExportExcel() {
        service.exportToFile("clients.xlsx");
        showAlert("Exportation réussie !");
    }

    @FXML
    public void onImportExcel() {
        service.importFromFile("clients.xlsx");
        try {
            data.setAll(service.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        showAlert("Importation réussie !");
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

        try {
            data.addAll(service.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        tableView.setItems(data);
    }
}


