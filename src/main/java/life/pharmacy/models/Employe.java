package life.pharmacy.models;


import javafx.beans.property.*;

/** ===================== EMPLOYE ===================== */
public class Employe {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty nomComplet = new SimpleStringProperty(this, "nomComplet");
    private final StringProperty role = new SimpleStringProperty(this, "role");
    private final StringProperty login = new SimpleStringProperty(this, "login");
    private final StringProperty motDePasseHash = new SimpleStringProperty(this, "motDePasseHash");
    private final StringProperty permissions = new SimpleStringProperty(this, "permissions"); // JSON/CSV

    public Employe(int id, String nomComplet, String role, String login, String motDePasseHash, String permissions) {
        this.id.set(id);
        this.nomComplet.set(nomComplet);
        this.role.set(role);
        this.login.set(login);
        this.motDePasseHash.set(motDePasseHash);
        this.permissions.set(permissions);
    }

    public Employe() {}

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getNomComplet() { return nomComplet.get(); }
    public void setNomComplet(String value) { nomComplet.set(value); }
    public StringProperty nomCompletProperty() { return nomComplet; }

    public String getRole() { return role.get(); }
    public void setRole(String value) { role.set(value); }
    public StringProperty roleProperty() { return role; }

    public String getLogin() { return login.get(); }
    public void setLogin(String value) { login.set(value); }
    public StringProperty loginProperty() { return login; }

    public String getMotDePasseHash() { return motDePasseHash.get(); }
    public void setMotDePasseHash(String value) { motDePasseHash.set(value); }
    public StringProperty motDePasseHashProperty() { return motDePasseHash; }

    public String getPermissions() { return permissions.get(); }
    public void setPermissions(String value) { permissions.set(value); }
    public StringProperty permissionsProperty() { return permissions; }
}
