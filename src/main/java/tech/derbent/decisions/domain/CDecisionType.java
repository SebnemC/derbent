package tech.derbent.decisions.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.decisions.view.CDecisionTypeView;
import tech.derbent.projects.domain.CProject;

/** CDecisionType - Domain entity representing decision categorization types. Provides classification for project decisions to support decision
 * tracking and analysis. Layer: Domain (MVC) Standard decision types: STRATEGIC, TACTICAL, OPERATIONAL, TECHNICAL, BUDGET
 * @author Derbent Team
 * @since 1.0 */
@Entity
@Table (name = "cdecisiontype")
@AttributeOverride (name = "id", column = @Column (name = "cdecisiontype_id"))
public class CDecisionType extends CTypeEntity<CDecisionType> {

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return "#dc3545"; // Red color for decision type entities
	}

	public static String getIconFilename() { return "vaadin:tags"; }

	public static Class<?> getViewClassStatic() { return CDecisionTypeView.class; }

	@Column (name = "requires_approval", nullable = false)
	@NotNull
	@AMetaData (
			displayName = "Requires Approval", required = true, readOnly = false, defaultValue = "false",
			description = "Whether decisions of this type require approval to proceed", hidden = false, order = 7
	)
	private Boolean requiresApproval = false;

	public CDecisionType() {
		super();
		this.requiresApproval = false;
	}

	public CDecisionType(final String name, final CProject project) {
		super(CDecisionType.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}

	public Boolean getRequiresApproval() { return requiresApproval; }

	public boolean requiresApproval() {
		return Boolean.TRUE.equals(requiresApproval);
	}

	public void setRequiresApproval(final Boolean requiresApproval) { this.requiresApproval = requiresApproval; }
}
