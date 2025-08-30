package life.pharmacy.controllers.dialogcontrollers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import life.pharmacy.models.Produit;

import java.net.URL;
import java.util.ResourceBundle;

public class ProduitDialogController implements Initializable {
    @FXML
    private TextField txtNom;
    @FXML private TextField txtGenerique;
    @FXML private TextField txtCodeBarres;
    @FXML private TextField txtPrixAchat;
    @FXML private TextField txtPrixVente;
    @FXML private TextField txtStock;

    private Produit produit;

    public void setProduit(Produit p) {
        this.produit = p;
        if (p != null) {
            txtNom.setText(p.getNomCommercial());
            txtGenerique.setText(p.getNomGenerique());
            txtCodeBarres.setText(p.getCodeBarres());
            txtPrixAchat.setText(String.valueOf(p.getPrixAchat()));
            txtPrixVente.setText(String.valueOf(p.getPrixVente()));
            txtStock.setText(String.valueOf(p.getStock()));
        }
    }

    public Produit getProduit() {
        return produit;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (produit != null) {
            produit.setNomCommercial(txtNom.getText());
            produit.setNomGenerique(txtGenerique.getText());
            produit.setCodeBarres(txtCodeBarres.getText());
            produit.setPrixAchat(Double.parseDouble(txtPrixAchat.getText()));
            produit.setPrixVente(Double.parseDouble(txtPrixVente.getText()));
            produit.setStock(Integer.parseInt(txtStock.getText()));
        }
        ((Stage) txtNom.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        produit = null;
        ((Stage) txtNom.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
