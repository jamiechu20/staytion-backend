package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http    .csrf(AbstractHttpConfigurer::disable)   // 關閉 CSRF 防護，因為 API 可能會在前端直接調用
                .cors(Customizer.withDefaults())                // 啟用 CORS（配合 WebMvcConfigurer）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::handleUnauthorized)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/bookings/**",
                                "/api/hotels/**",
                                "/api/top-hotels",
                                "/api/city/{id}/hotel-count",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/ws/**").permitAll() // 允許無需驗證的路徑
                        .anyRequest().authenticated()  // 其他所有請求需進行驗證
                );

        return http.build();
    }

    private void handleUnauthorized(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    AuthenticationException e) throws IOException {
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.getWriter().write("Unauthorized: Invalid or missing token");
    }
    // WebMvcConfigurer 設定允許跨網域 + credentials
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500", "http://127.0.0.1:5501")  // 允許前端的域名（根據實際需要修改）
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允許的 HTTP 方法
                        .allowedHeaders("*")
                        .allowCredentials(true);  // 允許跨域請求攜帶憑證（如 cookies）
            }
        };
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 用於處理密碼的加密
    }


}


