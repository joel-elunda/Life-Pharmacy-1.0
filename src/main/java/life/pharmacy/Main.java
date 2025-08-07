package life.pharmacy;

import javafx.application.Application;
import javafx.stage.Stage;
import life.pharmacy.views.DashboardView;
import life.pharmacy.views.LoginView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Affiche la fenêtre de connexion
        LoginView loginView = new LoginView(utilisateur -> {
            DashboardView dashboard = new DashboardView(utilisateur);
            dashboard.show(); // Affiche le stage
        });
        loginView.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
