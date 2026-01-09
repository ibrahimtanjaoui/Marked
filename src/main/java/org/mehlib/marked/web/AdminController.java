package org.mehlib.marked.web;

import jakarta.validation.Valid;
import java.util.List;
import org.mehlib.marked.dao.entities.Institution;
import org.mehlib.marked.dao.entities.InstitutionAdmin;
import org.mehlib.marked.dto.InstitutionAdminRequest;
import org.mehlib.marked.dto.InstitutionRequest;
import org.mehlib.marked.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ==================== Dashboard ====================

    /**
     * Admin dashboard.
     * GET /admin/{adminId}
     */
    @GetMapping("/{adminId}")
    public String dashboard(@PathVariable Long adminId, Model model) {
        try {
            var admin = adminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Admin not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute(
                "institutionCount",
                adminService.countInstitutions()
            );
            model.addAttribute(
                "institutionAdminCount",
                adminService.countInstitutionAdmins()
            );
            model.addAttribute(
                "professorCount",
                adminService.countTotalProfessors()
            );
            model.addAttribute(
                "studentCount",
                adminService.countTotalStudents()
            );

            return "admin/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    // ==================== Institution Management ====================

    /**
     * List all institutions.
     * GET /admin/{adminId}/institutions
     */
    @GetMapping("/{adminId}/institutions")
    public String listInstitutions(@PathVariable Long adminId, Model model) {
        try {
            var admin = adminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Admin not found")
                );

            List<Institution> institutions = adminService.getAllInstitutions();

            model.addAttribute("admin", admin);
            model.addAttribute("institutions", institutions);

            return "admin/institutions/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create institution form.
     * GET /admin/{adminId}/institutions/new
     */
    @GetMapping("/{adminId}/institutions/new")
    public String showCreateInstitutionForm(
        @PathVariable Long adminId,
        Model model
    ) {
        try {
            var admin = adminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Admin not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute(
                "institutionRequest",
                new InstitutionRequest(null, null, null, null)
            );

            return "admin/institutions/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new institution.
     * POST /admin/{adminId}/institutions
     */
    @PostMapping("/{adminId}/institutions")
    public String createInstitution(
        @PathVariable Long adminId,
        @Valid @ModelAttribute InstitutionRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = adminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "admin/institutions/form";
        }

        try {
            adminService.createInstitution(
                request.name(),
                request.description(),
                request.foundedAt(),
                request.address()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Institution created successfully!"
            );
            return "redirect:/admin/" + adminId + "/institutions";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = adminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "admin/institutions/form";
        }
    }

    /**
     * View institution details.
     * GET /admin/{adminId}/institutions/{institutionId}
     */
    @GetMapping("/{adminId}/institutions/{institutionId}")
    public String viewInstitution(
        @PathVariable Long adminId,
        @PathVariable Long institutionId,
        Model model
    ) {
        try {
            var admin = adminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Admin not found")
                );

            var institution = adminService
                .getInstitution(institutionId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution not found")
                );

            List<InstitutionAdmin> institutionAdmins =
                adminService.getInstitutionAdminsByInstitution(institutionId);

            model.addAttribute("admin", admin);
            model.addAttribute("institution", institution);
            model.addAttribute("institutionAdmins", institutionAdmins);

            return "admin/institutions/detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show edit institution form.
     * GET /admin/{adminId}/institutions/{institutionId}/edit
     */
    @GetMapping("/{adminId}/institutions/{institutionId}/edit")
    public String showEditInstitutionForm(
        @PathVariable Long adminId,
        @PathVariable Long institutionId,
        Model model
    ) {
        try {
            var admin = adminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Admin not found")
                );

            var institution = adminService
                .getInstitution(institutionId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute("institution", institution);
            model.addAttribute(
                "institutionRequest",
                new InstitutionRequest(
                    institution.getName(),
                    institution.getDescription(),
                    institution.getFoundedAt(),
                    institution.getAddress()
                )
            );

            return "admin/institutions/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update an institution.
     * POST /admin/{adminId}/institutions/{institutionId}
     */
    @PostMapping("/{adminId}/institutions/{institutionId}")
    public String updateInstitution(
        @PathVariable Long adminId,
        @PathVariable Long institutionId,
        @Valid @ModelAttribute InstitutionRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = adminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "admin/institutions/form";
        }

        try {
            adminService.updateInstitution(
                institutionId,
                request.name(),
                request.description(),
                request.foundedAt(),
                request.address()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Institution updated successfully!"
            );
            return "redirect:/admin/" + adminId + "/institutions";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = adminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "admin/institutions/form";
        }
    }

    /**
     * Delete an institution.
     * POST /admin/{adminId}/institutions/{institutionId}/delete
     */
    @PostMapping("/{adminId}/institutions/{institutionId}/delete")
    public String deleteInstitution(
        @PathVariable Long adminId,
        @PathVariable Long institutionId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            adminService.deleteInstitution(institutionId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Institution deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/" + adminId + "/institutions";
    }

    // ==================== Institution Admin Management ====================

    /**
     * List all institution admins.
     * GET /admin/{adminId}/institution-admins
     */
    @GetMapping("/{adminId}/institution-admins")
    public String listInstitutionAdmins(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long institutionId,
        Model model
    ) {
        try {
            var admin = adminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Admin not found")
                );

            List<InstitutionAdmin> institutionAdmins;
            if (institutionId != null) {
                institutionAdmins =
                    adminService.getInstitutionAdminsByInstitution(
                        institutionId
                    );
            } else {
                institutionAdmins = adminService.getAllInstitutionAdmins();
            }

            List<Institution> institutions = adminService.getAllInstitutions();

            model.addAttribute("admin", admin);
            model.addAttribute("institutionAdmins", institutionAdmins);
            model.addAttribute("institutions", institutions);
            model.addAttribute("selectedInstitutionId", institutionId);

            return "admin/institution-admins/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create institution admin form.
     * GET /admin/{adminId}/institution-admins/new
     */
    @GetMapping("/{adminId}/institution-admins/new")
    public String showCreateInstitutionAdminForm(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long institutionId,
        Model model
    ) {
        try {
            var admin = adminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Admin not found")
                );

            List<Institution> institutions = adminService.getAllInstitutions();

            model.addAttribute("admin", admin);
            model.addAttribute("institutions", institutions);
            model.addAttribute(
                "institutionAdminRequest",
                new InstitutionAdminRequest(
                    null,
                    null,
                    null,
                    null,
                    institutionId
                )
            );

            return "admin/institution-admins/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new institution admin.
     * POST /admin/{adminId}/institution-admins
     */
    @PostMapping("/{adminId}/institution-admins")
    public String createInstitutionAdmin(
        @PathVariable Long adminId,
        @Valid @ModelAttribute InstitutionAdminRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = adminService.getUser(adminId).orElse(null);
            List<Institution> institutions = adminService.getAllInstitutions();
            model.addAttribute("admin", admin);
            model.addAttribute("institutions", institutions);
            return "admin/institution-admins/form";
        }

        try {
            adminService.createInstitutionAdmin(
                request.institutionId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Institution Admin created successfully!"
            );
            return "redirect:/admin/" + adminId + "/institution-admins";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = adminService.getUser(adminId).orElse(null);
            List<Institution> institutions = adminService.getAllInstitutions();
            model.addAttribute("admin", admin);
            model.addAttribute("institutions", institutions);
            return "admin/institution-admins/form";
        }
    }

    /**
     * Show edit institution admin form.
     * GET /admin/{adminId}/institution-admins/{institutionAdminId}/edit
     */
    @GetMapping("/{adminId}/institution-admins/{institutionAdminId}/edit")
    public String showEditInstitutionAdminForm(
        @PathVariable Long adminId,
        @PathVariable Long institutionAdminId,
        Model model
    ) {
        try {
            var admin = adminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Admin not found")
                );

            List<InstitutionAdmin> allAdmins =
                adminService.getAllInstitutionAdmins();
            var institutionAdmin = allAdmins
                .stream()
                .filter(ia -> ia.getId().equals(institutionAdminId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Institution> institutions = adminService.getAllInstitutions();

            model.addAttribute("admin", admin);
            model.addAttribute("institutionAdmin", institutionAdmin);
            model.addAttribute("institutions", institutions);
            model.addAttribute(
                "institutionAdminRequest",
                new InstitutionAdminRequest(
                    institutionAdmin.getFirstName(),
                    institutionAdmin.getFamilyName(),
                    institutionAdmin.getEmail(),
                    null,
                    institutionAdmin.getInstitution().getId()
                )
            );

            return "admin/institution-admins/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update an institution admin.
     * POST /admin/{adminId}/institution-admins/{institutionAdminId}
     */
    @PostMapping("/{adminId}/institution-admins/{institutionAdminId}")
    public String updateInstitutionAdmin(
        @PathVariable Long adminId,
        @PathVariable Long institutionAdminId,
        @Valid @ModelAttribute InstitutionAdminRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = adminService.getUser(adminId).orElse(null);
            List<Institution> institutions = adminService.getAllInstitutions();
            model.addAttribute("admin", admin);
            model.addAttribute("institutions", institutions);
            return "admin/institution-admins/form";
        }

        try {
            adminService.updateInstitutionAdmin(
                institutionAdminId,
                request.firstName(),
                request.lastName(),
                request.email(),
                request.institutionId()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Institution Admin updated successfully!"
            );
            return "redirect:/admin/" + adminId + "/institution-admins";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = adminService.getUser(adminId).orElse(null);
            List<Institution> institutions = adminService.getAllInstitutions();
            model.addAttribute("admin", admin);
            model.addAttribute("institutions", institutions);
            return "admin/institution-admins/form";
        }
    }

    /**
     * Delete an institution admin.
     * POST /admin/{adminId}/institution-admins/{institutionAdminId}/delete
     */
    @PostMapping("/{adminId}/institution-admins/{institutionAdminId}/delete")
    public String deleteInstitutionAdmin(
        @PathVariable Long adminId,
        @PathVariable Long institutionAdminId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            adminService.deleteInstitutionAdmin(institutionAdminId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Institution Admin deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/" + adminId + "/institution-admins";
    }
}
