package life.pharmacy.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Facture {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final ObjectProperty<Client> client = new SimpleObjectProperty<>(this, "client");
    private final ObjectProperty<Employe> employe = new SimpleObjectProperty<>(this, "employe");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(this, "date");
    private final DoubleProperty montantTotal = new SimpleDoubleProperty(this, "montantTotal");
    private final StringProperty modePaiement = new SimpleStringProperty(this, "modePaiement");

    public Facture() {}
    public Facture(int id, Client client, Employe employe, LocalDate date, double montantTotal, String modePaiement) {
        setId(id); setClient(client); setEmploye(employe); setDate(date); setMontantTotal(montantTotal); setModePaiement(modePaiement);
    }

    public int getId(){return id.get();} public void setId(int v){id.set(v);} public IntegerProperty idProperty(){return id;}
    public Client getClient(){return client.get();} public void setClient(Client v){client.set(v);} public ObjectProperty<Client> clientProperty(){return client;}
    public Employe getEmploye(){return employe.get();} public void setEmploye(Employe v){employe.set(v);} public ObjectProperty<Employe> employeProperty(){return employe;}
    public LocalDate getDate(){return date.get();} public void setDate(LocalDate v){date.set(v);} public ObjectProperty<LocalDate> dateProperty(){return date;}
    public double getMontantTotal(){return montantTotal.get();} public void setMontantTotal(double v){montantTotal.set(v);} public DoubleProperty montantTotalProperty(){return montantTotal;}
    public String getModePaiement(){return modePaiement.get();} public void setModePaiement(String v){modePaiement.set(v);} public StringProperty modePaiementProperty(){return modePaiement;}
}