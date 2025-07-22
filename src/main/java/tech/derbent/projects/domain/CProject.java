package tech.derbent.projects.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CProject - Domain entity representing projects. Layer: Domain (MVC) Inherits from CEntityDB to provide database
 * functionality.
 */
@Entity
@Table(name = "cproject") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "project_id")) // Override the default column name for the ID field
public class CProject extends CEntityDB {

    @Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(displayName = "Project Name", required = true, readOnly = false, defaultValue = "", description = "Name of the project", hidden = false)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
