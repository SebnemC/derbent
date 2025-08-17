package tech.derbent.decisions.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.domain.CDecisionApproval;
import tech.derbent.users.domain.CUser;

/**
 * CDecisionApprovalService - Service class for CDecisionApproval entities. Layer: Service (MVC) Provides business logic
 * operations for decision approval management including validation, creation, approval workflow operations, and
 * user-based queries.
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CDecisionApprovalService extends CAbstractNamedEntityService<CDecisionApproval> {

    public CDecisionApprovalService(final CDecisionApprovalRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Approves an approval with optional comments.
     * 
     * @param approval
     *            the approval to approve - must not be null
     * @param comments
     *            optional approval comments
     * @return the updated approval
     */
    @Transactional
    public CDecisionApproval approveApproval(final CDecisionApproval approval, final String comments) {

        if (approval == null) {
            LOGGER.error("approveApproval called with null approval");
            throw new IllegalArgumentException("Approval cannot be null");
        }
        approval.approve(comments);
        return repository.saveAndFlush(approval);
    }

    /**
     * Counts approved approvals for a specific decision.
     * 
     * @param decision
     *            the decision
     * @return count of approved approvals
     */
    @Transactional(readOnly = true)
    public long countApprovedApprovals(final CDecision decision) {

        if (decision == null) {
            LOGGER.warn("countApprovedApprovals called with null decision");
            return 0;
        }
        return ((CDecisionApprovalRepository) repository).countByDecisionAndIsApprovedTrue(decision);
    }

    /**
     * Counts pending approvals for a specific decision.
     * 
     * @param decision
     *            the decision
     * @return count of pending approvals
     */
    @Transactional(readOnly = true)
    public long countPendingApprovals(final CDecision decision) {

        if (decision == null) {
            LOGGER.warn("countPendingApprovals called with null decision");
            return 0;
        }
        return ((CDecisionApprovalRepository) repository).countByDecisionAndIsApprovedIsNull(decision);
    }

    /**
     * Finds all approvals assigned to a specific user.
     * 
     * @param approver
     *            the approver user
     * @return list of approvals assigned to the user
     */
    @Transactional(readOnly = true)
    public List<CDecisionApproval> findByApprover(final CUser approver) {

        if (approver == null) {
            LOGGER.warn("findByApprover called with null approver");
            return List.of();
        }
        return ((CDecisionApprovalRepository) repository).findByApprover(approver);
    }

    /**
     * Finds all approvals for a specific decision.
     * 
     * @param decision
     *            the decision
     * @return list of approvals for the decision
     */
    @Transactional(readOnly = true)
    public List<CDecisionApproval> findByDecision(final CDecision decision) {

        if (decision == null) {
            LOGGER.warn("findByDecision called with null decision");
            return List.of();
        }
        return ((CDecisionApprovalRepository) repository).findByDecision(decision);
    }

    /**
     * Finds approvals for a decision with eagerly loaded approver.
     * 
     * @param decision
     *            the decision
     * @return list of approvals with loaded approver
     */
    @Transactional(readOnly = true)
    public List<CDecisionApproval> findByDecisionWithApprover(final CDecision decision) {

        if (decision == null) {
            LOGGER.warn("findByDecisionWithApprover called with null decision");
            return List.of();
        }
        return ((CDecisionApprovalRepository) repository).findByDecisionWithApprover(decision);
    }

    /**
     * Finds overdue approvals for a specific user.
     * 
     * @param approver
     *            the approver user
     * @return list of overdue approvals for the user
     */
    @Transactional(readOnly = true)
    public List<CDecisionApproval> findOverdueApprovalsByUser(final CUser approver) {

        if (approver == null) {
            LOGGER.warn("findOverdueApprovalsByUser called with null approver");
            return List.of();
        }
        return ((CDecisionApprovalRepository) repository).findOverdueApprovalsByApprover(approver);
    }

    /**
     * Finds pending approvals for a specific user.
     * 
     * @param approver
     *            the approver user
     * @return list of pending approvals for the user
     */
    @Transactional(readOnly = true)
    public List<CDecisionApproval> findPendingApprovalsByUser(final CUser approver) {

        if (approver == null) {
            LOGGER.warn("findPendingApprovalsByUser called with null approver");
            return List.of();
        }
        return ((CDecisionApprovalRepository) repository).findByApproverAndIsApprovedIsNull(approver);
    }

    @Override
    protected Class<CDecisionApproval> getEntityClass() {
        return CDecisionApproval.class;
    }

    /**
     * Checks if an approval is overdue.
     * 
     * @param approval
     *            the approval to check
     * @return true if due date has passed and approval is still pending
     */
    @Transactional(readOnly = true)
    public boolean isApprovalOverdue(final CDecisionApproval approval) {

        if (approval == null) {
            LOGGER.warn("isApprovalOverdue called with null approval");
            return false;
        }
        return approval.isOverdue();
    }

    /**
     * Rejects an approval with optional comments.
     * 
     * @param approval
     *            the approval to reject - must not be null
     * @param comments
     *            optional rejection comments
     * @return the updated approval
     */
    @Transactional
    public CDecisionApproval rejectApproval(final CDecisionApproval approval, final String comments) {

        if (approval == null) {
            LOGGER.error("rejectApproval called with null approval");
            throw new IllegalArgumentException("Approval cannot be null");
        }
        approval.reject(comments);
        return repository.saveAndFlush(approval);
    }

    /**
     * Resets an approval to pending state.
     * 
     * @param approval
     *            the approval to reset - must not be null
     * @return the updated approval
     */
    @Transactional
    public CDecisionApproval resetApprovalToPending(final CDecisionApproval approval) {

        if (approval == null) {
            LOGGER.error("resetApprovalToPending called with null approval");
            throw new IllegalArgumentException("Approval cannot be null");
        }
        approval.resetToPending();
        return repository.saveAndFlush(approval);
    }

    /**
     * Updates the due date of an approval.
     * 
     * @param approval
     *            the approval to update - must not be null
     * @param dueDate
     *            the new due date - can be null
     * @return the updated approval
     */
    @Transactional
    public CDecisionApproval updateDueDate(final CDecisionApproval approval, final LocalDateTime dueDate) {

        if (approval == null) {
            LOGGER.error("updateDueDate called with null approval");
            throw new IllegalArgumentException("Approval cannot be null");
        }
        approval.setDueDate(dueDate);
        return repository.saveAndFlush(approval);
    }

    /**
     * Updates the priority of an approval.
     * 
     * @param approval
     *            the approval to update - must not be null
     * @param priority
     *            the new priority (1=Critical, 5=Optional)
     * @return the updated approval
     */
    @Transactional
    public CDecisionApproval updatePriority(final CDecisionApproval approval, final Integer priority) {
        LOGGER.info("updatePriority called with approval: {}, priority: {}",
                approval != null ? approval.getName() : "null", priority);

        if (approval == null) {
            LOGGER.error("updatePriority called with null approval");
            throw new IllegalArgumentException("Approval cannot be null");
        }
        approval.setApprovalPriority(priority);
        return repository.saveAndFlush(approval);
    }
}