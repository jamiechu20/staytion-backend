package com.example.demo.controller;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.EmailRequest;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class AuthController {

    private final MailService mailService;
	private final AuthService authService;

	// Google 登入
	@PostMapping("/auth/google-login")
	@Operation(summary = "Google登入")
	public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody Map<String, String> googleUserData) {
		String email = googleUserData.get("email");
		String firstName = googleUserData.get("firstName");
		String lastName = googleUserData.get("lastName");

		return authService.handleGoogleLogin(email, firstName, lastName);
	}

	// 檢查email是否存在
	@Operation(summary = "檢查電子郵件是否已註冊", description = "提供電子郵件來檢查該郵箱是否已經註冊過")
	@PostMapping("/auth/check-email")
	public ResponseEntity<Map<String, Boolean>> checkEmailExist(@RequestBody @Valid EmailRequest request) {
		boolean exists = authService.checkEmail(request.getEmail());
		return ResponseEntity.ok(Map.of("exists", exists));
	}

	// 使用密碼登入
	@Operation(summary = "使用密碼登入", description = "根據使用者提供的帳號和密碼進行登入，成功後返回 JWT Token")
	@PostMapping("/auth/login-password")
	public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginRequest request) {
		String token =  authService.loginWithPassword(request);
		return ResponseEntity.ok(Map.of("token", token));
	}

	// 註冊並呼叫Service處理註冊與寄信
	@Operation(summary = "註冊新用戶", description = "提供註冊所需資料(email、password、lastName、firstName)並創建新用戶，註冊成功會發送確認郵件")
	@PostMapping("/auth/register")
	public ResponseEntity<Map<String, String>> register(@RequestBody @Valid RegisterRequest request) {
		authService.register(request);
		return ResponseEntity.ok(Map.of("message", "註冊成功，驗證郵件已發送"));
	}

	// 檢查使用者email是否經過驗證
	@Operation(summary = "檢查使用者email是否經過驗證", description = "判斷使用者是否有點擊驗證連結")
	@GetMapping("/auth/check-email-verified")
	public ResponseEntity<Map<String, Boolean>> checkEmailVerified(@RequestParam String email)
			throws FirebaseAuthException {
		UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
		boolean verified = userRecord.isEmailVerified();
		return ResponseEntity.ok(Map.of("emailVerified", verified));
	}

	// 重新發送驗證信
	@PostMapping("/auth/resend-verification")
	@Operation(summary = "重新發送驗證信")
	public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> body)
			throws FirebaseAuthException {
		String email = body.get("email");
		String link = FirebaseAuth.getInstance().generateEmailVerificationLink(email);
		String html = "<p>請點擊以下連結完成帳號驗證：</p><a href=\"" + link + "\">驗證帳號</a>";
		System.out.println(link);
		mailService.sendHtmlMail(email, "重新寄發驗證信", html);
		return ResponseEntity.ok(Map.of("message", "驗證信已寄出"));
	}

	// 忘記密碼，發送驗證連結
	@Operation(summary = "忘記密碼 - 發送重設密碼郵件", description = "用戶輸入註冊時的郵件地址，系統會發送重設密碼的郵件。")
	@PostMapping("/auth/forgot-password")
	public ResponseEntity<Map<String, String>> sendResetPasswordEmail(@RequestBody ForgotPasswordRequest request) {
		authService.sendResetPasswordEmail(request.getEmail());
		return ResponseEntity.ok(Map.of("message", "重設密碼郵件已發送"));
	}

	// 重設密碼
	@Operation(summary = "重設密碼", description = "用戶提供重設密碼的 Token 和新密碼，進行密碼重設。")
	@PostMapping("/auth/reset-password")
	public ResponseEntity<Map<String, String>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
		return authService.resetPassword(request.getToken(), request.getNewPassword(), request.getConfirmPassword());
	}

}
