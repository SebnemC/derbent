package tech.derbent.decisions.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.base.domain.CStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionStatus - Domain entity representing decision status types. Layer: Domain (MVC)
 * Inherits from CStatus to provide status functionality for decisions. This entity
 * defines the possible statuses a decision can have (e.g., DRAFT, UNDER_REVIEW, APPROVED,
 * REJECTED, IMPLEMENTED).
 */
@Entity
@Table (name = "cdecisionstatus")
@AttributeOverride (name = "id", column = @Column (name = "decision_status_id"))
public class CDecisionStatus extends CStatus<CDecisionStatus> {

	@Column (name = "is_final", nullable = false)
	@MetaData (
		displayName = "Is Final Status", required = true, readOnly = false,
		defaultValue = "false",
		description = "Indicates if this is a final status (implemented/rejected)",
		hidden = false, order = 4
	)
	private boolean isFinal = false;

	@Column (name = "requires_approval", nullable = false)
	@MetaData (
		displayName = "Requires Approval", required = true, readOnly = false,
		defaultValue = "false",
		description = "Whether decisions with this status require approval to proceed",
		hidden = false, order = 7
	)
	private boolean requiresApproval = false;

	/**
	 * Constructor with name and description.
	 * @param name        the name of the decision status - must not be null or empty
	 * @param description detailed description of the decision status - can be null
	 */
	public CDecisionStatus(final String name, final CProject project) {
		super(CDecisionStatus.class, name, project);
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof CDecisionStatus)) {
			return false;
		}
		return super.equals(o);
	}

	/**
	 * Checks if this status indicates completion of the decision process.
	 * @return true if this is a final status
	 */
	public boolean isCompleted() { return isFinal; }

	public boolean isFinal() { return isFinal; }

	/**
	 * Checks if decisions with this status are pending approval.
	 * @return true if approval is required and status is not final
	 */
	public boolean isPendingApproval() { return requiresApproval && !isFinal; }

	public boolean isRequiresApproval() { return requiresApproval; }

	public void setFinal(final boolean isFinal) {
		this.isFinal = isFinal;
		updateLastModified();
	}

	public void setRequiresApproval(final boolean requiresApproval) {
		this.requiresApproval = requiresApproval;
		updateLastModified();
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}
