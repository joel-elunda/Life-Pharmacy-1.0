package life.pharmacy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 400);
        stage.setResizable(false);
        stage.setTitle("Life Pharmacy 1.0");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(Launcher.class, args);
    }
}
