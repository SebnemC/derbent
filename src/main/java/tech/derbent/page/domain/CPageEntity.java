package tech.derbent.page.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CProjectItem;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.page.view.CPageEntityView;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cpageentity")
@AttributeOverride (name = "id", column = @Column (name = "pageentity_id"))
public class CPageEntity extends CProjectItem<CPageEntity> {

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() { return "#207bff"; }

	public static String getStaticIconFilename() { return "vaadin:tasks"; }

	public static Class<? extends CAbstractEntityDBPage<?>> getViewClassStatic() { return CPageEntityView.class; }

	@Column (nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Icon method/File", required = true, readOnly = false, defaultValue = "class:tech.derbent.meetings.view.CMeetingsView",
			description = "class:tech.derbent.meetings.view.CMeetingsView", hidden = false, order = 70, maxLength = 100
	)
	private String icon;
	@Column (nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Menu Order", required = true, readOnly = false, defaultValue = "1.1", description = "Menu Order", hidden = false,
			order = 70, maxLength = 100
	)
	private String menuOrder;
	@Column (nullable = false, length = 100, unique = true)
	@Size (max = 100)
	@AMetaData (
			displayName = "Page Title", required = true, readOnly = false, defaultValue = "Title of Page", description = "Title of Page",
			hidden = false, order = 70, maxLength = 100
	)
	private String pageTitle;
	@Column (nullable = false, length = 100, unique = true)
	@Size (max = 100)
	@AMetaData (
			displayName = "Page route", required = true, readOnly = false, defaultValue = "", description = "Page section to be displayed under",
			hidden = false, order = 70, maxLength = 100
	)
	private String route;
	@Column (nullable = false, length = 100, unique = true)
	@Size (max = 100)
	@AMetaData (
			displayName = "Title", required = true, readOnly = false, defaultValue = "Project.Page",
			description = "Use like, Project.Page, separate parent with . ", hidden = false, order = 70, maxLength = 100
	)
	private String title;
	@Column (nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Security Permissions", required = false, readOnly = false, defaultValue = "PermitAll",
			description = "Security permissions: PermitAll, RolesAllowed(...), or empty for anonymous", hidden = false, order = 80, maxLength = 500
	)
	private String securityPermissions;
	@Column (nullable = true, length = 200)
	@Size (max = 200)
	@AMetaData (
			displayName = "Parent Menu", required = false, readOnly = false, defaultValue = "",
			description = "Parent menu path for hierarchical navigation (e.g., 'Project', 'Settings')", hidden = false, order = 90, maxLength = 200
	)
	private String parentMenu;
	@Column (nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Order Priority", required = false, readOnly = false, defaultValue = "1.0",
			description = "Menu ordering priority (decimal number)", hidden = false, order = 100, maxLength = 50
	)
	private String orderPriority;
	@Column (nullable = true, length = 300)
	@Size (max = 300)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false, defaultValue = "", description = "Page description for documentation",
			hidden = false, order = 110, maxLength = 300
	)
	private String pageDescription;
	@Column (nullable = true)
	@AMetaData (
			displayName = "Enabled", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this page is enabled and accessible", hidden = false, order = 120
	)
	private Boolean enabled;

	/** Default constructor for JPA. */
	public CPageEntity() {
		super();
		initializeDefaults();
	}

	public CPageEntity(final String name, final CProject project) {
		super(CPageEntity.class, name, project);
		initializeDefaults();
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return getName();
	}

	public String getIcon() { return icon; }

	public String getMenuOrder() { return menuOrder; }

	public String getPageTitle() { return pageTitle; }

	public String getRoute() { return route; }

	public String getTitle() { return title; }

	public String getSecurityPermissions() { return securityPermissions; }

	public String getParentMenu() { return parentMenu; }

	public String getOrderPriority() { return orderPriority; }

	public String getPageDescription() { return pageDescription; }

	public Boolean getEnabled() { return enabled; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		icon = getStaticIconFilename();
		menuOrder = "1.1";
		// must be unique
		route = "UnknownHTTPRoute";
		// must be unique
		title = "Unknown.NewPage";
		securityPermissions = "PermitAll";
		parentMenu = "";
		orderPriority = "1.0";
		pageDescription = "";
		enabled = Boolean.TRUE;
	}

	public void setIcon(String icon) { this.icon = icon; }

	public void setMenuOrder(String menuOrder) { this.menuOrder = menuOrder; }

	public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

	public void setRoute(final String route) { this.route = route; }

	public void setTitle(String title) { this.title = title; }

	public void setSecurityPermissions(String securityPermissions) { this.securityPermissions = securityPermissions; }

	public void setParentMenu(String parentMenu) { this.parentMenu = parentMenu; }

	public void setOrderPriority(String orderPriority) { this.orderPriority = orderPriority; }

	public void setPageDescription(String pageDescription) { this.pageDescription = pageDescription; }

	public void setEnabled(Boolean enabled) { this.enabled = enabled; }

	@Override
	public Class<? extends CAbstractEntityDBPage<?>> getViewClass() { // TODO Auto-generated method stub
		return CPageEntity.getViewClassStatic();
	}
}
