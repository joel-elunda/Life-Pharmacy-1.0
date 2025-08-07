package life.pharmacy.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Utilisateur {

    private IntegerProperty id;
    private StringProperty nom;
    private StringProperty email;
    private StringProperty motDePasse;
    private StringProperty role; // "admin", "manager", "caissier"

    public Utilisateur() {
    }

    public Utilisateur(IntegerProperty id, StringProperty nom, StringProperty email, StringProperty motDePasse, StringProperty role) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public Utilisateur(StringProperty nom, StringProperty motDePasse, StringProperty role) {
        this.nom = nom;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public Utilisateur(IntegerProperty id, StringProperty nom, StringProperty motDePasse, StringProperty role) {
        this.id = id;
        this.nom = nom;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public Utilisateur(int id, String nom, String email, String motDePasse, String role) {
        this.id = new SimpleIntegerProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.email = new SimpleStringProperty(email);
        this.motDePasse = new SimpleStringProperty(motDePasse);
        this.role = new SimpleStringProperty(role);
    }

    public Utilisateur(int id, String nom, String motDePasse, String role) {
        this.id = new SimpleIntegerProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.motDePasse = new SimpleStringProperty(motDePasse);
        this.role = new SimpleStringProperty(role);
    }

    public Utilisateur(String nom, String motDePasse, String role) {
        this.nom = new SimpleStringProperty(nom);
        this.motDePasse = new SimpleStringProperty(motDePasse);
        this.role = new SimpleStringProperty(role);
    }


    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNom() {
        return nom.get();
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getMotDePasse() {
        return motDePasse.get();
    }

    public StringProperty motDePasseProperty() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse.set(motDePasse);
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public void setRole(String role) {
        this.role.set(role);
    }
}
