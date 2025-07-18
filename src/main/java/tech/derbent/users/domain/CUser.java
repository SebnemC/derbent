package tech.derbent.users.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.projects.domain.CProject;

@Entity
@Table(name = "cuser") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "user_id")) // Override the default column name for the ID field
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("USER")
public class CUser extends CEntityDB {

	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = false)
	@Size(max = MAX_LENGTH_NAME)
	@MetaData(displayName = "User Name", required = true, readOnly = false, defaultValue = "-", description = "User's first name", hidden = false)
	private String name;
	@Column(name = "lastname", nullable = true, length = MAX_LENGTH_NAME, unique = false)
	@MetaData(displayName = "Last Name", required = true, readOnly = false, defaultValue = "-", description = "User's lastname", hidden = false)
	@Size(max = MAX_LENGTH_NAME)
	private String lastname;
	@MetaData(displayName = "Login", required = true, readOnly = false, defaultValue = "-", description = "Login name for the system", hidden = false)
	@Column(name = "login", nullable = true, length = MAX_LENGTH_NAME, unique = true)
	@Size(max = MAX_LENGTH_NAME)
	private String login;
	@MetaData(displayName = "Email", required = true, readOnly = false, defaultValue = "", description = "", hidden = false)
	@Column(name = "email", nullable = true, length = MAX_LENGTH_NAME, unique = false)
	@Size(max = MAX_LENGTH_NAME)
	private String email;
	@MetaData(displayName = "Phone", required = true, readOnly = false, defaultValue = "-", description = "Phone number", hidden = false)
	@Column(name = "phone", nullable = true, length = MAX_LENGTH_NAME, unique = false)
	@Size(max = MAX_LENGTH_NAME)
	private String phone;
	@ManyToMany
	@JoinTable(name = "cuser_project", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "project_id"))
	private Set<CProject> projects = new HashSet<>();

	public String getEmail() { return email; }

	public String getLastname() { return lastname; }

	public String getLogin() { return login; }

	public String getName() { return name; }

	public String getPhone() { return phone; }

	public Set<CProject> getProjects() { return projects; }

	public void setEmail(final String email) { this.email = email; }

	public void setLastname(final String lastname) { this.lastname = lastname; }

	public void setLogin(final String login) { this.login = login; }

	public void setName(final String name) { this.name = name; }

	public void setPhone(final String phone) { this.phone = phone; }

	public void setProjects(final Set<CProject> projects) { this.projects = projects; }
}
