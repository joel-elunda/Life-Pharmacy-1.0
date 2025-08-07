package life.pharmacy.models;

public class Produit {
    private int id;
    private String nom;
    private String codeBarre;
    private double prixUnitaire;
    private int quantite;
    private boolean tva;


    public Produit() {
    }

    public Produit(int id, String nom, String codeBarre, double prixUnitaire, int quantite, boolean tva) {
        this.id = id;
        this.nom = nom;
        this.codeBarre = codeBarre;
        this.prixUnitaire = prixUnitaire;
        this.quantite = quantite;
        this.tva = tva;
    }

    public Produit(int id, String nom, double prix, int quantite) {
        this.id = id;
        this.nom = nom;
        this.prixUnitaire = prix;
        this.quantite = quantite;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getCodeBarre() {
        return codeBarre;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public int getQuantite() {
        return quantite;
    }

    public boolean isTva() {
        return tva;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public void setTva(boolean tva) {
        this.tva = tva;
    }
}
