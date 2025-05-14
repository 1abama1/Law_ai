package org.example.bank.Security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String username, String role) {
        log.debug("Generating token for user: {} with role: {}", username, role);
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        log.debug("Generated token: {}", token);
        return token;
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 дней
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String extractUsername(String token) {
        String username = Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token).getBody().getSubject();
        log.debug("Extracted username from token: {}", username);
        return username;
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            log.debug("Token validation successful");
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractRole(String token) {
        String role = (String) Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token).getBody().get("role");
        log.debug("Extracted role from token: {}", role);
        return role;
    }
}