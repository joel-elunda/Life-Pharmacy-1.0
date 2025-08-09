package life.pharmacy;

import javafx.application.Application;
import javafx.stage.Stage;
import life.pharmacy.models.Utilisateur;
import life.pharmacy.utils.Session;
import life.pharmacy.views.FacturationView;
import life.pharmacy.views.LoginView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Ouvre d'abord la fenêtre de login
        LoginView login = new LoginView((Utilisateur user) -> {
            // Quand login OK : save session et ouvrir la caisse
            Session.setCurrentUser(user);

            // Ouvrir la vue de facturation sur le même primaryStage
            FacturationView fv = new FacturationView();
            fv.show(primaryStage);
        });

        // Montrer la fenêtre de login
        login.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
