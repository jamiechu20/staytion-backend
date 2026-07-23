package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.EmailNotVerifiedException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

	private final JwtService jwtService;

	private final FirebaseService firebaseService;

	@Value("${app.reset.password.url}")
	private String resetPasswordUrl;

    @Value("${app.email.verify.url}")
    private String verifyEmailUrl;

    // 檢查 email 是否存在
    public boolean checkEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	// 使用email+密碼登入
	public String loginWithPassword(LoginRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new InvalidCredentialsException("帳號或密碼錯誤"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("帳號或密碼錯誤");
        }
        if(!user.getEmailVerified()){
            throw  new EmailNotVerifiedException("電子郵件尚未驗證");
        }
        return jwtService.createToken(user);
    }

    // 註冊帳號
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("此Email已註冊帳號");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTel(request.getTel());
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.createEmailVerificationToken(user);
        String verifyLink = verifyEmailUrl + "?token=" + token;
        String html = "<p>請點擊以下連結驗證信箱：</p><a href=\"" + verifyLink + "\">驗證信箱</a>";
        String email = user.getEmail();
        mailService.sendHtmlMail(email, "驗證信箱", html);

    }

    // 用於處理 Google 登入後的用戶資料
    public ResponseEntity<Map<String, Object>> handleGoogleLogin(String email, String firstName, String lastName) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // Firebase 同步可選擇性抽出或加參數控制
        firebaseService.createFirebaseUser(email, firstName, lastName);

        String token = jwtService.createToken(user);

        return ResponseEntity.ok(Map.of("message",
                userOptional.isPresent() ? "User already exists" : "New user created", "user", user, "token", token));
    }

    // 發送重設密碼連結
    public void sendResetPasswordEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(user -> {
            String token = jwtService.createToken(user); // JWT: 含 email、15 分鐘有效
            String resetLink = resetPasswordUrl + "?token=" + token;
            String html = "<p>請點擊以下連結重設密碼：</p><a href=\"" + resetLink + "\">重設密碼</a>";
            mailService.sendHtmlMail(email, "重設密碼", html);
        });
    }

	// 重設密碼
	public ResponseEntity<Map<String, String>> resetPassword(String token, String newPassword, String confirmPassword) {
		// 檢查新密碼和確認密碼是否一致
		if (!newPassword.equals(confirmPassword)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "密碼與確認密碼不一致"));
		}

		try {
			String email = jwtService.getEmailFromToken(token);
			User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("找不到使用者"));

			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);

			// 返回成功訊息
			return ResponseEntity.ok(Map.of("message", "密碼已更新，請重新登入！"));
		} catch (Exception e) {
			// 返回錯誤訊息
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Token 錯誤或已過期"));
		}
	}

}
