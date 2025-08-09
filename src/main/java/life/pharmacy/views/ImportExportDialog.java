package life.pharmacy.views;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import life.pharmacy.services.ClientService;
import life.pharmacy.services.FournisseurService;
import life.pharmacy.services.ProduitService;

import java.io.File;

public class ImportExportDialog extends Stage{

    public static void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Import / Export des données");

        Label lblModel = new Label("Sélectionnez un modèle :");
        ComboBox<String> cbModel = new ComboBox<>();
        cbModel.getItems().addAll("Client", "Fournisseur", "Produit", "Facture", "Utilisateur", "Recette");
        cbModel.getSelectionModel().selectFirst();

        Button btnImport = new Button("Importer");
        Button btnExport = new Button("Exporter");

        btnImport.setOnAction(e -> {
            String model = cbModel.getValue();
            FileChooser fc = new FileChooser();
            fc.setTitle("Sélectionnez un fichier CSV/Excel à importer");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel/CSV", "*.xlsx", "*.xls", "*.csv"));
            File file = fc.showOpenDialog(dialog);
            if (file != null) {
                switch (model) {
                    case "Client" -> ClientService.importCSV(file);
                    case "Fournisseur" -> FournisseurService.importCSV(file);
                    case "Produit" -> ProduitService.importCSV(file);
                }
                showAlert("Importation réussie pour " + model + " !");
            }
        });

        btnExport.setOnAction(e -> {
            String model = cbModel.getValue();
            FileChooser fc = new FileChooser();
            fc.setTitle("Enregistrer le fichier exporté");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
            File file = fc.showSaveDialog(dialog);
            if (file != null) {
                switch (model) {
                    case "Client" -> ClientService.exportCSV(file);
                    case "Fournisseur" -> FournisseurService.exportCSV(file);
                    case "Produit" -> ProduitService.exportCSV(file);
                }
                showAlert("Exportation réussie pour " + model + " !");
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(lblModel, 0, 0);
        grid.add(cbModel, 1, 0);
        grid.add(btnImport, 0, 1);
        grid.add(btnExport, 1, 1);

        dialog.setScene(new Scene(grid));
        dialog.showAndWait();
    }

    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
