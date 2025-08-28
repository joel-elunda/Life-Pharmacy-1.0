package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import life.pharmacy.models.Fournisseur;
import life.pharmacy.services.FournisseurService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FournisseurController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML private TableView<Fournisseur> tableView;
    @FXML private TableColumn<Fournisseur, Number> colId;
    @FXML private TableColumn<Fournisseur, String> colNom;
    @FXML private TableColumn<Fournisseur, String> colContact;
    @FXML private TableColumn<Fournisseur, String> colTel;
    @FXML private TableColumn<Fournisseur, String> colEmail;

    private final FournisseurService service = new FournisseurService();

    @FXML public void initialize(){

    }

    private void reload(String q){
        try {
            List<Fournisseur> data = (q==null || q.isBlank()) ? service.getAll() : service.search(q);
            tableView.setItems(FXCollections.observableArrayList(data));
        } catch(Exception e){ new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Fournisseur started... ");
        colId.setCellValueFactory(c->c.getValue().idProperty());
        colNom.setCellValueFactory(c->c.getValue().nomProperty());
        colContact.setCellValueFactory(c->c.getValue().contactProperty());
        colTel.setCellValueFactory(c->c.getValue().telephoneProperty());
        colEmail.setCellValueFactory(c->c.getValue().emailProperty());
        reload(null);
        searchField.textProperty().addListener((o,ov,nv)->reload(nv));
    }
}
