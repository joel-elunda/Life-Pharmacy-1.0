package life.pharmacy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import life.pharmacy.controllers.DashboardController;

import java.io.IOException;
import java.util.Objects;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("login-view.fxml"));
        FXMLLoader fxmlDashboardLoader = new FXMLLoader(Launcher.class.getResource("dashboard-view.fxml"));

        fxmlDashboardLoader.load();
        DashboardController controller  = fxmlDashboardLoader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(fxmlLoader.load(), 300, 400);
        stage.setResizable(false);
        stage.setTitle("Authentification - Life Pharmacy");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/logo.png")))); // ton icône
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(Launcher.class, args);
    }
}
