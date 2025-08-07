package life.pharmacy.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import life.pharmacy.services.RecetteService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecetteView {

    private BorderPane view;
    private LineChart<String, Number> lineChart;
    private BarChart<String, Number> barChart;
    private TableView<RecetteService.PeriodTotal> table;
    private ObservableList<RecetteService.PeriodTotal> tableData;

    private DatePicker dpStart, dpEnd;
    private ChoiceBox<String> cbGranularity;

    private final DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // champs additionnels
    private ChoiceBox<String> sourceChoice; // "Recettes table" | "Factures (calculé)"


    public RecetteView() {
        view = new BorderPane();
        view.setPadding(new Insets(10));


        // dans le constructeur, initialisation top controls
        sourceChoice = new ChoiceBox<>(FXCollections.observableArrayList("Recettes table", "Factures (calculé)"));
        sourceChoice.setValue("Factures (calculé)"); // par défaut, on veut le calcul depuis factures

        ComboBox<String> paymentMethodChoice = new ComboBox<>();
        paymentMethodChoice.getItems().addAll("Tous", "Espèces", "Mobile Money", "Carte Bancaire");
        paymentMethodChoice.setValue("Tous");


        // Top controls
        dpStart = new DatePicker(LocalDate.now().minusDays(30));
        dpEnd = new DatePicker(LocalDate.now());
        cbGranularity = new ChoiceBox<>(FXCollections.observableArrayList("DAY", "MONTH", "YEAR"));
        cbGranularity.setValue("DAY");

        Button btnRefresh = new Button("Rafraîchir");
        btnRefresh.setOnAction(e -> refresh());

        Button btnExport = new Button("Exporter CSV");
        btnExport.setOnAction(e -> exportCsv());

        // ajoute sourceChoice dans top HBox
        HBox top = new HBox(8,
                new Label("Période :"), dpStart,
                new Label("→"), dpEnd,
                new Label("Granularité :"), cbGranularity,
                new Label("Source :"), sourceChoice, btnRefresh, btnExport);

        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(8));
        view.setTop(top);

        // Charts
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Période");
        yAxis.setLabel("Montant (CDF)");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Recettes");

        CategoryAxis bx = new CategoryAxis();
        NumberAxis by = new NumberAxis();
        barChart = new BarChart<>(bx, by);
        barChart.setTitle("Recettes (barres)");
        barChart.setLegendVisible(false);

        // Table
        table = new TableView<>();
        TableColumn<RecetteService.PeriodTotal, String> colPeriod = new TableColumn<>("Période");
        colPeriod.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPeriod()));
        TableColumn<RecetteService.PeriodTotal, Number> colTotal = new TableColumn<>("Total (CDF)");
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getTotal()));
        table.getColumns().addAll(colPeriod, colTotal);
        tableData = FXCollections.observableArrayList();
        table.setItems(tableData);


        // Center layout: charts above, table below
        VBox center = new VBox(10, lineChart, barChart, table);
        center.setPadding(new Insets(8));
        view.setCenter(center);

        refresh(); // premier affichage
    }

    public BorderPane getView() {
        return view;
    }


    private void exportCsv() {
        try {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Exporter Recettes (CSV)");
            chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV", "*.csv"));
            File file = chooser.showSaveDialog(view.getScene().getWindow());
            if (file == null) return;

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("Période;Total\n");
                for (RecetteService.PeriodTotal p : tableData) {
                    bw.write(p.getPeriod() + ";" + p.getTotal() + "\n");
                }
            }
            new Alert(Alert.AlertType.INFORMATION, "Export CSV terminé : " + file.getAbsolutePath()).showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur export CSV : " + ex.getMessage()).showAndWait();
        }
    }


    public void refresh() {
        LocalDate start = dpStart.getValue() != null ? dpStart.getValue() : LocalDate.now().minusDays(30);
        LocalDate end = dpEnd.getValue() != null ? dpEnd.getValue() : LocalDate.now();
        String gran = cbGranularity.getValue() != null ? cbGranularity.getValue() : "DAY";
        String source = sourceChoice.getValue();

        List<RecetteService.PeriodTotal> data;
        if ("Factures (calculé)".equals(source)) {
            // Agrégation directe depuis factures
            data = RecetteService.getAggregatedFromFactures(start, end, gran);
        } else {
            // Utilise la table recettes existante
            data = RecetteService.getAggregated(start, end, gran);
        }

        // Mettre à jour la table et les charts
        tableData.setAll(data);
        XYChart.Series<String, Number> seriesLine = new XYChart.Series<>();
        XYChart.Series<String, Number> seriesBar = new XYChart.Series<>();
        for (RecetteService.PeriodTotal p : data) {
            seriesLine.getData().add(new XYChart.Data<>(p.getPeriod(), p.getTotal()));
            seriesBar.getData().add(new XYChart.Data<>(p.getPeriod(), p.getTotal()));
        }

        lineChart.getData().clear();
        barChart.getData().clear();
        lineChart.getData().add(seriesLine);
        barChart.getData().add(seriesBar);
    }

}
