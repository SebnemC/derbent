package tech.derbent.risks.domain;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.base.domain.CStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CRiskStatus - Domain entity representing risk status types. Layer: Domain (MVC)
 * Inherits from CStatus to provide status functionality for risks. This entity defines
 * the possible statuses a risk can have (e.g., IDENTIFIED, MITIGATED, RESOLVED, CLOSED).
 */
@Entity
@Table (name = "criskstatus")
@AttributeOverride (name = "id", column = @Column (name = "criskstatus_id"))
public class CRiskStatus extends CStatus<CRiskStatus> {

	public static String getIconColorCode() {
		return "#6c757d"; // Gray color for status entities
	}

	public static String getIconFilename() { return "vaadin:flag"; }

	@Column (name = "is_final", nullable = false)
	@MetaData (
		displayName = "Is Final Status", required = true, readOnly = false,
		defaultValue = "false",
		description = "Indicates if this is a final status (resolved/closed)",
		hidden = false, order = 4
	)
	private Boolean isFinal = Boolean.FALSE;

	/**
	 * Default constructor for JPA.
	 */
	public CRiskStatus() {
		super();
		// Initialize with default values for JPA
		this.isFinal = Boolean.FALSE;
	}

	public CRiskStatus(final String name, final CProject project) {
		super(CRiskStatus.class, name, project);
	}

	public CRiskStatus(final String name, final CProject project,
		final String description, final String color, final Boolean isFinal) {
		super(CRiskStatus.class, name, project);
		setDescription(description);
		setColor(color);
		this.isFinal = isFinal;
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof CRiskStatus)) {
			return false;
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), isFinal);
	}

	public Boolean isFinal() { return isFinal; }

	public void setIsFinal(final Boolean isFinal) { this.isFinal = isFinal; }

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}