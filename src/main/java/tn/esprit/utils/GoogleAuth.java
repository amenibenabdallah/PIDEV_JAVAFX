package tn.esprit.utils;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import javafx.scene.control.TextInputDialog;

import java.awt.*;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class  GoogleAuth {
    private static final String CLIENT_ID = "572574796043-4hqfmsq9f5b7brvd3l3j5cmfiajllh0n.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";  // Laisse vide si non fourni

    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    public static GoogleIdToken authenticate() throws Exception {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPES)
                .build();

        String url = flow.newAuthorizationUrl().setRedirectUri("urn:ietf:wg:oauth:2.0:oob").build();

        // Ouvrir le lien dans le navigateur
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        } else {
            System.out.println("Ouvre ce lien dans ton navigateur : " + url);
        }

        // Boîte de dialogue pour saisir le code
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Google Auth");
        dialog.setHeaderText("Entrez le code d'authentification");
        dialog.setContentText("Code:");

        String code = dialog.showAndWait().orElse("");

        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri("urn:ietf:wg:oauth:2.0:oob")
                .execute();

        return GoogleIdToken.parse(JSON_FACTORY, tokenResponse.getIdToken());
    }
}