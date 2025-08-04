package tech.derbent.meetings.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.abstracts.interfaces.CKanbanType;
import tech.derbent.projects.domain.CProject;

/**
 * CMeetingType - Domain entity representing meeting types. Layer: Domain (MVC) Inherits from CEntityOfProject to
 * provide project-aware type functionality for meetings.
 */
@Entity
@Table(name = "cmeetingtype")
@AttributeOverride(name = "id", column = @Column(name = "cmeetingtype_id"))
public class CMeetingType extends CTypeEntity<CMeetingType> implements CKanbanType {

    /**
     * Default constructor for JPA.
     */
    public CMeetingType() {
        super();
    }

    public CMeetingType(final String name, final CProject project) {
        super(CMeetingType.class, name, project);
    }

    public static String getIconColorCode() {
        return "#28a745"; // Green color for meeting type entities
    }

    public static String getIconFilename() { return "vaadin:tags"; }
}