package com.pokiepaws.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FirebaseConfig {

  public FirebaseConfig(
      @Value("${app.firebase.service-account-path:}") String serviceAccountPath,
      @Value("${app.firebase.service-account-json-base64:}") String serviceAccountJsonBase64) {

    if (!FirebaseApp.getApps().isEmpty()) {
      return;
    }

    try {
      GoogleCredentials credentials =
          resolveCredentials(serviceAccountPath, serviceAccountJsonBase64);
      if (credentials == null) {
        log.info("Firebase Admin SDK is not configured. Mobile push notifications are disabled.");
        return;
      }

      FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
      FirebaseApp.initializeApp(options);
      log.info("Firebase Admin SDK initialized.");
    } catch (IOException | RuntimeException ex) {
      log.warn(
          "Firebase Admin SDK could not be initialized. Mobile push notifications are disabled. Reason: {}",
          ex.getMessage());
    }
  }

  private GoogleCredentials resolveCredentials(String path, String jsonBase64) throws IOException {
    if (path != null && !path.isBlank()) {
      try (FileInputStream serviceAccount = new FileInputStream(path)) {
        return GoogleCredentials.fromStream(serviceAccount);
      }
    }

    if (jsonBase64 != null && !jsonBase64.isBlank()) {
      byte[] decoded = Base64.getDecoder().decode(jsonBase64);
      return GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
    }

    return null;
  }
}
