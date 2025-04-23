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
    private static final String FLASK_API_URL = "https://localhost:5000/predict_cv_score"; // URL for Flask API
    private final Connection conn; // Database connection

    public EvaluationService() {
        // Get database connection from MyDataBase class
        this.conn = MyDataBase.getInstance().getCnx();
    }

    // Read the API key from config.properties file (for Affinda and Flask)
    private String getApiKey(String keyName) throws IOException {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream("C:/Users/LENOVO/Desktop/PIDEV_JAVAFX/config.properties");
        props.load(fis);
        fis.close();

        String apiKey = props.getProperty(keyName);
        if (apiKey == null) {
            throw new IOException(keyName + " not found in config.properties");
        }
        return apiKey;
    }

    // Parse the CV using Affinda API
    public JSONObject parseCv(String cvPath) throws IOException {
        File file = new File(cvPath);
        if (!file.exists()) {
            System.out.println("CV file not found: " + cvPath);
            return null;
        }

        String apiKey = getApiKey("affinda.api.key");
        URL url = new URL(AFFINDA_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=----SimpleBoundary");

        OutputStream os = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
        writer.append("------SimpleBoundary\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"cv.pdf\"\r\n");
        writer.append("Content-Type: application/pdf\r\n");
        writer.append("\r\n").flush();

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

        int statusCode = conn.getResponseCode();
        if (statusCode != 200) {
            System.out.println("Affinda API error: " + statusCode);
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

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        System.out.println("Raw API response: " + response.toString());
        return new JSONObject(response.toString());
    }

    // Call Flask API to predict CV score
    private double callFlaskApi(double educationLevel, int yearsOfExperience, int skillsCount, int certificationsCount) throws IOException {
        String flaskApiKey = getApiKey("flask.api.key");

        URL url = new URL(FLASK_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-API-Key", flaskApiKey);
        conn.setRequestProperty("Content-Type", "application/json");

        // Create JSON payload
        JSONObject jsonInput = new JSONObject();
        jsonInput.put("education_level", educationLevel);
        jsonInput.put("years_of_experience", yearsOfExperience);
        jsonInput.put("skills_count", skillsCount);
        jsonInput.put("certifications_count", certificationsCount);

        // Send JSON payload
        OutputStream os = conn.getOutputStream();
        os.write(jsonInput.toString().getBytes("UTF-8"));
        os.flush();
        os.close();

        int statusCode = conn.getResponseCode();
        if (statusCode != 200) {
            System.out.println("Flask API error: " + statusCode);
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorReader.close();
            System.out.println("Error response: " + errorResponse.toString());
            return 0; // Default score on error
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.optDouble("score", 0); // Default to 0 if score not found
    }

    // Evaluate the instructor based on their CV
    public Evaluation evaluateInstructeur(instructeurs instructeur) throws Exception {
        String cvPath = instructeur.getCv();
        JSONObject cvData = parseCv(cvPath);
        if (cvData == null) {
            System.out.println("Evaluation failed: No CV data returned from Affinda API.");
            return null;
        }

        JSONObject data = cvData.optJSONObject("data");
        if (data == null) {
            System.out.println("No 'data' field in API response. Proceeding with empty data.");
            data = new JSONObject();
        }

        // Extract education and map to a numeric level
        String education = "N/A";
        double educationLevel = 0;
        if (data.has("education") && data.getJSONArray("education").length() > 0) {
            JSONObject educationObj = data.getJSONArray("education").getJSONObject(0);
            if (educationObj.has("qualification")) {
                education = educationObj.optString("qualification", "N/A");
            } else if (educationObj.has("accreditation")) {
                JSONObject accreditation = educationObj.optJSONObject("accreditation");
                if (accreditation != null) {
                    education = accreditation.optString("education", "N/A");
                }
            }
            // Map education to a numeric level (simplified mapping)
            if (education.toLowerCase().contains("phd") || education.toLowerCase().contains("doctorate")) {
                educationLevel = 5;
            } else if (education.toLowerCase().contains("master")) {
                educationLevel = 4;
            } else if (education.toLowerCase().contains("bachelor")) {
                educationLevel = 3;
            } else if (education.toLowerCase().contains("associate")) {
                educationLevel = 2;
            } else if (!education.equals("N/A")) {
                educationLevel = 1; // Some education
            }
        }

        // Extract years of experience
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
            }
        } else if (data.has("totalYearsExperience")) {
            yearsOfExperience = data.optInt("totalYearsExperience", 0);
        }

        // Extract skills and count them
        String skills = "N/A";
        int skillsCount = 0;
        if (data.has("skills") && data.getJSONArray("skills").length() > 0) {
            skillsCount = data.getJSONArray("skills").length();
            JSONObject skillObj = data.getJSONArray("skills").getJSONObject(0);
            skills = skillObj.optString("name", "N/A");
        }

        // Extract certifications and count them
        String certifications = "N/A";
        int certificationsCount = 0;
        if (data.has("certifications") && data.getJSONArray("certifications").length() > 0) {
            certificationsCount = data.getJSONArray("certifications").length();
            Object certObj = data.getJSONArray("certifications").get(0);
            if (certObj instanceof JSONObject) {
                certifications = ((JSONObject) certObj).optString("name", "N/A");
            } else if (certObj instanceof String) {
                certifications = (String) certObj;
            }
        }

        // Call Flask API to get the score
        double score = callFlaskApi(educationLevel, yearsOfExperience, skillsCount, certificationsCount);

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
        evaluation.setStatus(1);
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
        stmt.setInt(4, statusCode);
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