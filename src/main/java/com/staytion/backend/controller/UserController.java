package com.staytion.backend.controller;

import java.util.Map;

import com.staytion.backend.dto.response.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.staytion.backend.dto.UserUpdateRequest;
import com.staytion.backend.entity.User;
import com.staytion.backend.service.JwtService;
import com.staytion.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

	private final JwtService jwtService;
	private final UserService userService;

	// 使用token取得個人資料
	@Operation(summary = "取得使用者個人資料", description = "通過 Token 獲取使用者個人資料")
	@GetMapping("/user/profile")
	public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
		}
		String token = authHeader.substring(7);
		String email = jwtService.parseToken(token);
		User user = userService.getUserByEmail(email);
		UserResponse userResponse = UserResponse.from(user);
		return ResponseEntity.ok(Map.of("user", userResponse));
	}

	// 更新使用者個人資料
	@Operation(summary = "更新使用者個人資料", description = "使用者可以更新自己的個人資料，如名稱、密碼、電話等")
	@PutMapping("/user/profile")
	public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token,
			@RequestBody UserUpdateRequest request) {
		String email = jwtService.parseToken(token.substring(7)); // 解析 token 獲取 email
		User updatedUser = userService.updateUserProfile(email, request.getFirstName(), request.getLastName(),
				request.getPassword(), request.getTel());
		UserResponse userResponse = UserResponse.from(updatedUser);
		return ResponseEntity.ok(Map.of("user", userResponse));
	}

	// 刪除使用者
	@Operation(summary = "刪除使用者帳戶", description = "通過 Token 刪除使用者帳戶，刪除後無法恢復")
	@DeleteMapping("/user/account")
	public ResponseEntity<Map<String, String>> deleteAccount(@RequestHeader("Authorization") String token) {
		String email = jwtService.parseToken(token.substring(7));
		String message = userService.deleteUser(email);
		return ResponseEntity.ok(Map.of("message", message));
	}
}
