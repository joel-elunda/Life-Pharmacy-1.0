package life.pharmacy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import life.pharmacy.controllers.DashboardController;

import java.io.IOException;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("login-view.fxml"));
        FXMLLoader fxmlDashboardLoader = new FXMLLoader(Launcher.class.getResource("dashboard-view.fxml"));

        fxmlDashboardLoader.load();
        DashboardController controller  = fxmlDashboardLoader.getController();
        controller.setStage(stage); // injection du stage

        Scene scene = new Scene(fxmlLoader.load(), 300, 400);
        stage.setResizable(false);
        stage.setTitle("Se connecter - Life Pharmacy");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(Launcher.class, args);
    }
}
