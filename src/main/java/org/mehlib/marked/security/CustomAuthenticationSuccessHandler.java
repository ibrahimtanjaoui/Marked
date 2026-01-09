package org.mehlib.marked.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * Custom authentication success handler that redirects users to their
 * appropriate dashboard based on their role after successful login.
 */
public class CustomAuthenticationSuccessHandler
    implements AuthenticationSuccessHandler
{

    private final CustomUserDetailsService userDetailsService;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomAuthenticationSuccessHandler(
        CustomUserDetailsService userDetailsService
    ) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        // Store user info in session for easy access
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        HttpSession session = request.getSession();
        session.setAttribute("userId", userDetails.getId());
        session.setAttribute("userEmail", userDetails.getEmail());
        session.setAttribute("userFullName", userDetails.getFullName());
        session.setAttribute("userRole", userDetails.getRole().getSimpleName());

        // Check if there was a saved request (e.g., user tried to access a protected page)
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            // Only redirect to saved request if it's a valid target
            // Exclude login pages, error pages, and other invalid targets
            if (isValidRedirectTarget(targetUrl)) {
                requestCache.removeRequest(request, response);
                response.sendRedirect(targetUrl);
                return;
            }
            // Clear invalid saved request
            requestCache.removeRequest(request, response);
        }

        // Redirect based on role
        String redirectUrl = determineTargetUrl(userDetails);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Checks if the given URL is a valid redirect target after login.
     * Excludes login pages, logout, error pages, and static resources.
     *
     * @param targetUrl the URL to validate
     * @return true if the URL is a valid redirect target
     */
    private boolean isValidRedirectTarget(String targetUrl) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return false;
        }

        // Exclude login-related pages (both our custom /auth/login and Spring's default /login)
        if (targetUrl.contains("/auth/") || targetUrl.contains("/login")) {
            return false;
        }

        // Exclude logout
        if (targetUrl.contains("/logout")) {
            return false;
        }

        // Exclude error pages
        if (targetUrl.contains("/error")) {
            return false;
        }

        // Exclude static resources
        if (
            targetUrl.contains("/css/") ||
            targetUrl.contains("/js/") ||
            targetUrl.contains("/img/") ||
            targetUrl.contains("/fonts/") ||
            targetUrl.contains("/favicon.ico")
        ) {
            return false;
        }

        // Exclude actuator endpoints
        if (targetUrl.contains("/actuator")) {
            return false;
        }

        // Exclude H2 console
        if (targetUrl.contains("/h2-console")) {
            return false;
        }

        return true;
    }

    /**
     * Determines the redirect URL based on the user's role.
     *
     * @param userDetails the authenticated user details
     * @return the URL to redirect to
     */
    private String determineTargetUrl(CustomUserDetails userDetails) {
        UserRole role = userDetails.getRole();
        Long userId = userDetails.getId();

        return switch (role) {
            case ROLE_STUDENT -> "/students/" + userId;
            case ROLE_PROFESSOR -> "/professors/" + userId;
            case ROLE_INSTITUTION_ADMIN -> "/institution-admin/" + userId;
            case ROLE_ADMIN -> "/admin";
        };
    }
}
