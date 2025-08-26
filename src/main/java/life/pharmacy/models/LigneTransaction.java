package life.pharmacy.models;

import javafx.beans.property.*;

/** ===================== LIGNE TRANSACTION ===================== */
public class LigneTransaction {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty transactionId = new SimpleIntegerProperty(this, "transactionId");
    private final IntegerProperty produitId = new SimpleIntegerProperty(this, "produitId");
    private final IntegerProperty quantite = new SimpleIntegerProperty(this, "quantite");
    private final DoubleProperty prixUnitaire = new SimpleDoubleProperty(this, "prixUnitaire");
    private final DoubleProperty sousTotal = new SimpleDoubleProperty(this, "sousTotal");
    private final StringProperty numeroOrdonnance = new SimpleStringProperty(this, "numeroOrdonnance");

    public LigneTransaction() {}

    public LigneTransaction(int id, int transactionId, int produitId, int quantite, double prixUnitaire) {
        this.id.set(id);
        this.transactionId.set(transactionId);
        this.produitId.set(produitId);
        this.quantite.set(quantite);
        this.prixUnitaire.set(prixUnitaire);
        this.sousTotal.set(quantite * prixUnitaire); // Calcul du sous-total
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getTransactionId() { return transactionId.get(); }
    public void setTransactionId(int value) { transactionId.set(value); }
    public IntegerProperty transactionIdProperty() { return transactionId; }

    public int getProduitId() { return produitId.get(); }
    public void setProduitId(int value) { produitId.set(value); }
    public IntegerProperty produitIdProperty() { return produitId; }

    public int getQuantite() { return quantite.get(); }
    public void setQuantite(int value) { quantite.set(value); }
    public IntegerProperty quantiteProperty() { return quantite; }

    public double getPrixUnitaire() { return prixUnitaire.get(); }
    public void setPrixUnitaire(double value) { prixUnitaire.set(value); }
    public DoubleProperty prixUnitaireProperty() { return prixUnitaire; }

    public double getSousTotal() { return sousTotal.get(); }
    public void setSousTotal(double value) { sousTotal.set(value); }
    public DoubleProperty sousTotalProperty() { return sousTotal; }

    public String getNumeroOrdonnance() { return numeroOrdonnance.get(); }
    public void setNumeroOrdonnance(String value) { numeroOrdonnance.set(value); }
    public StringProperty numeroOrdonnanceProperty() { return numeroOrdonnance; }
}
