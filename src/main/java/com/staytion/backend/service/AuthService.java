package com.staytion.backend.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.staytion.backend.dto.GoogleUserInfo;
import com.staytion.backend.dto.response.GoogleLoginResult;
import com.staytion.backend.exception.DuplicateEmailException;
import com.staytion.backend.exception.EmailNotVerifiedException;
import com.staytion.backend.exception.InvalidCredentialsException;
import com.staytion.backend.exception.PasswordConfirmationMismatchException;
import com.staytion.backend.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.staytion.backend.dto.LoginRequest;
import com.staytion.backend.dto.RegisterRequest;
import com.staytion.backend.entity.User;
import com.staytion.backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

	private final JwtService jwtService;

    private final GoogleTokenVerifierService googleTokenVerifierService;

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
        String email = user.getEmail();
        mailService.sendTemplatedMail(email, "驗證信箱", "verify-email", Map.of(
                "message", "請點擊以下連結驗證信箱：",
                "linkText", "驗證信箱",
                "link", verifyLink
        ));

    }

    // Google 登入後的用戶資料
    public GoogleLoginResult handleGoogleLogin(String idToken) {
        GoogleUserInfo googleUserInfo = googleTokenVerifierService.verify(idToken);
        String email = googleUserInfo.email();
        String firstName = googleUserInfo.firstName();
        String lastName = googleUserInfo.lastName();
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        Boolean isNewUser = userOptional.isEmpty();
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

        String token = jwtService.createToken(user);

        return new GoogleLoginResult(user, token, isNewUser);
    }

    // 發送重設密碼連結
    public void sendResetPasswordEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(user -> {
            String token = jwtService.createToken(user); // JWT: 含 email、15 分鐘有效
            String resetLink = resetPasswordUrl + "?token=" + token;
            mailService.sendTemplatedMail(email, "重設密碼", "reset-password", Map.of("link", resetLink));
        });
    }

	// 重設密碼
	public void resetPassword(String token, String newPassword, String confirmPassword) {
		if (!newPassword.equals(confirmPassword)) {
			throw new PasswordConfirmationMismatchException("新密碼與確認密碼不一致");
		}

        String email = jwtService.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("找不到使用者"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
	}

}
