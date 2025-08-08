package life.pharmacy.views;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import life.pharmacy.models.Client;
import life.pharmacy.models.Produit;
import life.pharmacy.services.ClientService;
import life.pharmacy.services.ProduitService;

public class FacturationView {

    private final ClientService clientService = new ClientService();
    private final ProduitService produitService = new ProduitService();

    private final TableView<ProduitFacture> tableProduits = new TableView<>();
    private final ObservableList<ProduitFacture> produitsFacture = FXCollections.observableArrayList();

    private final Label lblSousTotal = new Label("0.00");
    private final Label lblTaxes = new Label("0.00");
    private final Label lblTotal = new Label("0.00");

    public void afficher(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // --- TOP BAR ---
        HBox topBar = creerTopBar(stage);
        root.setTop(topBar);

        // --- CLIENT + PRODUITS ---
        VBox centre = new VBox(15);
        centre.getChildren().addAll(creerSectionClient(), creerSectionProduits());
        root.setCenter(centre);

        // --- BAS : RÉSUMÉ + ACTIONS ---
        VBox bas = new VBox(15);
        bas.getChildren().addAll(creerSectionResume(), creerBoutonsActions());
        root.setBottom(bas);

        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("Facturation - Pharmacie");
        stage.setScene(scene);
        stage.show();
    }

    private HBox creerTopBar(Stage stage) {
        Label titre = new Label("💊 Facturation Pharmacie");
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button btnNouvelle = new Button("Nouvelle facture");
        Button btnRapports = new Button("Rapports");
        Button btnDeconnexion = new Button("Déconnexion");

        HBox bar = new HBox(20, titre, new Spacer(), btnNouvelle, btnRapports, btnDeconnexion);
        bar.setPadding(new Insets(10));
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private VBox creerSectionClient() {
        ComboBox<Client> cmbClients = new ComboBox<>();
        cmbClients.setItems(FXCollections.observableArrayList(clientService.getAll()));
        cmbClients.setPromptText("Sélectionner un client");

        Button btnAjoutClient = new Button("➕ Ajouter client");

        HBox ligne = new HBox(10, cmbClients, btnAjoutClient);
        ligne.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(5, new Label("Client"), ligne);
        return box;
    }

    private VBox creerSectionProduits() {
        TextField txtRecherche = new TextField();
        txtRecherche.setPromptText("Rechercher un produit...");
        txtRecherche.textProperty().addListener((obs, old, nouv) -> {
            // TODO: filtrer la liste selon le texte
        });

        // Colonnes
        TableColumn<ProduitFacture, String> colNom = new TableColumn<>("Produit");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<ProduitFacture, Number> colPrix = new TableColumn<>("Prix");
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));

        TableColumn<ProduitFacture, Number> colQuantite = new TableColumn<>("Quantité");
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colQuantite.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.NumberStringConverter()));
        colQuantite.setOnEditCommit(e -> {
            e.getRowValue().setQuantite((Integer) e.getNewValue());
            calculerTotaux();
        });

        TableColumn<ProduitFacture, Number> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrix() * c.getValue().getQuantite()));

        tableProduits.getColumns().addAll(colNom, colPrix, colQuantite, colTotal);
        tableProduits.setItems(produitsFacture);
        tableProduits.setEditable(true);

        Button btnAjouter = new Button("➕ Ajouter produit");
        btnAjouter.setOnAction(e -> {
            // TODO: ouvrir un sélecteur de produit
        });

        VBox box = new VBox(10, new Label("Produits"), txtRecherche, tableProduits, btnAjouter);
        return box;
    }

    private VBox creerSectionResume() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Sous-total :"), 0, 0);
        grid.add(lblSousTotal, 1, 0);

        grid.add(new Label("Taxes :"), 0, 1);
        grid.add(lblTaxes, 1, 1);

        grid.add(new Label("Total :"), 0, 2);
        grid.add(lblTotal, 1, 2);

        VBox box = new VBox(5, new Label("Résumé facture"), grid);
        return box;
    }

    private HBox creerBoutonsActions() {
        Button btnValider = new Button("✅ Valider");
        btnValider.setOnAction(e -> {
            // TODO: enregistrer la facture
        });

        Button btnImprimer = new Button("🖨️ Imprimer");
        Button btnAnnuler = null;
        btnAnnuler.setOnAction(e -> {
            produitsFacture.clear();
            calculerTotaux();
        });

        btnAnnuler = new Button("❌ Annuler");

        HBox bar = new HBox(15, btnValider, btnImprimer, btnAnnuler);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(10, 0, 0, 0));
        return bar;
    }

    private void calculerTotaux() {
        double sousTotal = produitsFacture.stream().mapToDouble(p -> p.getPrix() * p.getQuantite()).sum();
        double taxes = sousTotal * 0.16;
        double total = sousTotal + taxes;

        lblSousTotal.setText(String.format("%.2f", sousTotal));
        lblTaxes.setText(String.format("%.2f", taxes));
        lblTotal.setText(String.format("%.2f", total));
    }

    // --- Classe interne pour lier Produit à la facture ---
    public static class ProduitFacture {
        private final StringProperty nom;
        private final DoubleProperty prix;
        private final IntegerProperty quantite;

        public ProduitFacture(String nom, double prix, int quantite) {
            this.nom = new SimpleStringProperty(nom);
            this.prix = new SimpleDoubleProperty(prix);
            this.quantite = new SimpleIntegerProperty(quantite);
        }

        public String getNom() { return nom.get(); }
        public void setNom(String value) { nom.set(value); }
        public StringProperty nomProperty() { return nom; }

        public double getPrix() { return prix.get(); }
        public void setPrix(double value) { prix.set(value); }
        public DoubleProperty prixProperty() { return prix; }

        public int getQuantite() { return quantite.get(); }
        public void setQuantite(int value) { quantite.set(value); }
        public IntegerProperty quantiteProperty() { return quantite; }
    }

    // --- Espaceur dynamique pour HBox ---
    private static class Spacer extends Region {
        public Spacer() {
            HBox.setHgrow(this, Priority.ALWAYS);
        }
    }
}
