package life.pharmacy.controllers.dialogcontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import life.pharmacy.models.Fournisseur;

import java.net.URL;
import java.util.ResourceBundle;

public class FournisseurDialogController implements Initializable {


    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtEmail;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private Stage dialogStage;
    private Fournisseur fournisseur;
    private boolean okClicked = false;

    // Appelé par le MainApp ou le contrôleur parent pour injecter la fenêtre
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Remplit les champs quand on édite un fournisseur existant
    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;

        if (fournisseur != null) {
            txtNom.setText(fournisseur.getNom());
            txtAdresse.setText(fournisseur.getAdresse());
            txtTelephone.setText(fournisseur.getTelephone());
            txtEmail.setText(fournisseur.getEmail());
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            fournisseur.setNom(txtNom.getText());
            fournisseur.setAdresse(txtAdresse.getText());
            fournisseur.setTelephone(txtTelephone.getText());
            fournisseur.setEmail(txtEmail.getText());

            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            errorMessage += "Nom du fournisseur requis !\n";
        }
        if (txtTelephone.getText() == null || txtTelephone.getText().trim().isEmpty()) {
            errorMessage += "Téléphone requis !\n";
        }
        if (txtEmail.getText() == null || txtEmail.getText().trim().isEmpty()) {
            errorMessage += "Email requis !\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            System.err.println("Erreur validation Fournisseur : \n" + errorMessage);
            return false;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
