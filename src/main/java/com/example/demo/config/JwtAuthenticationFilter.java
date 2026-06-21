//package com.example.demo.config;
//
//import com.example.demo.repository.UserRepository;
//import com.example.demo.service.JwtService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import com.example.demo.entity.User;
//
//import java.io.IOException;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtService jwtService;
//    private UserRepository userRepository;
//
//    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
//        this.jwtService = jwtService;
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String path = request.getRequestURI();
//        System.out.println("JwtFilter intercept path: " + path);  // debug log
//
//        if (path.startsWith("/api/auth/register")
//                || path.startsWith("/api/auth/login-password")
//                || path.startsWith("/api/auth/google-login")
//                || path.startsWith("/api/auth/check-email")
//                || path.startsWith("/api/auth/forgot-password")
//                || path.startsWith("/api/auth/reset-password")
//                || path.startsWith("/v3/api-docs")
//                || path.startsWith("/swagger-ui")
//                || path.startsWith("/swagger-ui.html")
//                || path.startsWith("/ws")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String token = authHeader.substring(7).trim(); // Remove "Bearer " prefix
//        System.out.println("取得的 token: [" + token + "]");
//        try {
//            Long userId = jwtService.getUserIdFromToken(token);
//            System.out.println("token 解出來的 userId: " + userId);
//            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                User user = userRepository.findById(userId).orElse(null);
//                if (user != null) {
//                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                            user, null, null
//                    );
//                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    SecurityContextHolder.getContext().setAuthentication(authToken);
//                    request.setAttribute("userId", userId);
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("JWT 驗證失敗：" + e.getMessage());
//        }
//        filterChain.doFilter(request, response);
//    }
//}
