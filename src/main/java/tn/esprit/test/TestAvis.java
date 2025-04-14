package tn.esprit.test;

import tn.esprit.models.Avis;
import tn.esprit.services.ServiceAvis;
import java.time.LocalDateTime;

public class TestAvis {
    public static void main(String[] args) {
        ServiceAvis serviceAvis = new ServiceAvis();

        // Step 1: Clear existing data for a clean test
        System.out.println("Clearing existing Avis records for formation ID 1...");
        for (Avis avis : serviceAvis.getAll()) {
            if (avis.getFormationId() == 1) {
                serviceAvis.delete(avis);
            }
        }

        // Step 2: Add two Avis records
        System.out.println("\n=== Adding Two Avis Records ===");
        Avis avis1 = new Avis(3.0f, "Average course.", LocalDateTime.now(), 1);
        serviceAvis.add(avis1);

        Avis avis2 = new Avis(5.0f, "Excellent course!", LocalDateTime.now(), 1);
        serviceAvis.add(avis2);

        System.out.println("All Avis records after adding:");
        for (Avis a : serviceAvis.getAll()) {
            System.out.println(a);
        }

        // Step 3: Update the first Avis (avis1)
        System.out.println("\n=== Updating the First Avis (ID: " + avis1.getId() + ") ===");
        Avis updatedAvis1 = new Avis(4.0f, "Updated: Good course!", LocalDateTime.now(), 1);
        updatedAvis1.setId(avis1.getId());
        serviceAvis.update(updatedAvis1);

        System.out.println("All Avis records after updating:");
        for (Avis a : serviceAvis.getAll()) {
            System.out.println(a);
        }
    }
}
