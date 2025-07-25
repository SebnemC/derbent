package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityDB;

@Entity
@Table(name = "cuserprojectsettings") // table name for the entity
@AttributeOverride(name = "id", column = @Column(name = "cuserprojectsettings_id")) // Override the default column name for the ID field
public class CUserProjectSettings extends CEntityDB {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private CUser user;
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    @Column(name = "role")
    private String role;
    @Column
    private String permission;

    public CUserProjectSettings() {
    }

    public String getPermission() {
        return permission;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getRole() {
        return role;
    }

    public CUser getUser() {
        return user;
    }

    public void setPermission(final String permission) {
        this.permission = permission;
    }

    public void setProjectId(final Long projectId) {
        this.projectId = projectId;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public void setUser(final CUser user) {
        this.user = user;
    }
}