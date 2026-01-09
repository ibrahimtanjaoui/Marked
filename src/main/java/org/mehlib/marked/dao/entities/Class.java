package org.mehlib.marked.dao.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "class")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "academic_year_start")
    private LocalDate academicYearStart;

    @Column(name = "academic_year_end")
    private LocalDate academicYearEnd;

    @CreationTimestamp
    @Column(name = "created_on")
    private Instant createdOn;

    @UpdateTimestamp
    @Column(name = "last_updated_on")
    private Instant lastUpdatedOn;

    @OneToMany(mappedBy = "academicClass", fetch = FetchType.LAZY)
    private List<Section> sections = new ArrayList<>();

    @OneToMany(mappedBy = "academicClass", fetch = FetchType.LAZY)
    private List<Semester> semesters = new ArrayList<>();

    @ManyToOne
    private Major major;
}
