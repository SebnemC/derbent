package tech.derbent.companies.view;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;

/**
 * CCompanyView - View for managing companies
 * Layer: View (MVC)
 * Provides CRUD operations for CCompany entities with master-detail layout
 */
@Route("companies/:company_id?/:action?(edit)")
@PageTitle("Company Master Detail")
@Menu(order = 1, icon = "vaadin:building", title = "Settings.Companies")
@PermitAll // When security is enabled, allow all authenticated users
public class CCompanyView extends CAbstractMDPage<CCompany> {

    private static final long serialVersionUID = 1L;
    private final String ENTITY_ID_FIELD = "company_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "companies/%s/edit";

    /**
     * Constructor for CCompanyView
     * Annotated with @Autowired to let Spring inject dependencies
     * @param entityService the CCompanyService instance
     */
    @Autowired
    public CCompanyView(final CCompanyService entityService) {
        super(CCompany.class, entityService);
        LOGGER.info("CCompanyView constructor called with entityService: {}", entityService.getClass().getSimpleName());
        addClassNames("companies-view");
    }

    @Override
    protected void createDetailsLayout() {
        LOGGER.info("Creating details layout for CCompanyView");
        createEntityDetails();
    }

    protected void createEntityDetails() {
        LOGGER.info("Creating entity details for CCompanyView");
        
        try {
            // Create form using MetaData annotations and CEntityFormBuilder
            final var formLayout = CEntityFormBuilder.buildForm(CCompany.class, getBinder());
            getBaseDetailsLayout().add(formLayout);
            
            LOGGER.debug("Entity details created successfully for CCompanyView");
        } catch (final Exception e) {
            LOGGER.error("Error creating entity details for CCompanyView", e);
            showErrorNotification("Failed to create company form: " + e.getMessage());
        }
    }

    @Override
    protected void createGridForEntity() {
        LOGGER.info("Creating grid for companies");
        
        // Add columns for key company information
        grid.addColumn(CCompany::getName)
            .setAutoWidth(true)
            .setHeader("Company Name")
            .setSortable(true);
            
        grid.addColumn(CCompany::getDescription)
            .setAutoWidth(true)
            .setHeader("Description")
            .setSortable(false);
            
        grid.addColumn(CCompany::getAddress)
            .setAutoWidth(true)
            .setHeader("Address")
            .setSortable(false);
            
        grid.addColumn(CCompany::getPhone)
            .setAutoWidth(true)
            .setHeader("Phone")
            .setSortable(false);
            
        grid.addColumn(CCompany::getEmail)
            .setAutoWidth(true)
            .setHeader("Email")
            .setSortable(true);
            
        grid.addColumn(CCompany::getWebsite)
            .setAutoWidth(true)
            .setHeader("Website")
            .setSortable(false);
            
        grid.addColumn(company -> company.isEnabled() ? "Active" : "Inactive")
            .setAutoWidth(true)
            .setHeader("Status")
            .setSortable(true);
        
        // Set data provider using the entityService
        grid.setItems(query -> entityService
            .list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
    }

    @Override
    protected CButton createSaveButton(final String buttonText) {
        LOGGER.info("Creating enhanced save button for CCompanyView with custom company validation");
        return CButton.createPrimary(buttonText, com.vaadin.flow.component.icon.VaadinIcon.CHECK.create(), e -> {
            try {
                if (getCurrentEntity() == null) {
                    LOGGER.warn("No current entity set when save button clicked - this should not happen");
                    return;
                }
                
                // Additional validation for company-specific rules
                if (!validateCompanyData()) {
                    LOGGER.warn("Company data validation failed");
                    new CWarningDialog("Please check the company data and fix any validation errors.").open();
                    return;
                }
                
                // Determine if this is a new entity (no ID) or existing entity
                final boolean isNewEntity = getCurrentEntity().getId() == null;
                LOGGER.debug("Saving company entity - isNew: {}, entity: {}", isNewEntity, getCurrentEntity());
                
                getBinder().writeBean(getCurrentEntity());
                final CCompany savedEntity = (CCompany) entityService.save(getCurrentEntity());
                
                // Update current entity with saved version (for new entities, this will have the ID)
                setCurrentEntity(savedEntity);
                
                clearForm();
                refreshGrid();
                
                final String message = isNewEntity ? "New company created successfully" : "Company updated successfully";
                LOGGER.info("Company saved successfully: {} - {}", savedEntity.getName(), message);
                Notification.show(message);
                
                // Navigate back to the current view (list mode)
                UI.getCurrent().navigate(getClass());
            } catch (final ValidationException validationException) {
                LOGGER.error("Validation error while saving company", validationException);
                new CWarningDialog("Failed to save the data. Please check that all required fields are filled and values are valid.").open();
            } catch (final Exception exception) {
                LOGGER.error("Unexpected error during save operation", exception);
                new CWarningDialog("An unexpected error occurred while saving. Please try again.").open();
            }
        });
    }

    /**
     * Validates company-specific business rules
     * @return true if validation passes, false otherwise
     */
    private boolean validateCompanyData() {
        LOGGER.debug("validateCompanyData called for entity: {}", getCurrentEntity() != null ? getCurrentEntity().getId() : "null");
        
        if (getCurrentEntity() == null) {
            LOGGER.warn("Cannot validate null entity");
            return false;
        }
        
        // Check if company name is provided
        if (getCurrentEntity().getName() == null || getCurrentEntity().getName().trim().isEmpty()) {
            LOGGER.warn("Company name is required but not provided");
            showErrorNotification("Company name is required");
            return false;
        }
        
        // Check name uniqueness
        final CCompanyService companyService = (CCompanyService) entityService;
        if (!companyService.isNameUnique(getCurrentEntity().getName(), getCurrentEntity().getId())) {
            LOGGER.warn("Company name is not unique: {}", getCurrentEntity().getName());
            showErrorNotification("Company name already exists. Please choose a different name.");
            return false;
        }
        
        // Validate email format if provided
        if (getCurrentEntity().getEmail() != null && !getCurrentEntity().getEmail().trim().isEmpty()) {
            if (!isValidEmail(getCurrentEntity().getEmail())) {
                LOGGER.warn("Invalid email format: {}", getCurrentEntity().getEmail());
                showErrorNotification("Please enter a valid email address");
                return false;
            }
        }
        
        // Validate website URL format if provided
        if (getCurrentEntity().getWebsite() != null && !getCurrentEntity().getWebsite().trim().isEmpty()) {
            if (!isValidUrl(getCurrentEntity().getWebsite())) {
                LOGGER.warn("Invalid website URL format: {}", getCurrentEntity().getWebsite());
                showErrorNotification("Please enter a valid website URL (e.g., https://example.com)");
                return false;
            }
        }
        
        LOGGER.debug("Company data validation passed");
        return true;
    }

    /**
     * Validates email format using simple regex
     * @param email the email to validate
     * @return true if email format is valid
     */
    private boolean isValidEmail(final String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Empty email is valid (optional field)
        }
        
        final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Validates URL format
     * @param url the URL to validate
     * @return true if URL format is valid
     */
    private boolean isValidUrl(final String url) {
        if (url == null || url.trim().isEmpty()) {
            return true; // Empty URL is valid (optional field)
        }
        
        final String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
        return url.matches(urlRegex);
    }

    @Override
    protected void setupToolbar() {
        // Empty implementation as required by abstract parent
    }

    @Override
    protected String getEntityRouteIdField() { 
        return ENTITY_ID_FIELD; 
    }

    @Override
    protected String getEntityRouteTemplateEdit() { 
        return ENTITY_ROUTE_TEMPLATE_EDIT; 
    }

    @Override
    protected void initPage() {
        // Auto-generated method stub - can be used for additional page initialization
    }

    @Override
    protected CCompany newEntity() {
        LOGGER.debug("Creating new CCompany entity");
        final CCompany company = new CCompany();
        company.setEnabled(true); // Default to enabled
        return company;
    }

    @Override
    protected void populateForm(final CCompany value) {
        super.populateForm(value);
        LOGGER.info("Populating form with company data: {}", 
            value != null ? value.getName() : "null");
    }

    @Override
    protected Div createDetailsTabLeftContent() {
        // Create custom tab content for companies view
        final Div detailsTabLabel = new Div();
        detailsTabLabel.setText("Company Details");
        detailsTabLabel.setClassName("details-tab-label");
        return detailsTabLabel;
    }

    /**
     * Shows an error notification to the user
     * @param message the error message to display
     */
    private void showErrorNotification(final String message) {
        LOGGER.debug("showErrorNotification called with message: {}", message);
        
        if (message == null || message.trim().isEmpty()) {
            LOGGER.warn("Attempt to show error notification with null or empty message");
            return;
        }
        
        final Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
    }

    /**
     * Navigates to the company view
     * @param companyId the ID of the company to view (null for list view)
     */
    public static void navigateToCompany(final Long companyId) {
        if (companyId == null) {
            UI.getCurrent().navigate(CCompanyView.class);
        } else {
            UI.getCurrent().navigate("companies/" + companyId);
        }
    }

    /**
     * Navigates to the company edit view
     * @param companyId the ID of the company to edit
     */
    public static void navigateToCompanyEdit(final Long companyId) {
        if (companyId != null) {
            UI.getCurrent().navigate("companies/" + companyId + "/edit");
        }
    }
}