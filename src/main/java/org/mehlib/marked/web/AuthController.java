package org.mehlib.marked.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.mehlib.marked.dao.entities.Professor;
import org.mehlib.marked.dao.entities.Student;
import org.mehlib.marked.security.CustomUserDetails;
import org.mehlib.marked.security.CustomUserDetailsService;
import org.mehlib.marked.security.ProfessorRegistrationRequest;
import org.mehlib.marked.security.RegistrationException;
import org.mehlib.marked.security.RegistrationService;
import org.mehlib.marked.security.StudentRegistrationRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling authentication: login, signup, and logout.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
        SecurityContextHolder.getContextHolderStrategy();

    public AuthController(
        RegistrationService registrationService,
        AuthenticationManager authenticationManager,
        CustomUserDetailsService userDetailsService
    ) {
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Display login page.
     * Redirects authenticated users to their dashboard.
     */
    @GetMapping("/login")
    public String loginPage(
        @RequestParam(value = "error", required = false) String error,
        @RequestParam(value = "logout", required = false) String logout,
        @RequestParam(value = "expired", required = false) String expired,
        @RequestParam(value = "registered", required = false) String registered,
        Model model
    ) {
        // Redirect authenticated users to their dashboard
        String redirectUrl = getAuthenticatedUserRedirect();
        if (redirectUrl != null) {
            return redirectUrl;
        }

        if (error != null) {
            model.addAttribute(
                "errorMessage",
                "Invalid email or password. Please try again."
            );
        }
        if (logout != null) {
            model.addAttribute(
                "successMessage",
                "You have been logged out successfully."
            );
        }
        if (expired != null) {
            model.addAttribute(
                "errorMessage",
                "Your session has expired. Please log in again."
            );
        }
        if (registered != null) {
            model.addAttribute(
                "successMessage",
                "Registration successful! Please log in with your credentials."
            );
        }

        return "auth/login";
    }

    /**
     * Display signup page.
     * Redirects authenticated users to their dashboard.
     */
    @GetMapping("/signup")
    public String signupPage(Model model) {
        // Redirect authenticated users to their dashboard
        String redirectUrl = getAuthenticatedUserRedirect();
        if (redirectUrl != null) {
            return redirectUrl;
        }

        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "auth/signup";
    }

    /**
     * Helper method to get redirect URL for authenticated users.
     * Returns null if user is not authenticated.
     */
    private String getAuthenticatedUserRedirect() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (
            authentication != null &&
            authentication.isAuthenticated() &&
            authentication.getPrincipal() instanceof CustomUserDetails
        ) {
            CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            return switch (userDetails.getRole()) {
                case ROLE_STUDENT -> "redirect:/students/" + userId;
                case ROLE_PROFESSOR -> "redirect:/professors/" + userId;
                case ROLE_INSTITUTION_ADMIN -> "redirect:/institution-admin/" +
                userId;
                case ROLE_ADMIN -> "redirect:/admin";
            };
        }
        return null;
    }

    /**
     * Handle signup form submission.
     */
    @PostMapping("/signup")
    public String processSignup(
        @Valid @ModelAttribute("signupForm") SignupForm form,
        BindingResult bindingResult,
        HttpServletRequest request,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        // Check password confirmation
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "auth/signup";
        }

        try {
            // Register based on role
            if ("student".equalsIgnoreCase(form.getRole())) {
                if (
                    form.getStudentId() == null || form.getStudentId().isBlank()
                ) {
                    model.addAttribute(
                        "errorMessage",
                        "Student ID is required for student registration."
                    );
                    return "auth/signup";
                }

                StudentRegistrationRequest studentRequest =
                    new StudentRegistrationRequest(
                        form.getEmail(),
                        form.getPassword(),
                        form.getFirstName(),
                        form.getFamilyName(),
                        form.getStudentId()
                    );
                Student student = registrationService.registerStudent(
                    studentRequest
                );

                // Auto-login after registration
                autoLogin(
                    form.getEmail(),
                    form.getPassword(),
                    request,
                    response
                );
                return "redirect:/students/" + student.getId();
            } else if ("professor".equalsIgnoreCase(form.getRole())) {
                ProfessorRegistrationRequest professorRequest =
                    new ProfessorRegistrationRequest(
                        form.getEmail(),
                        form.getPassword(),
                        form.getFirstName(),
                        form.getFamilyName()
                    );
                Professor professor = registrationService.registerProfessor(
                    professorRequest
                );

                // Auto-login after registration
                autoLogin(
                    form.getEmail(),
                    form.getPassword(),
                    request,
                    response
                );
                return "redirect:/professors/" + professor.getId();
            } else {
                model.addAttribute(
                    "errorMessage",
                    "Please select a valid role."
                );
                return "auth/signup";
            }
        } catch (RegistrationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/signup";
        } catch (AuthenticationException e) {
            // Registration succeeded but auto-login failed, redirect to login page
            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Registration successful! Please log in with your credentials."
            );
            return "redirect:/auth/login?registered=true";
        }
    }

    /**
     * Programmatically authenticate a user after registration.
     */
    private void autoLogin(
        String email,
        String password,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        UsernamePasswordAuthenticationToken token =
            new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(
            token
        );

        SecurityContext context =
            securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        // Store user info in session
        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();
        request.getSession().setAttribute("userId", userDetails.getId());
        request.getSession().setAttribute("userEmail", userDetails.getEmail());
        request
            .getSession()
            .setAttribute("userFullName", userDetails.getFullName());
        request
            .getSession()
            .setAttribute("userRole", userDetails.getRole().getSimpleName());
    }

    /**
     * Form object for signup validation.
     */
    public static class SignupForm {

        @NotBlank(message = "First name is required")
        @Size(
            min = 2,
            max = 50,
            message = "First name must be between 2 and 50 characters"
        )
        private String firstName;

        @NotBlank(message = "Family name is required")
        @Size(
            min = 2,
            max = 50,
            message = "Family name must be between 2 and 50 characters"
        )
        private String familyName;

        @NotBlank(message = "Email is required")
        @Email(message = "Please enter a valid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(
            min = 8,
            max = 100,
            message = "Password must be between 8 and 100 characters"
        )
        private String password;

        @NotBlank(message = "Please confirm your password")
        private String confirmPassword;

        @NotBlank(message = "Please select a role")
        private String role;

        // Student-specific field (optional based on role)
        private String studentId;

        // Getters and setters
        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }
    }
}
