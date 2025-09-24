package life.pharmacy.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import life.pharmacy.models.*;
import life.pharmacy.services.*;
import life.pharmacy.utils.ExportImportService;

import java.time.LocalDateTime;

public class DashboardController implements Initializable {

    // Clients
    @FXML private TextField searchClientField;
    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, Number> colClientId;
    @FXML private TableColumn<Client, String> colClientNom;
    @FXML private TableColumn<Client, String> colClientTel;

    // Produits
    @FXML private TextField searchProductField;
    @FXML private TableView<Produit> productTable;
    @FXML private TableColumn<Produit, Number> colProdId;
    @FXML private TableColumn<Produit, String> colProdNom;
    @FXML private TableColumn<Produit, String> colProdCategorie;
    @FXML private TableColumn<Produit, Number> colProdPrix;
    @FXML private TableColumn<Produit, Number> colProdStock;
    @FXML private Spinner<Integer> qtySpinner;
    @FXML private Button btnAddToCart;

    // Panier
    @FXML private TableView<LigneTransaction> cartTable;
    @FXML private TableColumn<LigneTransaction, String> colCartProd;
    @FXML private TableColumn<LigneTransaction, Number> colCartQty;
    @FXML private TableColumn<LigneTransaction, Number> colCartPrice;
    @FXML private TableColumn<LigneTransaction, Number> colCartSubtotal;
    @FXML private Button btnRemoveLine, btnClearCart;
    @FXML private Label totalLabel;

    // Paiement / preview
    @FXML private ComboBox<String> paymentCombo;
    @FXML private Label currentUserLabel;
    @FXML private TextArea invoicePreview;
    @FXML private Button printButton, cancelButton;
    @FXML private Button logoutButton;

    private final ClientService clientService = new ClientService();
    private final ProduitService produitService = new ProduitService();
    private final FactureService factureService = new FactureService();
    private final FournisseurService fournisseurService = new FournisseurService();
    private final EmployeService employeService = new EmployeService();
    private final RecetteService recetteService = new RecetteService();
    private final TransactionService transactionService = new TransactionService();
    private final LigneTransactionService ligneService = new LigneTransactionService();

    private FournisseurController fournisseurController;
    private FactureController factureController;
    private EmployeController employeController;

    private final ObservableList<LigneTransaction> panier = FXCollections.observableArrayList();
    private Employe currentUser;
    public static ExportImportService exportImportService = new ExportImportService();

    @FXML private Button refreshButton;
    @FXML private Button btnAddClient;
    @FXML private Button facturesButton;
    @FXML private Button employesButton;
    @FXML private Button recettesButton;
    @FXML private ToolBar toolBar;

    private Stage stage;


    public void setCurrentUser(Employe user) {
        this.currentUser = user;
        System.out.println("Connecté : " + user.getNomComplet() + " (" + user.getRole() + ")");

        switch (user.getRole()) {
            case "Administrateur":
                // Admin → accès total
                break;
            case "Pharmacien(ne)", "Caissier(e)":
                toolBar.getItems().removeAll(employesButton, recettesButton);
                break;
            default:
                toolBar.getItems().removeAll(employesButton, recettesButton, facturesButton);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    @FXML
    private void onAddClient(ActionEvent event) {
        this.handleClients(event);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        try {

            FXMLLoader fournisseurLoader = new FXMLLoader(getClass().getResource("/life/pharmacy/fournisseur-view.fxml"));
            fournisseurLoader.load();
            fournisseurController = fournisseurLoader.getController();

            // Charger Factures
            FXMLLoader factureLoader = new FXMLLoader(getClass().getResource("/life/pharmacy/facture-view.fxml"));
            factureLoader.load();
            factureController = factureLoader.getController();

            // Charger Employés
            FXMLLoader employeLoader = new FXMLLoader(getClass().getResource("/life/pharmacy/employe-view.fxml"));
            employeLoader.load();
            employeController = employeLoader.getController();

            if (fournisseurController != null && fournisseurController.tableView != null) {
                fournisseurController.tableView.setItems(FXCollections.observableArrayList(fournisseurService.getAll()));
                fournisseurController.reloadFournisseurs(null);
            }

            if (factureController != null && factureController.tableView != null) {
                factureController.tableView.setItems(FXCollections.observableArrayList(factureService.getAll()));
                factureController.reloadFactures(null);
            }

            if (employeController != null && employeController.tableView != null) {
                employeController.tableView.setItems(FXCollections.observableArrayList(employeService.getAll()));
                employeController.reloadEmploye();
            }

            reloadClients();
            reloadProduits();

            new Alert(Alert.AlertType.INFORMATION, "Données mises à jour avec succès !").showAndWait();

            clientTable.setItems(FXCollections.observableArrayList(clientService.getAll()));
            productTable.setItems(FXCollections.observableArrayList(produitService.getAll()));
            fournisseurController.tableView.setItems(FXCollections.observableArrayList(fournisseurService.getAll()));
            factureController.tableView.setItems(FXCollections.observableArrayList(factureService.getAll()));
            employeController.tableView.setItems(FXCollections.observableArrayList(employeService.getAll()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Ajout au panier
    @FXML
    private void onAddToCart() {
        Produit p = productTable.getSelectionModel().getSelectedItem();
        if (p == null) { alert("Sélectionnez un produit"); return; }
        int qte = qtySpinner.getValue();
        if (qte <= 0) { alert("Quantité invalide"); return; }
        if (p.getStock() < qte) { alert("Stock insuffisant"); return; }

        // ligne déjà présente ? (par produitId)
        for (LigneTransaction lt : panier) {
            if (lt.getProduitId() == p.getId()) {
                lt.setProduit(p);
                lt.setQuantite(lt.getQuantite() + qte);
                updateTotalsAndPreview();
                return;
            }
        }

        LigneTransaction line = new LigneTransaction(0, 0, p.getId(), p.getNomCommercial(), qte, p.getPrixVente(), null);
        line.setProduit(p);
        line.setProduitNom(p.getNomCommercial()); // pour affichage immédiat dans la table
        panier.add(line);
        updateTotalsAndPreview();
    }

    @FXML
    private void onRemoveLine(){ var sel = cartTable.getSelectionModel().getSelectedItem(); if (sel!=null){panier.remove(sel); updateTotalsAndPreview();}}

    @FXML
    private void onClearCart(){ panier.clear(); updateTotalsAndPreview(); }

    private void updateTotalsAndPreview() {
        double total = panier.stream().mapToDouble(LigneTransaction::getSousTotal).sum();
        totalLabel.setText(String.format("%.2f", total));

        Client cli = clientTable.getSelectionModel().getSelectedItem();

        StringBuilder sb = new StringBuilder();
        sb.append("FACTURE\n");
        sb.append("Client: ").append(cli != null ? cli.getNomComplet() : "Anonyme").append("\n");
        sb.append("Caissier: ").append(currentUser != null ? currentUser.getNomComplet() : "").append("\n");
        sb.append("Mode de paiement: ").append(
                paymentCombo.getValue() != null ? paymentCombo.getValue() : "Non défini"
        ).append("\n\n");

        sb.append(String.format("%-30s %5s %10s %12s\n", "Produit", "Qté", "P.U.", "Sous-Total"));
        sb.append("---------------------------------------------------------------\n");
        for (LigneTransaction lt : panier) {
            String nom = (lt.getProduit() != null)
                    ? lt.getProduit().getNomCommercial()
                    : lt.getProduitNom(); // fallback sur le champ produitNom
                    sb.append(String.format("%-30s %5d %10.2f %12.2f\n",
                    nom, lt.getQuantite(), lt.getPrixUnitaire(), lt.getSousTotal()));
        }
//        for (LigneTransaction lt : panier) {
//            sb.append(String.format("%-30s %5d %10.2f %12.2f\n",
//                    lt.getProduit().getNomCommercial(), lt.getQuantite(), lt.getPrixUnitaire(), lt.getSousTotal()));
//        }
        sb.append("---------------------------------------------------------------\n");
        sb.append(String.format("TOTAL: %.2f\n", total));
        invoicePreview.setText(sb.toString());
    }

    @FXML
    private void onPrintAndSave() {
        if (panier.isEmpty()) {
            alert("Panier vide");
            return;
        }

        String mode = paymentCombo.getValue();
        Client cli = clientTable.getSelectionModel().getSelectedItem(); // peut être null (vente anonyme)
        double total = panier.stream().mapToDouble(LigneTransaction::getSousTotal).sum();

        // Impression simple (aperçu texte) : PrinterJob optionnel — ici on enregistre surtout
        try {
            // si aucun client sélectionné → utiliser "Anonyme" (id = 1)
            int clientId = (cli != null) ? cli.getId() : 1;

            // Enregistrer transaction
            Transaction t = new Transaction(
                    0,
                    LocalDateTime.now(),
                    total,
                    "payé",
                    (mode != null ? mode : "Non défini"),
                    clientId,
                    currentUser.getId()
            );

            transactionService.add(t);

            // Récupérer ID transaction créée (si service renvoie l'id, sinon on recharge le max)
            int transId = transactionService.getLastInsertId();

            // Enregistrer lignes + MAJ stock
            for (LigneTransaction lt : panier) {
                lt.setTransactionId(transId);
                ligneService.add(lt);
                Produit p = lt.getProduit();
                p.setStock(p.getStock() - lt.getQuantite());
                produitService.update(p);
            }

            alert("Facture enregistrée !");
            panier.clear();
            updateTotalsAndPreview();
            reloadProduits(); // stock rafraîchi
        } catch (Exception e) {
            e.printStackTrace();
            showError(e);
        }
    }

    @FXML private void onCancel(){ panier.clear(); updateTotalsAndPreview(); }

    private void alert(String msg){ new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    public static void showError(Throwable t){ new Alert(Alert.AlertType.ERROR, t.getMessage()).showAndWait(); }

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        // Initialization code here
        System.out.println("Tableau de bord lancé...");

        // user courant (exemple : admin)
        currentUser = new Employe();
        currentUser.setId(1);
        currentUser.setNomComplet("Administrateur");
        currentUser.setRole("Administrateur");
        currentUserLabel.setText(currentUser.getNomComplet());

        // colonnes clients
        colClientId.setCellValueFactory(c -> c.getValue().idProperty());
        colClientId.setVisible(false);
        colClientNom.setCellValueFactory(c -> c.getValue().nomCompletProperty());
        colClientTel.setCellValueFactory(c -> c.getValue().telephoneProperty());

        // colonnes produits
        colProdId.setCellValueFactory(c -> c.getValue().idProperty());
        colProdId.setVisible(false);
        colProdNom.setCellValueFactory(c -> c.getValue().nomCommercialProperty());
        colProdCategorie.setCellValueFactory(c -> c.getValue().categorieProperty());
        colProdPrix.setCellValueFactory(c -> c.getValue().prixVenteProperty());
        colProdStock.setCellValueFactory(c -> c.getValue().stockProperty());

        // colonnes panier
        colCartProd.setCellValueFactory(cellData -> {
            LigneTransaction lt = cellData.getValue();
            if (lt.getProduit() != null) {
                return lt.getProduit().nomCommercialProperty();
            } else {
                return new SimpleStringProperty(lt.getProduitNom()); // fallback sur produitNom
            }
        });

        colCartQty.setCellValueFactory(c -> c.getValue().quantiteProperty());
        colCartPrice.setCellValueFactory(c -> c.getValue().prixUnitaireProperty());
        colCartSubtotal.setCellValueFactory(c -> c.getValue().sousTotalProperty());

        cartTable.setItems(panier);

        // données
        reloadClients();
        reloadProduits();

        // spinner qté
        qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));

        // modes de paiement
        paymentCombo.getItems().setAll("Espèces", "Carte", "Mobile Money", "Chèque");
        paymentCombo.getSelectionModel().selectFirst();

        // filtre clients
        searchClientField.textProperty().addListener((obs, old, val) -> reloadClients(val));
        // filtre produits
        searchProductField.textProperty().addListener((obs, old, val) -> reloadProduits(val));

        // total bind manuel (recalc à chaque modif de panier)
        panier.addListener((ListChangeListener<LigneTransaction>) change -> updateTotalsAndPreview());
    }

    public void openDashboard() {
        openView("/life/pharmacy/dashboard-view.fxml", "Dashboard - Life Pharmacy");
    }
    @FXML
    private void handleProduits(ActionEvent event) {
        openView("/life/pharmacy/produit-view.fxml", "Produits");
    }

    @FXML
    private void handleFactures(ActionEvent event) {
        openView("/life/pharmacy/facture-view.fxml", "Factures");
    }

    @FXML
    private void handleClients(ActionEvent event) {
        openView("/life/pharmacy/client-view.fxml", "Clients");
    }

    @FXML private void handleEmployes(ActionEvent event) {
        openView("/life/pharmacy/employe-view.fxml", "Employés");
    }

    @FXML
    private void handleFournisseurs(ActionEvent event) {
        openView("/life/pharmacy/fournisseur-view.fxml", "Fournisseurs");
    }

    @FXML
    private void handleRecettes(ActionEvent event) {
        openView("/life/pharmacy/recette-view.fxml", "Recettes");
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        // Fermer le dashboard et revenir à la page de login
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/life/pharmacy/login-view.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Connexion - Life Pharmacy");
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title + " - Life Pharmacy");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reloadClients() {
        reloadClients(null);
    }
    private void reloadClients(String q) {
        try {
            var list = (q == null || q.isBlank()) ? clientService.getAll() : clientService.search(q);
            clientTable.getItems().setAll(list);
        } catch (Exception e) {
            //showError(e);
            System.out.println("Création de la table clients...");
        }
    }

    private void reloadProduits() {
        reloadProduits(null);
    }
    private void reloadProduits(String q) {
        try {
            var list = (q == null || q.isBlank()) ? produitService.getAll() : produitService.search(q);
            productTable.getItems().setAll(list);
        } catch (Exception e) {
//            showError(e);
            System.out.println("Création de la table produits...");
        }
    }

}
