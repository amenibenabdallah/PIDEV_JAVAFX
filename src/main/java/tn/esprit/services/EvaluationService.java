package tn.esprit.services;

import tn.esprit.entities.instructeurs;
import tn.esprit.models.Evaluation;
import tn.esprit.utils.MyDataBase;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

public class EvaluationService {
    private static final String AFFINDA_API_URL = "https://api.affinda.com/v3/documents"; // URL for Affinda API
    private final Connection conn; // Database connection

    public EvaluationService() {
        // Get database connection from MyDataBase class
        this.conn = MyDataBase.getInstance().getCnx();
    }

    // Read the API key from config.properties file
    private String getAffindaApiKey() throws IOException {
        // Load the properties file
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream("config.properties");
        props.load(fis);
        fis.close();

        // Get the API key from the properties
        String apiKey = props.getProperty("affinda.api.key");
        if (apiKey == null) {
            throw new IOException("API key not found in config.properties");
        }
        return apiKey;
    }

    // Parse the CV using Affinda API
    public JSONObject parseCv(String cvPath) throws IOException {
        // Check if the CV file exists
        File file = new File(cvPath);
        if (!file.exists()) {
            System.out.println("CV file not found: " + cvPath);
            return null;
        }

        // Get the API key
        String apiKey = getAffindaApiKey();

        // Set up the connection to Affinda API
        URL url = new URL(AFFINDA_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true); // Allow sending data
        conn.setRequestMethod("POST"); // Use POST method
        conn.setRequestProperty("Authorization", "Bearer " + apiKey); // Add API key to header
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=----SimpleBoundary");

        // Send the CV file to the API
        OutputStream os = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
        writer.append("------SimpleBoundary\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"cv.pdf\"\r\n");
        writer.append("Content-Type: application/pdf\r\n");
        writer.append("\r\n").flush();

        // Read the CV file and send it
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        fis.close();
        os.flush();
        writer.append("\r\n").flush();
        writer.append("------SimpleBoundary--\r\n").flush();
        writer.close();

        // Check if the API call was successful
        int statusCode = conn.getResponseCode();
        if (statusCode != 201) {
            System.out.println("Affinda API error: " + statusCode);
            return null;
        }

        // Read the API response
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Convert the response to JSON
        return new JSONObject(response.toString());
    }

    // Evaluate the instructor based on their CV
    public Evaluation evaluateInstructeur(instructeurs instructeur) throws Exception {
        // Get the CV path from the instructor
        String cvPath = instructeur.getCvPath();
        JSONObject cvData = parseCv(cvPath);
        if (cvData == null) {
            return null; // If CV parsing failed, return null
        }

        // Get the main data from the API response
        JSONObject data = cvData.getJSONObject("data");

        // Extract education (e.g., degree or qualification)
        String education = "N/A";
        if (data.has("education") && data.getJSONArray("education").length() > 0) {
            education = data.getJSONArray("education").getJSONObject(0).optString("qualification", "N/A");
        }

        // Extract years of experience
        int yearsOfExperience = 0;
        if (data.has("workExperience") && data.getJSONArray("workExperience").length() > 0) {
            yearsOfExperience = data.getJSONArray("workExperience").getJSONObject(0).optInt("years", 0);
        }

        // Extract skills
        String skills = "N/A";
        if (data.has("skills") && data.getJSONArray("skills").length() > 0) {
            skills = data.getJSONArray("skills").getJSONObject(0).optString("name", "N/A");
        }

        // Extract certifications
        String certifications = "N/A";
        if (data.has("certifications") && data.getJSONArray("certifications").length() > 0) {
            certifications = data.getJSONArray("certifications").getJSONObject(0).optString("name", "N/A");
        }

        // Calculate a simple score (out of 100)
        double score = 0;
        if (!education.equals("N/A")) score += 25; // Add 25 points if education is present
        score += yearsOfExperience * 2; // Add 2 points for each year of experience
        if (!skills.equals("N/A")) score += 25; // Add 25 points if skills are present
        if (!certifications.equals("N/A")) score += 25; // Add 25 points if certifications are present
        if (score > 100) score = 100; // Cap the score at 100

        // Determine the level based on the score
        String niveau;
        if (score >= 80) niveau = "EXCELLENT";
        else if (score >= 60) niveau = "GOOD";
        else if (score >= 40) niveau = "AVERAGE";
        else niveau = "POOR";

        // Create a new Evaluation object
        Evaluation evaluation = new Evaluation();
        evaluation.setInstructorId(instructeur.getId());
        evaluation.setScore(score);
        evaluation.setNiveau(niveau);
        evaluation.setStatus("COMPLETED");
        evaluation.setEducation(education);
        evaluation.setYearsOfExperience(yearsOfExperience);
        evaluation.setSkills(skills);
        evaluation.setCertifications(certifications);
        evaluation.setInstructeur(instructeur);

        // Save the evaluation to the database
        String sql = "INSERT INTO evaluation (instructor_id, score, niveau, status, date_creation, education, years_of_experience, skills, certifications) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, evaluation.getInstructorId());
        stmt.setDouble(2, evaluation.getScore());
        stmt.setString(3, evaluation.getNiveau());
        stmt.setString(4, evaluation.getStatus());
        stmt.setDate(5, java.sql.Date.valueOf(evaluation.getDateCreation()));
        stmt.setString(6, evaluation.getEducation());
        stmt.setInt(7, evaluation.getYearsOfExperience());
        stmt.setString(8, evaluation.getSkills());
        stmt.setString(9, evaluation.getCertifications());
        stmt.executeUpdate();
        stmt.close();

        return evaluation;
    }
}