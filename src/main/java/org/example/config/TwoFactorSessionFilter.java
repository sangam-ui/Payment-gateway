package org.example.config;

import org.example.service.OtpAuthService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TwoFactorSessionFilter extends OncePerRequestFilter {

    private static final String SESSION_HEADER = "X-Session-Token";

    private final OtpAuthService otpAuthService;

    public TwoFactorSessionFilter(OtpAuthService otpAuthService) {
        this.otpAuthService = otpAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/") || path.startsWith("/api/v1/auth/") || path.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(SESSION_HEADER);
        if (!otpAuthService.isValidSession(authentication.getName(), token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"2FA required\",\"data\":{\"code\":\"OTP_REQUIRED\"}}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

