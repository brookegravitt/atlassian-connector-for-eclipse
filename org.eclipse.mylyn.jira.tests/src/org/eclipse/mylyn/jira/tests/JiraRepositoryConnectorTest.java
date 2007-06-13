/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.context.core.ContextCorePlugin;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.ITaskDataHandler;
import org.eclipse.mylyn.tasks.core.RepositoryAttachment;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public class JiraRepositoryConnectorTest extends TestCase {

	private TaskRepository repository;

	private TaskRepositoryManager manager;

	private AbstractRepositoryConnector connector;

	private ITaskDataHandler dataHandler;
	
	private JiraClient server;

	private String customFieldId;

	private String customFieldName;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
		
		JiraClientFacade.getDefault().clearClients();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void init(String url) throws Exception {
		String kind = JiraUiPlugin.REPOSITORY_KIND;

		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.ADMIN);
		repository = new TaskRepository(kind, url);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		server = JiraClientFacade.getDefault().getJiraClient(repository);

		customFieldName = "Free Text";
		customFieldId = JiraTestUtils.getCustomField(server, customFieldName);
		assertNotNull("Unable to find custom field id", customFieldId);
		
		credentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		repository = new TaskRepository(kind, url);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);

		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());

		connector = manager.getRepositoryConnector(kind);
		assertEquals(connector.getRepositoryType(), kind);

		TasksUiPlugin.getSynchronizationManager().setForceSyncExec(true);

		dataHandler = connector.getTaskDataHandler();

		server = JiraClientFacade.getDefault().getJiraClient(repository);
	}

	public void testChangeTaskRepositorySettings() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);
		assertEquals(repository.getUserName(), repository.getUserName());

		EditRepositoryWizard wizard = new EditRepositoryWizard(repository);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();

		wizard.getSettingsPage().setUserId("newuser");
		assertTrue(wizard.performFinish());

		server = JiraClientFacade.getDefault().getJiraClient(repository);
		assertEquals("newuser", server.getUserName());
	}

	public void testUpdateTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);
		
		Issue issue = JiraTestUtils.createIssue(server, "testUpdateTask");
		AbstractTask task = connector.createTaskFromExistingId(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(false, task.isCompleted());
		assertNull(task.getDueDate());

		String issueKey = issue.getKey();
		
		RepositoryTaskData taskData;
		
		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		taskData.addAttributeValue(customFieldId, "foo");
		taskData.addAttributeValue(RepositoryTaskAttribute.COMMENT_NEW, "add comment");
		RepositoryOperation leaveOperation = new RepositoryOperation("leave", "");
		taskData.addOperation(leaveOperation);
		taskData.setSelectedOperation(leaveOperation);
		
//		issue = server.getIssueByKey(issueKey);
//		issue.setCustomFields(new CustomField[] { new CustomField(fieldId, //
//				"com.atlassian.jira.plugin.system.customfieldtypes:textfield", // 
//				fieldName, Collections.singletonList("foo")) });
//		server.updateIssue(issue, "add comment");
		
		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());

		issue = server.getIssueByKey(issueKey);
		assertCustomField(issue, customFieldId, customFieldName, "foo");
		
		{
			String operation = JiraTestUtils.getOperation(server, issue.getKey(), "resolve");
			assertNotNull("Unable to find id for resolve operation", operation);

//			Map<String, String[]> params = new HashMap<String, String[]>();
//			params.put(JiraAttribute.RESOLUTION.getParamName(), new String[] {JiraTestUtils.getFixedResolution(server).getId()});
//			params.put(JiraAttribute.COMMENT_NEW.getParamName(), new String[] {"comment"});
//			
//			// server.resolveIssue(issue, JiraTestUtils.getFixedResolution(server), null, "comment", JiraClient.ASSIGNEE_DEFAULT, "");
//			server.advanceIssueWorkflow(issue, operation, params);
			
			taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
			taskData.setAttributeValue(RepositoryTaskAttribute.RESOLUTION, JiraTestUtils.getFixedResolution(server).getId());
			taskData.setAttributeValue(RepositoryTaskAttribute.COMMENT_NEW, "comment");

			RepositoryOperation resolveOperation = new RepositoryOperation(operation, "resolve");
			taskData.addOperation(resolveOperation);
			taskData.setSelectedOperation(resolveOperation);
			dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());
		}
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(true, task.isCompleted());
		assertNotNull(task.getCompletionDate());

		issue = server.getIssueByKey(issueKey);
		assertCustomField(issue, customFieldId, customFieldName, "foo");
		
		{		
			String operation = JiraTestUtils.getOperation(server, issue.getKey(), "close");
			assertNotNull("Unable to find id for close operation", operation);

//			Map<String, String[]> params = new HashMap<String, String[]>();
//			params.put(JiraAttribute.RESOLUTION.getParamName(), new String[] {JiraTestUtils.getFixedResolution(server).getId()});
//			params.put(JiraAttribute.COMMENT_NEW.getParamName(), new String[] {"comment"});
//			
//			// server.closeIssue(issue, JiraTestUtils.getFixedResolution(server), null, "comment", JiraClient.ASSIGNEE_DEFAULT, "");
//			server.advanceIssueWorkflow(issue, operation, params);
			
			taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
			taskData.setAttributeValue(RepositoryTaskAttribute.RESOLUTION, JiraTestUtils.getFixedResolution(server).getId());
			taskData.setAttributeValue(RepositoryTaskAttribute.COMMENT_NEW, "comment");

			RepositoryOperation resolveOperation = new RepositoryOperation(operation, "close");
			taskData.addOperation(resolveOperation);
			taskData.setSelectedOperation(resolveOperation);
			dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());
		}
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(true, task.isCompleted());
		assertNotNull(task.getCompletionDate());

		issue = server.getIssueByKey(issueKey);
		assertCustomField(issue, customFieldId, customFieldName, "foo");
	}

	private void assertCustomField(Issue issue, String fieldId, String fieldName, String value) {
		CustomField customField;
		customField = issue.getCustomFieldById(fieldId);
		assertNotNull("Expecting to see custom field " + fieldName, customField);
		assertEquals(fieldName, customField.getName());
		assertEquals(1, customField.getValues().size());
		assertEquals(value, customField.getValues().get(0));
	}

	public void testAttachContext() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Issue issue = JiraTestUtils.createIssue(server, "testAttachContext");
		AbstractTask task = connector.createTaskFromExistingId(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals("testAttachContext", task.getSummary());

		File sourceContextFile = ContextCorePlugin.getContextManager().getFileForContext(task.getHandleIdentifier());
		JiraTestUtils.writeFile(sourceContextFile, "Mylar".getBytes());
		sourceContextFile.deleteOnExit();

		assertTrue(connector.attachContext(repository, task, "", new NullProgressMonitor()));
		
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);

		Set<RepositoryAttachment> contextAttachments = connector.getContextAttachments(repository, task);
		assertEquals(1, contextAttachments.size());
		
		RepositoryAttachment attachment = contextAttachments.iterator().next();
		assertTrue(connector.retrieveContext(repository, task, attachment, System.getProperty("java.io.tmpdir"), new NullProgressMonitor()));
	}
	
}
