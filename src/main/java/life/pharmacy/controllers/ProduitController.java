package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import life.pharmacy.models.Produit;
import life.pharmacy.services.ProduitService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProduitController implements Initializable {

    @FXML private TableView<Produit> produitTable;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colNomCommercial;
    @FXML private TableColumn<Produit, String> colNomGenerique;
    @FXML private TableColumn<Produit, String> colCategorie;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, Integer> colStock;

    @FXML private TextField txtNomCommercia;
    @FXML private TextField txtNomGenerique;
    @FXML private TextField txtCategorie;
    @FXML private TextField txtPrixVente;
    @FXML private TextField txtStock;
    @FXML private TextField searchField;
    @FXML private Button importButton, exportButton, addButton, editButton, deleteButton;

    private final ProduitService produitService = new ProduitService();
    private final ObservableList<Produit> produits = FXCollections.observableArrayList();


    @FXML
    private void ajouterProduit() {
        Produit p = new Produit(
                0,
                txtNomCommercia.getText(),
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
        txtNomCommercia.clear();
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
