package org.mehlib.marked.dao.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "semester")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 5, max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 20)
    @Column(name = "label", length = 20)
    private String label;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(name = "created_on")
    private Instant createdOn;

    @UpdateTimestamp
    @Column(name = "last_updated_on")
    private Instant lastUpdatedOn;

    @ManyToOne
    private Class academicClass;

    @OneToMany(mappedBy = "semester", fetch = FetchType.LAZY)
    private List<Module> modules = new ArrayList<>();

    @OneToMany(mappedBy = "semester", fetch = FetchType.LAZY)
    private List<CourseAssignment> courseAssignments = new ArrayList<>();
}
