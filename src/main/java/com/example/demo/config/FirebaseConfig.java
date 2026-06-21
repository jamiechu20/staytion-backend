package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            System.out.println("開始初始化 Firebase");
            InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("firebase/staytion-serviceAccountKey.json");

            if (serviceAccount == null) {
                throw new RuntimeException("找不到 serviceAccountKey.json，請確認路徑");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase 初始化完成");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
