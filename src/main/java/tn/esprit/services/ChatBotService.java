package tn.esprit.services;

import java.util.HashMap;
import java.util.Map;

public class ChatBotService {

    private final Map<String, String> keywordResponses = new HashMap<>();

    public ChatBotService() {
        initializeKnowledgeBase();
    }

    private void initializeKnowledgeBase() {
        keywordResponses.put("inscription", "Pour vous inscrire, cliquez sur 'Inscription' et remplissez vos informations personnelles.");
        keywordResponses.put("compte", "Pour créer un compte sur Formini, utilisez le bouton 'S'inscrire' sur la page d'accueil.");
        keywordResponses.put("formation", "Pour choisir une formation, parcourez notre catalogue et cliquez sur 'S'inscrire' à la formation de votre choix.");
        keywordResponses.put("payer", "Après l'inscription, vous pourrez effectuer le paiement en ligne en toute sécurité.");
        keywordResponses.put("code promo", "Après avoir terminé deux formations, un code promo vous sera envoyé par email.");
        keywordResponses.put("email", "Le code promo est envoyé automatiquement à votre adresse email après deux inscriptions.");
        keywordResponses.put("support", "Si vous avez un problème, contactez notre support via la rubrique 'Aide'.");
    }

    public String askQuestion(String question) {
        String cleanedQuestion = question.trim().toLowerCase();

        // Gérer les salutations
        if (cleanedQuestion.contains("hello") || cleanedQuestion.contains("salut") || cleanedQuestion.contains("bonjour")) {
            return "Bonjour 👋 ! Je suis votre assistant Formini. Comment puis-je vous aider aujourd'hui ?";
        }

        // Chercher des mots-clés
        for (Map.Entry<String, String> entry : keywordResponses.entrySet()) {
            if (cleanedQuestion.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "Désolé, je n'ai pas compris votre question. Veuillez essayer de reformuler ou contacter le support.";
    }
}
