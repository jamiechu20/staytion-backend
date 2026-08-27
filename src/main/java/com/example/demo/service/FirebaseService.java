package com.example.demo.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);

    // 用來創建新用戶
    public String createFirebaseUser(String email, String firstName, String lastName) {
        try {
            // 創建新用戶的請求資料
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setDisplayName(firstName + " " + lastName)
                    .setEmailVerified(true); // 可以根據需要設置是否驗證過email

            // 執行創建用戶
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

            // 返回創建的用戶UID
            return userRecord.getUid();

        } catch (Exception e) {
            log.error("創建 Firebase 使用者失敗", e);
            return null;
        }
    }
    
    // 刪除 firebase 用戶
    public void deleteFirebaseUser(String email) {
        try {
            // 先透過 email 找出 Firebase UID
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            String uid = userRecord.getUid();

            // 根據 UID 刪除 Firebase 使用者
            FirebaseAuth.getInstance().deleteUser(uid);

            log.info("Firebase 使用者已刪除: {}", email);
        } catch (Exception e) {
            throw new RuntimeException("刪除 Firebase 使用者失敗: " + e.getMessage(), e);
        }
    }
}
