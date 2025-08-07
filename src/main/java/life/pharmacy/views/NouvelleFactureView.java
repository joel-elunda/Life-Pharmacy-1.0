package life.pharmacy.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import life.pharmacy.models.Client;
import life.pharmacy.models.DetailFacture;
import life.pharmacy.models.Facture;
import life.pharmacy.models.Produit;
import life.pharmacy.services.ClientService;
import life.pharmacy.services.FactureService;
import life.pharmacy.services.ProduitService;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class NouvelleFactureView extends Stage {
    private ComboBox<Client> cbClient;
    private CheckBox cbTVA;
    private TableView<DetailFacture> table;
    private ObservableList<DetailFacture> details;
    private Label lblMontant;
    private Runnable onSave;

    public NouvelleFactureView(Runnable onSave) {
        this.onSave = onSave;
        setTitle("Nouvelle Facture");
        initModality(Modality.APPLICATION_MODAL);

        // Clients
        cbClient = new ComboBox<>();
        cbClient.setItems(FXCollections.observableArrayList(ClientService.getAll()));
        cbClient.setPromptText("Sélectionner un client (optionnel)");

        // TVA
        cbTVA = new CheckBox("Inclure TVA (16%)");
        cbTVA.setSelected(true);

        // Détails produits
        details = FXCollections.observableArrayList();
        table = new TableView<>(details);

        TableColumn<DetailFacture, String> colProduit = new TableColumn<>("Produit");
        colProduit.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getProduit().getNom()));

        TableColumn<DetailFacture, Number> colQte = new TableColumn<>("Qté");
        colQte.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getQuantite()));

        TableColumn<DetailFacture, Number> colPrix = new TableColumn<>("Prix");
        colPrix.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(
                c.getValue().getPrixUnitaire()));

        TableColumn<DetailFacture, Number> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(
                c.getValue().getTotal()));

        table.getColumns().addAll(colProduit, colQte, colPrix, colTotal);

        // Bouton ajouter produit
        Button btnAjouterProduit = new Button("Ajouter produit");
        btnAjouterProduit.setOnAction(e -> showAddProductForm());

        // Montant
        lblMontant = new Label("Montant TTC : 0.0");

        // Boutons
        Button btnEnregistrer = new Button("Enregistrer");
        btnEnregistrer.setOnAction(e -> saveFacture());

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setOnAction(e -> close());

        HBox buttons = new HBox(10, btnEnregistrer, btnAnnuler);

        VBox root = new VBox(10,
                cbClient,
                cbTVA,
                table,
                btnAjouterProduit,
                lblMontant,
                buttons
        );
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 600, 400);
        setScene(scene);
    }

    private void showAddProductForm() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter produit");

        ComboBox<Produit> cbProduit = new ComboBox<>();
        cbProduit.setItems(FXCollections.observableArrayList(ProduitService.getAll()));

        TextField txtQte = new TextField();
        txtQte.setPromptText("Quantité");

        VBox content = new VBox(10, cbProduit, txtQte);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Produit produit = cbProduit.getValue();
                int quantite = Integer.parseInt(txtQte.getText());

                if (produit != null && quantite > 0 && quantite <= produit.getQuantite()) {
                    details.add(new DetailFacture(0, produit, quantite, produit.getPrixUnitaire()));
                    updateMontant();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Quantité invalide ou produit indisponible.");
                    alert.showAndWait();
                }
            }
        });
    }

    private void updateMontant() {
        double montantHT = details.stream().mapToDouble(DetailFacture::getTotal).sum();
        double montantTVA = cbTVA.isSelected() ? montantHT * 0.16 : 0.0;
        double montantTTC = montantHT + montantTVA;

        lblMontant.setText("Montant TTC : " + montantTTC);
    }

    private void saveFacture() {
        if (details.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez ajouter au moins un produit.");
            alert.showAndWait();
            return;
        }

        double montantHT = details.stream().mapToDouble(DetailFacture::getTotal).sum();
        double montantTVA = cbTVA.isSelected() ? montantHT * 0.16 : 0.0;
        double montantTTC = montantHT + montantTVA;

        Facture facture = new Facture(0,
                LocalDateTime.now(),
                cbClient.getValue(),
                montantHT,
                montantTVA,
                montantTTC);

        facture.setDetails(new ArrayList<>(details));

        FactureService.insert(facture);

        if (onSave != null) onSave.run();
        close();
    }
}
