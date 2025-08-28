package life.pharmacy.models;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Recette {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final DoubleProperty montant = new SimpleDoubleProperty();
    private final StringProperty periode = new SimpleStringProperty();

    public Recette(int id, LocalDate date, double montant, String periode) {
        this.id.set(id);
        this.date.set(date);
        this.montant.set(montant);
        this.periode.set(periode);
    }

    public IntegerProperty idProperty() { return id; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public DoubleProperty montantProperty() { return montant; }
    public StringProperty periodeProperty() { return periode; }

    public int getId() { return id.get(); }
    public LocalDate getDate() { return date.get(); }
    public double getMontant() { return montant.get(); }
    public String getPeriode() { return periode.get(); }
}
