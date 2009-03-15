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
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.editor.BambooEditor;
import com.atlassian.connector.eclipse.internal.bamboo.ui.editor.BambooEditorInput;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * Action to open a Bamboo Rich Editor
 * 
 * @author Thomas Ehrnhoefer
 */
public class OpenBambooEditorAction extends BaseSelectionListenerAction {

	private final TreeViewer buildViewer;

	public OpenBambooEditorAction(TreeViewer buildViewer) {
		super(null);
		this.buildViewer = buildViewer;
	}

	@Override
	public void run() {
		ISelection s = buildViewer.getSelection();
		if (s instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) s;
			Object selected = selection.iterator().next();
			if (selected instanceof BambooBuild) {
				BambooBuild build = (BambooBuild) selected;
				TaskRepository repository = TasksUi.getRepositoryManager().getRepository(
						BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl());
				BambooEditorInput input = new BambooEditorInput(repository, build);
				try {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
								"Failed to open Bamboo Rich Editor: no available workbench window. Please try again."));
					} else {
						window.getActivePage().openEditor(input, BambooEditor.ID, true);
					}
				} catch (PartInitException e) {
					StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
							"Failed to open Bamboo Rich Editor: " + e.getMessage(), e));
				}
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return selection.size() == 1;
	}
}