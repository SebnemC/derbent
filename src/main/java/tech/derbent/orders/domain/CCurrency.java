package tech.derbent.orders.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.orders.view.CCurrencyView;
import tech.derbent.projects.domain.CProject;

/** CCurrency - Domain entity representing currencies used in orders. Layer: Domain (MVC) Defines the various currencies that can be used in order
 * transactions, including currency code, symbol, and exchange rate information. This entity extends CEntityNamed and adds currency-specific fields
 * such as currency code (USD, EUR, etc.) and symbol ($, €, etc.). */
@Entity
@Table (name = "ccurrency")
@AttributeOverride (name = "id", column = @Column (name = "currency_id"))
public class CCurrency extends CEntityOfProject<CCurrency> {
	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return "#20c917"; // Teal color for order entities
	}

	public static String getIconFilename() { return "vaadin:dollar"; }

	public static Class<?> getViewClass() { return CCurrencyView.class; }

	@Column (name = "currency_code", nullable = false, length = 3, unique = false)
	@Size (max = 3, min = 3, message = "Currency code must be exactly 3 characters")
	@AMetaData (
			displayName = "Currency Code", required = true, readOnly = false, description = "ISO 4217 currency code (e.g., USD, EUR, GBP)",
			hidden = false, order = 2, maxLength = 3
	)
	private String currencyCode;
	@Column (name = "currency_symbol", nullable = true, length = 5)
	@Size (max = 5)
	@AMetaData (
			displayName = "Symbol", required = false, readOnly = false, description = "Currency symbol (e.g., $, €, £)", hidden = false, order = 3,
			maxLength = 5
	)
	private String currencySymbol;

	public CCurrency() {
		super(CCurrency.class, "New Currency", null);
	}

	public CCurrency(final CProject project, final String name) {
		super(CCurrency.class, name, project);
	}

	public String getCurrencyCode() { return currencyCode; }

	public String getCurrencySymbol() { return currencySymbol; }

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
		updateLastModified();
	}

	public void setCurrencySymbol(final String currencySymbol) {
		this.currencySymbol = currencySymbol;
		updateLastModified();
	}

	@Override
	public String toString() {
		return currencyCode != null ? currencyCode + " (" + getName() + ")" : super.toString();
	}
}
