package tech.derbent.users.tests;

import tech.derbent.abstracts.tests.CGenericViewTest;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.view.CUsersView;

/**
 * CUsersViewGenericTest - Users view test using generic superclass
 * Inherits all common test patterns: navigation, view loading, new item creation,
 * grid interactions, accessibility, and ComboBox testing.
 * 
 * Uses class annotations and metadata instead of magic strings.
 * Only takes screenshots on test failures to reduce overhead.
 */
public class CUsersViewGenericTest extends CGenericViewTest<CUser> {

	@Override
	protected Class<?> getViewClass() {
		return CUsersView.class;
	}

	@Override
	protected Class<CUser> getEntityClass() {
		return CUser.class;
	}
}