package life.pharmacy.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import life.pharmacy.models.Utilisateur;
import life.pharmacy.services.UtilisateurService;

public class UtilisateurView {
    private BorderPane view;
    private TableView<Utilisateur> table;
    private ObservableList<Utilisateur> utilisateurs;
    private Button btnAjouter, btnModifier, btnSupprimer;

    public UtilisateurView(Utilisateur currentUser) {
        view = new BorderPane();
        utilisateurs = FXCollections.observableArrayList(UtilisateurService.getAll());

        table = new TableView<>(utilisateurs);

        TableColumn<Utilisateur, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(cell -> cell.getValue().nomProperty());

        TableColumn<Utilisateur, String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(cell -> cell.getValue().roleProperty());

        table.getColumns().addAll(colNom, colRole);

        // Boutons
        btnAjouter = new Button("Ajouter");
        btnModifier = new Button("Modifier");
        btnSupprimer = new Button("Supprimer");

        HBox actions = new HBox(10, btnAjouter, btnModifier, btnSupprimer);
        actions.setPadding(new Insets(10));

        view.setCenter(table);
        view.setBottom(actions);

        // Gestion des rôles
        if (!"admin".equals(currentUser.getRole())) {
            btnAjouter.setDisable(true);
            btnModifier.setDisable(true);
            btnSupprimer.setDisable(true);
        }

        initActions();
    }

    private void initActions() {
        btnAjouter.setOnAction(e -> showForm(null));
        btnModifier.setOnAction(e -> {
            Utilisateur selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showForm(selected);
            }
        });
        btnSupprimer.setOnAction(e -> {
            Utilisateur selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet utilisateur ?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        UtilisateurService.delete(selected.getId());
                        refresh();
                    }
                });
            }
        });
    }

    private void showForm(Utilisateur utilisateur) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle(utilisateur == null ? "Ajouter un utilisateur" : "Modifier un utilisateur");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField tfNom = new TextField();
        PasswordField tfPassword = new PasswordField();
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("admin", "manager", "caissier");

        if (utilisateur != null) {
            tfNom.setText(utilisateur.getNom());
            tfPassword.setText(utilisateur.getMotDePasse());
            cbRole.setValue(utilisateur.getRole());
        }

        grid.add(new Label("Nom :"), 0, 0);
        grid.add(tfNom, 1, 0);
        grid.add(new Label("Mot de passe :"), 0, 1);
        grid.add(tfPassword, 1, 1);
        grid.add(new Label("Rôle :"), 0, 2);
        grid.add(cbRole, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (utilisateur == null) {
                    Utilisateur newUser = new Utilisateur(tfNom.getText(), tfPassword.getText(), cbRole.getValue());
                    UtilisateurService.save(newUser);
                } else {
                    utilisateur.setNom(tfNom.getText());
                    utilisateur.setMotDePasse(tfPassword.getText());
                    utilisateur.setRole(cbRole.getValue());
                    UtilisateurService.update(utilisateur);
                }
                refresh();
            }
            return null;
        });

        dialog.showAndWait();
    }

    public BorderPane getView() {
        return view;
    }

    public void refresh() {
        utilisateurs.setAll(UtilisateurService.getAll());
    }
}
