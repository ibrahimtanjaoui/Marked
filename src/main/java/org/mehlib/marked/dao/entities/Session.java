package org.mehlib.marked.dao.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "session")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @NotNull
    @Column(name = "type", nullable = false)
    private SessionType type = SessionType.REGULAR;

    @Size(min = 6, max = 6)
    @Column(name = "session_code", length = 6)
    private String sessionCode;

    @Column(name = "code_generated_at")
    private Instant codeGeneratedAt;

    @CreationTimestamp
    @Column(name = "created_on")
    private Instant createdOn;

    @UpdateTimestamp
    @Column(name = "last_updated_on")
    private Instant lastUpdatedOn;

    @ManyToOne
    private TimeTable timeTable;

    @ManyToOne
    private Calendar calendar;

    @OneToMany(mappedBy = "session")
    private List<Attendance> attendance = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private List<Section> sections = new ArrayList<>();
}
