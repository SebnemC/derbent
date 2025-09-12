package tech.derbent.comments.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CEvent;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.users.domain.CUser;

/** CComment - Domain entity representing user comments on activities. Layer: Domain (MVC) Inherits from CEvent to provide event-based functionality
 * with audit fields. Comments are linked to activities and contain: - Command text (comment content) - Author information (inherited from CEvent) -
 * Date/timestamp (inherited from CEvent) - Priority level - Activity reference - Project context (inherited from CEvent) Comments are displayed in
 * historic order within activity views. */
@Entity
@Table (name = "ccomment")
@AttributeOverride (name = "id", column = @Column (name = "comment_id"))
public class CComment extends CEvent<CComment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComment.class);
	// Comment text content
	@Column (name = "comment_text", nullable = false, length = 4000)
	@Size (max = 4000)
	@AMetaData (
			displayName = "Comment Text", required = true, readOnly = false, description = "The comment content text", hidden = false, order = 1,
			maxLength = 4000
	)
	private String commentText;
	// Activity this comment belongs to
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "activity_id", nullable = false)
	@OnDelete (action = OnDeleteAction.CASCADE) // Ensures DB-level cascade delete
	@AMetaData (
			displayName = "Activity", required = true, readOnly = false, description = "Activity this comment belongs to", hidden = false, order = 2,
			dataProviderBean = "CActivityService"
	)
	private CActivity activity;
	// Priority of the comment
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "priority_id", nullable = true)
	@AMetaData (
			displayName = "Priority", required = false, readOnly = false, description = "Priority level of this comment", hidden = false, order = 3,
			dataProviderBean = "CCommentPriorityService"
	)
	private CCommentPriority priority;
	// Flag for important comments
	@Column (name = "is_important", nullable = false)
	@AMetaData (
			displayName = "Important", required = false, readOnly = false, defaultValue = "false", description = "Mark this comment as important",
			hidden = false, order = 4
	)
	private Boolean important = Boolean.FALSE;

	/** Default constructor for JPA. */
	public CComment() {
		super();
		// Initialize with default values for JPA
		this.important = false;
	}

	/** Constructor with comment text, activity, and author.
	 * @param commentText the comment content text - must not be null or empty
	 * @param activity    the activity this comment belongs to - must not be null
	 * @param author      the user who created this comment - must not be null */
	public CComment(final String commentText, final CActivity activity, final CUser author) {
		super(CComment.class);
		setAuthor(author);
		this.commentText = commentText;
		this.activity = activity;
	}

	/** Constructor with comment text, activity, author, and priority.
	 * @param commentText the comment content text - must not be null or empty
	 * @param activity    the activity this comment belongs to - must not be null
	 * @param author      the user who created this comment - must not be null
	 * @param priority    the priority level of this comment */
	public CComment(final String commentText, final CActivity activity, final CUser author, final CCommentPriority priority) {
		this(commentText, activity, author);
		this.priority = priority;
	}

	public CActivity getActivity() { return activity; }

	public String getActivityName() {
		if (activity == null) {
			return "No Activity";
		}
		try {
			// Safe access to avoid LazyInitializationException
			return activity.getName();
		} catch (final org.hibernate.LazyInitializationException e) {
			LOGGER.debug("LazyInitializationException accessing activity name, returning safe value", e);
			return "Activity#" + (activity.getId() != null ? activity.getId() : "unknown");
		}
	}

	/** Get a short preview of the comment text (first 100 characters).
	 * @return short preview of comment text */
	public String getCommentPreview() {
		if (commentText == null) {
			return "";
		}
		return commentText.length() > 100 ? commentText.substring(0, 100) + "..." : commentText;
	}

	public String getCommentText() { return commentText; }

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}

	public CCommentPriority getPriority() { return priority; }

	public String getPriorityName() { return (priority != null) ? priority.getName() : "Normal"; }

	@Override
	public Class<? extends CAbstractEntityDBPage<?>> getViewClass() { return null; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (this.commentText == null) {
			this.commentText = "";
		}
	}

	public Boolean isImportant() { return important; }

	public void setActivity(final CActivity activity) {
		Check.notNull(activity, "Activity cannot be null");
		this.activity = activity;
	}

	public void setCommentText(final String commentText) {
		Check.notBlank(commentText, "Comment text cannot be null or empty");
		this.commentText = commentText;
	}

	public void setImportant(final Boolean important) { this.important = important; }

	public void setPriority(final CCommentPriority priority) { this.priority = priority; }

	@Override
	public String toString() {
		return String.format("CComment{id=%d, activity=%s, author=%s, preview=%s}", getId(), getActivityName(), getAuthorName(), getCommentPreview());
	}
}
