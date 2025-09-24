package life.pharmacy.models;


import javafx.beans.property.*;
import java.time.LocalDate;

/** ===================== CLIENT ===================== */
public class Client {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty nomComplet = new SimpleStringProperty(this, "nomComplet");
    private final StringProperty adresse = new SimpleStringProperty(this, "adresse");
    private final StringProperty telephone = new SimpleStringProperty(this, "telephone");
    private final StringProperty email = new SimpleStringProperty(this, "email");
    private final StringProperty conditionsMedicales = new SimpleStringProperty(this, "conditionsMedicales");
    private final StringProperty allergies = new SimpleStringProperty(this, "allergies");

    public Client() {
        // Default constructor
    }

    public Client(int id, String nomComplet, String adresse, String telephone, String email, String conditionsMedicales, String allergies) {
        this.id.set(id);
        this.nomComplet.set(nomComplet);
        this.adresse.set(adresse);
        this.telephone.set(telephone);
        this.email.set(email);
        this.conditionsMedicales.set(conditionsMedicales);
        this.allergies.set(allergies);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getNomComplet() { return nomComplet.get(); }
    public void setNomComplet(String value) { nomComplet.set(value); }
    public StringProperty nomCompletProperty() { return nomComplet; }

    public String getAdresse() { return adresse.get(); }
    public void setAdresse(String value) { adresse.set(value); }
    public StringProperty adresseProperty() { return adresse; }

    public String getTelephone() { return telephone.get(); }
    public void setTelephone(String value) { telephone.set(value); }
    public StringProperty telephoneProperty() { return telephone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    public String getConditionsMedicales() { return conditionsMedicales.get(); }
    public void setConditionsMedicales(String value) { conditionsMedicales.set(value); }
    public StringProperty conditionsMedicalesProperty() { return conditionsMedicales; }

    public String getAllergies() { return allergies.get(); }
    public void setAllergies(String value) { allergies.set(value); }
    public StringProperty allergiesProperty() { return allergies; }
}
