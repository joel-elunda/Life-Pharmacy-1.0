package life.pharmacy.controllers.dialogcontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import life.pharmacy.models.Employe;
import java.net.URL;
import java.util.ResourceBundle;

public class EmployeDialogController implements Initializable {

    @FXML
    private TextField txtNom;
    @FXML private TextField txtRole;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private Stage dialogStage;
    private Employe employe;
    private boolean okClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;

        if (employe != null) {
            txtNom.setText(employe.getNomComplet());
            txtRole.setText(employe.getRole());
            txtLogin.setText(employe.getLogin());
            txtPassword.setText(employe.getMotDePasseHash());
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            employe.setNomComplet(txtNom.getText());
            employe.setRole(txtRole.getText());
            employe.setLogin(txtLogin.getText());
            employe.setMotDePasseHash(txtPassword.getText());

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
            errorMessage += "Nom requis !\n";
        }
        if (txtRole.getText() == null || txtRole.getText().trim().isEmpty()) {
            errorMessage += "Rôle requis !\n";
        }
        if (txtLogin.getText() == null || txtLogin.getText().trim().isEmpty()) {
            errorMessage += "Login requis !\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            System.err.println("Erreur validation Employé :\n" + errorMessage);
            return false;
        }
    }

    public Employe getEmploye() {
        return employe;
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
