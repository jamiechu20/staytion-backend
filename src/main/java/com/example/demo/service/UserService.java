package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final FirebaseService firebaseService;

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


    public String deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("找不到使用者"));

        // 刪除 Firebase 帳號
        firebaseService.deleteFirebaseUser(email);

        // 刪除資料庫中的使用者
        userRepository.delete(user);

        return "用戶已刪除";
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("找不到使用者"));
    }
}
