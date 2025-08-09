package life.pharmacy.views;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import life.pharmacy.models.Utilisateur;
import life.pharmacy.services.*;

import java.io.File;

public class DashboardView extends Stage {

    private BorderPane view;

    private ClientView clientView;
    private ProduitView produitView;
    private FactureView factureView;
    private RecetteView recetteView;
    private UtilisateurView utilisateurView;

    public DashboardView(Utilisateur utilisateur) {

        produitView = new ProduitView();
        clientView = new ClientView();
        factureView = new FactureView();

        // dans le constructeur
        recetteView = new RecetteView();


        // --- Création des onglets ---
        TabPane tabPane = new TabPane();

        Tab tabProduits = new Tab("Produits", new ProduitView().getView());
        tabProduits.setClosable(false);

        Tab tabClients = new Tab("Clients", new ClientView().getView());
        tabClients.setClosable(false);

        Tab tabFournisseurs = new Tab("Fournisseurs", new FournisseurView().getView());
        tabFournisseurs.setClosable(false);

        Tab tabFactures = new Tab("Factures", new FactureView().getView());
        tabFactures.setClosable(false);

        Tab tabRecettes = new Tab("Recettes", new RecetteView().getView());
        tabRecettes.setClosable(false);

        Tab tabUtilisateurs = new Tab("Utilisateurs", new UtilisateurView(utilisateur).getView());
        tabUtilisateurs.setClosable(false);

        // === Contrôle des droits ===
        switch (utilisateur.getRole()) {
            case "admin" ->
                    tabPane.getTabs().addAll(tabProduits, tabClients, tabFournisseurs, tabFactures, tabRecettes, tabUtilisateurs);
            case "manager" ->
                    tabPane.getTabs().addAll(tabProduits, tabClients, tabFournisseurs, tabFactures, tabRecettes);
            case "caissier" -> tabPane.getTabs().addAll(tabClients, tabFactures);
        }

        // barre d’actions (boutons du haut)
        // on peut aussi masquer certains boutons


        // --- Barre d’actions en haut ---
        Button btnFacturer = new Button("Facturer");
        Button btnNouvelleFacture = new Button("Nouvelle facture");
        Button btnImporterExcel = new Button("Importer Excel");
        Button btnRapports = new Button("Rapports");
        Button btnLogout = new Button("Déconnexion");

        btnImporterExcel.setDisable(!utilisateur.getRole().equals("admin"));

        btnLogout.setOnAction(e -> {
            // Ferme le dashboard et réaffiche le login
            this.hide();
            LoginView loginView = new LoginView(user -> {
                DashboardView newDashboard = new DashboardView(user);
                this.setScene(newDashboard.getScene());
                this.show();
            });
            loginView.show();
        });

        btnImporterExcel.setOnAction(e -> {
            ChoiceDialog<String> choice = new ChoiceDialog<>("Produits",
                    "Produits", "Clients", "Utilisateurs", "Recettes", "Factures");
            choice.setTitle("Importer Excel");
            choice.setHeaderText("Choisir le type de données à importer");
            choice.setContentText("Type :");
            choice.showAndWait().ifPresent(type -> {
                FileChooser fc = new FileChooser();
                fc.setTitle("Importer " + type);
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx", "*.xls"));
                File file = fc.showOpenDialog(this); // stage : ton primaryStage / Stage courant
                if (file == null) return;

                // Exécuter l'import en background
                javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            switch (type) {
                                case "Produits" -> ProduitService.importCSV(file);
                                case "Clients" -> ClientService.importCSV(file);
                                case "Utilisateurs" -> UtilisateurService.importCSV(file);
                                case "Recettes" -> RecetteService.importCSV(file);
                                case "Factures" -> FactureService.importCSV(file);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            throw ex;
                        }
                        return null;
                    }
                };

                task.setOnSucceeded(ev -> {
                    // refresh UI sur le thread JavaFX
                    switch (type) {
                        case "Produits" -> {
                            if (produitView != null) produitView.refresh();
                        }
                        case "Clients" -> {
                            if (clientView != null) clientView.refresh();
                        }
                        case "Utilisateurs" -> {
                            if (utilisateurView != null) utilisateurView.refresh();
                        }
                        case "Recettes" -> {
                            if (recetteView != null) recetteView.refresh();
                        }
                        case "Factures" -> {
                            if (factureView != null) factureView.refresh();
                        }
                    }
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Import " + type + " terminé !");
                    a.showAndWait();
                });

                task.setOnFailed(ev -> {
                    Throwable ex = task.getException();
                    Alert a = new Alert(Alert.AlertType.ERROR, "Erreur pendant l'import : " + (ex != null ? ex.getMessage() : "Erreur inconnue"));
                    a.showAndWait();
                });

                new Thread(task).start();
            });
        });


        HBox actionsBar = new HBox(10, btnFacturer, btnNouvelleFacture, btnImporterExcel, btnRapports, btnLogout);
        actionsBar.setPadding(new Insets(10));

        // --- Placement général ---
        view = new BorderPane();
        view.setTop(actionsBar);
        view.setCenter(tabPane);

        this.setScene(new Scene(view, 900, 600));
    }

    public BorderPane getView() {
        return view;
    }
}
