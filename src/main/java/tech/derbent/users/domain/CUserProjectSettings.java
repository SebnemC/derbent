package tech.derbent.users.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cuserprojectsettings") // table name for the entity
@AttributeOverride (name = "id", column = @Column (name = "cuserprojectsettings_id"))
public class CUserProjectSettings extends CEntityDB<CUserProjectSettings> {

	@ManyToOne
	@JoinColumn (name = "user_id", nullable = false)
	private CUser user;

	@ManyToOne
	@JoinColumn (name = "project_id", nullable = false)
	private CProject project;

	@Column (name = "role")
	private String role;

	@Column
	private String permission;

	// Resource allocation fields - "relqtwd" = Resource aLlocation Quantity With Time Due
	@Column (name = "allocated_hours", precision = 10, scale = 2)
	@DecimalMin(value = "0.0", message = "Allocated hours must be non-negative")
	@DecimalMax(value = "99999.99", message = "Allocated hours cannot exceed 99999.99")
	@MetaData (
		displayName = "Allocated Hours", required = false, readOnly = false,
		description = "Total hours allocated to this user for this project", hidden = false, order = 5
	)
	private BigDecimal allocatedHours;

	@Column (name = "hourly_rate", precision = 10, scale = 2)
	@DecimalMin(value = "0.0", message = "Hourly rate must be non-negative")
	@MetaData (
		displayName = "Hourly Rate", required = false, readOnly = false,
		description = "Hourly rate for this user on this project", hidden = false, order = 6
	)
	private BigDecimal hourlyRate;

	@Column (name = "start_date")
	@MetaData (
		displayName = "Start Date", required = false, readOnly = false,
		description = "Date when user assignment starts", hidden = false, order = 7
	)
	private LocalDate startDate;

	@Column (name = "due_date")
	@MetaData (
		displayName = "Due Date", required = false, readOnly = false,
		description = "Date when user assignment is due to complete", hidden = false, order = 8
	)
	private LocalDate dueDate;

	@Column (name = "workload_percentage", precision = 5, scale = 2)
	@DecimalMin(value = "0.0", message = "Workload percentage must be non-negative")
	@DecimalMax(value = "100.0", message = "Workload percentage cannot exceed 100%")
	@MetaData (
		displayName = "Workload %", required = false, readOnly = false,
		description = "Percentage of user's time allocated to this project", hidden = false, order = 9
	)
	private BigDecimal workloadPercentage;

	@Column (name = "is_active", nullable = false)
	@MetaData (
		displayName = "Active", required = true, readOnly = false, defaultValue = "true",
		description = "Whether this assignment is currently active", hidden = false, order = 10
	)
	private Boolean isActive = Boolean.TRUE;

	public CUserProjectSettings() {
		super(CUserProjectSettings.class);
	}

	// Original getters and setters
	public String getPermission() { return permission; }

	public CProject getProject() { return project; }

	public String getRole() { return role; }

	public CUser getUser() { return user; }

	public void setPermission(final String permission) { this.permission = permission; }

	public void setProject(final CProject project) { this.project = project; }

	public void setRole(final String role) { this.role = role; }

	public void setUser(final CUser user) { this.user = user; }

	// New getters and setters for resource allocation fields
	public BigDecimal getAllocatedHours() { return allocatedHours; }

	public void setAllocatedHours(final BigDecimal allocatedHours) { 
		this.allocatedHours = allocatedHours; 
	}

	public BigDecimal getHourlyRate() { return hourlyRate; }

	public void setHourlyRate(final BigDecimal hourlyRate) { 
		this.hourlyRate = hourlyRate; 
	}

	public LocalDate getStartDate() { return startDate; }

	public void setStartDate(final LocalDate startDate) { 
		this.startDate = startDate; 
	}

	public LocalDate getDueDate() { return dueDate; }

	public void setDueDate(final LocalDate dueDate) { 
		this.dueDate = dueDate; 
	}

	public BigDecimal getWorkloadPercentage() { return workloadPercentage; }

	public void setWorkloadPercentage(final BigDecimal workloadPercentage) { 
		this.workloadPercentage = workloadPercentage; 
	}

	public Boolean getIsActive() { return isActive; }

	public void setIsActive(final Boolean isActive) { 
		this.isActive = isActive != null ? isActive : Boolean.TRUE; 
	}

	// Business methods for resource allocation
	public boolean isOverdue() {
		return dueDate != null && isActive && LocalDate.now().isAfter(dueDate);
	}

	public BigDecimal calculateTotalCost() {
		if (allocatedHours != null && hourlyRate != null) {
			return allocatedHours.multiply(hourlyRate);
		}
		return BigDecimal.ZERO;
	}

	public boolean hasTimeAllocation() {
		return allocatedHours != null && allocatedHours.compareTo(BigDecimal.ZERO) > 0;
	}

	public boolean hasWorkloadAssignment() {
		return workloadPercentage != null && workloadPercentage.compareTo(BigDecimal.ZERO) > 0;
	}
}