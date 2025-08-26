package life.pharmacy.models;

import javafx.beans.property.*;
import java.time.LocalDate;

/** ===================== PRODUIT ===================== */
public class Produit {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty nomCommercial = new SimpleStringProperty(this, "nomCommercial");
    private final StringProperty nomGenerique = new SimpleStringProperty(this, "nomGenerique");
    private final StringProperty forme = new SimpleStringProperty(this, "forme");
    private final StringProperty dosage = new SimpleStringProperty(this, "dosage");
    private final StringProperty conditionnement = new SimpleStringProperty(this, "conditionnement");
    private final StringProperty fabricant = new SimpleStringProperty(this, "fabricant");
    private final StringProperty codeBarres = new SimpleStringProperty(this, "codeBarres");
    private final DoubleProperty prixVente = new SimpleDoubleProperty(this, "prixVente");
    private final DoubleProperty prixAchat = new SimpleDoubleProperty(this, "prixAchat");
    private final StringProperty statut = new SimpleStringProperty(this, "statut");
    private final StringProperty categorie = new SimpleStringProperty(this, "categorie");
    private final BooleanProperty prescriptionRequise = new SimpleBooleanProperty(this, "prescriptionRequise");
    private final ObjectProperty<LocalDate> dateExpiration = new SimpleObjectProperty<>(this, "dateExpiration");
    private final StringProperty numeroLot = new SimpleStringProperty(this, "numeroLot");
    private final IntegerProperty stock = new SimpleIntegerProperty(this, "stock");
    private final IntegerProperty seuilAlerte = new SimpleIntegerProperty(this, "seuilAlerte");

    public Produit() {}
    /**
     * Constructeur par défaut pour la classe Produit.
     * Il initialise les propriétés avec des valeurs par défaut.
     */
    public Produit(
            int id,
            String nomCommercial,
            String nomGenerique,
            String forme,
            String dosage,
            String conditionnement,
            String fabricant,
            String codeBarres,
            double prixVente,
            double prixAchat,
            String statut,
            String categorie,
            boolean prescriptionRequise,
            LocalDate dateExpiration,
            String numeroLot,
            int stock,
            int seuilAlerte) {

        this.id.set(id);
        this.nomCommercial.set(nomCommercial);
        this.nomGenerique.set(nomGenerique);
        this.forme.set(forme);
        this.dosage.set(dosage);
        this.conditionnement.set(conditionnement);
        this.fabricant.set(fabricant);
        this.codeBarres.set(codeBarres);
        this.prixVente.set(prixVente);
        this.prixAchat.set(prixAchat);
        this.statut.set(statut);
        this.categorie.set(categorie);
        this.prescriptionRequise.set(prescriptionRequise);
        this.dateExpiration.set(dateExpiration);
        this.numeroLot.set(numeroLot);
        this.stock.set(stock);
        this.seuilAlerte.set(seuilAlerte);
    }




    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getNomCommercial() { return nomCommercial.get(); }
    public void setNomCommercial(String value) { nomCommercial.set(value); }
    public StringProperty nomCommercialProperty() { return nomCommercial; }

    public String getNomGenerique() { return nomGenerique.get(); }
    public void setNomGenerique(String value) { nomGenerique.set(value); }
    public StringProperty nomGeneriqueProperty() { return nomGenerique; }

    public String getForme() { return forme.get(); }
    public void setForme(String value) { forme.set(value); }
    public StringProperty formeProperty() { return forme; }

    public String getDosage() { return dosage.get(); }
    public void setDosage(String value) { dosage.set(value); }
    public StringProperty dosageProperty() { return dosage; }

    public String getConditionnement() { return conditionnement.get(); }
    public void setConditionnement(String value) { conditionnement.set(value); }
    public StringProperty conditionnementProperty() { return conditionnement; }

    public String getFabricant() { return fabricant.get(); }
    public void setFabricant(String value) { fabricant.set(value); }
    public StringProperty fabricantProperty() { return fabricant; }

    public String getCodeBarres() { return codeBarres.get(); }
    public void setCodeBarres(String value) { codeBarres.set(value); }
    public StringProperty codeBarresProperty() { return codeBarres; }

    public double getPrixVente() { return prixVente.get(); }
    public void setPrixVente(double value) { prixVente.set(value); }
    public DoubleProperty prixVenteProperty() { return prixVente; }

    public double getPrixAchat() { return prixAchat.get(); }
    public void setPrixAchat(double value) { prixAchat.set(value); }
    public DoubleProperty prixAchatProperty() { return prixAchat; }

    public String getStatut() { return statut.get(); }
    public void setStatut(String value) { statut.set(value); }
    public StringProperty statutProperty() { return statut; }

    public String getCategorie() { return categorie.get(); }
    public void setCategorie(String value) { categorie.set(value); }
    public StringProperty categorieProperty() { return categorie; }

    public boolean isPrescriptionRequise() { return prescriptionRequise.get(); }
    public void setPrescriptionRequise(boolean value) { prescriptionRequise.set(value); }
    public BooleanProperty prescriptionRequiseProperty() { return prescriptionRequise; }

    public LocalDate getDateExpiration() { return dateExpiration.get(); }
    public void setDateExpiration(LocalDate value) { dateExpiration.set(value); }
    public ObjectProperty<LocalDate> dateExpirationProperty() { return dateExpiration; }

    public String getNumeroLot() { return numeroLot.get(); }
    public void setNumeroLot(String value) { numeroLot.set(value); }
    public StringProperty numeroLotProperty() { return numeroLot; }

    public int getStock() { return stock.get(); }
    public void setStock(int value) { stock.set(value); }
    public IntegerProperty stockProperty() { return stock; }

    public int getSeuilAlerte() { return seuilAlerte.get(); }
    public void setSeuilAlerte(int value) { seuilAlerte.set(value); }
    public IntegerProperty seuilAlerteProperty() { return seuilAlerte; }
}
