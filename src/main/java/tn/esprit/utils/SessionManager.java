package tn.esprit.utils;

import tn.esprit.models.User;

public class SessionManager {

    private static SessionManager instance;
    private static User utilisateurConnecte;

    // 🔒 Constructeur privé pour empêcher l'instanciation directe
    public SessionManager() {}

    // ➤ Obtenir l'instance unique
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ➤ Setter & Getter utilisateur connecté
    public void setUtilisateurConnecte(User utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public static User getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    // ➤ Déconnexion
    public void logout() {
        this.utilisateurConnecte = null;
        System.out.println("✅ Déconnecté avec succès !");
    }

    // ➤ Vérifier si un utilisateur est connecté
    public boolean isConnected() {
        return utilisateurConnecte != null;
    }
}
