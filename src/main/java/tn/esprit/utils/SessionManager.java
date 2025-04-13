package tn.esprit.utils;

import tn.esprit.models.users;

public class SessionManager {
    private static users utilisateurConnecte;

    public static void setUtilisateurConnecte(users utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static users getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static void clearSession() {
        utilisateurConnecte = null;
    }



}
