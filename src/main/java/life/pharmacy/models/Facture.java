package life.pharmacy.models;

import java.time.LocalDateTime;
import java.util.List;

public class Facture {
    private int id;
    private LocalDateTime date;
    private Client client; // Peut être null (client non obligatoire)
    private double montantHT;
    private double montantTVA;
    private double montantTTC;
    private List<DetailFacture> details;

    public Facture(int id, LocalDateTime date, Client client, double montantHT, double montantTVA, double montantTTC) {
        this.id = id;
        this.date = date;
        this.client = client;
        this.montantHT = montantHT;
        this.montantTVA = montantTVA;
        this.montantTTC = montantTTC;
    }

    public Facture() {
    }

    public Facture(int factureId) {
        this.id = factureId;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Client getClient() {
        return client;
    }

    public double getMontantHT() {
        return montantHT;
    }

    public double getMontantTVA() {
        return montantTVA;
    }

    public double getMontantTTC() {
        return montantTTC;
    }

    public List<DetailFacture> getDetails() {
        return details;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setMontantHT(double montantHT) {
        this.montantHT = montantHT;
    }

    public void setMontantTVA(double montantTVA) {
        this.montantTVA = montantTVA;
    }

    public void setMontantTTC(double montantTTC) {
        this.montantTTC = montantTTC;
    }

    public void setDetails(List<DetailFacture> details) {
        this.details = details;
    }
}
