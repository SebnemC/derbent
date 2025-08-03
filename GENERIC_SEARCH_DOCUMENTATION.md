# Generic Search Functionality Documentation

## Overview

The generic search functionality provides a unified, reflection-based approach to searching entities across the Derbent application. This system eliminates code duplication and ensures consistent search behavior across all entity services.

## Architecture

### Core Components

1. **CGenericSearchService** - Utility class providing reflection-based search methods
2. **Enhanced Abstract Services** - Base service classes with common search functionality
3. **Specification-based Queries** - Type-safe query building using JPA specifications

### Class Hierarchy

```
CAbstractService<T>
├── Generic field search using reflection
├── Searchable field introspection
└── Field existence checking

CAbstractNamedEntityService<T> extends CAbstractService<T>
├── Name pattern search (case-insensitive, partial matching)
├── Description pattern search
└── Enhanced name-based operations

CEntityOfProjectService<T> extends CAbstractNamedEntityService<T>
├── Project-based search
├── AssignedTo user search
├── CreatedBy user search
└── Centralized project entity initialization
```

## Usage Examples

### Basic Field Search

```java
// Search by any field using reflection
List<CProject> projects = projectService.findByField("name", "Sample Project");
List<CUser> users = userService.findByField("email", "user@example.com");

// With pagination
Pageable pageable = PageRequest.of(0, 10);
List<CDecision> decisions = decisionService.findByField("priority", "HIGH", pageable);
```

### Enhanced Name Search

```java
// Partial name matching (case-insensitive)
List<CProject> projects = projectService.findByNamePattern("Sample");
List<CUser> users = userService.findByNamePattern("John");

// Description search
List<CDecision> decisions = decisionService.findByDescriptionPattern("urgent");
```

### Project Entity Search

```java
// Search by project
List<CDecision> decisions = decisionService.findByProjectGeneric(project);
List<CActivity> activities = activityService.findByProjectGeneric(project);

// Search by assigned user
List<CDecision> userDecisions = decisionService.findByAssignedTo(user);
List<CActivity> userActivities = activityService.findByAssignedTo(user);

// Search by creator
List<CDecision> createdDecisions = decisionService.findByCreatedBy(user);
```

### Field Introspection

```java
// Check if entity has a specific field
boolean hasField = projectService.hasField("name"); // true
boolean hasField = projectService.hasField("nonExistent"); // false

// Get all searchable fields
List<String> fields = decisionService.getSearchableFields();
// Returns: ["name", "description", "project", "assignedTo", "createdBy", ...]
```

## Key Features

### 1. Reflection-Based Search
- Search by any entity field without manual implementation
- Automatic type checking and field validation
- Support for complex object relationships

### 2. Specification Building
- Type-safe JPA specifications for complex queries
- Automatic null handling and validation
- Support for case-insensitive and partial matching

### 3. Code Reuse
- Common search patterns implemented once in base classes
- Automatic inheritance by all entity services
- Centralized initialization logic

### 4. Pagination Support
- All search methods support pagination
- Default unpaged variants for convenience
- Consistent pagination behavior across services

## Search Method Patterns

### Naming Convention
- `findByField(fieldName, value)` - Generic field search
- `findBy{FieldName}(value)` - Specific field search
- `findBy{FieldName}Pattern(pattern)` - Pattern matching search

### Return Types
- `List<EntityClass>` - List of matching entities
- `Page<EntityClass>` - Paginated results (when using specifications directly)

### Error Handling
- Graceful handling of null values
- Empty list return for invalid searches
- Detailed logging for debugging

## Benefits

### For Developers
- **Reduced Code Duplication**: Common search logic implemented once
- **Consistent API**: Same search methods available across all services
- **Type Safety**: Generic constraints ensure proper field access
- **Easy Extension**: New search methods automatically available

### For Maintenance
- **Single Point of Change**: Search logic updates affect all services
- **Better Testing**: Common functionality tested once
- **Documentation**: Centralized search behavior documentation

## Implementation Details

### Field Validation
```java
public static boolean hasField(Class<?> entityClass, String fieldName) {
    // Checks entire class hierarchy including inherited fields
    // Excludes system fields (id, version, serialVersionUID)
}
```

### Null Safety
```java
public static <T> Specification<T> createFieldSpec(String fieldName, Object fieldValue) {
    return (root, query, criteriaBuilder) -> {
        if (fieldValue == null) {
            return criteriaBuilder.isNull(root.get(fieldName));
        }
        return criteriaBuilder.equal(root.get(fieldName), fieldValue);
    };
}
```

### Performance Considerations
- Reflection operations are cached where possible
- Lazy loading initialization is handled automatically
- Pagination reduces memory usage for large result sets

## Migration Guide

### From Custom Search Methods
Old approach:
```java
// In each service class
public List<CDecision> findByProject(CProject project) {
    return repository.findByProject(project);
}
```

New approach:
```java
// Inherited automatically from CEntityOfProjectService
List<CDecision> decisions = decisionService.findByProjectGeneric(project);
```

### Adding New Search Capabilities
1. Add search method to appropriate abstract service class
2. Use `CGenericSearchService` for reflection-based implementation
3. All concrete services inherit the functionality automatically

## Best Practices

1. **Use Specific Methods When Available**: Prefer `findByProjectGeneric()` over `findByField("project", project)`
2. **Consider Pagination**: Use paginated versions for large datasets
3. **Validate Input**: Check field existence before searching if uncertain
4. **Handle Nulls**: All search methods handle null inputs gracefully
5. **Log Appropriately**: Search operations are logged for debugging

## Future Enhancements

- **Query Caching**: Cache frequently used specifications
- **Index Optimization**: Automatic index suggestions for searched fields
- **Advanced Filtering**: Composite search criteria support
- **Search Analytics**: Track search patterns for optimization