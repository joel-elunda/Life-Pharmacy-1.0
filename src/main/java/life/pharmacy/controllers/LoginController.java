package life.pharmacy.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import life.pharmacy.config.DatabaseInitializer;
import life.pharmacy.services.AuthService;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
        String u = usernameField.getText();
        String p = passwordField.getText();
        try {
            if (authService.authenticate(u, p)) {
                // ouvrir dashboard
                var root = FXMLLoader.load(getClass().getResource("/views/dashboard-view.fxml"));
                var stage = new Stage();
                stage.setTitle("Life Pharmacy — Dashboard");
                var scene = new Scene((Parent) root);
                scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
                ((Stage) loginButton.getScene().getWindow()).close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Identifiants incorrects").showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DatabaseInitializer.initializeDatabase();
        System.out.println("Login started...");
    }
}
