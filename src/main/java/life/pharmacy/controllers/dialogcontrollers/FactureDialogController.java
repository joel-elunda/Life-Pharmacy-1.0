package life.pharmacy.controllers.dialogcontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import life.pharmacy.models.Facture;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class FactureDialogController implements Initializable {
    @FXML
    private TextField clientField;
    @FXML private TextField employeField;
    @FXML private DatePicker dateField;
    @FXML private TextField montantField;
    @FXML private TextField paiementField;

    private Facture facture;

    public Facture getFacture() {
        return facture;
    }

    @FXML
    private void initialize() {
    }

    public void setFacture(Facture f) {
        this.facture = f;
        clientField.setText(String.valueOf(f.getClient().getId()));
        employeField.setText(String.valueOf(f.getEmploye().getId()));
        dateField.setValue(f.getDate());
        montantField.setText(String.valueOf(f.getMontantTotal()));
        paiementField.setText(f.getModePaiement());
    }

    public void onOk() {
        facture = new Facture(
                facture != null ? facture.getId() : (int) (System.currentTimeMillis() % 100000),
                null, // ici tu lieras au vrai Client
                null, // ici tu lieras au vrai Employe
                dateField.getValue() != null ? dateField.getValue() : LocalDate.now(),
                Double.parseDouble(montantField.getText()),
                paiementField.getText()
        );
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
