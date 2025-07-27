package tech.derbent.abstracts.domains;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;

@MappedSuperclass
public abstract class CEntityNamed extends CEntityDB {

	private static final Logger logger = LoggerFactory.getLogger(CEntityNamed.class);

	protected static final int MAX_LENGTH_NAME = 100;

	@Column (name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
	@Size (max = MAX_LENGTH_NAME)
	@MetaData (
		displayName = "Name", required = true, readOnly = false, defaultValue = "",
		description = "Name", hidden = false, order = 0, maxLength = MAX_LENGTH_NAME
	)
	private String name;

	@Column (name = "description", nullable = true, length = 2000)
	@Size (max = 2000)
	@MetaData (
		displayName = "Description", required = false, readOnly = false,
		defaultValue = "", description = "Detailed description of the project",
		hidden = false, order = 1, maxLength = 2000
	)
	private String description;

	// Audit fields
	@Column (name = "created_date", nullable = true)
	@MetaData (
		displayName = "Created Date", required = false, readOnly = true,
		description = "Date and time when the activity was created", hidden = false,
		order = 80
	)
	private LocalDateTime createdDate;

	@Column (name = "last_modified_date", nullable = true)
	@MetaData (
		displayName = "Last Modified", required = false, readOnly = true,
		description = "Date and time when the activity was last modified", hidden = false,
		order = 81
	)
	private LocalDateTime lastModifiedDate;

	public CEntityNamed() {
		super();
	}

	public CEntityNamed(final String name) {
		this();
		this.name = name;
		this.description = null; // Default description is null

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("constructor called with null or empty name for {}",
				getClass().getSimpleName());
		}
	}

	public CEntityNamed(final String name, final String description) {
		this(name);
		this.description = description;
	}

	public LocalDateTime getCreatedDate() { return createdDate; }

	public String getDescription() { return description; }

	public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }

	public String getName() { return name; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();

		if (this.createdDate == null) {
			this.createdDate = LocalDateTime.now();
		}
		this.lastModifiedDate = LocalDateTime.now();
	}

	public void setCreatedDate(final LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	public void setLastModifiedDate(final LocalDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public void setName(final String name) {

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("setname called with null or empty name for {}",
				getClass().getSimpleName());
		}
		this.name = name;
	}

	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}

	/**
	 * Update the last modified date to now.
	 */
	protected void updateLastModified() {
		this.lastModifiedDate = LocalDateTime.now();
	}
}
