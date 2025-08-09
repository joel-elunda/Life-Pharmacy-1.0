package life.pharmacy.models;

public class Fournisseur {
    private int id;
    private String nom;
    private String contact;
    private String adresse;

    public Fournisseur( ) {

    }
    public Fournisseur(int id, String nom, String contact, String adresse) {
        this.id = id;
        this.nom = nom;
        this.contact = contact;
        this.adresse = adresse;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getContact() {
        return contact;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
