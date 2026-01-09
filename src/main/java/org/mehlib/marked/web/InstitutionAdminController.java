package org.mehlib.marked.web;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.mehlib.marked.dao.entities.*;
import org.mehlib.marked.dao.entities.Class;
import org.mehlib.marked.dto.*;
import org.mehlib.marked.service.InstitutionAdminService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/institution-admin")
public class InstitutionAdminController {

    private final InstitutionAdminService institutionAdminService;

    public InstitutionAdminController(
        InstitutionAdminService institutionAdminService
    ) {
        this.institutionAdminService = institutionAdminService;
    }

    // ==================== Dashboard ====================

    /**
     * Institution Admin dashboard.
     * GET /institution-admin/{adminId}
     */
    @GetMapping("/{adminId}")
    public String dashboard(@PathVariable Long adminId, Model model) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            Long institutionId = admin.getInstitution().getId();

            model.addAttribute("admin", admin);
            model.addAttribute("institution", admin.getInstitution());
            model.addAttribute(
                "departmentCount",
                institutionAdminService.countDepartments(institutionId)
            );
            model.addAttribute(
                "professorCount",
                institutionAdminService.countProfessors(institutionId)
            );
            model.addAttribute(
                "studentCount",
                institutionAdminService.countStudents(institutionId)
            );
            model.addAttribute(
                "classCount",
                institutionAdminService.countClasses(institutionId)
            );
            model.addAttribute(
                "sectionCount",
                institutionAdminService.countSections(institutionId)
            );

            return "institution-admin/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    // ==================== Department Management ====================

    /**
     * List all departments for the institution.
     * GET /institution-admin/{adminId}/departments
     */
    @GetMapping("/{adminId}/departments")
    public String listDepartments(@PathVariable Long adminId, Model model) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);

            return "institution-admin/departments/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create department form.
     * GET /institution-admin/{adminId}/departments/new
     */
    @GetMapping("/{adminId}/departments/new")
    public String showCreateDepartmentForm(
        @PathVariable Long adminId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute(
                "departmentRequest",
                new DepartmentRequest(
                    null,
                    null,
                    admin.getInstitution().getId()
                )
            );

            return "institution-admin/departments/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new department.
     * POST /institution-admin/{adminId}/departments
     */
    @PostMapping("/{adminId}/departments")
    public String createDepartment(
        @PathVariable Long adminId,
        @Valid @ModelAttribute DepartmentRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "institution-admin/departments/form";
        }

        try {
            institutionAdminService.createDepartment(
                request.institutionId(),
                request.name(),
                request.description()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Department created successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/departments";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "institution-admin/departments/form";
        }
    }

    /**
     * Show edit department form.
     * GET /institution-admin/{adminId}/departments/{departmentId}/edit
     */
    @GetMapping("/{adminId}/departments/{departmentId}/edit")
    public String showEditDepartmentForm(
        @PathVariable Long adminId,
        @PathVariable Long departmentId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );
            var department = departments
                .stream()
                .filter(d -> d.getId().equals(departmentId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Department not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute("department", department);
            model.addAttribute(
                "departmentRequest",
                new DepartmentRequest(
                    department.getName(),
                    department.getDescription(),
                    admin.getInstitution().getId()
                )
            );

            return "institution-admin/departments/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update a department.
     * POST /institution-admin/{adminId}/departments/{departmentId}
     */
    @PostMapping("/{adminId}/departments/{departmentId}")
    public String updateDepartment(
        @PathVariable Long adminId,
        @PathVariable Long departmentId,
        @Valid @ModelAttribute DepartmentRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "institution-admin/departments/form";
        }

        try {
            institutionAdminService.updateDepartment(
                departmentId,
                request.name(),
                request.description()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Department updated successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/departments";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "institution-admin/departments/form";
        }
    }

    /**
     * Delete a department.
     * POST /institution-admin/{adminId}/departments/{departmentId}/delete
     */
    @PostMapping("/{adminId}/departments/{departmentId}/delete")
    public String deleteDepartment(
        @PathVariable Long adminId,
        @PathVariable Long departmentId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteDepartment(departmentId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Department deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/institution-admin/" + adminId + "/departments";
    }

    // ==================== Major Management ====================

    /**
     * List all majors for the institution.
     * GET /institution-admin/{adminId}/majors
     */
    @GetMapping("/{adminId}/majors")
    public String listMajors(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long departmentId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Major> majors;
            if (departmentId != null) {
                majors = institutionAdminService.getMajorsByDepartment(
                    departmentId
                );
            } else {
                majors = institutionAdminService.getMajorsByInstitution(
                    admin.getInstitution().getId()
                );
            }

            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("majors", majors);
            model.addAttribute("departments", departments);
            model.addAttribute("selectedDepartmentId", departmentId);

            return "institution-admin/majors/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create major form.
     * GET /institution-admin/{adminId}/majors/new
     */
    @GetMapping("/{adminId}/majors/new")
    public String showCreateMajorForm(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long departmentId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            model.addAttribute(
                "majorRequest",
                new MajorRequest(null, null, departmentId)
            );

            return "institution-admin/majors/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new major.
     * POST /institution-admin/{adminId}/majors
     */
    @PostMapping("/{adminId}/majors")
    public String createMajor(
        @PathVariable Long adminId,
        @Valid @ModelAttribute MajorRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            return "institution-admin/majors/form";
        }

        try {
            institutionAdminService.createMajor(
                request.departmentId(),
                request.name(),
                request.description()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Major created successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/majors";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            return "institution-admin/majors/form";
        }
    }

    /**
     * Show edit major form.
     * GET /institution-admin/{adminId}/majors/{majorId}/edit
     */
    @GetMapping("/{adminId}/majors/{majorId}/edit")
    public String showEditMajorForm(
        @PathVariable Long adminId,
        @PathVariable Long majorId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Major> majors = institutionAdminService.getMajorsByInstitution(
                admin.getInstitution().getId()
            );
            var major = majors
                .stream()
                .filter(m -> m.getId().equals(majorId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Major not found")
                );

            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("major", major);
            model.addAttribute("departments", departments);
            model.addAttribute(
                "majorRequest",
                new MajorRequest(
                    major.getName(),
                    major.getDescription(),
                    major.getDepartment().getId()
                )
            );

            return "institution-admin/majors/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update a major.
     * POST /institution-admin/{adminId}/majors/{majorId}
     */
    @PostMapping("/{adminId}/majors/{majorId}")
    public String updateMajor(
        @PathVariable Long adminId,
        @PathVariable Long majorId,
        @Valid @ModelAttribute MajorRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            return "institution-admin/majors/form";
        }

        try {
            institutionAdminService.updateMajor(
                majorId,
                request.name(),
                request.description()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Major updated successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/majors";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            return "institution-admin/majors/form";
        }
    }

    /**
     * Delete a major.
     * POST /institution-admin/{adminId}/majors/{majorId}/delete
     */
    @PostMapping("/{adminId}/majors/{majorId}/delete")
    public String deleteMajor(
        @PathVariable Long adminId,
        @PathVariable Long majorId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteMajor(majorId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Major deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/institution-admin/" + adminId + "/majors";
    }

    // ==================== Class Management ====================

    /**
     * List all classes for the institution.
     * GET /institution-admin/{adminId}/classes
     */
    @GetMapping("/{adminId}/classes")
    public String listClasses(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long majorId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Class> classes;
            if (majorId != null) {
                classes = institutionAdminService.getClassesByMajor(majorId);
            } else {
                classes = institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );
            }

            List<Major> majors = institutionAdminService.getMajorsByInstitution(
                admin.getInstitution().getId()
            );

            model.addAttribute("admin", admin);
            model.addAttribute("classes", classes);
            model.addAttribute("majors", majors);
            model.addAttribute("selectedMajorId", majorId);

            return "institution-admin/classes/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create class form.
     * GET /institution-admin/{adminId}/classes/new
     */
    @GetMapping("/{adminId}/classes/new")
    public String showCreateClassForm(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long majorId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Major> majors = institutionAdminService.getMajorsByInstitution(
                admin.getInstitution().getId()
            );

            model.addAttribute("admin", admin);
            model.addAttribute("majors", majors);
            model.addAttribute(
                "classRequest",
                new ClassRequest(null, null, null, null, majorId)
            );

            return "institution-admin/classes/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new class.
     * POST /institution-admin/{adminId}/classes
     */
    @PostMapping("/{adminId}/classes")
    public String createClass(
        @PathVariable Long adminId,
        @Valid @ModelAttribute ClassRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Major> majors = institutionAdminService.getMajorsByInstitution(
                admin.getInstitution().getId()
            );
            model.addAttribute("admin", admin);
            model.addAttribute("majors", majors);
            return "institution-admin/classes/form";
        }

        try {
            institutionAdminService.createClass(
                request.majorId(),
                request.name(),
                request.description(),
                request.academicYearStart(),
                request.academicYearEnd()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Class created successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/classes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Major> majors = institutionAdminService.getMajorsByInstitution(
                admin.getInstitution().getId()
            );
            model.addAttribute("admin", admin);
            model.addAttribute("majors", majors);
            return "institution-admin/classes/form";
        }
    }

    /**
     * Show edit class form.
     * GET /institution-admin/{adminId}/classes/{classId}/edit
     */
    @GetMapping("/{adminId}/classes/{classId}/edit")
    public String showEditClassForm(
        @PathVariable Long adminId,
        @PathVariable Long classId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );
            var academicClass = classes
                .stream()
                .filter(c -> c.getId().equals(classId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Class not found")
                );

            List<Major> majors = institutionAdminService.getMajorsByInstitution(
                admin.getInstitution().getId()
            );

            model.addAttribute("admin", admin);
            model.addAttribute("academicClass", academicClass);
            model.addAttribute("majors", majors);
            model.addAttribute(
                "classRequest",
                new ClassRequest(
                    academicClass.getName(),
                    academicClass.getDescription(),
                    academicClass.getAcademicYearStart(),
                    academicClass.getAcademicYearEnd(),
                    academicClass.getMajor().getId()
                )
            );

            return "institution-admin/classes/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update a class.
     * POST /institution-admin/{adminId}/classes/{classId}
     */
    @PostMapping("/{adminId}/classes/{classId}")
    public String updateClass(
        @PathVariable Long adminId,
        @PathVariable Long classId,
        @Valid @ModelAttribute ClassRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Major> majors = institutionAdminService.getMajorsByInstitution(
                admin.getInstitution().getId()
            );
            model.addAttribute("admin", admin);
            model.addAttribute("majors", majors);
            return "institution-admin/classes/form";
        }

        try {
            institutionAdminService.updateClass(
                classId,
                request.name(),
                request.description(),
                request.academicYearStart(),
                request.academicYearEnd()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Class updated successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/classes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Major> majors = institutionAdminService.getMajorsByInstitution(
                admin.getInstitution().getId()
            );
            model.addAttribute("admin", admin);
            model.addAttribute("majors", majors);
            return "institution-admin/classes/form";
        }
    }

    /**
     * Delete a class.
     * POST /institution-admin/{adminId}/classes/{classId}/delete
     */
    @PostMapping("/{adminId}/classes/{classId}/delete")
    public String deleteClass(
        @PathVariable Long adminId,
        @PathVariable Long classId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteClass(classId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Class deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/institution-admin/" + adminId + "/classes";
    }

    // ==================== Section Management ====================

    /**
     * List all sections for the institution.
     * GET /institution-admin/{adminId}/sections
     */
    @GetMapping("/{adminId}/sections")
    public String listSections(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long classId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Section> sections;
            if (classId != null) {
                sections = institutionAdminService.getSectionsByClass(classId);
            } else {
                sections = institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );
            }

            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("sections", sections);
            model.addAttribute("classes", classes);
            model.addAttribute("selectedClassId", classId);

            return "institution-admin/sections/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create section form.
     * GET /institution-admin/{adminId}/sections/new
     */
    @GetMapping("/{adminId}/sections/new")
    public String showCreateSectionForm(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long classId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("classes", classes);
            model.addAttribute(
                "sectionRequest",
                new SectionRequest(null, null, classId)
            );

            return "institution-admin/sections/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new section.
     * POST /institution-admin/{adminId}/sections
     */
    @PostMapping("/{adminId}/sections")
    public String createSection(
        @PathVariable Long adminId,
        @Valid @ModelAttribute SectionRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("classes", classes);
            return "institution-admin/sections/form";
        }

        try {
            institutionAdminService.createSection(
                request.classId(),
                request.name(),
                request.description()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Section created successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/sections";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("classes", classes);
            return "institution-admin/sections/form";
        }
    }

    /**
     * Show edit section form.
     * GET /institution-admin/{adminId}/sections/{sectionId}/edit
     */
    @GetMapping("/{adminId}/sections/{sectionId}/edit")
    public String showEditSectionForm(
        @PathVariable Long adminId,
        @PathVariable Long sectionId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Section> sections =
                institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );
            var section = sections
                .stream()
                .filter(s -> s.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Section not found")
                );

            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("section", section);
            model.addAttribute("classes", classes);
            model.addAttribute(
                "sectionRequest",
                new SectionRequest(
                    section.getName(),
                    section.getDescription(),
                    section.getAcademicClass().getId()
                )
            );

            return "institution-admin/sections/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update a section.
     * POST /institution-admin/{adminId}/sections/{sectionId}
     */
    @PostMapping("/{adminId}/sections/{sectionId}")
    public String updateSection(
        @PathVariable Long adminId,
        @PathVariable Long sectionId,
        @Valid @ModelAttribute SectionRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("classes", classes);
            return "institution-admin/sections/form";
        }

        try {
            institutionAdminService.updateSection(
                sectionId,
                request.name(),
                request.description()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Section updated successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/sections";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("classes", classes);
            return "institution-admin/sections/form";
        }
    }

    /**
     * Delete a section.
     * POST /institution-admin/{adminId}/sections/{sectionId}/delete
     */
    @PostMapping("/{adminId}/sections/{sectionId}/delete")
    public String deleteSection(
        @PathVariable Long adminId,
        @PathVariable Long sectionId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteSection(sectionId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Section deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/institution-admin/" + adminId + "/sections";
    }

    // ==================== Professor Management ====================

    /**
     * List all professors for the institution.
     * GET /institution-admin/{adminId}/professors
     */
    @GetMapping("/{adminId}/professors")
    public String listProfessors(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long departmentId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Professor> professors;
            if (departmentId != null) {
                professors = institutionAdminService.getProfessorsByDepartment(
                    departmentId
                );
            } else {
                professors = institutionAdminService.getProfessorsByInstitution(
                    admin.getInstitution().getId()
                );
            }

            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("professors", professors);
            model.addAttribute("departments", departments);
            model.addAttribute("selectedDepartmentId", departmentId);

            return "institution-admin/professors/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create professor form.
     * GET /institution-admin/{adminId}/professors/new
     */
    @GetMapping("/{adminId}/professors/new")
    public String showCreateProfessorForm(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long departmentId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            model.addAttribute("roles", ProfessorRole.values());
            model.addAttribute(
                "professorRequest",
                new ProfessorRequest(
                    null,
                    null,
                    null,
                    null,
                    ProfessorRole.FACULTY_MEMBER,
                    admin.getInstitution().getId(),
                    departmentId
                )
            );

            return "institution-admin/professors/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new professor.
     * POST /institution-admin/{adminId}/professors
     */
    @PostMapping("/{adminId}/professors")
    public String createProfessor(
        @PathVariable Long adminId,
        @Valid @ModelAttribute ProfessorRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            model.addAttribute("roles", ProfessorRole.values());
            return "institution-admin/professors/form";
        }

        try {
            institutionAdminService.createProfessor(
                request.institutionId(),
                request.departmentId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password(),
                request.role()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Professor created successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/professors";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            model.addAttribute("roles", ProfessorRole.values());
            return "institution-admin/professors/form";
        }
    }

    /**
     * Show edit professor form.
     * GET /institution-admin/{adminId}/professors/{professorId}/edit
     */
    @GetMapping("/{adminId}/professors/{professorId}/edit")
    public String showEditProfessorForm(
        @PathVariable Long adminId,
        @PathVariable Long professorId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Professor> professors =
                institutionAdminService.getProfessorsByInstitution(
                    admin.getInstitution().getId()
                );
            var professor = professors
                .stream()
                .filter(p -> p.getId().equals(professorId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("professor", professor);
            model.addAttribute("departments", departments);
            model.addAttribute("roles", ProfessorRole.values());
            model.addAttribute(
                "professorRequest",
                new ProfessorRequest(
                    professor.getFirstName(),
                    professor.getFamilyName(),
                    professor.getEmail(),
                    null,
                    professor.getRole(),
                    admin.getInstitution().getId(),
                    professor.getDepartment().getId()
                )
            );

            return "institution-admin/professors/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update a professor.
     * POST /institution-admin/{adminId}/professors/{professorId}
     */
    @PostMapping("/{adminId}/professors/{professorId}")
    public String updateProfessor(
        @PathVariable Long adminId,
        @PathVariable Long professorId,
        @Valid @ModelAttribute ProfessorRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            model.addAttribute("roles", ProfessorRole.values());
            return "institution-admin/professors/form";
        }

        try {
            institutionAdminService.updateProfessor(
                professorId,
                request.firstName(),
                request.lastName(),
                request.email(),
                request.role(),
                request.departmentId()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Professor updated successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/professors";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Department> departments =
                institutionAdminService.getDepartmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("departments", departments);
            model.addAttribute("roles", ProfessorRole.values());
            return "institution-admin/professors/form";
        }
    }

    /**
     * Delete a professor.
     * POST /institution-admin/{adminId}/professors/{professorId}/delete
     */
    @PostMapping("/{adminId}/professors/{professorId}/delete")
    public String deleteProfessor(
        @PathVariable Long adminId,
        @PathVariable Long professorId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteProfessor(professorId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Professor deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/institution-admin/" + adminId + "/professors";
    }

    // ==================== Student Management ====================

    /**
     * List all students for the institution.
     * GET /institution-admin/{adminId}/students
     */
    @GetMapping("/{adminId}/students")
    public String listStudents(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long sectionId,
        @RequestParam(required = false) Long classId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Student> students;
            if (sectionId != null) {
                students = institutionAdminService.getStudentsBySection(
                    sectionId
                );
            } else if (classId != null) {
                students = institutionAdminService.getStudentsByClass(classId);
            } else {
                students = institutionAdminService.getStudentsByInstitution(
                    admin.getInstitution().getId()
                );
            }

            List<Section> sections =
                institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );
            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("students", students);
            model.addAttribute("sections", sections);
            model.addAttribute("classes", classes);
            model.addAttribute("selectedSectionId", sectionId);
            model.addAttribute("selectedClassId", classId);

            return "institution-admin/students/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create student form.
     * GET /institution-admin/{adminId}/students/new
     */
    @GetMapping("/{adminId}/students/new")
    public String showCreateStudentForm(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long sectionId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Section> sections =
                institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("sections", sections);
            model.addAttribute("statuses", StudentStatus.values());
            model.addAttribute(
                "studentRequest",
                new StudentRequest(
                    null,
                    null,
                    null,
                    null,
                    StudentStatus.REGISTERED,
                    admin.getInstitution().getId(),
                    sectionId
                )
            );

            return "institution-admin/students/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new student.
     * POST /institution-admin/{adminId}/students
     */
    @PostMapping("/{adminId}/students")
    public String createStudent(
        @PathVariable Long adminId,
        @Valid @ModelAttribute StudentRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Section> sections =
                institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("sections", sections);
            model.addAttribute("statuses", StudentStatus.values());
            return "institution-admin/students/form";
        }

        try {
            institutionAdminService.createStudent(
                request.institutionId(),
                request.sectionId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.studentId(),
                request.status()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Student created successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/students";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Section> sections =
                institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("sections", sections);
            model.addAttribute("statuses", StudentStatus.values());
            return "institution-admin/students/form";
        }
    }

    /**
     * Show edit student form.
     * GET /institution-admin/{adminId}/students/{studentId}/edit
     */
    @GetMapping("/{adminId}/students/{studentId}/edit")
    public String showEditStudentForm(
        @PathVariable Long adminId,
        @PathVariable Long studentId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Student> students =
                institutionAdminService.getStudentsByInstitution(
                    admin.getInstitution().getId()
                );
            var student = students
                .stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Student not found")
                );

            List<Section> sections =
                institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("student", student);
            model.addAttribute("sections", sections);
            model.addAttribute("statuses", StudentStatus.values());
            model.addAttribute(
                "studentRequest",
                new StudentRequest(
                    student.getFirstName(),
                    student.getFamilyName(),
                    student.getEmail(),
                    student.getStudentId(),
                    student.getStatus(),
                    admin.getInstitution().getId(),
                    student.getSection().getId()
                )
            );

            return "institution-admin/students/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update a student.
     * POST /institution-admin/{adminId}/students/{studentId}
     */
    @PostMapping("/{adminId}/students/{studentId}")
    public String updateStudent(
        @PathVariable Long adminId,
        @PathVariable Long studentId,
        @Valid @ModelAttribute StudentRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Section> sections =
                institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("sections", sections);
            model.addAttribute("statuses", StudentStatus.values());
            return "institution-admin/students/form";
        }

        try {
            institutionAdminService.updateStudent(
                studentId,
                request.firstName(),
                request.lastName(),
                request.email(),
                request.studentId(),
                request.status(),
                request.sectionId()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Student updated successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/students";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Section> sections =
                institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("sections", sections);
            model.addAttribute("statuses", StudentStatus.values());
            return "institution-admin/students/form";
        }
    }

    /**
     * Delete a student.
     * POST /institution-admin/{adminId}/students/{studentId}/delete
     */
    @PostMapping("/{adminId}/students/{studentId}/delete")
    public String deleteStudent(
        @PathVariable Long adminId,
        @PathVariable Long studentId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteStudent(studentId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Student deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/institution-admin/" + adminId + "/students";
    }

    // ==================== Session Management ====================

    /**
     * List all sessions for the institution.
     * GET /institution-admin/{adminId}/sessions
     */
    @GetMapping("/{adminId}/sessions")
    public String listSessions(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long sectionId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime to,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Session> sessions;
            if (sectionId != null) {
                sessions = institutionAdminService.getSessionsBySection(
                    sectionId
                );
            } else if (from != null && to != null) {
                sessions = institutionAdminService.getSessionsByDateRange(
                    admin.getInstitution().getId(),
                    from,
                    to
                );
            } else {
                sessions = institutionAdminService.getSessionsByInstitution(
                    admin.getInstitution().getId()
                );
            }

            List<Section> sections =
                institutionAdminService.getSectionsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("sessions", sessions);
            model.addAttribute("sections", sections);
            model.addAttribute("selectedSectionId", sectionId);
            model.addAttribute("from", from);
            model.addAttribute("to", to);

            return "institution-admin/sessions/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    // ==================== Course Management ====================

    /**
     * List all courses.
     * GET /institution-admin/{adminId}/courses
     */
    @GetMapping("/{adminId}/courses")
    public String listCourses(@PathVariable Long adminId, Model model) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Course> courses = institutionAdminService.getAllCourses();

            model.addAttribute("admin", admin);
            model.addAttribute("courses", courses);

            return "institution-admin/courses/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create course form.
     * GET /institution-admin/{adminId}/courses/new
     */
    @GetMapping("/{adminId}/courses/new")
    public String showCreateCourseForm(
        @PathVariable Long adminId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute(
                "courseRequest",
                new CourseRequest(null, null, null)
            );

            return "institution-admin/courses/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new course.
     * POST /institution-admin/{adminId}/courses
     */
    @PostMapping("/{adminId}/courses")
    public String createCourse(
        @PathVariable Long adminId,
        @Valid @ModelAttribute CourseRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "institution-admin/courses/form";
        }

        try {
            institutionAdminService.createCourse(
                request.name(),
                request.label(),
                request.description()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Course created successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/courses";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "institution-admin/courses/form";
        }
    }

    /**
     * Show edit course form.
     * GET /institution-admin/{adminId}/courses/{courseId}/edit
     */
    @GetMapping("/{adminId}/courses/{courseId}/edit")
    public String showEditCourseForm(
        @PathVariable Long adminId,
        @PathVariable Long courseId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Course> courses = institutionAdminService.getAllCourses();
            var course = courses
                .stream()
                .filter(c -> c.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Course not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute("course", course);
            model.addAttribute(
                "courseRequest",
                new CourseRequest(
                    course.getName(),
                    course.getLabel(),
                    course.getDescription()
                )
            );

            return "institution-admin/courses/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update a course.
     * POST /institution-admin/{adminId}/courses/{courseId}
     */
    @PostMapping("/{adminId}/courses/{courseId}")
    public String updateCourse(
        @PathVariable Long adminId,
        @PathVariable Long courseId,
        @Valid @ModelAttribute CourseRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "institution-admin/courses/form";
        }

        try {
            institutionAdminService.updateCourse(
                courseId,
                request.name(),
                request.label(),
                request.description()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Course updated successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/courses";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            return "institution-admin/courses/form";
        }
    }

    /**
     * Delete a course.
     * POST /institution-admin/{adminId}/courses/{courseId}/delete
     */
    @PostMapping("/{adminId}/courses/{courseId}/delete")
    public String deleteCourse(
        @PathVariable Long adminId,
        @PathVariable Long courseId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteCourse(courseId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Course deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/institution-admin/" + adminId + "/courses";
    }

    // ==================== Calendar Management ====================

    /**
     * List all calendar entries.
     * GET /institution-admin/{adminId}/calendar
     */
    @GetMapping("/{adminId}/calendar")
    public String listCalendarEntries(
        @PathVariable Long adminId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime to,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Calendar> calendarEntries;
            if (from != null && to != null) {
                calendarEntries =
                    institutionAdminService.getCalendarEntriesByDateRange(
                        from,
                        to
                    );
            } else {
                calendarEntries =
                    institutionAdminService.getAllCalendarEntries();
            }

            model.addAttribute("admin", admin);
            model.addAttribute("calendarEntries", calendarEntries);
            model.addAttribute("from", from);
            model.addAttribute("to", to);

            return "institution-admin/calendar/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create calendar entry form.
     * GET /institution-admin/{adminId}/calendar/new
     */
    @GetMapping("/{adminId}/calendar/new")
    public String showCreateCalendarForm(
        @PathVariable Long adminId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute("dayTypes", DayType.values());
            model.addAttribute("daysOfWeek", java.time.DayOfWeek.values());
            model.addAttribute(
                "calendarRequest",
                new CalendarRequest(null, null, null, DayType.WORKDAY)
            );

            return "institution-admin/calendar/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new calendar entry.
     * POST /institution-admin/{adminId}/calendar
     */
    @PostMapping("/{adminId}/calendar")
    public String createCalendarEntry(
        @PathVariable Long adminId,
        @Valid @ModelAttribute CalendarRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            model.addAttribute("dayTypes", DayType.values());
            model.addAttribute("daysOfWeek", java.time.DayOfWeek.values());
            return "institution-admin/calendar/form";
        }

        try {
            institutionAdminService.createCalendarEntry(
                request.dayOfWeek(),
                request.date(),
                request.holidayName(),
                request.dayType()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Calendar entry created successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/calendar";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            model.addAttribute("dayTypes", DayType.values());
            model.addAttribute("daysOfWeek", java.time.DayOfWeek.values());
            return "institution-admin/calendar/form";
        }
    }

    /**
     * Show edit calendar entry form.
     * GET /institution-admin/{adminId}/calendar/{calendarId}/edit
     */
    @GetMapping("/{adminId}/calendar/{calendarId}/edit")
    public String showEditCalendarForm(
        @PathVariable Long adminId,
        @PathVariable Long calendarId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Calendar> calendarEntries =
                institutionAdminService.getAllCalendarEntries();
            var calendar = calendarEntries
                .stream()
                .filter(c -> c.getId().equals(calendarId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Calendar entry not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute("calendar", calendar);
            model.addAttribute("dayTypes", DayType.values());
            model.addAttribute("daysOfWeek", java.time.DayOfWeek.values());
            model.addAttribute(
                "calendarRequest",
                new CalendarRequest(
                    calendar.getDayOfWeek(),
                    calendar.getDate(),
                    calendar.getHolidayName(),
                    calendar.getDayType()
                )
            );

            return "institution-admin/calendar/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update a calendar entry.
     * POST /institution-admin/{adminId}/calendar/{calendarId}
     */
    @PostMapping("/{adminId}/calendar/{calendarId}")
    public String updateCalendarEntry(
        @PathVariable Long adminId,
        @PathVariable Long calendarId,
        @Valid @ModelAttribute CalendarRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            model.addAttribute("dayTypes", DayType.values());
            model.addAttribute("daysOfWeek", java.time.DayOfWeek.values());
            return "institution-admin/calendar/form";
        }

        try {
            institutionAdminService.updateCalendarEntry(
                calendarId,
                request.dayOfWeek(),
                request.date(),
                request.holidayName(),
                request.dayType()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Calendar entry updated successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/calendar";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            model.addAttribute("dayTypes", DayType.values());
            model.addAttribute("daysOfWeek", java.time.DayOfWeek.values());
            return "institution-admin/calendar/form";
        }
    }

    /**
     * Delete a calendar entry.
     * POST /institution-admin/{adminId}/calendar/{calendarId}/delete
     */
    @PostMapping("/{adminId}/calendar/{calendarId}/delete")
    public String deleteCalendarEntry(
        @PathVariable Long adminId,
        @PathVariable Long calendarId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteCalendarEntry(calendarId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Calendar entry deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/institution-admin/" + adminId + "/calendar";
    }

    // ==================== Semester Management ====================

    /**
     * List all semesters for a class.
     * GET /institution-admin/{adminId}/classes/{classId}/semesters
     */
    @GetMapping("/{adminId}/classes/{classId}/semesters")
    public String listSemesters(
        @PathVariable Long adminId,
        @PathVariable Long classId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Semester> semesters =
                institutionAdminService.getSemestersByClass(classId);

            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );
            var academicClass = classes
                .stream()
                .filter(c -> c.getId().equals(classId))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Class not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute("semesters", semesters);
            model.addAttribute("academicClass", academicClass);

            return "institution-admin/semesters/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create semester form.
     * GET /institution-admin/{adminId}/classes/{classId}/semesters/new
     */
    @GetMapping("/{adminId}/classes/{classId}/semesters/new")
    public String showCreateSemesterForm(
        @PathVariable Long adminId,
        @PathVariable Long classId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            model.addAttribute("admin", admin);
            model.addAttribute("classId", classId);
            model.addAttribute(
                "semesterRequest",
                new SemesterRequest(null, null, null, null, null, classId)
            );

            return "institution-admin/semesters/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new semester.
     * POST /institution-admin/{adminId}/classes/{classId}/semesters
     */
    @PostMapping("/{adminId}/classes/{classId}/semesters")
    public String createSemester(
        @PathVariable Long adminId,
        @PathVariable Long classId,
        @Valid @ModelAttribute SemesterRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            model.addAttribute("classId", classId);
            return "institution-admin/semesters/form";
        }

        try {
            institutionAdminService.createSemester(
                classId,
                request.name(),
                request.label(),
                request.description(),
                request.startDate(),
                request.endDate()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Semester created successfully!"
            );
            return (
                "redirect:/institution-admin/" +
                adminId +
                "/classes/" +
                classId +
                "/semesters"
            );
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            model.addAttribute("classId", classId);
            return "institution-admin/semesters/form";
        }
    }

    /**
     * Show edit semester form.
     * GET /institution-admin/{adminId}/semesters/{semesterId}/edit
     */
    @GetMapping("/{adminId}/semesters/{semesterId}/edit")
    public String showEditSemesterForm(
        @PathVariable Long adminId,
        @PathVariable Long semesterId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            // Find the semester by getting all classes and their semesters
            List<Class> classes =
                institutionAdminService.getClassesByInstitution(
                    admin.getInstitution().getId()
                );

            Semester semester = null;
            for (Class c : classes) {
                List<Semester> semesters =
                    institutionAdminService.getSemestersByClass(c.getId());
                semester = semesters
                    .stream()
                    .filter(s -> s.getId().equals(semesterId))
                    .findFirst()
                    .orElse(null);
                if (semester != null) break;
            }

            if (semester == null) {
                throw new IllegalArgumentException("Semester not found");
            }

            model.addAttribute("admin", admin);
            model.addAttribute("semester", semester);
            model.addAttribute("classId", semester.getAcademicClass().getId());
            model.addAttribute(
                "semesterRequest",
                new SemesterRequest(
                    semester.getName(),
                    semester.getLabel(),
                    semester.getDescription(),
                    semester.getStartDate(),
                    semester.getEndDate(),
                    semester.getAcademicClass().getId()
                )
            );

            return "institution-admin/semesters/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Update a semester.
     * POST /institution-admin/{adminId}/semesters/{semesterId}
     */
    @PostMapping("/{adminId}/semesters/{semesterId}")
    public String updateSemester(
        @PathVariable Long adminId,
        @PathVariable Long semesterId,
        @Valid @ModelAttribute SemesterRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            model.addAttribute("classId", request.classId());
            return "institution-admin/semesters/form";
        }

        try {
            institutionAdminService.updateSemester(
                semesterId,
                request.name(),
                request.label(),
                request.description(),
                request.startDate(),
                request.endDate()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Semester updated successfully!"
            );
            return (
                "redirect:/institution-admin/" +
                adminId +
                "/classes/" +
                request.classId() +
                "/semesters"
            );
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            model.addAttribute("admin", admin);
            model.addAttribute("classId", request.classId());
            return "institution-admin/semesters/form";
        }
    }

    /**
     * Delete a semester.
     * POST /institution-admin/{adminId}/semesters/{semesterId}/delete
     */
    @PostMapping("/{adminId}/semesters/{semesterId}/delete")
    public String deleteSemester(
        @PathVariable Long adminId,
        @PathVariable Long semesterId,
        @RequestParam Long classId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteSemester(semesterId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Semester deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return (
            "redirect:/institution-admin/" +
            adminId +
            "/classes/" +
            classId +
            "/semesters"
        );
    }

    // ==================== Course Assignment Management ====================

    /**
     * List course assignments for the institution.
     * GET /institution-admin/{adminId}/course-assignments
     */
    @GetMapping("/{adminId}/course-assignments")
    public String listCourseAssignments(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long professorId,
        @RequestParam(required = false) Long semesterId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<CourseAssignment> assignments;
            if (professorId != null) {
                assignments =
                    institutionAdminService.getCourseAssignmentsByProfessor(
                        professorId
                    );
            } else if (semesterId != null) {
                assignments =
                    institutionAdminService.getCourseAssignmentsBySemester(
                        semesterId
                    );
            } else {
                assignments =
                    institutionAdminService.getCourseAssignmentsByInstitution(
                        admin.getInstitution().getId()
                    );
            }

            List<Professor> professors =
                institutionAdminService.getProfessorsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("assignments", assignments);
            model.addAttribute("professors", professors);
            model.addAttribute("selectedProfessorId", professorId);
            model.addAttribute("selectedSemesterId", semesterId);

            return "institution-admin/course-assignments/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create course assignment form.
     * GET /institution-admin/{adminId}/course-assignments/new
     */
    @GetMapping("/{adminId}/course-assignments/new")
    public String showCreateCourseAssignmentForm(
        @PathVariable Long adminId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<Professor> professors =
                institutionAdminService.getProfessorsByInstitution(
                    admin.getInstitution().getId()
                );
            List<Course> courses = institutionAdminService.getAllCourses();

            model.addAttribute("admin", admin);
            model.addAttribute("professors", professors);
            model.addAttribute("courses", courses);
            model.addAttribute("lectureTypes", LectureType.values());
            model.addAttribute(
                "courseAssignmentRequest",
                new CourseAssignmentRequest(
                    null,
                    LectureType.LECTURE,
                    null,
                    null,
                    null
                )
            );

            return "institution-admin/course-assignments/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new course assignment.
     * POST /institution-admin/{adminId}/course-assignments
     */
    @PostMapping("/{adminId}/course-assignments")
    public String createCourseAssignment(
        @PathVariable Long adminId,
        @Valid @ModelAttribute CourseAssignmentRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Professor> professors =
                institutionAdminService.getProfessorsByInstitution(
                    admin.getInstitution().getId()
                );
            List<Course> courses = institutionAdminService.getAllCourses();
            model.addAttribute("admin", admin);
            model.addAttribute("professors", professors);
            model.addAttribute("courses", courses);
            model.addAttribute("lectureTypes", LectureType.values());
            return "institution-admin/course-assignments/form";
        }

        try {
            institutionAdminService.createCourseAssignment(
                request.professorId(),
                request.semesterId(),
                request.courseId(),
                request.type(),
                request.description()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Course assignment created successfully!"
            );
            return (
                "redirect:/institution-admin/" + adminId + "/course-assignments"
            );
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<Professor> professors =
                institutionAdminService.getProfessorsByInstitution(
                    admin.getInstitution().getId()
                );
            List<Course> courses = institutionAdminService.getAllCourses();
            model.addAttribute("admin", admin);
            model.addAttribute("professors", professors);
            model.addAttribute("courses", courses);
            model.addAttribute("lectureTypes", LectureType.values());
            return "institution-admin/course-assignments/form";
        }
    }

    /**
     * Delete a course assignment.
     * POST /institution-admin/{adminId}/course-assignments/{assignmentId}/delete
     */
    @PostMapping("/{adminId}/course-assignments/{assignmentId}/delete")
    public String deleteCourseAssignment(
        @PathVariable Long adminId,
        @PathVariable Long assignmentId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteCourseAssignment(assignmentId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Course assignment deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return (
            "redirect:/institution-admin/" + adminId + "/course-assignments"
        );
    }

    // ==================== TimeTable Management ====================

    /**
     * List timetables for the institution.
     * GET /institution-admin/{adminId}/timetables
     */
    @GetMapping("/{adminId}/timetables")
    public String listTimeTables(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long courseAssignmentId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<TimeTable> timeTables;
            if (courseAssignmentId != null) {
                timeTables =
                    institutionAdminService.getTimeTablesByCourseAssignment(
                        courseAssignmentId
                    );
            } else {
                timeTables = institutionAdminService.getTimeTablesByInstitution(
                    admin.getInstitution().getId()
                );
            }

            List<CourseAssignment> assignments =
                institutionAdminService.getCourseAssignmentsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("timeTables", timeTables);
            model.addAttribute("assignments", assignments);
            model.addAttribute(
                "selectedCourseAssignmentId",
                courseAssignmentId
            );

            return "institution-admin/timetables/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show create timetable form.
     * GET /institution-admin/{adminId}/timetables/new
     */
    @GetMapping("/{adminId}/timetables/new")
    public String showCreateTimeTableForm(
        @PathVariable Long adminId,
        @RequestParam(required = false) Long courseAssignmentId,
        Model model
    ) {
        try {
            var admin = institutionAdminService
                .getUser(adminId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Institution Admin not found")
                );

            List<CourseAssignment> assignments =
                institutionAdminService.getCourseAssignmentsByInstitution(
                    admin.getInstitution().getId()
                );

            model.addAttribute("admin", admin);
            model.addAttribute("assignments", assignments);
            model.addAttribute("daysOfWeek", java.time.DayOfWeek.values());
            model.addAttribute(
                "timeTableRequest",
                new TimeTableRequest(null, null, null, courseAssignmentId)
            );

            return "institution-admin/timetables/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Create a new timetable entry.
     * POST /institution-admin/{adminId}/timetables
     */
    @PostMapping("/{adminId}/timetables")
    public String createTimeTable(
        @PathVariable Long adminId,
        @Valid @ModelAttribute TimeTableRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<CourseAssignment> assignments =
                institutionAdminService.getCourseAssignmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("assignments", assignments);
            model.addAttribute("daysOfWeek", java.time.DayOfWeek.values());
            return "institution-admin/timetables/form";
        }

        try {
            institutionAdminService.createTimeTable(
                request.courseAssignmentId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Timetable entry created successfully!"
            );
            return "redirect:/institution-admin/" + adminId + "/timetables";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            var admin = institutionAdminService.getUser(adminId).orElse(null);
            List<CourseAssignment> assignments =
                institutionAdminService.getCourseAssignmentsByInstitution(
                    admin.getInstitution().getId()
                );
            model.addAttribute("admin", admin);
            model.addAttribute("assignments", assignments);
            model.addAttribute("daysOfWeek", java.time.DayOfWeek.values());
            return "institution-admin/timetables/form";
        }
    }

    /**
     * Delete a timetable entry.
     * POST /institution-admin/{adminId}/timetables/{timeTableId}/delete
     */
    @PostMapping("/{adminId}/timetables/{timeTableId}/delete")
    public String deleteTimeTable(
        @PathVariable Long adminId,
        @PathVariable Long timeTableId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            institutionAdminService.deleteTimeTable(timeTableId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Timetable entry deleted successfully!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/institution-admin/" + adminId + "/timetables";
    }
}
