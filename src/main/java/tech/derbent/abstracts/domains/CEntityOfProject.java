package tech.derbent.abstracts.domains;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

@MappedSuperclass
public abstract class CEntityOfProject extends CEntityNamed {

	// Many risks belong to one project
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "project_id", nullable = false)
	private CProject project;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "assigned_to_id", nullable = true)
	@MetaData (
		displayName = "Assigned To", required = false, readOnly = false,
		description = "User assigned to this activity", hidden = false, order = 10,
		dataProviderBean = "CUserService"
	)
	private CUser assignedTo;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "created_by_id", nullable = true)
	@MetaData (
		displayName = "Created By", required = false, readOnly = true,
		description = "User who created this activity", hidden = false, order = 11,
		dataProviderBean = "CUserService"
	)
	private CUser createdBy;

	// Default constructor for JPA
	public CEntityOfProject() {
		super();
		this.project = null; // This should be set later
	}

	public CEntityOfProject(final CProject project) {
		this.project = project;
	}

	public CEntityOfProject(final String name, final CProject project) {
		super(name);
		this.project = project;
	}

	public CEntityOfProject(final String name, final CProject project,
		final CUser assignedTo) {
		super(name);
		this.project = project;
		this.assignedTo = assignedTo;
	}

	/**
	 * Gets the assigned user for this entity.
	 * 
	 * @return the assigned user
	 */
	public CUser getAssignedTo() {
		return assignedTo;
	}

	/**
	 * Sets the assigned user for this entity.
	 * 
	 * @param assignedTo the user to assign
	 */
	public void setAssignedTo(final CUser assignedTo) {
		this.assignedTo = assignedTo;
	}

	/**
	 * Gets the user who created this entity.
	 * 
	 * @return the creator user
	 */
	public CUser getCreatedBy() {
		return createdBy;
	}

	/**
	 * Sets the user who created this entity.
	 * 
	 * @param createdBy the creator user
	 */
	public void setCreatedBy(final CUser createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * Gets the project this entity belongs to.
	 * 
	 * @return the project
	 */
	public CProject getProject() {
		return project;
	}

	/**
	 * Sets the project this entity belongs to.
	 * 
	 * @param project the project to set
	 */
	public void setProject(final CProject project) {
		this.project = project;
	}

	public String getProjectName() {
		return (project != null) ? project.getName() : "No Project";
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}
}