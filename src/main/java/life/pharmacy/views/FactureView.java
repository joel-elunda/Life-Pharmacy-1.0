package life.pharmacy.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import life.pharmacy.models.Facture;
import life.pharmacy.models.Utilisateur;
import life.pharmacy.services.FactureService;

public class FactureView {
    private BorderPane view;
    private TableView<Facture> table;
    private ObservableList<Facture> factures;
    private Utilisateur currentUser; // utilisateur connecté (pour vérifier les droits)

    public FactureView() {
        factures = FXCollections.observableArrayList(FactureService.getAll());

        // TableView
        table = new TableView<>();
        table.setItems(factures);

        TableColumn<Facture, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDate().toString()));

        TableColumn<Facture, String> colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getClient() != null ? c.getValue().getClient().getNom() : "N/A"));

        TableColumn<Facture, Number> colMontantTTC = new TableColumn<>("Montant TTC");
        colMontantTTC.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(
                c.getValue().getMontantTTC()));


        // Colonne Actions (Aperçu/Imprimer)
        TableColumn<Facture, Void> colActions = new TableColumn<>("Actions");
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnPreview = new Button("Aperçu / Imprimer");
            private final Button btnDelete = new Button("Supprimer");

            {
                btnPreview.setOnAction(event -> {
                    Facture facture = getTableView().getItems().get(getIndex());
                    ImpressionFactureView preview = new ImpressionFactureView(facture);
                    preview.showAndWait();
                });

                // Supprimer
                btnDelete.setOnAction(event -> {
                    Facture facture = getTableView().getItems().get(getIndex());
                    if (facture == null) return;

                    // Confirmation utilisateur avant suppression
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Voulez-vous vraiment supprimer la facture n°" + facture.getId() + " ?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirmer la suppression");
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            boolean ok = FactureService.deleteByIdIfAllowed(facture.getId(), currentUser.getRole());
                            if (!ok) {
                                // Suppression refusée (droits insuffisants ou erreur)
                                Alert err = new Alert(Alert.AlertType.ERROR,
                                        "Suppression interdite ou erreur. Seuls les admins peuvent supprimer une facture.");
                                err.setHeaderText("Suppression impossible");
                                err.showAndWait();
                            } else {
                                // Suppression réussie -> rafraîchir la table
                                refresh();
                                Alert info = new Alert(Alert.AlertType.INFORMATION, "Facture supprimée.");
                                info.showAndWait();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnPreview);
                }
            }
        });


        table.getColumns().addAll(colDate, colClient, colMontantTTC, colActions);

        // Boutons
        Button btnNouvelle = new Button("Nouvelle Facture");


        btnNouvelle.setOnAction(e -> {
            NouvelleFactureView form = new NouvelleFactureView(() -> refresh());
            form.show();
        });

        Button btnDetails = new Button("Détails");
        btnDetails.setOnAction(e -> {
            Facture selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showDetails(selected);
            }
        });

        HBox actions = new HBox(10, btnNouvelle, btnDetails);
        actions.setPadding(new Insets(10));

        view = new BorderPane();
        view.setCenter(table);
        view.setBottom(actions);
    }

    public BorderPane getView() {
        return view;
    }

    public void refresh() {
        factures.setAll(FactureService.getAll());
    }

    private void showDetails(Facture facture) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la facture");
        alert.setHeaderText("Facture #" + facture.getId());

        StringBuilder details = new StringBuilder();
        details.append("Date : ").append(facture.getDate()).append("\n");
        details.append("Client : ").append(facture.getClient() != null ? facture.getClient().getNom() : "N/A").append("\n");
        details.append("Montant HT : ").append(facture.getMontantHT()).append("\n");
        details.append("Montant TVA : ").append(facture.getMontantTVA()).append("\n");
        details.append("Montant TTC : ").append(facture.getMontantTTC()).append("\n\n");

        details.append("Produits :\n");
        facture.getDetails().forEach(d ->
                details.append("- ")
                        .append(d.getProduit().getNom())
                        .append(" x").append(d.getQuantite())
                        .append(" @ ").append(d.getPrixUnitaire())
                        .append(" = ").append(d.getTotal())
                        .append("\n")
        );

        alert.setContentText(details.toString());
        alert.showAndWait();
    }
}
