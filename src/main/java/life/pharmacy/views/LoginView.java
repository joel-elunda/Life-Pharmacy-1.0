package life.pharmacy.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import life.pharmacy.models.Utilisateur;
import life.pharmacy.services.UtilisateurService;

public class LoginView extends Stage {
    private TextField txtEmail;
    private PasswordField txtPassword;
    private Label lblMessage;

    public interface LoginSuccessHandler {
        void onLogin(Utilisateur utilisateur);
    }

    public LoginView(LoginSuccessHandler successHandler) {
        setTitle("Life Pharmacy - Connexion");
        setResizable(false);

        // Logo
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/logo.png")));
        logo.setFitHeight(80);
        logo.setPreserveRatio(true);

        // Titre
        Label lblTitre = new Label("Connexion à Life Pharmacy");
        lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Email
        txtEmail = new TextField();
        txtEmail.setPromptText("Email, ex: sample@monmail.com ");
        txtEmail.setPrefWidth(250);
        txtEmail.setPrefHeight(30);
        txtEmail.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");

        // Mot de passe
        txtPassword = new PasswordField();
        txtPassword.setPromptText("Mot de passe (*** ****");
        txtPassword.setPrefWidth(250);
        txtPassword.setPrefHeight(30);
        txtPassword.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");

        // Message d'erreur
        lblMessage = new Label();
        lblMessage.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Bouton connexion
        Button btnLogin = new Button("Se connecter");
        btnLogin.setPrefWidth(300);
        btnLogin.setPrefHeight(30);
        btnLogin.setAlignment(Pos.CENTER);
        btnLogin.setDefaultButton(true);
        btnLogin.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 5; -fx-padding: 8 16; "
        );
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 5; -fx-padding: 8 16;"
        ));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 5; -fx-padding: 8 16;"
        ));

        btnLogin.setOnAction(e -> {
            String email = txtEmail.getText().trim();
            String mdp = txtPassword.getText();

            if (email.isEmpty() || mdp.isEmpty()) {
                lblMessage.setText("Veuillez saisir email et mot de passe.");
                return;
            }

            Utilisateur user = UtilisateurService.login(email, mdp);
            if (user != null) {
                lblMessage.setText("");
                successHandler.onLogin(user);
                close();
            } else {
                lblMessage.setText("Email ou mot de passe incorrect.");
            }
        });

        VBox content = new VBox(10, logo, lblTitre, txtEmail, txtPassword, btnLogin, lblMessage);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        // Card design
        VBox card = new VBox(content);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10,0,0,2);"
        );

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: #ecf0f1;"); // fond gris clair

        Scene scene = new Scene(root, 350, 400);
        setScene(scene);
    }
}
