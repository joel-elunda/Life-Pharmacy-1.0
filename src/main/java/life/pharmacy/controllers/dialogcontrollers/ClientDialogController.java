package life.pharmacy.controllers.dialogcontrollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import life.pharmacy.models.Client;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientDialogController implements Initializable {
    @FXML private TextField txtNomComplet;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtAdresse;

    private Client client;

    public void setClient(Client c) {
        this.client = c;
        if (c != null) {
            txtNomComplet.setText(c.getNomComplet());
            txtTelephone.setText(c.getTelephone());
            txtEmail.setText(c.getEmail());
            txtAdresse.setText(c.getAdresse());
        }
    }

    public Client getClient() { return client; }

    @FXML
    private void handleSave(ActionEvent event) {
        if (client != null) {
            client.setNomComplet(txtNomComplet.getText());
            client.setTelephone(txtTelephone.getText());
            client.setEmail(txtEmail.getText());
            client.setAdresse(txtAdresse.getText());
        }
        ((Stage) txtNomComplet.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        client = null;
        ((Stage) txtNomComplet.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
