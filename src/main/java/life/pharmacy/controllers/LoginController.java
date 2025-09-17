package life.pharmacy.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import life.pharmacy.Launcher;
import life.pharmacy.config.DatabaseInitializer;
import life.pharmacy.models.Employe;
import life.pharmacy.services.AuthService;
import life.pharmacy.services.EmployeService;

import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    public TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        try {
            if (authService.authenticate(u, p)) {
                // ouvrir dashboard
                var root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/life/pharmacy/dashboard-view.fxml")));
                var stage = new Stage();
                stage.setTitle("Life Pharmacy — Tableau de bord");
                var scene = new Scene((Parent) root);
//                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/theme.css")).toExternalForm());
                stage.setScene(scene);
                stage.show();
                ((Stage) loginButton.getScene().getWindow()).close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Identifiants incorrects! Veuillez réessayer.").showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DatabaseInitializer.initializeDatabase();
        System.out.println("Authentification...");
    }
}
