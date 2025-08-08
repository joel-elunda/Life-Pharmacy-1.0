package life.pharmacy.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainFacturationView extends Stage {

    public MainFacturationView() {
        setTitle("Life Pharmacy - Facturation");
        setResizable(true);

        // ====== HEADER ======
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.getStyleClass().add("header");

        // Logo
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/logo.png")));
        logoView.setFitHeight(50);
        logoView.setPreserveRatio(true);

        // Titre
        Label title = new Label("PharmaSoft - Facturation");
        title.getStyleClass().add("header-title");

        header.getChildren().addAll(logoView, title);

        // ====== BOUTONS PRINCIPAUX ======
        HBox menuButtons = new HBox(20);
        menuButtons.setAlignment(Pos.CENTER);
        menuButtons.setPadding(new Insets(20));

        Button btnFacturer = new Button("💰 Facturer");
        btnFacturer.getStyleClass().add("main-btn");
        btnFacturer.setOnAction(e -> facturer());

        Button btnNouvelleFacture = new Button("🆕 Nouvelle facture");
        btnNouvelleFacture.getStyleClass().add("main-btn");
        btnNouvelleFacture.setOnAction(e -> nouvelleFacture());

        Button btnRapports = new Button("📊 Rapports");
        btnRapports.getStyleClass().add("main-btn");
        btnRapports.setOnAction(e -> genererRapport());

        menuButtons.getChildren().addAll(btnFacturer, btnNouvelleFacture, btnRapports);

        // ====== LAYOUT PRINCIPAL ======
        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(menuButtons);

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(getClass().getResource("/css/facturation.css").toExternalForm());

        setScene(scene);
    }

    // ==== MÉTHODES À IMPLÉMENTER ====
    private void facturer() {
        System.out.println("Ouverture de l'écran de facturation...");
        // TODO : Implémenter logique de facturation
    }

    private void nouvelleFacture() {
        System.out.println("Préparer une nouvelle facture...");
        // TODO : Réinitialiser les champs / préparer l'interface
    }

    private void genererRapport() {
        System.out.println("Génération du rapport...");
        // TODO : Générer rapport PDF ou affichage statistique
    }
}
