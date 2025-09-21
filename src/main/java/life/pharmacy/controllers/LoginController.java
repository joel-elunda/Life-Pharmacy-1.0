package life.pharmacy.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
            Employe loggedIn = authService.authenticate(u, p);

            if (loggedIn != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/life/pharmacy/dashboard-view.fxml"));
                Parent root = loader.load();

                // Récupérer le contrôleur
                DashboardController dashboardController = loader.getController();
                dashboardController.setCurrentUser(loggedIn);

                Stage stage = new Stage();
                stage.setTitle("Life Pharmacy — Tableau de bord");
                stage.setScene(new Scene(root));

                // ✅ ajoute l’icône aussi pour le Dashboard
                stage.getIcons().add(new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/image/logo.png"))));

                stage.show();

                // Fermer login
                ((Stage) loginButton.getScene().getWindow()).close();

            } else {
                new Alert(Alert.AlertType.ERROR, "Identifiants incorrects! Veuillez réessayer.").showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DatabaseInitializer.initializeDatabase();
        System.out.println("Authentification...");
    }
}
