package tech.derbent.abstracts.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.formlayout.FormLayout;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.utils.CPanelDetails;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CScreenService;

@org.springframework.stereotype.Component
public final class CDetailsBuilder implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDetailsBuilder.class);

	public static ApplicationContext getApplicationContext() { return applicationContext; }

	private static Component processLine(final int counter, final CScreen screen, final CScreenLines line) {
		Check.notNull(line, "Line cannot be null");
		if (line.getRelationFieldName().equals(CEntityFieldService.SECTION)) {
			final CPanelDetails sectionPanel = new CPanelDetails(line.getSectionName(), line.getFieldCaption());
			return sectionPanel;
		}
		return null;
	}

	private HasComponents formLayout;
	private final Map<String, CPanelDetails> mapSectionPanels;
	CEntityFormBuilder<?> formBuilder = null;

	public CDetailsBuilder() {
		this.mapSectionPanels = new HashMap<>();
	}

	public HasComponents buildDetails(CScreen screen, final CEnhancedBinder<?> binder, final HasComponents layout) throws Exception {
		Check.notNull(screen, "Screen cannot be null");
		Check.notNull(binder, "Binder cannot be null");
		Check.notNull(applicationContext, "Details name cannot be null");
		if (layout != null) {
			formLayout = layout;
		} else {
			formLayout = new FormLayout();
		}
		final CScreenService screenService = applicationContext.getBean(CScreenService.class);
		Check.notNull(screenService, "Screen service cannot be null");
		// for lazy loading of screen lines
		final PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		if (!persistenceUtil.isLoaded(screen, "screenLines")) {
			screen = screenService.findByIdWithScreenLines(screen.getId());
		}
		if ((screen.getScreenLines() == null) || screen.getScreenLines().isEmpty()) {
			LOGGER.warn("No lines found for screen: {}", screen.getName());
			return new FormLayout(); // Return an empty layout if no lines are present
		}
		final Class<?> screenClass = CEntityFieldService.getEntityClass(screen.getEntityType());
		Check.notNull(screenClass, "Screen class cannot be null");
		formBuilder = new CEntityFormBuilder<>(screenClass, binder);
		//
		CPanelDetails currentSection = null;
		final int counter = 0;
		// screen.getScreenLines().size(); // Ensure lines are loaded
		final List<CScreenLines> lines = screen.getScreenLines();
		for (final CScreenLines line : lines) {
			if (line.getRelationFieldName().equals(CEntityFieldService.SECTION)) {
				// no more current section. switch to base
				currentSection = null;
			}
			if (currentSection != null) {
				currentSection.processLine(counter, screen, line, formBuilder);
				continue;
			}
			final Component component = processLine(counter, screen, line);
			if (component instanceof CPanelDetails) {
				formLayout.add(component);
				currentSection = (CPanelDetails) component;
				mapSectionPanels.put(currentSection.getName(), currentSection);
			}
		}
		return formLayout;
	}

	public Component getComponentByName(final String panelName, final String componentName) {
		final CPanelDetails panel = getSectionPanel(panelName);
		Check.notNull(panel, "Panel cannot be null");
		return panel.getComponentByName(componentName);
	}

	public CPanelDetails getSectionPanel(final String sectionName) {
		Check.notNull(sectionName, "Section name cannot be null");
		return mapSectionPanels.get(sectionName);
	}

	/** Sets the application context and initializes the data provider resolver. This method is called automatically by Spring.
	 * @param context the Spring application context */
	@Override
	public void setApplicationContext(final ApplicationContext context) {
		// Store the application context for String data provider resolution
		CDetailsBuilder.applicationContext = context;
	}
}
