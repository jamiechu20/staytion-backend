package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;

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

    // 檢查 email 是否存在
    public ResponseEntity<String> checkEmail(String email) {

		if (userRepository.existsByEmail(email)) {
			return ResponseEntity.ok("EXISTS");
		} else {
			// 帳號不存在
			return ResponseEntity.status(404).body("NOT_FOUND");
		}
	}

	// 使用email+密碼登入
	public ResponseEntity<Map<String, String>> loginWithPassword(LoginRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "帳號不存在"));

		// 檢查 Firebase 上的 email 是否已驗證
		try {
			UserRecord firebaseUser = FirebaseAuth.getInstance().getUserByEmail(request.getEmail());
			System.out.println("Firebase 驗證狀態：" + firebaseUser.isEmailVerified());
			if (!firebaseUser.isEmailVerified()) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "您的電子郵件尚未驗證。請查收驗證郵件。");
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Firebase 錯誤：" + e.getMessage());
		}

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "密碼錯誤");
		}

        String token = jwtService.createToken(user);

        return ResponseEntity.ok(Map.of("token", token));
    }

    // 註冊
    public ResponseEntity<Map<String, String>> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email 已註冊");
        }

        // 1. 儲存本地用戶
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTel(request.getTel());
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        try {
            // 2. 在 Firebase 建立帳號
            CreateRequest firebaseRequest = new CreateRequest().setEmail(request.getEmail())
                    .setPassword(request.getPassword());

            UserRecord firebaseUser = FirebaseAuth.getInstance().createUser(firebaseRequest);
            System.out.println("Firebase user created: " + firebaseUser.getUid());

			// 3. 產生驗證連結並寄信
			String link = FirebaseAuth.getInstance().generateEmailVerificationLink(request.getEmail());
			String html = "<p>請點擊以下連結完成帳號驗證：</p><a href=\"" + link + "\">驗證帳號</a>";
			mailService.sendHtmlMail(request.getEmail(), "帳號驗證信", html);
			System.out.println(link);

        } catch (Exception e) {
            throw new RuntimeException("Firebase 建立用戶或發送驗證信失敗: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "註冊成功，驗證郵件已發送"));
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
    public ResponseEntity<Map<String, String>> sendResetPasswordEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // 返回 404 錯誤，並將錯誤訊息包裝成 JSON 格式
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "該 email 不存在"));
        }

		User user = userOpt.get();
		String token = jwtService.createToken(user); // JWT: 含 email、15 分鐘有效
		String resetLink = resetPasswordUrl + "?token=" + token;
		System.out.println(resetLink);
		String html = "<p>請點擊以下連結重設密碼：</p><a href=\"" + resetLink + "\">重設密碼</a>";
		mailService.sendHtmlMail(email, "重設密碼", html);

        // 返回 200 OK，並將成功訊息包裝成 JSON 格式
        return ResponseEntity.ok(Map.of("message", "重設密碼連結已寄出"));
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
