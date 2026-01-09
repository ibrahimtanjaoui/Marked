package org.mehlib.marked.dao.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "course_assignment")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "lecture_type", nullable = false)
    private LectureType type = LectureType.LECTURE;

    @CreationTimestamp
    @Column(name = "created_on")
    private Instant createdOn;

    @UpdateTimestamp
    @Column(name = "last_updated_on")
    private Instant lastUpdatedOn;

    @ManyToOne
    private Professor professor;

    @ManyToOne
    private Semester semester;

    @ManyToOne
    private Course course;

    @OneToMany(mappedBy = "courseAssignment")
    private List<TimeTable> timetables = new ArrayList<>();
}
