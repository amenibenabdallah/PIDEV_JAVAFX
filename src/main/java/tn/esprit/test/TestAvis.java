package tn.esprit.test;

import tn.esprit.models.Avis;
import tn.esprit.services.ServiceAvis;
import java.time.LocalDateTime;

public class TestAvis {
    public static void main(String[] args) {
        ServiceAvis serviceAvis = new ServiceAvis();

        // Add some reviews for formation_id=1
        Avis avis1 = new Avis(4.5f, "Great course, very informative!", LocalDateTime.now(), 1);
        serviceAvis.add(avis1);

        Avis avis2 = new Avis(3.0f, "Average experience, needs improvement.", LocalDateTime.now(), 1);
        avis2.setDateCreation(LocalDateTime.now());
        serviceAvis.add(avis2);

        System.out.println("All Avis records:");
        for (Avis avis : serviceAvis.getAll()) {
            System.out.println(avis);
        }
    }
}