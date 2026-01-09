package org.mehlib.marked.dao.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Student extends User {

    @NotNull
    @Size(min = 5, max = 50)
    @Column(name = "student_id", length = 50, unique = true, nullable = false)
    private String studentId;

    @NotNull
    @Column(name = "status", nullable = false)
    private StudentStatus status = StudentStatus.REGISTERED;

    @ManyToOne
    private Institution institution;

    @ManyToOne
    private Section section;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<Attendance> attendance = new ArrayList<>();
}
