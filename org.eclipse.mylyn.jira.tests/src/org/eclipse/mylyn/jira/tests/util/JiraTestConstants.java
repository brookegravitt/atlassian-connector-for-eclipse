/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.util;

/**
 * @author Steffen Pingel
 */
public class JiraTestConstants {

	public static final String SERVER = System.getProperty("mylyn.jira.server", "mylyn.eclipse.org");

	public static final String JIRA_LATEST_URL = getServerUrl("jira-enterprise-3.13.1");

	public static final String JIRA_3_13_1_URL = getServerUrl("jira-enterprise-3.13.1");

	public static final String JIRA_BASIC_AUTH_URL = getServerUrl("jira-basic-auth");

	public static final String JIRA_4_0_0_URL = getServerUrl("jira-enterprise-4.0.0");

	private static final String getServerUrl(String version) {
		return "http://" + SERVER + "/" + version;
	}

}
