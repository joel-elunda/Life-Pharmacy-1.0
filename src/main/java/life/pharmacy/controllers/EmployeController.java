package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import life.pharmacy.models.Employe;
import life.pharmacy.services.EmployeService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EmployeController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<Employe> tableView;
    @FXML private TableColumn<Employe, Number> colId;
    @FXML
    private TableColumn<Employe, String> colNom;
    @FXML private TableColumn<Employe, String> colRole;
    @FXML private TableColumn<Employe, String> colLogin;

    private final EmployeService service = new EmployeService();

    private void reload(String q){
        try {
            List<Employe> data = (q==null || q.isBlank()) ? service.getAll() : service.search(q);
            tableView.setItems(FXCollections.observableArrayList(data));
        } catch(Exception e){ new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Employe started... ");
        colId.setCellValueFactory(c->c.getValue().idProperty());
        colNom.setCellValueFactory(c->c.getValue().nomCompletProperty());
        colRole.setCellValueFactory(c->c.getValue().roleProperty());
        colLogin.setCellValueFactory(c->c.getValue().loginProperty());
        reload(null);
        searchField.textProperty().addListener((o,ov,nv)->reload(nv));
    }
}
