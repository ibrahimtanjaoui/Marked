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
@Table(name = "institution")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 5, max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "founded_at")
    private LocalDate foundedAt;

    // For simplicity, a string is used to represent the
    // address, but its required to create a proper class
    // to represent locations
    @Size(max = 250)
    @Column(name = "address", length = 250)
    private String address;

    // Geolocation fields for attendance verification (required)
    @NotNull(message = "Institution latitude is required")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "Institution longitude is required")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @NotNull(message = "Institution radius is required")
    @Column(name = "radius_meters", nullable = false)
    private Double radiusMeters = 500.0; // Default 500 meters

    @CreationTimestamp
    @Column(name = "created_on")
    private Instant createdOn;

    @UpdateTimestamp
    @Column(name = "last_updated_on")
    private Instant lastUpdatedOn;

    @OneToMany(mappedBy = "institution", fetch = FetchType.LAZY)
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "institution", fetch = FetchType.LAZY)
    private List<Professor> professors = new ArrayList<>();

    @OneToMany(mappedBy = "institution", fetch = FetchType.LAZY)
    private List<InstitutionAdmin> institutionAdmins = new ArrayList<>();

    @OneToMany(mappedBy = "institution", fetch = FetchType.LAZY)
    private List<Department> departments = new ArrayList<>();
}
