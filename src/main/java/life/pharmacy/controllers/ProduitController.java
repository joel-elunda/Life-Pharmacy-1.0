package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import life.pharmacy.controllers.dialogcontrollers.ProduitDialogController;
import life.pharmacy.models.Produit;
import life.pharmacy.services.ProduitService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ProduitController implements Initializable {

    @FXML private TableView<Produit> produitTable;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colNomCommercial;
    @FXML private TableColumn<Produit, String> colNomGenerique;
    @FXML private TableColumn<Produit, String> colCategorie;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colStock;

    @FXML private TextField txtNomCommercial;
    @FXML private TextField txtNomGenerique;
    @FXML private TextField txtCategorie;
    @FXML private TextField txtPrixVente;
    @FXML private TextField txtStock;
    @FXML private TextField searchField;
    @FXML private Button importButton, exportButton, addButton, editButton, deleteButton;

    private final ProduitService produitService = new ProduitService();
    private final ObservableList<Produit> produits = FXCollections.observableArrayList();

    // === Bouton Ajouter ===
    @FXML
    private void handleAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/life/pharmacy/dialogs/produit-dialog.fxml"));
            Parent root = loader.load();

            ProduitDialogController controller = loader.getController();
            controller.setProduit(new Produit());

            Stage dialog = new Stage();
            dialog.setTitle("Ajouter un produit");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Produit nouveau = controller.getProduit();
            if (nouveau != null) {
                try {
                    produitService.add(nouveau);
                    produitTable.getItems().setAll(produitService.getAll());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // === Bouton Modifier ===
    @FXML
    private void handleModifier(ActionEvent event) {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/life/pharmacy/dialogs/produit-dialog.fxml"));
            Parent root = loader.load();

            ProduitDialogController controller = loader.getController();
            controller.setProduit(selected);

            Stage dialog = new Stage();
            dialog.setTitle("Modifier produit");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Produit updated = controller.getProduit();
            if (updated != null) {
                try {
                    produitService.update(updated);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                produitTable.refresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // === Bouton Supprimer ===
    @FXML
    private void handleSupprimer(ActionEvent event) {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                produitService.delete(selected.getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            produitTable.getItems().remove(selected);
        }
    }

    // === Bouton Rechercher ===
    @FXML
    private void handleRechercher(KeyEvent event) {
        String query = searchField.getText();
        List<Produit> resultats = null;
        try {
            resultats = produitService.search(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        produitTable.getItems().setAll(resultats);
    }

    // === Export Excel ===
    @FXML
    private void handleExportExcel(ActionEvent event) {
        produitService.exportToFile("produits.xlsx");
    }

    // === Import Excel ===
    @FXML
    private void handleImportExcel(ActionEvent event) {
        produitService.importFromFile("produits.xlsx");
        try {
            produitTable.getItems().setAll(produitService.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    private void ajouterProduit() {
        Produit p = new Produit(
                0,
                txtNomCommercial.getText(),
                txtNomGenerique.getText(),
                "", "", "", "", "", // autres propriétés simplifiées
                Double.parseDouble(txtPrixVente.getText()),
                0.0, "", txtCategorie.getText(),
                false, null, "", Integer.parseInt(txtStock.getText()), 0
        );

        try {
            produitService.add(p);
            produits.setAll(produitService.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        clearFields();
    }

    @FXML
    private void supprimerProduit() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                produitService.delete(selected.getId());
                produits.setAll(produitService.getAll());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @FXML
    private void rechercherProduit() {
        try {
            produits.setAll(produitService.search(searchField.getText()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void clearFields() {
        txtNomCommercial.clear();
        txtNomGenerique.clear();
        txtCategorie.clear();
        txtPrixVente.clear();
        txtStock.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        colNomCommercial.setCellValueFactory(cell -> cell.getValue().nomCommercialProperty());
        colNomGenerique.setCellValueFactory(cell -> cell.getValue().nomGeneriqueProperty());
        colCategorie.setCellValueFactory(cell -> cell.getValue().categorieProperty());
        colPrix.setCellValueFactory(cell -> cell.getValue().prixVenteProperty().asObject());
        colStock.setCellValueFactory(cell -> cell.getValue().stockProperty().asObject());

        try {
            produits.addAll(produitService.getAll());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        produitTable.setItems(produits);
    }
}
