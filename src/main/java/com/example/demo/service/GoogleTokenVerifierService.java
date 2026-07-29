package com.example.demo.service;

import com.example.demo.dto.GoogleUserInfo;
import com.example.demo.exception.InvalidGoogleTokenException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenVerifierService {
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierService(@Value("${google.client.id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleUserInfo verify(String idToken) {
        GoogleIdToken googleIdToken;
        try {
            googleIdToken = verifier.verify(idToken);
        } catch (GeneralSecurityException | IOException e){
            throw new InvalidGoogleTokenException("Google 登入驗證失敗", e);
        }
        if(googleIdToken==null){
            throw new InvalidGoogleTokenException("Google 登入驗證失敗");
        }
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        String email = payload.getEmail();
        Boolean emailVerified = payload.getEmailVerified();
        if(emailVerified==null || !emailVerified){
            throw new InvalidGoogleTokenException("Google 登入驗證失敗");
        }
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        return new GoogleUserInfo(email, firstName, lastName);
    }

}
