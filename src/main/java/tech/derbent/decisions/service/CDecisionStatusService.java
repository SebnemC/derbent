package tech.derbent.decisions.service;

import java.time.Clock;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionStatusService - Service class for CDecisionStatus entities. Layer: Service
 * (MVC) Provides business logic operations for decision status management including
 * validation, creation, and workflow management.
 */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionStatusService extends CAbstractNamedEntityService<CDecisionStatus> {

	public CDecisionStatusService(final CDecisionStatusRepository repository,
		final Clock clock) {
		super(repository, clock);
	}

	/**
	 * Creates a new decision status with basic properties.
	 * @param name        the decision status name - must not be null or empty
	 * @param description the description - can be null
	 * @param isFinal     whether this is a final status
	 * @return the created decision status
	 */
	@Transactional
	public CDecisionStatus createDecisionStatus(final String name, final CProject project,
		final String description, final boolean isFinal) {
		LOGGER.info(
			"createDecisionStatus called with name: {}, description: {}, isFinal: {}",
			name, description, isFinal);

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.error("createDecisionStatus called with null or empty name");
			throw new IllegalArgumentException(
				"Decision status name cannot be null or empty");
		}
		final CDecisionStatus decisionStatus = new CDecisionStatus(name.trim(), project);
		decisionStatus.setFinal(isFinal);
		decisionStatus.setDescription(description);
		return repository.saveAndFlush(decisionStatus);
	}

	/**
	 * Creates a new decision status with all properties.
	 * @param name             the decision status name - must not be null or empty
	 * @param description      the description - can be null
	 * @param color            the hex color code - can be null
	 * @param isFinal          whether this is a final status
	 * @param allowsEditing    whether decisions with this status can be edited
	 * @param requiresApproval whether decisions with this status require approval
	 * @return the created decision status
	 */
	@Transactional
	public CDecisionStatus createDecisionStatus(final String name, final CProject project,
		final String description, final String color, final boolean isFinal,
		final boolean requiresApproval) {
		LOGGER.info(
			"createDecisionStatus called with name: {}, description: {}, color: {}, isFinal: {}, requiresApproval: {}",
			name, description, color, isFinal, requiresApproval);

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.error("createDecisionStatus called with null or empty name");
			throw new IllegalArgumentException(
				"Decision status name cannot be null or empty");
		}
		final CDecisionStatus decisionStatus = new CDecisionStatus(name.trim(), project,
			description, color, isFinal, requiresApproval);
		return repository.saveAndFlush(decisionStatus);
	}

	@Override
	protected CDecisionStatus createNewEntityInstance() {
		return new CDecisionStatus();
	}

	/**
	 * Finds all non-final decision statuses ordered by sort order.
	 * @return list of non-final decision statuses sorted by sort order
	 */
	@Transactional (readOnly = true)
	public List<CDecisionStatus> findAllActiveOrdered() {
		LOGGER.info("findAllActiveOrdered called for decision statuses");
		return ((CDecisionStatusRepository) repository)
			.findByIsFinalFalseOrderBySortOrderAsc();
	}

	/**
	 * Finds all final decision statuses.
	 * @return list of final decision statuses
	 */
	@Transactional (readOnly = true)
	public List<CDecisionStatus> findAllFinal() {
		LOGGER.info("findAllFinal called for decision statuses");
		return ((CDecisionStatusRepository) repository).findByIsFinalTrue();
	}

	/**
	 * Finds all decision statuses ordered by sort order.
	 * @return list of decision statuses sorted by sort order
	 */
	@Transactional (readOnly = true)
	public List<CDecisionStatus> findAllOrdered() {
		LOGGER.info("findAllOrdered called for decision statuses");
		return ((CDecisionStatusRepository) repository).findAllByOrderBySortOrderAsc();
	}

	/**
	 * Finds decision statuses that require approval.
	 * @return list of decision statuses that require approval
	 */
	@Transactional (readOnly = true)
	public List<CDecisionStatus> findRequiringApproval() {
		LOGGER.info("findRequiringApproval called for decision statuses");
		return ((CDecisionStatusRepository) repository).findByRequiresApprovalTrue();
	}

	/**
	 * Checks if this status indicates completion of the decision process.
	 * @param decisionStatus the decision status to check
	 * @return true if this is a final status
	 */
	@Transactional (readOnly = true)
	public boolean isStatusCompleted(final CDecisionStatus decisionStatus) {
		LOGGER.info("isStatusCompleted called with decisionStatus: {}",
			decisionStatus != null ? decisionStatus.getName() : "null");

		if (decisionStatus == null) {
			LOGGER.warn("isStatusCompleted called with null decisionStatus");
			return false;
		}
		return decisionStatus.isCompleted();
	}

	/**
	 * Checks if decisions with this status are pending approval.
	 * @param decisionStatus the decision status to check
	 * @return true if approval is required and status is not final
	 */
	@Transactional (readOnly = true)
	public boolean isStatusPendingApproval(final CDecisionStatus decisionStatus) {
		LOGGER.info("isStatusPendingApproval called with decisionStatus: {}",
			decisionStatus != null ? decisionStatus.getName() : "null");

		if (decisionStatus == null) {
			LOGGER.warn("isStatusPendingApproval called with null decisionStatus");
			return false;
		}
		return decisionStatus.isPendingApproval();
	}

	/**
	 * Updates the sort order of a decision status.
	 * @param decisionStatus the decision status to update - must not be null
	 * @param sortOrder      the new sort order
	 * @return the updated decision status
	 */
	@Transactional
	public CDecisionStatus updateSortOrder(final CDecisionStatus decisionStatus,
		final Integer sortOrder) {
		LOGGER.info("updateSortOrder called with decisionStatus: {}, sortOrder: {}",
			decisionStatus != null ? decisionStatus.getName() : "null", sortOrder);

		if (decisionStatus == null) {
			LOGGER.error("updateSortOrder called with null decisionStatus");
			throw new IllegalArgumentException("Decision status cannot be null");
		}
		decisionStatus.setSortOrder(sortOrder);
		return repository.saveAndFlush(decisionStatus);
	}

	/**
	 * Updates the properties of a decision status.
	 * @param decisionStatus   the decision status to update - must not be null
	 * @param allowsEditing    whether decisions with this status can be edited
	 * @param requiresApproval whether decisions with this status require approval
	 * @return the updated decision status
	 */
	@Transactional
	public CDecisionStatus updateStatusProperties(final CDecisionStatus decisionStatus,
		final boolean requiresApproval) {
		LOGGER.info(
			"updateStatusProperties called with decisionStatus: {}, requiresApproval: {}",
			decisionStatus != null ? decisionStatus.getName() : "null", requiresApproval);

		if (decisionStatus == null) {
			LOGGER.error("updateStatusProperties called with null decisionStatus");
			throw new IllegalArgumentException("Decision status cannot be null");
		}
		decisionStatus.setRequiresApproval(requiresApproval);
		return repository.saveAndFlush(decisionStatus);
	}
}