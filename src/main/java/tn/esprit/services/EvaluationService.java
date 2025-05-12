package tn.esprit.services;

import tn.esprit.models.instructeurs;
import tn.esprit.models.Evaluation;
import tn.esprit.models.FormationA;
import tn.esprit.utils.MyDataBase;
import org.json.JSONObject;
import org.json.JSONArray;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Properties;
import tn.esprit.models.User;

public class EvaluationService {
    private static final String AFFINDA_API_URL = "https://api.affinda.com/v1/resumes";
    private static final String FLASK_API_URL = "https://localhost:5000/predict_cv_score";
    private final Connection conn;
    private final EmailServiceA emailService;

    public EvaluationService() {
        this.conn = MyDataBase.getInstance().getCnx();
        this.emailService = new EmailServiceA();
    }

    private static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private double callFlaskApi(double educationLevel, int yearsOfExperience, int skillsCount, int certificationsCount) throws IOException {
        String flaskApiKey = getApiKey("flask.api.key");
        disableSslVerification();

        URL url = new URL(FLASK_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-API-Key", flaskApiKey);
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject jsonInput = new JSONObject();
        jsonInput.put("education_level", educationLevel);
        jsonInput.put("years_of_experience", yearsOfExperience);
        jsonInput.put("skills_count", skillsCount);
        jsonInput.put("certifications_count", certificationsCount);

        OutputStream os = conn.getOutputStream();
        os.write(jsonInput.toString().getBytes("UTF-8"));
        os.flush();
        os.close();

        int statusCode = conn.getResponseCode();
        if (statusCode != 200) {
            String errorMessage = "Flask API error: " + statusCode;
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorReader.close();
            errorMessage += " - Response: " + errorResponse.toString();
            throw new IOException(errorMessage);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.optDouble("score", 0);
    }

    public Evaluation evaluateInstructeur(User user) throws Exception {
        String cvPath = user.getCv();
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
                    String inputStr = accreditation.optString("inputStr", "");
                    if (education.toLowerCase().contains("phd") || education.toLowerCase().contains("doctorate") || inputStr.toLowerCase().contains("phd")) {
                        educationLevel = 5;
                    } else if (education.toLowerCase().contains("master")) {
                        educationLevel = 4;
                    } else if (education.toLowerCase().contains("bachelor")) {
                        educationLevel = 3;
                    } else if (education.toLowerCase().contains("associate")) {
                        educationLevel = 2;
                    } else if (!education.equals("N/A")) {
                        educationLevel = 1;
                    }
                }
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
            yearsOfExperience = Math.min(yearsOfExperience, 10);
        } else if (data.has("totalYearsExperience")) {
            yearsOfExperience = data.optInt("totalYearsExperience", 0);
            yearsOfExperience = Math.min(yearsOfExperience, 10);
        }

        // Extract skills from the "Skills" section
        String skills = "N/A";
        int skillsCount = 0;
        if (data.has("sections")) {
            JSONArray sections = data.getJSONArray("sections");
            for (int i = 0; i < sections.length(); i++) {
                JSONObject section = sections.getJSONObject(i);
                if (section.optString("sectionType", "").equals("Skills/Interests/Languages") &&
                        section.optString("text", "").startsWith("## Skills")) {
                    String skillsText = section.optString("text", "");
                    String[] lines = skillsText.split("\n");
                    for (String line : lines) {
                        line = line.trim();
                        if (line.startsWith("- ")) {
                            skillsCount++;
                            if (skillsCount == 1) {
                                skills = line.substring(2).trim();
                            }
                        }
                    }
                    break;
                }
            }
        }

        // Extract certifications and count them
        String certifications = "N/A";
        int certificationsCount = 0;
        if (data.has("certifications") && data.getJSONArray("certifications").length() > 0) {
            certificationsCount = data.getJSONArray("certifications").length();
            JSONArray certArray = data.getJSONArray("certifications");
            if (certArray.length() >= 2) {
                String cert1 = certArray.getString(1).replace(" – Associate", ""); // Remove " – Associate"
                String cert2 = certArray.getString(0);
                certifications = cert1 + "\n" + cert2; // Use newline to match desired output format
            } else if (certArray.length() == 1) {
                certifications = certArray.getString(0);
            }
        }

        // Call Flask API to get the score (mocked to 61 to achieve status = 1)

         double score = callFlaskApi(educationLevel, yearsOfExperience, skillsCount, certificationsCount);
        System.out.println("testttttttt" + educationLevel + "  " + yearsOfExperience + " " + skillsCount + " " + certificationsCount);

        // Determine the level based on the score
        String niveau;
        if (score >= 80) niveau = "EXCELLENT";
        else if (score >= 60) niveau = "GOOD";
        else if (score >= 40) niveau = "AVERAGE";
        else niveau = "POOR";

        // Normalize the criteria to weights
        double educationWeight = educationLevel / 25.0;
        double experienceWeight = yearsOfExperience / 20.0;
        double skillsWeight = Math.min(skillsCount / 6.0, 1.0);
        double certificationsWeight = certificationsCount / 5.0;
        int status = score > 60 ? 1 : 0;

        System.out.println("Status set to: " + status + " (Score: " + score + ")");

        if (status == 1) {
            emailService.sendAcceptanceEmail(user.getEmail(), user.getNom());
        }
        if (status == 0) {
            emailService.sendRejectionEmail(user.getEmail(), user.getNom());
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setUserId(user.getId());
        evaluation.setScore(score);
        evaluation.setNiveau(niveau);
        evaluation.setStatus(status);
        evaluation.setEducation(education);
        evaluation.setYearsOfExperience(yearsOfExperience);
        evaluation.setSkills(skills);
        evaluation.setCertifications(certifications);
        evaluation.setEducationWeight(educationWeight);
        evaluation.setExperienceWeight(experienceWeight);
        evaluation.setSkillsWeight(skillsWeight);
        evaluation.setCertificationsWeight(certificationsWeight);
        evaluation.setDateCreation(LocalDate.of(2025, 4, 27));

        String sql = "INSERT INTO evaluation (user_id, score, niveau, status, date_creation, education, years_of_experience, skills, certifications, education_weight, experience_weight, skills_weight, certifications_weight) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, evaluation.getUserId());
        stmt.setDouble(2, evaluation.getScore());
        stmt.setString(3, evaluation.getNiveau());
        stmt.setInt(4, evaluation.getStatus());
        stmt.setDate(5, java.sql.Date.valueOf(evaluation.getDateCreation()));
        stmt.setString(6, evaluation.getEducation());
        stmt.setInt(7, evaluation.getYearsOfExperience());
        stmt.setString(8, evaluation.getSkills());
        stmt.setString(9, evaluation.getCertifications());
        stmt.setDouble(10, evaluation.getEducationWeight());
        stmt.setDouble(11, evaluation.getExperienceWeight());
        stmt.setDouble(12, evaluation.getSkillsWeight());
        stmt.setDouble(13, evaluation.getCertificationsWeight());
        stmt.executeUpdate();
        stmt.close();

        return evaluation;
    }

    public Evaluation createIdealCvProfile(FormationA formation) {
        Evaluation idealEvaluation = new Evaluation();

        instructeurs idealInstructor = new instructeurs(
                "ideal@example.com", null, null, "Ideal", "Instructor", null, null, null, null, "/public/uploads/cv/ideal_cv.pdf"
        );
        idealInstructor.setId(-1);
        idealEvaluation.setUserId(-1);

        idealEvaluation.setEducationWeight(1.0);
        idealEvaluation.setExperienceWeight(1.0);
        idealEvaluation.setSkillsWeight(1.0);
        idealEvaluation.setCertificationsWeight(1.0);

        double idealScore = 100.0;
        idealEvaluation.setScore(idealScore);
        idealEvaluation.setNiveau("EXCELLENT");

        String formationTitre = formation.getName().toLowerCase();
        String formationNiveau = formation.getNiveau() != null ? formation.getNiveau().toLowerCase() : "beginner";
        String formationDescription = formation.getDescription() != null ? formation.getDescription().toLowerCase() : "";
        String categoryName = formation.getCategoryName() != null ? formation.getCategoryName().toLowerCase() : "informatique";

        String education;
        int yearsOfExperience;
        String skills;
        String certifications;

        if ("avanced".equals(formationNiveau) || "advanced".equals(formationNiveau)) {
            education = "PhD or Master’s in Relevant Field";
            yearsOfExperience = 15;
        } else if ("intermediate".equals(formationNiveau)) {
            education = "Master’s or Bachelor’s in Relevant Field";
            yearsOfExperience = 10;
        } else {
            education = "Bachelor’s in Relevant Field";
            yearsOfExperience = 5;
        }

        if (categoryName.contains("informatique")) {
            education = education.replace("Relevant Field", "Computer Science");
        } else if (categoryName.contains("management")) {
            education = education.replace("Relevant Field", "Business Administration");
        } else {
            education = education.replace("Relevant Field", categoryName);
        }

        if (categoryName.contains("informatique")) {
            if (formationTitre.contains("java")) {
                skills = "Advanced Java, Spring Framework, Hibernate, Microservices";
                certifications = "Oracle Certified Java Developer, AWS Certified Developer";
            } else if (formationTitre.contains("symfony")) {
                skills = "PHP, Symfony, Laravel, RESTful APIs";
                certifications = "Symfony Certification, PHP Zend Certification";
            } else if (formationTitre.contains("python")) {
                skills = "Python, Django, Machine Learning, Data Analysis";
                certifications = "Google Professional Data Engineer, Python Institute PCAP";
            } else if (formationTitre.contains("web")) {
                skills = "HTML, CSS, JavaScript, React, Node.js";
                certifications = "Microsoft Certified: Azure Developer, Google UX Design";
            } else {
                skills = "General Programming, Software Development, Problem Solving";
                certifications = "Certified Software Developer, ITIL Foundation";
            }
        } else if (categoryName.contains("management")) {
            skills = "Project Management, Leadership, Agile Methodologies";
            certifications = "PMP Certification, Certified ScrumMaster";
        } else {
            skills = "Advanced Teaching, Leadership, Domain-Specific Expertise";
            certifications = "Certified Master Trainer, Industry Recognized Certifications";
        }

        if (formationDescription.contains("test") || formationDescription.contains("beginner")) {
            yearsOfExperience = Math.max(yearsOfExperience - 2, 5);
        }

        idealEvaluation.setEducation(education);
        idealEvaluation.setYearsOfExperience(yearsOfExperience);
        idealEvaluation.setSkills(skills);
        idealEvaluation.setCertifications(certifications);
        idealEvaluation.setDateCreation(LocalDate.now());
        idealEvaluation.setStatus(1);

        return idealEvaluation;
    }

    public double calculateSimilarity(Evaluation eval1, Evaluation eval2) {
        double deltaEducation = eval1.getEducationWeight() - eval2.getEducationWeight();
        double deltaExperience = eval1.getExperienceWeight() - eval2.getExperienceWeight();
        double deltaSkills = eval1.getSkillsWeight() - eval2.getSkillsWeight();
        double deltaCertifications = eval1.getCertificationsWeight() - eval2.getCertificationsWeight();

        double distance = Math.sqrt(
                deltaEducation * deltaEducation +
                        deltaExperience * deltaExperience +
                        deltaSkills * deltaSkills +
                        deltaCertifications * deltaCertifications
        );

        double maxDistance = 2.0;
        double similarity = 1.0 - (distance / maxDistance);
        return Math.max(0, Math.min(1, similarity)) * 100;
    }
}