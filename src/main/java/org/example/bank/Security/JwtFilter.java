package org.example.bank.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String path = req.getServletPath();
        log.debug("Processing request for path: {}", path);
        
        // Allow all requests to authentication endpoints and Swagger
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/h2-console/") || 
            path.startsWith("/swagger-ui/") || 
            path.startsWith("/v3/api-docs/")) {
            log.debug("Allowing request to public endpoint: {}", path);
            chain.doFilter(req, res);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        log.debug("Authorization header: {}", authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Processing JWT token");
            String username = jwtUtil.extractUsername(token);
            log.debug("Extracted username from token: {}", username);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.debug("Loaded user details. Authorities: {}", userDetails.getAuthorities());
                
                if (jwtUtil.validate(token)) {
                    log.debug("Token validated successfully for user: {}", username);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Authentication set in SecurityContext. Authorities: {}", auth.getAuthorities());
                } else {
                    log.warn("Token validation failed for user: {}", username);
                }
            }
        } else {
            log.warn("No valid Authorization header found");
        }
        chain.doFilter(req, res);
    }
}
