package com.staytion.backend.service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.staytion.backend.exception.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.staytion.backend.entity.Hotel;
import com.staytion.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long EXP_TIME = 2; // 120 分鐘
    private static final String EMAIL_VERIFICATION_PURPOSE = "email-verification";
    private static final long EMAIL_VERIFY_EXP_HOURS = 24;
    public String createToken(User user) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

	    List<String> hotelNames = user.getHotels().stream()
	        .map(Hotel::getHname)
	        .collect(Collectors.toList());

        return Jwts.builder().setSubject(user.getEmail()).claim("id", user.getId()).claim("hotels", hotelNames).setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(EXP_TIME, ChronoUnit.HOURS))).signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public String createEmailVerificationToken(User user) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder().setSubject(user.getEmail()).claim("purpose", EMAIL_VERIFICATION_PURPOSE)
                                .setIssuedAt(new Date()).setExpiration(Date.from(Instant.now().plus(EMAIL_VERIFY_EXP_HOURS, ChronoUnit.HOURS)))
                                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    // 驗證連結 - token
    public String getEmailFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            throw new InvalidTokenException("Token 無效或已過期", e);
        }
    }

    public String parseToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            log.error("Token 解析失敗", e);
            throw new RuntimeException("Token 解析失敗");
        }
    }

    // 從 token 解析使用者 ID
    public Long getUserIdFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            log.debug("JWT Payload Claims: {}", claims);
            return claims.get("id", Long.class);
        } catch (Exception e) {
            log.debug("無法解析使用者 ID，錯誤訊息：{}", e.getMessage());
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