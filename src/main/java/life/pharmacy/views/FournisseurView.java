package life.pharmacy.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import life.pharmacy.models.Fournisseur;
import life.pharmacy.services.FournisseurService;

public class FournisseurView {
    private BorderPane view;
    private TableView<Fournisseur> table;
    private ObservableList<Fournisseur> fournisseurs;

    public FournisseurView() {
        fournisseurs = FXCollections.observableArrayList(FournisseurService.getAll());

        // TableView
        table = new TableView<>();
        table.setItems(fournisseurs);

        TableColumn<Fournisseur, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));

        TableColumn<Fournisseur, String> colContact = new TableColumn<>("Contact");
        colContact.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getContact()));

        TableColumn<Fournisseur, String> colAdresse = new TableColumn<>("Adresse");
        colAdresse.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAdresse()));

        table.getColumns().addAll(colNom, colContact, colAdresse);

        // Boutons CRUD
        Button btnAjouter = new Button("Ajouter");
        btnAjouter.setOnAction(e -> showForm(null));

        Button btnModifier = new Button("Modifier");
        btnModifier.setOnAction(e -> {
            Fournisseur selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showForm(selected);
        });

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setOnAction(e -> {
            Fournisseur selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                FournisseurService.delete(selected.getId());
                refresh();
            }
        });

        HBox actions = new HBox(10, btnAjouter, btnModifier, btnSupprimer);
        actions.setPadding(new Insets(10));

        view = new BorderPane();
        view.setCenter(table);
        view.setBottom(actions);
    }

    public BorderPane getView() {
        return view;
    }

    private void showForm(Fournisseur fournisseur) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(fournisseur == null ? "Ajouter Fournisseur" : "Modifier Fournisseur");

        TextField txtNom = new TextField(fournisseur != null ? fournisseur.getNom() : "");
        txtNom.setPromptText("Nom");

        TextField txtContact = new TextField(fournisseur != null ? fournisseur.getContact() : "");
        txtContact.setPromptText("Contact");

        TextField txtAdresse = new TextField(fournisseur != null ? fournisseur.getAdresse() : "");
        txtAdresse.setPromptText("Adresse");

        VBox content = new VBox(10, txtNom, txtContact, txtAdresse);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String nom = txtNom.getText();
                String contact = txtContact.getText();
                String adresse = txtAdresse.getText();

                if (fournisseur == null) {
                    FournisseurService.insert(new Fournisseur(0, nom, contact, adresse));
                } else {
                    fournisseur.setNom(nom);
                    fournisseur.setContact(contact);
                    fournisseur.setAdresse(adresse);
                    FournisseurService.update(fournisseur);
                }
                refresh();
            }
        });
    }

    public void refresh() {
        fournisseurs.setAll(FournisseurService.getAll());
    }
}
