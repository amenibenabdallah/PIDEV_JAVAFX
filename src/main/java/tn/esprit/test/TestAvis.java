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

        // Step 2: Add three Avis records
        System.out.println("\n=== Adding Three Avis Records ===");
        Avis avis1 = new Avis(3.0f, "Average course.", LocalDateTime.now(), 1);
        serviceAvis.add(avis1);

        Avis avis2 = new Avis(5.0f, "Excellent course!", LocalDateTime.now(), 1);
        serviceAvis.add(avis2);

        Avis avis3 = new Avis(4.0f, "Good course.", LocalDateTime.now(), 1);
        serviceAvis.add(avis3);

        System.out.println("All Avis records after adding:");
        for (Avis a : serviceAvis.getAll()) {
            System.out.println(a);
        }

        // Step 3: Update the second Avis (avis2)
        System.out.println("\n=== Updating the Second Avis (ID: " + avis2.getId() + ") ===");
        Avis updatedAvis2 = new Avis(2.0f, "Updated: Not so great.", LocalDateTime.now(), 1);
        updatedAvis2.setId(avis2.getId());
        serviceAvis.update(updatedAvis2);

        System.out.println("All Avis records after updating:");
        for (Avis a : serviceAvis.getAll()) {
            System.out.println(a);
        }

        // Step 4: Delete the third Avis (avis3)
        System.out.println("\n=== Deleting the Third Avis (ID: " + avis3.getId() + ") ===");
        serviceAvis.delete(avis3);

        System.out.println("All Avis records after deleting the third Avis:");
        for (Avis a : serviceAvis.getAll()) {
            System.out.println(a);
        }

        // Step 5: Clean up - Delete the remaining Avis records
        System.out.println("\n=== Cleaning Up: Deleting Remaining Avis ===");
        serviceAvis.delete(avis1);
        serviceAvis.delete(avis2);

        System.out.println("All Avis records after final cleanup:");
        for (Avis a : serviceAvis.getAll()) {
            System.out.println(a);
        }
    }
    }
