/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class RepositoryConfigurationAction extends Action implements IMenuCreator {
	private Menu menu;

	@Override
	public void run() {
		Display.getDefault().asyncExec(new Runnable() {
			@SuppressWarnings("restriction")
			public void run() {
				NewRepositoryWizard repositoryWizard = new NewRepositoryWizard(BambooCorePlugin.CONNECTOR_KIND);

				WizardDialog repositoryDialog = new TaskRepositoryWizardDialog(null, repositoryWizard);
				repositoryDialog.create();
				repositoryDialog.getShell().setText("Add New Bamboo Repository...");
				repositoryDialog.setBlockOnOpen(true);
				repositoryDialog.open();
			}
		});
	}

	public void dispose() {
		if (menu != null) {
			menu.dispose();
			menu = null;
		}
	}

	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		addActions();
		return menu;
	}

	private void addActions() {
		// add repository action
		Action addRepositoryAction = new Action() {
			@SuppressWarnings("restriction")
			@Override
			public void run() {
				NewRepositoryWizard repositoryWizard = new NewRepositoryWizard(BambooCorePlugin.CONNECTOR_KIND);

				WizardDialog repositoryDialog = new TaskRepositoryWizardDialog(null, repositoryWizard);
				repositoryDialog.create();
				repositoryDialog.getShell().setText("Add New Bamboo Repository...");
				repositoryDialog.setBlockOnOpen(true);
				repositoryDialog.open();
			}
		};
		ActionContributionItem addRepoACI = new ActionContributionItem(addRepositoryAction);
		addRepositoryAction.setText("Add New Repository...");
		addRepositoryAction.setImageDescriptor(BambooImages.ADD_REPOSITORY);
		addRepoACI.fill(menu, -1);

		boolean separatorAdded = false;

		//open repository configuration action
		for (final TaskRepository repository : TasksUi.getRepositoryManager().getRepositories(
				BambooCorePlugin.CONNECTOR_KIND)) {
			if (!separatorAdded) {
				new Separator().fill(menu, -1);
				separatorAdded = true;
			}
			Action openRepositoryConfigurationAction = new Action() {
				@Override
				public void run() {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							TasksUiUtil.openEditRepositoryWizard(repository);
						}
					});
				}
			};
			ActionContributionItem openRepoConfigACI = new ActionContributionItem(openRepositoryConfigurationAction);
			openRepositoryConfigurationAction.setText(NLS.bind("Properties for {0}...", repository.getRepositoryLabel()));
			openRepoConfigACI.fill(menu, -1);
		}

		new Separator().fill(menu, -1);

		//goto repository action
		Action gotoTaskRepositoryViewAction = new Action() {
			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					@SuppressWarnings("restriction")
					public void run() {
						try {
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
									TaskRepositoriesView.ID);
						} catch (PartInitException e) {
							StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
									"Failed to show Task Repositories View"));
						}
					}
				});
			}
		};
		ActionContributionItem gotoRepoViewACI = new ActionContributionItem(gotoTaskRepositoryViewAction);
		gotoTaskRepositoryViewAction.setText("Show Task Repositories View");
		gotoTaskRepositoryViewAction.setImageDescriptor(BambooImages.REPOSITORIES);
		gotoRepoViewACI.fill(menu, -1);
	}

	public Menu getMenu(Menu parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		addActions();
		return menu;
	}
}
