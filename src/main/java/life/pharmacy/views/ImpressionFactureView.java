package life.pharmacy.views;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import life.pharmacy.models.DetailFacture;
import life.pharmacy.models.Facture;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Vue d'aperçu / impression de facture avec choix de format : Ticket (thermique étroit) ou A4.
 */
public class ImpressionFactureView extends Stage {

    private final Facture facture;
    private final BorderPane root;
    private Node currentContentNode;
    private final ChoiceBox<String> formatChoice;

    private static final String FORMAT_TICKET = "Ticket (80mm)";
    private static final String FORMAT_A4 = "A4";

    public ImpressionFactureView(Facture facture) {
        this.facture = facture;
        setTitle("Aperçu - Facture #" + facture.getId());
        initModality(Modality.APPLICATION_MODAL);

        root = new BorderPane();
        root.setPadding(new Insets(12));

        // Top bar : choix format + titre
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Aperçu Facture");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #0D47A1;");

        formatChoice = new ChoiceBox<>();
        formatChoice.getItems().addAll(FORMAT_TICKET, FORMAT_A4);
        formatChoice.setValue(FORMAT_A4);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(title, spacer, new Label("Format :"), formatChoice);
        root.setTop(topBar);

        // center: preview area (will be built depending on format)
        rebuildContent();

        // bottom: buttons
        Button btnPrint = new Button("🖨 Imprimer");
        btnPrint.setStyle("-fx-background-color: #0D47A1; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPrint.setOnAction(e -> printCurrent());

        Button btnPdf = new Button("⬇️ Exporter en PDF");
        btnPdf.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPdf.setOnAction(e -> exportPdfDialog());

        Button btnClose = new Button("Fermer");
        btnClose.setOnAction(e -> close());

        HBox btns = new HBox(10, btnPdf, btnPrint, btnClose);
        btns.setAlignment(Pos.CENTER_RIGHT);
        btns.setPadding(new Insets(8, 0, 0, 0));

        VBox bottomBox = new VBox(btns);
        bottomBox.setPadding(new Insets(6));
        root.setBottom(bottomBox);

        // Rebuild preview when format changes
        formatChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> rebuildContent());

        Scene scene = new Scene(root, 760, 720);
        setScene(scene);
    }

    // Rebuilds the preview content according to selected format
    private void rebuildContent() {
        String format = formatChoice.getValue();
        if (FORMAT_TICKET.equals(format)) {
            currentContentNode = buildTicketView();
            BorderPane.setAlignment(currentContentNode, Pos.TOP_CENTER);
            root.setCenter(new ScrollPane(currentContentNode));
        } else {
            currentContentNode = buildA4View();
            BorderPane.setAlignment(currentContentNode, Pos.TOP_CENTER);
            root.setCenter(new ScrollPane(currentContentNode));
        }
    }

    // Build narrow ticket-style VBox
    private Node buildTicketView() {
        VBox content = new VBox(6);
        content.setPadding(new Insets(8));
        content.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
        content.setPrefWidth(300); // approx ticket width in pixels

        ImageView logo = tryLoadLogo();
        if (logo != null) {
            logo.setFitHeight(40);
            logo.setPreserveRatio(true);
            HBox h = new HBox(logo);
            h.setAlignment(Pos.CENTER);
            content.getChildren().add(h);
        }

        Label name = new Label("LIFE PHARMACY");
        name.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #0D47A1;");
        name.setAlignment(Pos.CENTER);
        name.setMaxWidth(Double.MAX_VALUE);
        name.setTextAlignment(TextAlignment.CENTER);
        content.getChildren().add(name);

        Label addr = new Label("20 boulevard Kamanyola\nLIKASA - RDC");
        addr.setTextAlignment(TextAlignment.CENTER);
        addr.setWrapText(true);
        content.getChildren().add(addr);

        content.getChildren().add(new Separator());

        Label infos = new Label("Facture #" + facture.getId() + "\n" +
                facture.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                "Client: " + (facture.getClient() != null ? facture.getClient().getNom() : "N/A"));
        content.getChildren().add(infos);

        content.getChildren().add(new Separator());

        // products compact
        VBox productsBox = new VBox(4);
        for (DetailFacture d : facture.getDetails()) {
            String nameStr = (d.getProduit() != null && d.getProduit().getNom() != null)
                    ? d.getProduit().getNom()
                    : (d.getProduitNom() != null ? d.getProduitNom() : "Produit supprimé");
            Label line1 = new Label(nameStr);
            Label line2 = new Label(d.getQuantite() + " x " + String.format("%.2f CDF", d.getPrixUnitaire())
                    + " = " + String.format("%.2f CDF", d.getTotal()));
            line1.setStyle("-fx-font-size: 11;");
            line2.setStyle("-fx-font-size: 11;");
            productsBox.getChildren().addAll(line1, line2);
            productsBox.getChildren().add(new Separator());
        }
        content.getChildren().add(productsBox);

        // totals
        VBox totals = new VBox(2);
        totals.setAlignment(Pos.CENTER_RIGHT);
        totals.getChildren().add(new Label(String.format("HT: %.2f CDF", facture.getMontantHT())));
        totals.getChildren().add(new Label(String.format("TVA: %.2f CDF", facture.getMontantTVA())));
        Label ttc = new Label(String.format("TOTAL: %.2f CDF", facture.getMontantTTC()));
        ttc.setStyle("-fx-font-weight: bold; -fx-text-fill: #0D47A1;");
        totals.getChildren().add(ttc);
        content.getChildren().add(totals);

        Label note = new Label("Merci pour votre confiance");
        note.setTextAlignment(TextAlignment.CENTER);
        note.setWrapText(true);
        content.getChildren().add(note);

        return content;
    }

    // Build full A4 style VBox (similar to previous professional layout)
    private Node buildA4View() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 12;");
        content.setMaxWidth(760);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView logoView = tryLoadLogo();
        if (logoView != null) {
            logoView.setFitHeight(60);
            logoView.setPreserveRatio(true);
            header.getChildren().add(logoView);
        }

        VBox headText = new VBox(2);
        Label appName = new Label("LIFE PHARMACY 1.0");
        appName.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0D47A1;");
        Label company = new Label("20 boulevard Kamanyola, LIKASA, RDC");
        company.setTextFill(Color.DARKGRAY);
        Label contact = new Label("Contact : +243 992 095 566");
        contact.setTextFill(Color.DARKGRAY);
        headText.getChildren().addAll(appName, company, contact);

        header.getChildren().add(headText);

        Separator sep = new Separator();

        Label infos = new Label(buildInfosText());
        infos.setTextAlignment(TextAlignment.LEFT);

        VBox productsBox = new VBox(6);
        productsBox.setPadding(new Insets(8));
        productsBox.setMaxWidth(720);
        productsBox.setStyle("-fx-border-color: #1976D2; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");

        HBox headerRow = new HBox(10);
        headerRow.setPadding(new Insets(6, 8, 6, 8));
        headerRow.setStyle("-fx-background-color: #E3F2FD; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label col1 = new Label("Produit");
        col1.setPrefWidth(360);
        Label col2 = new Label("Qté");
        col2.setPrefWidth(80);
        Label col3 = new Label("Prix");
        col3.setPrefWidth(120);
        Label col4 = new Label("Total");
        col4.setPrefWidth(120);
        headerRow.getChildren().addAll(col1, col2, col3, col4);
        productsBox.getChildren().add(headerRow);

        for (DetailFacture d : facture.getDetails()) {
            HBox row = new HBox(10);
            row.setPadding(new Insets(6, 8, 6, 8));
            String produitName = (d.getProduit() != null && d.getProduit().getNom() != null)
                    ? d.getProduit().getNom()
                    : (d.getProduitNom() != null ? d.getProduitNom() : "Produit supprimé");
            Label lblName = new Label(truncate(produitName, 60));
            lblName.setPrefWidth(360);
            Label lblQte = new Label(String.valueOf(d.getQuantite()));
            lblQte.setPrefWidth(80);
            Label lblPrix = new Label(String.format("%.2f CDF", d.getPrixUnitaire()));
            lblPrix.setPrefWidth(120);
            Label lblTotal = new Label(String.format("%.2f CDF", d.getTotal()));
            lblTotal.setPrefWidth(120);
            row.getChildren().addAll(lblName, lblQte, lblPrix, lblTotal);
            productsBox.getChildren().add(row);
        }

        VBox totalsBox = new VBox(6);
        totalsBox.setAlignment(Pos.CENTER_RIGHT);
        totalsBox.setPadding(new Insets(8));
        Label totalHT = new Label(String.format("Montant HT : %.2f CDF", facture.getMontantHT()));
        Label tva = new Label(String.format("TVA : %.2f CDF", facture.getMontantTVA()));
        Label totalTTC = new Label(String.format("TOTAL TTC : %.2f CDF", facture.getMontantTTC()));
        totalTTC.setStyle("-fx-font-weight: bold; -fx-text-fill: #0D47A1; -fx-font-size: 14;");
        totalsBox.getChildren().addAll(totalHT, tva, totalTTC);

        Label note = new Label("Merci pour votre confiance. Conservez ce reçu pour vos archives.");
        note.setTextFill(Color.GRAY);
        note.setWrapText(true);

        content.getChildren().addAll(header, sep, infos, productsBox, totalsBox, note);
        return content;
    }

    private String buildInfosText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Facture N° : ").append(facture.getId()).append("\n");
        sb.append("Date : ").append(facture.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append("Client : ").append(facture.getClient() != null ? facture.getClient().getNom() : "N/A").append("\n");
        return sb.toString();
    }

    private ImageView tryLoadLogo() {
        try {
            var is = getClass().getResourceAsStream("/images/logo.png");
            if (is == null) return null;
            Image img = new Image(is);
            return new ImageView(img);
        } catch (Exception ex) {
            return null;
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    // Print current content node
    private void printCurrent() {
        if (currentContentNode == null) return;
        print(currentContentNode);
    }

    private void print(Node node) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            new Alert(Alert.AlertType.ERROR, "Aucun job d'impression disponible.").showAndWait();
            return;
        }
        boolean proceed = job.showPrintDialog(this);
        if (!proceed) return;
        boolean ok = job.printPage(node);
        if (ok) job.endJob();
    }

    // Export displayed node to PDF adapted to chosen format
    private void exportPdfDialog() {
        if (currentContentNode == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter en PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        File out = chooser.showSaveDialog(this);
        if (out == null) return;

        try {
            exportNodeToPdfAdapted(currentContentNode, out, formatChoice.getValue());
            new Alert(Alert.AlertType.INFORMATION, "PDF exporté : " + out.getAbsolutePath()).showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur export PDF : " + ex.getMessage()).showAndWait();
        }
    }

    /**
     * Exporte un Node JavaFX en PDF en prenant un snapshot et dessinant l'image dans une page PDF
     * adaptée au format choisi.
     */
    private void exportNodeToPdfAdapted(Node node, File outputPdf, String format) throws IOException {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        WritableImage wi = node.snapshot(params, null);
        BufferedImage buffered = SwingFXUtils.fromFXImage(wi, null);

        try (PDDocument doc = new PDDocument()) {
            PDPage page;
            if (FORMAT_TICKET.equals(format)) {
                // Ticket width ~80mm -> convert to points (~72 dpi): 80mm = 3.1496 in -> *72 = ~226.8 points
                float pageWidth = 227f;
                // Height: set A4-like tall page or dynamic based on image height
                float pageHeight = Math.max(400f, buffered.getHeight() + 80f);
                PDRectangle rect = new PDRectangle(pageWidth, pageHeight);
                page = new PDPage(rect);
                doc.addPage(page);
            } else {
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
            }

            var pdImage = LosslessFactory.createFromImage(doc, buffered);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);

            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox.getWidth();
            float pageHeight = mediaBox.getHeight();
            float margin = 20f;

            float availableW = pageWidth - margin * 2;
            float availableH = pageHeight - margin * 2;

            float imgW = pdImage.getWidth();
            float imgH = pdImage.getHeight();

            float scale = Math.min(availableW / imgW, availableH / imgH);

            float drawW = imgW * scale;
            float drawH = imgH * scale;

            float x = (pageWidth - drawW) / 2;
            float y = pageHeight - margin - drawH;

            contentStream.drawImage(pdImage, x, y, drawW, drawH);
            contentStream.close();

            doc.save(outputPdf);
        }
    }
}
