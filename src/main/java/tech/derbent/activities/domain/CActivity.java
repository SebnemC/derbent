package tech.derbent.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.projects.domain.CProject;

/**
 * CActivity domain entity representing project activities.
 * Layer: Domain/Model (MVC)
 * 
 * This entity stores activity information including name and project association.
 * Each activity belongs to exactly one project (ManyToOne relationship).
 * Uses lazy loading for project association to optimize performance.
 */
@Entity
@Table(name = "cactivity") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "activity_id")) // Override the default column name for the ID field
public class CActivity extends CEntityDB {

	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
	@Size(max = MAX_LENGTH_NAME)
	@MetaData(displayName = "Activity Name", required = true, readOnly = false, defaultValue = "-", description = "Activity name", hidden = false)
	private String name;
	
	// Many activities belong to one project - lazy loading to avoid N+1 queries
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private CProject project;

	/**
	 * Default constructor required by JPA.
	 * Project will be set later through setter or constructor with parameters.
	 */
	public CActivity() {
		// Default constructor - project will be set later
	}

	/**
	 * Constructor with all required fields.
	 * @param name The name of the activity
	 * @param project The project this activity belongs to
	 */
	public CActivity(final String name, final CProject project) {
		this.name = name;
		this.project = project;
	}

	/**
	 * Gets the activity name.
	 * @return The name of the activity
	 */
	public String getName() { 
		return name; 
	}

	/**
	 * Gets the associated project.
	 * Note: This may trigger lazy loading if accessed outside of a transaction.
	 * @return The project this activity belongs to
	 */
	public CProject getProject() { 
		return project; 
	}

	/**
	 * Sets the activity name.
	 * @param name The new name for the activity
	 */
	public void setName(final String name) { 
		this.name = name; 
	}

	/**
	 * Sets the associated project.
	 * @param project The project this activity belongs to
	 */
	public void setProject(final CProject project) { 
		this.project = project; 
	}
}
