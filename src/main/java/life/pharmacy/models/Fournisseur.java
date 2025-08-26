package life.pharmacy.models;


import javafx.beans.property.*;

/** ===================== FOURNISSEUR ===================== */
public class Fournisseur {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty nom = new SimpleStringProperty(this, "nom");
    private final StringProperty contact = new SimpleStringProperty(this, "contact");
    private final StringProperty telephone = new SimpleStringProperty(this, "telephone");
    private final StringProperty email = new SimpleStringProperty(this, "email");
    private final StringProperty adresse = new SimpleStringProperty(this, "adresse");
    private final StringProperty conditionsPaiement = new SimpleStringProperty(this, "conditionsPaiement");

    public Fournisseur(int id, String nom, String contact, String telephone, String email, String adresse, String conditionsPaiement) {
        this.id.set(id);
        this.nom.set(nom);
        this.contact.set(contact);
        this.telephone.set(telephone);
        this.email.set(email);
        this.adresse.set(adresse);
        this.conditionsPaiement.set(conditionsPaiement);
    }
    public Fournisseur() {}

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getNom() { return nom.get(); }
    public void setNom(String value) { nom.set(value); }
    public StringProperty nomProperty() { return nom; }

    public String getContact() { return contact.get(); }
    public void setContact(String value) { contact.set(value); }
    public StringProperty contactProperty() { return contact; }

    public String getTelephone() { return telephone.get(); }
    public void setTelephone(String value) { telephone.set(value); }
    public StringProperty telephoneProperty() { return telephone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    public String getAdresse() { return adresse.get(); }
    public void setAdresse(String value) { adresse.set(value); }
    public StringProperty adresseProperty() { return adresse; }

    public String getConditionsPaiement() { return conditionsPaiement.get(); }
    public void setConditionsPaiement(String value) { conditionsPaiement.set(value); }
    public StringProperty conditionsPaiementProperty() { return conditionsPaiement; }
}
