package tech.derbent.meetings.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.interfaces.CKanbanType;
import tech.derbent.projects.domain.CProject;

/** CMeetingType - Domain entity representing meeting types. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project-aware type
 * functionality for meetings. */
@Entity
@Table (name = "cmeetingtype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cmeetingtype_id"))
public class CMeetingType extends CTypeEntity<CMeetingType> implements CKanbanType {

	public static final String DEFAULT_COLOR = "#17a2b8";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String VIEW_NAME = "Meeting Types View";

	/** Default constructor for JPA. */
	public CMeetingType() {
		super();
	}

	public CMeetingType(final String name, final CProject project) {
		super(CMeetingType.class, name, project);
	}
}
