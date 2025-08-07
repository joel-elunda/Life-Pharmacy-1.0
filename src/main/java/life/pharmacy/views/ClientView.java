package life.pharmacy.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import life.pharmacy.models.Client;
import life.pharmacy.services.ClientService;

public class ClientView {
    private BorderPane view;
    private TableView<Client> table;
    private ObservableList<Client> clients;

    public ClientView() {
        clients = FXCollections.observableArrayList(ClientService.getAll());

        // TableView
        table = new TableView<>();
        table.setItems(clients);

        TableColumn<Client, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));

        TableColumn<Client, String> colTelephone = new TableColumn<>("Téléphone");
        colTelephone.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTelephone()));

        TableColumn<Client, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));

        table.getColumns().addAll(colNom, colTelephone, colEmail);

        // Boutons CRUD
        Button btnAjouter = new Button("Ajouter");
        btnAjouter.setOnAction(e -> showForm(null));

        Button btnModifier = new Button("Modifier");
        btnModifier.setOnAction(e -> {
            Client selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showForm(selected);
        });

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setOnAction(e -> {
            Client selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ClientService.delete(selected.getId());
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

    private void showForm(Client client) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(client == null ? "Ajouter Client" : "Modifier Client");

        TextField txtNom = new TextField(client != null ? client.getNom() : "");
        txtNom.setPromptText("Nom");

        TextField txtTelephone = new TextField(client != null ? client.getTelephone() : "");
        txtTelephone.setPromptText("Téléphone");

        TextField txtEmail = new TextField(client != null ? client.getEmail() : "");
        txtEmail.setPromptText("Email");

        VBox content = new VBox(10, txtNom, txtTelephone, txtEmail);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String nom = txtNom.getText();
                String telephone = txtTelephone.getText();
                String email = txtEmail.getText();

                if (client == null) {
                    ClientService.insert(new Client(0, nom, telephone, email));
                } else {
                    client.setNom(nom);
                    client.setTelephone(telephone);
                    client.setEmail(email);
                    ClientService.update(client);
                }
                refresh();
            }
        });
    }

    public void refresh() {
        clients.setAll(ClientService.getAll());
    }
}
