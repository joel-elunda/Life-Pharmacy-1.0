package life.pharmacy.models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/** ===================== TRANSACTION ===================== */
public class Transaction {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final ObjectProperty<LocalDateTime> dateHeure = new SimpleObjectProperty<>(this, "dateHeure");
    private final DoubleProperty total = new SimpleDoubleProperty(this, "total");
    private final StringProperty statutPaiement = new SimpleStringProperty(this, "statutPaiement");
    private final StringProperty methodePaiement = new SimpleStringProperty(this, "methodePaiement");
    private final IntegerProperty clientId = new SimpleIntegerProperty(this, "clientId");
    private final IntegerProperty employeId = new SimpleIntegerProperty(this, "employeId");

    public Transaction() {}

    public Transaction(int id, LocalDateTime dateHeure, double total, String statutPaiement, String methodePaiement, int clientId, int employeId) {
        this.id.set(id);
        this.dateHeure.set(dateHeure);
        this.total.set(total);
        this.statutPaiement.set(statutPaiement);
        this.methodePaiement.set(methodePaiement);
        this.clientId.set(clientId);
        this.employeId.set(employeId);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public LocalDateTime getDateHeure() { return dateHeure.get(); }
    public void setDateHeure(LocalDateTime value) { dateHeure.set(value); }
    public ObjectProperty<LocalDateTime> dateHeureProperty() { return dateHeure; }

    public double getTotal() { return total.get(); }
    public void setTotal(double value) { total.set(value); }
    public DoubleProperty totalProperty() { return total; }

    public String getStatutPaiement() { return statutPaiement.get(); }
    public void setStatutPaiement(String value) { statutPaiement.set(value); }
    public StringProperty statutPaiementProperty() { return statutPaiement; }

    public String getMethodePaiement() { return methodePaiement.get(); }
    public void setMethodePaiement(String value) { methodePaiement.set(value); }
    public StringProperty methodePaiementProperty() { return methodePaiement; }

    public int getClientId() { return clientId.get(); }
    public void setClientId(int value) { clientId.set(value); }
    public IntegerProperty clientIdProperty() { return clientId; }

    public int getEmployeId() { return employeId.get(); }
    public void setEmployeId(int value) { employeId.set(value); }
    public IntegerProperty employeIdProperty() { return employeId; }
}
