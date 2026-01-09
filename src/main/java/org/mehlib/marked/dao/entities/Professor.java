package org.mehlib.marked.dao.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "professor")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Professor extends User {

    @NotNull
    @Column(name = "role", nullable = false)
    private ProfessorRole role = ProfessorRole.FACULTY_MEMBER;

    @ManyToOne
    private Institution institution;

    @ManyToOne
    private Department department;

    @OneToMany(mappedBy = "professor", fetch = FetchType.LAZY)
    private List<CourseAssignment> courseAssignment = new ArrayList<>();
}
