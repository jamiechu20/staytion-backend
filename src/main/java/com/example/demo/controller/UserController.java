package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
import com.example.demo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://127.0.0.1:5500")  
public class UserController {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	// 使用token取得個人資料
	@Operation(summary = "取得使用者個人資料", description = "通過 Token 獲取使用者個人資料")
	@GetMapping("/user/profile")
	public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
		System.out.println("Authorization header: " + authHeader);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
		}

		String token = authHeader.substring(7);
		System.out.println("Extracted token: " + token);

		String email = jwtService.parseToken(token);
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("找不到使用者"));

		return ResponseEntity.ok(user);
	}

	// 更新使用者個人資料
	@Operation(summary = "更新使用者個人資料", description = "使用者可以更新自己的個人資料，如名稱、密碼、電話等")
	@PutMapping("/user/update-profile")
	public ResponseEntity<User> updateProfile(@RequestHeader("Authorization") String token,
			@RequestBody UserUpdateRequest request) {
		String email = jwtService.parseToken(token.substring(7)); // 解析 token 獲取 email
		User updatedUser = userService.updateUserProfile(email, request.getFirstName(), request.getLastName(),
				request.getPassword(), request.getTel());
		return ResponseEntity.ok(updatedUser);
	}

	// 刪除使用者
	@Operation(summary = "刪除使用者帳戶", description = "通過 Token 刪除使用者帳戶，刪除後無法恢復")
	@DeleteMapping("/user/delete-account")
	public ResponseEntity<Map<String, String>> deleteAccount(@RequestHeader("Authorization") String token) {
		String email = jwtService.parseToken(token.substring(7));
		String message = userService.deleteUser(email);
		return ResponseEntity.ok(Map.of("message", message));
	}
}
