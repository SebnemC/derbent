package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CTypeEntity;

/**
 * CUserType - Domain entity representing user types. Layer: Domain (MVC) Inherits from
 * CTypeEntity to provide type functionality for users.
 */
@Entity
@Table(name = "cusertype")
@AttributeOverride(name = "id", column = @Column(name = "cusertype_id")) // Override the
																			// default
																			// column name
																			// for the ID
																			// field to
																			// follow
																			// classname_id
																			// convention
public class CUserType extends CTypeEntity {

	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
	@Size(max = MAX_LENGTH_NAME)
	@MetaData(displayName = "Type Name", required = true, readOnly = false,
		defaultValue = "", description = "Name of the user type", hidden = false,
		order = 1, maxLength = MAX_LENGTH_NAME)
	private String name;
	@Column(name = "description", nullable = true, length = MAX_LENGTH_DESCRIPTION)
	@Size(max = MAX_LENGTH_DESCRIPTION)
	@MetaData(displayName = "Description", required = false, readOnly = false,
		defaultValue = "", description = "Description of the user type", hidden = false,
		order = 2, maxLength = MAX_LENGTH_DESCRIPTION)
	private String description;

	/**
	 * Default constructor for JPA.
	 */
	public CUserType() {
		super();
	}

	/**
	 * Constructor with name.
	 * @param name the name of the user type
	 */
	public CUserType(final String name) {
		super();
		this.name = name;
	}

	/**
	 * Constructor with name and description.
	 * @param name        the name of the user type
	 * @param description the description of the user type
	 */
	public CUserType(final String name, final String description) {
		super();
		this.name = name;
		this.description = description;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CUserType)) {
			return false;
		}
		final CUserType that = (CUserType) o;
		return super.equals(that);
	}

	public String getDescription() { return description; }

	public String getName() { return name; }

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setName(final String name) { this.name = name; }

	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}
}