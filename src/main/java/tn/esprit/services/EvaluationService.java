package tn.esprit.services;

import tn.esprit.utils.MyDataBase;
import java.sql.Connection;
import org.json.JSONObject;
import java.io.File;
import java.net.URI;
import java.net.http.*;
import java.sql.Connection;

public class EvaluationService {
    private final Connection conn;
    private static final String AFFINDA_API_KEY = "YOUR_AFFINDA_API_KEY";
    private static final String AFFINDA_API_URL = "https://api.affinda.com/v3/documents";

    public EvaluationService() {
        this.conn = MyDataBase.getInstance().getCnx();
    }
    private JSONObject parseCvWithApi(String cvPath) {
        try {
            File file = new File(cvPath);
            if (!file.exists()) {
                System.err.println("❌ CV file not found: " + cvPath);
                return null;
            }

            HttpClient client = HttpClient.newBuilder().build();

            // Build multipart form data
            String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofMultiPart(
                    boundary,
                    new HttpRequest.MultiPartBodyPublisher.Part(
                            "file",
                            file,
                            "application/pdf",
                            file.getName()
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AFFINDA_API_URL))
                    .header("Authorization", "Bearer " + AFFINDA_API_KEY)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(body)
                    .build();

            // Placeholder: Response handling to be implemented
            // Simulate Affinda API response
            JSONObject response = new JSONObject();
            response.put("education", "Master's in Computer Science");
            response.put("years_of_experience", 5);
            response.put("skills", "Java, Python, JavaFX");
            response.put("certifications", "Oracle Certified Java Developer");
            return response;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du parsing du CV: " + e.getMessage());
            return null;
        }
    }
}
