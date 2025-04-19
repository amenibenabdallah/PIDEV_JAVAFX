package tn.esprit.services;

import tn.esprit.models.instructeurs;
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
    private static final String AFFINDA_API_URL = "https://api.affinda.com/v1/resumes"; // URL for Affinda API
    private final Connection conn; // Database connection

    public EvaluationService() {
        // Get database connection from MyDataBase class
        this.conn = MyDataBase.getInstance().getCnx();
    }

    // Read the API key from config.properties file
    private String getAffindaApiKey() throws IOException {
        // Load the properties file
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream("C:/Users/LENOVO/Desktop/PIDEV_JAVAFX/config.properties");
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
        if (statusCode != 200) { // Expect 200 OK based on test output
            System.out.println("Affinda API error: " + statusCode);
            // Read the error response for debugging
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorReader.close();
            System.out.println("Error response: " + errorResponse.toString());
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

        // Log the raw response for debugging
        System.out.println("Raw API response: " + response.toString());

        // Convert the response to JSON
        return new JSONObject(response.toString());
    }

    // Evaluate the instructor based on their CV
    public Evaluation evaluateInstructeur(instructeurs instructeur) throws Exception {
        // Get the CV path from the instructor
        String cvPath = instructeur.getCv();
        JSONObject cvData = parseCv(cvPath);
        if (cvData == null) {
            System.out.println("Evaluation failed: No CV data returned from Affinda API.");
            return null; // If CV parsing failed, return null
        }

        // Get the main data from the API response
        JSONObject data = cvData.optJSONObject("data");
        if (data == null) {
            System.out.println("No 'data' field in API response. Proceeding with empty data.");
            data = new JSONObject(); // Proceed with empty data
        }

        // Extract education (try multiple possible fields)
        String education = "N/A";
        if (data.has("education") && data.getJSONArray("education").length() > 0) {
            JSONObject educationObj = data.getJSONArray("education").getJSONObject(0);
            // Try different possible fields for education
            if (educationObj.has("qualification")) {
                education = educationObj.optString("qualification", "N/A");
            } else if (educationObj.has("accreditation")) {
                JSONObject accreditation = educationObj.optJSONObject("accreditation");
                if (accreditation != null) {
                    education = accreditation.optString("education", "N/A");
                }
            }
        }

        // Extract years of experience (try multiple possible fields)
        int yearsOfExperience = 0;
        if (data.has("workExperience") && data.getJSONArray("workExperience").length() > 0) {
            JSONObject workExp = data.getJSONArray("workExperience").getJSONObject(0);
            if (workExp.has("years")) {
                yearsOfExperience = workExp.optInt("years", 0);
            } else if (workExp.has("dates")) {
                JSONObject dates = workExp.optJSONObject("dates");
                if (dates != null && dates.has("monthsInPosition")) {
                    yearsOfExperience = dates.optInt("monthsInPosition", 0) / 12;
                }
            } else if (workExp.has("totalYearsExperience")) {
                yearsOfExperience = data.optInt("totalYearsExperience", 0);
            }
        } else if (data.has("totalYearsExperience")) {
            yearsOfExperience = data.optInt("totalYearsExperience", 0);
        }

        // Extract skills
        String skills = "N/A";
        if (data.has("skills") && data.getJSONArray("skills").length() > 0) {
            JSONObject skillObj = data.getJSONArray("skills").getJSONObject(0);
            skills = skillObj.optString("name", "N/A");
        }

        // Extract certifications (handle both object and string formats)
        String certifications = "N/A";
        if (data.has("certifications") && data.getJSONArray("certifications").length() > 0) {
            Object certObj = data.getJSONArray("certifications").get(0);
            if (certObj instanceof JSONObject) {
                certifications = ((JSONObject) certObj).optString("name", "N/A");
            } else if (certObj instanceof String) {
                certifications = (String) certObj;
            }
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
        evaluation.setStatus(1); // Set as string for internal use
        evaluation.setEducation(education);
        evaluation.setYearsOfExperience(yearsOfExperience);
        evaluation.setSkills(skills);
        evaluation.setCertifications(certifications);
        evaluation.setInstructeur(instructeur);

        // Map the status string to an integer
        int statusCode = "COMPLETED".equals(evaluation.getStatus()) ? 1 : 0;

        // Save the evaluation to the database
        String sql = "INSERT INTO evaluation (instructor_id, score, niveau, status, date_creation, education, years_of_experience, skills, certifications) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, evaluation.getInstructorId());
        stmt.setDouble(2, evaluation.getScore());
        stmt.setString(3, evaluation.getNiveau());
        stmt.setInt(4, statusCode); // Set as integer
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