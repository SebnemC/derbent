package tech.derbent.risks.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.projects.domain.CProject;

/**
 * CRisk domain entity representing project risks.
 * Layer: Domain/Model (MVC)
 * 
 * This entity stores risk information including name, severity level, and project association.
 * Each risk belongs to exactly one project (ManyToOne relationship).
 * Uses lazy loading for project association to optimize performance.
 */
@Entity
@Table(name = "crisk") // table name for the entity as the default is the class name in lowercase
public class CRisk extends CEntityDB {

	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
	@Size(max = MAX_LENGTH_NAME)
	@MetaData(displayName = "Risk Name", required = true, readOnly = false, defaultValue = "-", description = "Name of the risk", hidden = false)
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "risk_severity", nullable = false, length = 20)
	@MetaData(displayName = "Risk Severity", required = true, readOnly = false, defaultValue = "LOW", description = "Severity of the risk", hidden = false)
	private ERiskSeverity riskSeverity;
	
	// Many risks belong to one project - lazy loading to avoid N+1 queries
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private CProject project;

	/**
	 * Default constructor required by JPA.
	 * Project will be set later through setter or constructor with parameters.
	 */
	public CRisk() {
		// Default constructor - project will be set later
	}

	/**
	 * Constructor with all required fields.
	 * @param name The name of the risk
	 * @param riskSeverity The severity level of the risk
	 * @param project The project this risk belongs to
	 */
	public CRisk(final String name, final ERiskSeverity riskSeverity, final CProject project) {
		this.name = name;
		this.riskSeverity = riskSeverity;
		this.project = project;
	}

	/**
	 * Gets the risk name.
	 * @return The name of the risk
	 */
	public String getName() { 
		return name; 
	}

	/**
	 * Gets the associated project.
	 * Note: This may trigger lazy loading if accessed outside of a transaction.
	 * @return The project this risk belongs to
	 */
	public CProject getProject() { 
		return project; 
	}

	/**
	 * Gets the risk severity level.
	 * @return The severity level of the risk
	 */
	public ERiskSeverity getRiskSeverity() { 
		return riskSeverity; 
	}

	/**
	 * Sets the risk name.
	 * @param name The new name for the risk
	 */
	public void setName(final String name) { 
		this.name = name; 
	}

	/**
	 * Sets the associated project.
	 * @param project The project this risk belongs to
	 */
	public void setProject(final CProject project) { 
		this.project = project; 
	}

	/**
	 * Sets the risk severity level.
	 * @param riskSeverity The new severity level for the risk
	 */
	public void setRiskSeverity(final ERiskSeverity riskSeverity) { 
		this.riskSeverity = riskSeverity; 
	}
}