package org.mehlib.marked.web;

import org.mehlib.marked.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated (not anonymous)
        if (
            authentication != null &&
            authentication.isAuthenticated() &&
            authentication.getPrincipal() instanceof CustomUserDetails
        ) {
            CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            // Redirect based on role
            return switch (userDetails.getRole()) {
                case ROLE_STUDENT -> "redirect:/students/" + userId;
                case ROLE_PROFESSOR -> "redirect:/professors/" + userId;
                case ROLE_INSTITUTION_ADMIN -> "redirect:/institution-admin/" +
                userId;
                case ROLE_ADMIN -> "redirect:/admin";
            };
        }

        // Show landing page for unauthenticated users
        return "index";
    }
}
