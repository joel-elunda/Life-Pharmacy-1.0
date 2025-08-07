package life.pharmacy.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import life.pharmacy.models.Produit;
import life.pharmacy.services.ProduitService;

public class ProduitView {
    private BorderPane view;
    private TableView<Produit> table;
    private ObservableList<Produit> produits;

    public ProduitView() {
        produits = FXCollections.observableArrayList(ProduitService.getAll());

        // TableView
        table = new TableView<>();
        table.setItems(produits);

        TableColumn<Produit, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));

        TableColumn<Produit, String> colCode = new TableColumn<>("Code-barre");
        colCode.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCodeBarre()));

        TableColumn<Produit, Number> colPrix = new TableColumn<>("Prix Unitaire");
        colPrix.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrixUnitaire()));

        TableColumn<Produit, Number> colQuantite = new TableColumn<>("Quantité");
        colQuantite.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantite()));

        TableColumn<Produit, String> colTva = new TableColumn<>("TVA");
        colTva.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isTva() ? "Oui" : "Non"));

        table.getColumns().addAll(colNom, colCode, colPrix, colQuantite, colTva);

        // Boutons CRUD
        Button btnAjouter = new Button("Ajouter");
        btnAjouter.setOnAction(e -> showForm(null));

        Button btnModifier = new Button("Modifier");
        btnModifier.setOnAction(e -> {
            Produit selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showForm(selected);
        });

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setOnAction(e -> {
            Produit selected = table.getSelectionModel().getSelectedItem();
//            if (selected != null) {
//                ProduitService.delete(selected.getId());
//                refresh();
//            }
            if (!ProduitService.delete(selected.getId())) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible de supprimer : le produit est référencé par une ou plusieurs factures.");
                alert.showAndWait();
            } else {
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

    private void showForm(Produit produit) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(produit == null ? "Ajouter Produit" : "Modifier Produit");

        TextField txtNom = new TextField(produit != null ? produit.getNom() : "");
        txtNom.setPromptText("Nom");

        TextField txtCode = new TextField(produit != null ? produit.getCodeBarre() : "");
        txtCode.setPromptText("Code-barre");

        TextField txtPrix = new TextField(produit != null ? String.valueOf(produit.getPrixUnitaire()) : "");
        txtPrix.setPromptText("Prix unitaire");

        TextField txtQuantite = new TextField(produit != null ? String.valueOf(produit.getQuantite()) : "");
        txtQuantite.setPromptText("Quantité");

        CheckBox chkTva = new CheckBox("TVA incluse");
        if (produit != null) chkTva.setSelected(produit.isTva());

        VBox content = new VBox(10, txtNom, txtCode, txtPrix, txtQuantite, chkTva);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String nom = txtNom.getText();
                String code = txtCode.getText();
                double prix = Double.parseDouble(txtPrix.getText());
                int quantite = Integer.parseInt(txtQuantite.getText());
                boolean tva = chkTva.isSelected();

                if (produit == null) {
                    ProduitService.insert(new Produit(0, nom, code, prix, quantite, tva));
                } else {
                    produit.setNom(nom);
                    produit.setCodeBarre(code);
                    produit.setPrixUnitaire(prix);
                    produit.setQuantite(quantite);
                    produit.setTva(tva);
                    ProduitService.update(produit);
                }
                refresh();
            }
        });
    }

    public void refresh() {
        produits.setAll(ProduitService.getAll());
    }
}
