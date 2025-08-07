package life.pharmacy.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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

        // Email
        txtEmail = new TextField();
        txtEmail.setPromptText("Email");

        // Mot de passe
        txtPassword = new PasswordField();
        txtPassword.setPromptText("Mot de passe");

        // Message d'erreur
        lblMessage = new Label();
        lblMessage.setStyle("-fx-text-fill: red;");

        // Bouton connexion
        Button btnLogin = new Button("Se connecter");
        btnLogin.setDefaultButton(true);
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

        VBox root = new VBox(10,
                new Label("Connexion à Life Pharmacy"),
                txtEmail,
                txtPassword,
                btnLogin,
                lblMessage
        );
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(300);

        Scene scene = new Scene(root);
        setScene(scene);
    }
}
