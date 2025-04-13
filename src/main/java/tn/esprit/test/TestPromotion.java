package tn.esprit.test;

import tn.esprit.models.Promotion;
import tn.esprit.services.ServicePromotion;

import java.time.LocalDate;
import java.util.List;

public class TestPromotion {
    public static void main(String[] args) {
        ServicePromotion service = new ServicePromotion();

        System.out.println("Connected .....");

        // 🔸 Créer une nouvelle promotion avec inscriptionCoursId et apprenantId
        Promotion promo1 = new Promotion(
                "SUMMER2031",
                "Réduction estivale 30%",
                30.0,
                LocalDate.of(2025, 8, 15),
                57, // inscription_cours_id
                1 // apprenant_id
        );

        service.add(promo1); // tester l'ajout

        // 🔸 Lire toutes les promotions
        System.out.println("\n📋 Liste des promotions :");
        List<Promotion> promotions = service.getAll();
        for (Promotion p : promotions) {
            System.out.println(p);
        }

        // 🔸 Rechercher par code promo
        System.out.println("\n🔎 Recherche par code SUMMER2030 :");
        Promotion foundPromo = service.getByCode("SUMMER2030");
        if (foundPromo != null) {
            System.out.println("✅ Trouvée : " + foundPromo);
        } else {
            System.out.println("❌ Aucune promotion trouvée avec ce code.");
        }

        // 🔸 Mettre à jour une promotion (si elle existe)
        if (foundPromo != null) {
            foundPromo.setRemise(35.0);
            foundPromo.setDescription("Nouvelle description mise à jour");
            service.update(foundPromo);
        }

        // 🔸 Supprimer une promotion (si elle existe)
        if (foundPromo != null) {
            service.delete(foundPromo);
        }

        // 🔸 Affichage final
        System.out.println("\n📋 Promotions restantes :");
        List<Promotion> updatedPromotions = service.getAll();
        for (Promotion p : updatedPromotions) {
            System.out.println(p);
        }
    }
}
