package tech.derbent.orders.service;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.orders.domain.CApprovalStatus;

/**
 * CApprovalStatusRepository - Repository interface for CApprovalStatus entities. Layer: Service (MVC) Provides data
 * access operations for approval statuses, extending the standard CAbstractNamedRepository to inherit common CRUD and
 * query operations.
 */
public interface CApprovalStatusRepository extends CEntityOfProjectRepository<CApprovalStatus> {
    // Inherits standard operations from CAbstractNamedRepository Additional custom query
    // methods can be added here if needed
}