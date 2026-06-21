package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FirebaseService firebaseService;

    // 更新個人資料
    public User updateUserProfile(String email, String firstName, String lastName, String password, String tel) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("找不到使用者"));

        // 更新資料
        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName);
        }
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (tel != null && !tel.isBlank()) {
            user.setTel(tel);
        }

        // 保存更新後的資料
        return userRepository.save(user);
    }


    // 刪除帳號
    public String deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("找不到使用者"));

        // 刪除 Firebase 帳號（如有）
        firebaseService.deleteFirebaseUser(email); // 可選

        // 刪除資料庫中的使用者
        userRepository.delete(user);

        return "用戶已刪除";
    }


}
