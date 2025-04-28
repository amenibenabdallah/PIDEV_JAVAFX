package tn.esprit.utils;

import tn.esprit.models.User;

public class SessionManager {
    private static User utilisateurConnecte;

    public static void setUtilisateurConnecte(User utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static User getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static void clearSession() {
        utilisateurConnecte = null;
    }



}
