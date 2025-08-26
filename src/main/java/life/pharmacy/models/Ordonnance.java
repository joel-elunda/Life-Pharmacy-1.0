package life.pharmacy.models;


import javafx.beans.property.*;
import java.time.LocalDate;

/** ===================== ORDONNANCE ===================== */
public class Ordonnance {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty patientId = new SimpleIntegerProperty(this, "patientId");
    private final StringProperty medecin = new SimpleStringProperty(this, "medecin");
    private final ObjectProperty<LocalDate> dateEmission = new SimpleObjectProperty<>(this, "dateEmission");
    private final ObjectProperty<LocalDate> dateExpiration = new SimpleObjectProperty<>(this, "dateExpiration");
    private final StringProperty produitsPrescrits = new SimpleStringProperty(this, "produitsPrescrits"); // JSON/CSV
    private final StringProperty instructionsDosage = new SimpleStringProperty(this, "instructionsDosage");
    private final StringProperty statut = new SimpleStringProperty(this, "statut");
    private final StringProperty numeroUnique = new SimpleStringProperty(this, "numeroUnique");

    public Ordonnance() {}

    public Ordonnance(int id, int patientId, String medecin, LocalDate dateEmission, LocalDate dateExpiration,
                      String produitsPrescrits, String instructionsDosage, String statut, String numeroUnique) {
        this.id.set(id);
        this.patientId.set(patientId);
        this.medecin.set(medecin);
        this.dateEmission.set(dateEmission);
        this.dateExpiration.set(dateExpiration);
        this.produitsPrescrits.set(produitsPrescrits);
        this.instructionsDosage.set(instructionsDosage);
        this.statut.set(statut);
        this.numeroUnique.set(numeroUnique);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getPatientId() { return patientId.get(); }
    public void setPatientId(int value) { patientId.set(value); }
    public IntegerProperty patientIdProperty() { return patientId; }

    public String getMedecin() { return medecin.get(); }
    public void setMedecin(String value) { medecin.set(value); }
    public StringProperty medecinProperty() { return medecin; }

    public LocalDate getDateEmission() { return dateEmission.get(); }
    public void setDateEmission(LocalDate value) { dateEmission.set(value); }
    public ObjectProperty<LocalDate> dateEmissionProperty() { return dateEmission; }

    public LocalDate getDateExpiration() { return dateExpiration.get(); }
    public void setDateExpiration(LocalDate value) { dateExpiration.set(value); }
    public ObjectProperty<LocalDate> dateExpirationProperty() { return dateExpiration; }

    public String getProduitsPrescrits() { return produitsPrescrits.get(); }
    public void setProduitsPrescrits(String value) { produitsPrescrits.set(value); }
    public StringProperty produitsPrescritsProperty() { return produitsPrescrits; }

    public String getInstructionsDosage() { return instructionsDosage.get(); }
    public void setInstructionsDosage(String value) { instructionsDosage.set(value); }
    public StringProperty instructionsDosageProperty() { return instructionsDosage; }

    public String getStatut() { return statut.get(); }
    public void setStatut(String value) { statut.set(value); }
    public StringProperty statutProperty() { return statut; }

    public String getNumeroUnique() { return numeroUnique.get(); }
    public void setNumeroUnique(String value) { numeroUnique.set(value); }
    public StringProperty numeroUniqueProperty() { return numeroUnique; }
}
