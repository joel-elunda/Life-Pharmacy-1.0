package life.pharmacy.models;

public class DetailFacture {
    private int id;
    private Produit produit;
    private int quantite;
    private double prixUnitaire;
    private String produitNom;  // snapshot
    private Facture facture; // snapshot, not used in this class

    public DetailFacture() {
        // Default constructor
    }

    public DetailFacture(int id, Produit produit, int quantite, double prixUnitaire) {
        this.id = id;
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public DetailFacture(int id, Produit produit, int quantite, double prixUnitaire, String produitNom) {
        this.id = id;
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.produitNom = produitNom;
    }

    public DetailFacture(int id, int factureId, Produit produit, int quantite, double prixUnitaire) {
        this.id = id;
        this.facture = new Facture(factureId); // Assuming Facture has a constructor that takes an ID
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getId() {
        return id;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public double getTotal() {
        return prixUnitaire * quantite;
    }

    public String getProduitNom() {
        return produitNom;
    }

    public void setProduitNom(String produitNom) {
        this.produitNom = produitNom;
    }
}
