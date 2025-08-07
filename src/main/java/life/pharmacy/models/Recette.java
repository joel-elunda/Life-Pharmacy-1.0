package life.pharmacy.models;

import java.time.LocalDate;

public class Recette {
    private int id;
    private LocalDate date;
    private double montant;
    private String type; // ex: "jour","semaine","mois","annee" ou libre

    public Recette() {
    }

    public Recette(int id, LocalDate date, double montant, String type) {
        this.id = id;
        this.date = date;
        this.montant = montant;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getMontant() {
        return montant;
    }

    public String getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public void setType(String type) {
        this.type = type;
    }
}
