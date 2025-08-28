package life.pharmacy.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import life.pharmacy.models.Recette;
import life.pharmacy.services.RecetteService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RecetteController implements Initializable {

    @FXML private LineChart<String, Number> recetteChart;
    @FXML private Button printButton, refreshButton, exportButton;
    @FXML private ComboBox<String> filtrePeriode;

    private final RecetteService recetteService = new RecetteService();

    private void updateChart(String periode) {
        List<Recette> recettes = recetteService.getByPeriode(periode);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Chiffre d'affaires");

        for (Recette r : recettes) {
            series.getData().add(new XYChart.Data<String, Number>(r.getDate().toString(), r.getMontant()));
        }

        recetteChart.getData().clear();
        recetteChart.getData().add(series);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Recette started... ");
        filtrePeriode.setItems(FXCollections.observableArrayList(
                "Jour", "Semaine", "Mois", "Trimestre", "Semestre", "Année"
        ));
        filtrePeriode.getSelectionModel().select("Mois");

        updateChart("Mois");

        filtrePeriode.setOnAction(e -> updateChart(filtrePeriode.getValue()));
    }
}
