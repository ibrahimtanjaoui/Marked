package org.mehlib.marked.dao.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "attendance")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 500)
    @Column(name = "comment", length = 500)
    private String comment;

    @NotNull
    @Column(name = "status", nullable = false)
    private AttendanceStatus status = AttendanceStatus.NOT_MARKED;

    @Size(max = 1000)
    @Column(name = "justification_text", length = 1000)
    private String justificationText;

    @Column(name = "justification_status")
    @Enumerated(EnumType.STRING)
    private JustificationStatus justificationStatus;

    @Column(name = "justification_submitted_at")
    private Instant justificationSubmittedAt;

    @Column(name = "justification_reviewed_at")
    private Instant justificationReviewedAt;

    @ManyToOne
    @JoinColumn(name = "justification_reviewed_by")
    private Professor justificationReviewedBy;

    @CreationTimestamp
    @Column(name = "created_on")
    private Instant createdOn;

    @UpdateTimestamp
    @Column(name = "last_updated_on")
    private Instant lastUpdatedOn;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Session session;
}
