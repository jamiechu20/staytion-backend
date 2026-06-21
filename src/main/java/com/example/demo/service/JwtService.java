package com.example.demo.service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Hotel;
import com.example.demo.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    private final long EXP_TIME = 120 * 60 * 1000; // 120 分鐘

    public String createToken(User user) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());


	    List<String> hotelNames = user.getHotels().stream()
	        .map(Hotel::getHname)
	        .collect(Collectors.toList());

        return Jwts.builder().setSubject(user.getEmail()).claim("id", user.getId()).claim("hotels", hotelNames).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXP_TIME)).signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 驗證連結 - token
    public String getEmailFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.getSubject(); // email
        } catch (Exception e) {
            throw new RuntimeException("Token 無效或已過期");
        }
    }

    public String parseToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            e.printStackTrace();
            throw new RuntimeException("Token 解析失敗");
        }
    }

    // 從 token 解析使用者 ID
    public Long getUserIdFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            System.out.println("JWT Payload Claims: " + claims); //debug
            return claims.get("id", Long.class);
        } catch (Exception e) {
            System.out.println("無法解析使用者 ID，錯誤訊息：" + e.getMessage());
            throw new RuntimeException("無法解析使用者 ID");
        }
    }
    
    // 從授權標頭中擷取使用者 ID
    public Long extractUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("缺少或格式錯誤的 Authorization header");
        }
        String token = authHeader.replace("Bearer ", "");
        return getUserIdFromToken(token);
    }

}