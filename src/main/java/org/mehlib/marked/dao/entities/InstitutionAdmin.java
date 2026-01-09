package org.mehlib.marked.dao.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "institution_admin")
@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionAdmin extends User {

    @ManyToOne
    private Institution institution;
}
