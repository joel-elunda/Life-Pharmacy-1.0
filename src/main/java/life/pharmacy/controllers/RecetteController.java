package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import life.pharmacy.models.Recette;
import life.pharmacy.services.RecetteService;
import org.apache.poi.ss.formula.functions.T;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static life.pharmacy.controllers.DashboardController.exportImportService;

public class RecetteController implements Initializable {

    @FXML private ComboBox<String> comboPeriode;
    @FXML private Button btnActualiser;
    @FXML private Button btnImprimer;
    @FXML private Button btnExporter;
    @FXML private Button btnResetYear;
    @FXML private LineChart<String, Number> chartRecettes;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML
    private Label lblTotal; // pour afficher le total de la période (optionnel)

    private final RecetteService service = new RecetteService();

    // Recettes
    @FXML private void onExportRecettes() {
        if(exportImportService.exportRecettes(new DashboardController().getStage()))
            showAlert("Exportation réussie!");
    }

    @FXML private void onImportRecettes() {
        if(exportImportService.importRecettes(new DashboardController().getStage()))
            showAlert("Importation réussie!");
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Recette en cours... ");
//        comboPeriode.setItems(FXCollections.observableArrayList(
//                "Jour", "Semaine", "Mois", "Trimestre", "Semestre", "Année"
//        ));
//        comboPeriode.getSelectionModel().select("Mois");
        comboPeriode.getItems().addAll("Jour", "Semaine", "Mois", "Trimestre", "Semestre", "Année");
        comboPeriode.setValue("Mois");

        // initial load
        btnActualiser.setOnAction(e -> loadChart());
        btnExporter.setOnAction(e -> exportExcel());
        btnImprimer.setOnAction(e -> printChart());
        btnResetYear.setOnAction(e -> resetYearDialog());

        loadChart();

        handleRefresh();
    }

    private Stage getStage() {
        return (Stage) chartRecettes.getScene().getWindow();
    }

    private void loadChart() {
        String periode = comboPeriode.getValue();
        try {
            Map<String, Double> series = service.getRevenueSeries(periode);
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            serie.setName("Chiffre d'affaires");

            double total = 0.0;
            for (Map.Entry<String, Double> e : series.entrySet()) {
                String label = e.getKey();
                Number value = e.getValue();
                serie.getData().add(new XYChart.Data<>(label, value));
                total += value.doubleValue();
            }

            chartRecettes.getData().clear();
            chartRecettes.getData().add(serie);

            if (lblTotal != null) {
                lblTotal.setText(String.format("Total (%s) : %.2f", periode, total));
            }
        } catch (SQLException ex) {
            showError("Erreur lors du chargement des données : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Export via FileChooser -> appelle service.exportToFile(filename)
    private void exportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les recettes");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(getStage());
        if (file == null) return;

        try {
            service.exportToFile(file.getAbsolutePath());
            new Alert(Alert.AlertType.INFORMATION, "Export terminé avec succès !").showAndWait();
        } catch (SQLException | IOException e) {
            showError("Erreur d'export : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Impression du chart
    private void printChart() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(getStage())) {
            boolean success = job.printPage(chartRecettes);
            if (success) job.endJob();
        }
    }

    // Demande confirmation et réinitialise (supprime) les recettes d'une année
    private void resetYearDialog() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(java.time.LocalDate.now().getYear()));
        dialog.setTitle("Réinitialiser année");
        dialog.setHeaderText("Réinitialiser les recettes d'une année");
        dialog.setContentText("Entrez l'année (ex: 2025) :");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String input = result.get().trim();
        try {
            int year = Integer.parseInt(input);
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Voulez-vous vraiment supprimer toutes les recettes de l'année " + year + " ?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> ok = confirm.showAndWait();
            if (ok.isPresent() && ok.get() == ButtonType.YES) {
                service.resetRecettesYear(year);
                new Alert(Alert.AlertType.INFORMATION, "Recettes de l'année " + year + " supprimées.").showAndWait();
                loadChart();
            }
        } catch (NumberFormatException nfe) {
            new Alert(Alert.AlertType.ERROR, "Année invalide.").showAndWait();
        } catch (SQLException ex) {
            showError("Erreur lors de la réinitialisation : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    @FXML
    private void handleRefresh() {
        String periode = comboPeriode.getValue();

        List<Recette> recettes = null;
        try {
            recettes = service.getByPeriode(periode);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Chiffre d’affaires");

        for (Recette r : recettes) {
            series.getData().add(new XYChart.Data<>(r.getPeriode(), r.getMontant()));
        }

        chartRecettes.getData().clear();
        chartRecettes.getData().add(series);
    }

    @FXML
    private void handlePrint() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(chartRecettes.getScene().getWindow())) {
            boolean success = job.printPage(chartRecettes);
            if (success) {
                job.endJob();
            }
        }
    }


}
