package life.pharmacy.utils;

import life.pharmacy.models.Utilisateur;

/**
 * Petite classe utilitaire pour stocker l'utilisateur courant.
 */
public class Session {
    private static Utilisateur currentUser;

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static boolean isAdmin() {
        Utilisateur u = getCurrentUser();
        return u != null && "Admin".equalsIgnoreCase(u.getRole());
    }

    public static boolean isManager() {
        Utilisateur u = getCurrentUser();
        return u != null && ("Manager".equalsIgnoreCase(u.getRole()) || isAdmin());
    }
}
