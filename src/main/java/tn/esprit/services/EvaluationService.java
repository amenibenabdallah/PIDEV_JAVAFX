package tn.esprit.services;

import tn.esprit.utils.MyDataBase;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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

            String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
            URL url = new URL(AFFINDA_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + AFFINDA_API_KEY);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
                writer.append("Content-Type: application/pdf\r\n");
                writer.append("\r\n").flush();

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                os.flush();
                writer.append("\r\n").flush();

                writer.append("--").append(boundary).append("--\r\n").flush();
            }

            int statusCode = conn.getResponseCode();
            if (statusCode != 201) {
                String errorResponse = readStream(statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream());
                System.err.println("❌ Affinda API error: " + statusCode + " - " + errorResponse);
                return null;
            }

            String responseBody = readStream(conn.getInputStream());
            try {
                JSONObject apiResponse = new JSONObject(responseBody);
                JSONObject result = new JSONObject();
                result.put("education", apiResponse.optJSONObject("data")
                        .optJSONArray("education")
                        .optJSONObject(0, new JSONObject())
                        .optString("qualification", ""));
                int years = apiResponse.optJSONObject("data")
                        .optJSONArray("workExperience")
                        .optJSONObject(0, new JSONObject())
                        .optInt("years", 0);
                result.put("years_of_experience", years);
                result.put("skills", apiResponse.optJSONObject("data")
                        .optJSONArray("skills")
                        .optJSONObject(0, new JSONObject())
                        .optString("name", ""));
                result.put("certifications", apiResponse.optJSONObject("data")
                        .optJSONArray("certifications")
                        .optJSONObject(0, new JSONObject())
                        .optString("name", ""));
                return result;
            } catch (JSONException e) {
                System.err.println("❌ JSON parsing error in Affinda response: " + e.getMessage());
                return null;
            } catch (NoClassDefFoundError e) {
                System.err.println("❌ org.json library not found in classpath: " + e.getMessage());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du parsing du CV: " + e.getMessage());
            return null;
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}