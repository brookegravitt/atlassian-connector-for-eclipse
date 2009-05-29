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

package com.atlassian.connector.eclipse.internal.subversive.ui;

import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.CustomRepository;
import com.atlassian.connector.eclipse.ui.team.ICompareAnnotationModel;
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;
import com.atlassian.connector.eclipse.ui.team.RevisionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * Connector to handle connecting to a CVS repository
 * 
 * @author Shawn Minto
 */
public class SubversiveTeamResourceConnector implements ITeamResourceConnector {

	public SubversiveTeamResourceConnector() {
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean canHandleFile(String repoUrl, String filePath, IProgressMonitor monitor) {
		return true;
	}

	public boolean openCompareEditor(String repoUrl, String newFilePath, String oldFilePath, String oldRevisionString,
			String newRevisionString, ICompareAnnotationModel annotationModel, final IProgressMonitor monitor)
			throws CoreException {
		/*
		ISVNRemoteResource oldRemoteFile = getSvnRemoteFile(repoUrl, oldFilePath, newFilePath, oldRevisionString,
				newRevisionString, monitor);
		ISVNRemoteResource newRemoteFile = getSvnRemoteFile(repoUrl, newFilePath, oldFilePath, newRevisionString,
				oldRevisionString, monitor);

		if (oldRemoteFile != null && newRemoteFile != null) {
			ResourceEditionNode left = new ResourceEditionNode(newRemoteFile);
			ResourceEditionNode right = new ResourceEditionNode(oldRemoteFile);
			CompareEditorInput compareEditorInput = new CrucibleSubclipseCompareEditorInput(left, right,
					annotationModel);
			TeamUiUtils.openCompareEditorForInput(compareEditorInput);
			return true;
		}
		*/
		throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
				"Could not get revisions for {0}.", newFilePath)));
	}

	public SortedSet<Long> getRevisionsForFile(IFile file, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(file);
		/*
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(file);
		try {
			monitor.beginTask("Getting Revisions for " + file.getName(), IProgressMonitor.UNKNOWN);
			SVNRevision revision = SVNRevision.HEAD;
			ISVNRemoteResource remoteResource = local.getRemoteResource(revision);
			GetLogsCommand getLogsCommand = new GetLogsCommand(remoteResource, revision, new SVNRevision.Number(0),
					SVNRevision.HEAD, false, 0, null, true);
			getLogsCommand.run(monitor);
			ILogEntry[] logEntries = getLogsCommand.getLogEntries();
			SortedSet<Long> revisions = new TreeSet<Long>();
			for (ILogEntry logEntrie : logEntries) {
				revisions.add(new Long(logEntrie.getRevision().getNumber()));
			}
			return revisions;
		} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID,
						"Error while retrieving Revisions for file " + file.getName() + ".", e));
		}*/
		return null;
	}

	public Collection<RepositoryInfo> getRepositories(IProgressMonitor monitor) {
		IRepositoryLocation[] repositories = SVNRemoteStorage.instance().getRepositoryLocations();
		if (repositories == null) {
			return MiscUtil.buildArrayList();
		}

		List<RepositoryInfo> res = MiscUtil.buildArrayList(repositories.length);
		for (IRepositoryLocation repo : repositories) {
			res.add(new RepositoryInfo(repo.getUrl(), repo.getLabel()));
		}
		return res;
	}

	public Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> getLatestChangesets(String repositoryUrl,
			int limit, IProgressMonitor monitor, MultiStatus status) {
		/*
		ISVNRepositoryLocation[] repos = SVNUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryLocations(
				monitor);
		monitor.beginTask("Retrieving changeset for SVN (subclipse) repositories", repos.length);
		Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> map = new HashMap<CustomRepository, SortedSet<ICustomChangesetLogEntry>>();
		for (ISVNRepositoryLocation repo : repos) {
			//if a repository is given and the repo does not match the given repository, skip it
			if (repositoryUrl != null && !repositoryUrl.equals(repo.getUrl().toString())) {
				continue;
			}
			IProgressMonitor subMonitor = org.eclipse.mylyn.commons.net.Policy.subMonitorFor(monitor, 1);
			CustomRepository customRepository = new CustomRepository(repo.getUrl().toString());
			SortedSet<ICustomChangesetLogEntry> changesets = new TreeSet<ICustomChangesetLogEntry>();
			ISVNRemoteFolder rootFolder = repo.getRootFolder();

			if (limit > 0) { //do not retrieve unlimited revisions
				subMonitor.beginTask("Retrieving changesets for " + repo.getLabel(), 101);
				GetLogsCommand getLogsCommand = new GetLogsCommand(rootFolder, SVNRevision.HEAD, SVNRevision.HEAD,
						new SVNRevision.Number(0), false, limit, null, true);
				try {
					getLogsCommand.run(subMonitor);
					ILogEntry[] logEntries = getLogsCommand.getLogEntries();
					for (ILogEntry logEntry : logEntries) {
						LogEntryChangePath[] logEntryChangePaths = logEntry.getLogEntryChangePaths();
						String[] changed = new String[logEntryChangePaths.length];
						for (int i = 0; i < logEntryChangePaths.length; i++) {
							changed[i] = logEntryChangePaths[i].getPath();

						}
						ICustomChangesetLogEntry customEntry = new CustomChangeSetLogEntry(logEntry.getComment(),
								logEntry.getAuthor(), logEntry.getRevision().toString(), logEntry.getDate(), changed,
								customRepository);
						changesets.add(customEntry);
					}
				} catch (SVNException e) {
					status.add(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
							"Could not retrieve changesets from " + customRepository.getUrl(), e));
				}
			}
			map.put(customRepository, changesets);
			subMonitor.done();
		}
		return map;
		*/
		return null;
	}

	public Map<IFile, SortedSet<Long>> getRevisionsForFile(List<IFile> files, IProgressMonitor monitor)
			throws CoreException {
		Assert.isNotNull(files);
		/*
		Map<IFile, SortedSet<Long>> map = new HashMap<IFile, SortedSet<Long>>();

		monitor.beginTask("Getting Revisions", files.size());

		for (IFile file : files) {
			ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(file);
			IProgressMonitor submonitor = Policy.subMonitorFor(monitor, 1);
			try {
				submonitor.beginTask("Getting revisions for " + file.getName(), IProgressMonitor.UNKNOWN);
				SVNRevision revision = SVNRevision.HEAD;
				GetLogsCommand getLogsCommand = new GetLogsCommand(local.getRemoteResource(revision), revision,
						new SVNRevision.Number(0), SVNRevision.HEAD, false, 0, null, true);
				getLogsCommand.run(submonitor);
				ILogEntry[] logEntries = getLogsCommand.getLogEntries();
				SortedSet<Long> revisions = new TreeSet<Long>();
				for (ILogEntry logEntrie : logEntries) {
					revisions.add(new Long(logEntrie.getRevision().getNumber()));
				}
				map.put(file, revisions);
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID,
						"Error while retrieving Revisions for file " + file.getName() + ".", e));
			} finally {
				submonitor.done();
			}
		}
		return map;
		*/
		return null;
	}

	/*
	public ISVNRemoteFile getSvnRemoteFile(String repoUrl, String filePath, String otherRevisionFilePath,
			String revisionString, String otherRevisionString, final IProgressMonitor monitor) {
		if (repoUrl == null) {
			return null;
		}
		try {

			if (filePath.startsWith("/")) {
				filePath = filePath.substring(1);
			}

			IResource localResource = getLocalResourceFromFilePath(filePath);

			boolean localFileNotFound = localResource == null;

			if (localFileNotFound) {
				localResource = getLocalResourceFromFilePath(otherRevisionFilePath);
			}

			if (localResource != null) {
				SVNRevision svnRevision = SVNRevision.getRevision(revisionString);
				SVNRevision otherSvnRevision = SVNRevision.getRevision(otherRevisionString);
				return getRemoteFile(localResource, filePath, svnRevision, otherSvnRevision, localFileNotFound);
			}
		} catch (SVNException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ParseException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}
	*/

	public IEditorPart openFile(String repoUrl, String filePath, String otherRevisionFilePath, String revisionString,
			String otherRevisionString, final IProgressMonitor monitor) throws CoreException {
		if (repoUrl == null) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"No repository URL given.."));
		}
		/*
		try {

			IResource localResource = getLocalResourceFromFilePath(filePath);

			boolean localFileNotFound = localResource == null;

			if (localFileNotFound) {
				localResource = getLocalResourceFromFilePath(otherRevisionFilePath);
			}

			if (localResource != null) {
				SVNRevision svnRevision = SVNRevision.getRevision(revisionString);
				SVNRevision otherSvnRevision = SVNRevision.getRevision(otherRevisionString);
				ISVNLocalFile localFile = getLocalFile(localResource);

				if (localFile.getStatus().getLastChangedRevision().equals(svnRevision) && !localFile.isDirty()) {
					// the file is not dirty and we have the right local copy
					IEditorPart editorPart = TeamUiUtils.openLocalResource(localResource);
					if (editorPart == null) {
						throw new CoreException(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, NLS.bind(
								"Could not open editor for {0}.", localFile.getName())));
					}
					return editorPart;

				} else {
					final ISVNRemoteFile remoteFile = getRemoteFile(localResource, filePath, svnRevision,
							otherSvnRevision, localFileNotFound);
					if (remoteFile != null) {
						// we need to open the remote resource since the file is either dirty or the wrong revision

						IEditorPart editorPart = null;
						if (Display.getCurrent() != null) {
							editorPart = openRemoteSvnFile(remoteFile, monitor);
						} else {
							final IEditorPart[] part = new IEditorPart[1];
							PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
								public void run() {
									part[0] = openRemoteSvnFile(remoteFile, monitor);
								}
							});
							editorPart = part[0];
						}
						if (editorPart == null) {
							throw new CoreException(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, NLS.bind(
									"Could not open editor for {0}.", remoteFile.getName())));
						}
						return editorPart;
					} else {
						throw new CoreException(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, NLS.bind(
								"Could not get remote file for {0}.", filePath)));
					}
				}
			} else {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, NLS.bind(
						"Could not find local resource for {0}.", filePath)));
			}
		} catch (SVNException e) {
			Status status = new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, e.getMessage(), e);
			StatusHandler.log(status);
			throw new CoreException(status);
		} catch (ParseException e) {
			Status status = new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, e.getMessage(), e);
			StatusHandler.log(status);
			throw new CoreException(status);
		}
		*/
		return null;
	}

	public boolean canHandleEditorInput(IEditorInput editorInput) {
		/*
		if (editorInput instanceof FileEditorInput) {
			try {
				IFile file = ((FileEditorInput) editorInput).getFile();
				ISVNLocalFile localFile = getLocalFile(file);
				if (localFile != null && !localFile.isDirty()) {
					return true;
				}
			} catch (SVNException e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID,
						"Unable to get svn information for local file.", e));
			}
		} else if (editorInput instanceof RemoteFileEditorInput) {
			return true;
		}
		*/
		return false;
	}

	public CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview) {
		/*
		SVNUrl fileUrl = null;
		String revision = null;
		if (editorInput instanceof FileEditorInput) {
			// this is a local file that we know how to deal with
			try {
				IFile file = ((FileEditorInput) editorInput).getFile();
				ISVNLocalFile localFile = getLocalFile(file);
				if (localFile != null && !localFile.isDirty()) {
					fileUrl = localFile.getUrl();
					revision = localFile.getStatus().getLastChangedRevision().toString();
				}
			} catch (SVNException e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID,
						"Unable to get svn information for local file.", e));
			}
		} else if (editorInput instanceof RemoteFileEditorInput) {
			// this is a remote file that we know how to deal with
			RemoteFileEditorInput input = (RemoteFileEditorInput) editorInput;
			ISVNRemoteFile remoteFile = input.getSVNRemoteFile();
			fileUrl = remoteFile.getUrl();
			revision = remoteFile.getRevision().toString();
		}

		if (fileUrl != null && revision != null) {
			try {
				for (CrucibleFileInfo file : activeReview.getFiles()) {
					VersionedVirtualFile fileDescriptor = file.getFileDescriptor();
					VersionedVirtualFile oldFileDescriptor = file.getOldFileDescriptor();
					SVNUrl newFileUrl = null;
					String newAbsoluteUrl = getAbsoluteUrl(fileDescriptor);
					if (newAbsoluteUrl != null) {
						newFileUrl = new SVNUrl(newAbsoluteUrl);
					}

					SVNUrl oldFileUrl = null;
					String oldAbsoluteUrl = getAbsoluteUrl(oldFileDescriptor);
					if (oldAbsoluteUrl != null) {
						oldFileUrl = new SVNUrl(oldAbsoluteUrl);
					}
					if ((newFileUrl != null && newFileUrl.equals(fileUrl))
							|| (oldFileUrl != null && oldFileUrl.equals(fileUrl))) {
						if (revision.equals(fileDescriptor.getRevision())) {
							return new CrucibleFile(file, false);
						}
						if (revision.equals(oldFileDescriptor.getRevision())) {
							return new CrucibleFile(file, true);
						}
						return null;
					}
				}
			} catch (ValueNotYetInitialized e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID,
						"Review is not fully initialized.  Unable to get file from review.", e));
			} catch (MalformedURLException e) {
				// ignore
			}
		}
		*/
		return null;
	}

	/*
	private String getAbsoluteUrl(VersionedVirtualFile fileDescriptor) {
		//TODO might need some performance tweak, but works for now for M2
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {

			if (SVNWorkspaceRoot.isManagedBySubclipse(project)) {
				try {
					IPath fileIPath = new Path(fileDescriptor.getUrl());
					IResource resource = project.findMember(fileIPath);
					while (!fileIPath.isEmpty() && resource == null) {
						fileIPath = fileIPath.removeFirstSegments(1);
						resource = project.findMember(fileIPath);
					}
					if (resource == null) {
						continue;
					}

					ISVNResource projectResource = SVNWorkspaceRoot.getSVNResourceFor(resource);

					if (projectResource.getUrl().toString().endsWith(fileDescriptor.getUrl())) {
						return projectResource.getUrl().toString();
					}
				} catch (Exception e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
		return null;
	}*/

	/*
	private IEditorPart openRemoteSvnFile(ISVNRemoteFile remoteFile, IProgressMonitor monitor) {
		try {
			IWorkbench workbench = AtlassianCvsUiPlugin.getDefault().getWorkbench();
			IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();

			RemoteFileEditorInput editorInput = new CustomRemoteFileEditorInput(remoteFile, monitor);
			String editorId = getEditorId(workbench, remoteFile);
			return page.openEditor(editorInput, editorId);
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}
	*/

	/*
	private ISVNLocalFile getLocalFile(IResource localResource) throws SVNException {
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(localResource);

		if (local.isManaged()) {
			return (ISVNLocalFile) local;
		}
		return null;
	}
	*/

	/*
	private ISVNRemoteFile getRemoteFile(IResource localResource, String filePath, SVNRevision svnRevision,
			SVNRevision otherSvnRevision, boolean localFileNotFound) throws ParseException, SVNException {

		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(localResource);

		if (local.isManaged()) {
			if (localFileNotFound) {
				//file has been moved, so we have to do some funky file retrieval
				ISVNRepositoryLocation location = local.getRepository();

				SVNUrl svnUrl = local.getUrl();

				if (otherSvnRevision instanceof SVNRevision.Number) {
					return new RemoteFile(null, location, svnUrl, svnRevision, (SVNRevision.Number) svnRevision,
							new Date(), "");
				} else {
					return new RemoteFile(null, location, svnUrl, svnRevision, SVNRevision.INVALID_REVISION,
							new Date(), "");
				}
			} else {
				return (ISVNRemoteFile) local.getRemoteResource(svnRevision);
			}
		}

		return null;
	}
	*/

	/*
	private IResource getLocalResourceFromFilePath(String filePath) {
		if (filePath == null || filePath.length() <= 0) {
			return null;
		}
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {

			if (SVNWorkspaceRoot.isManagedBySubclipse(project)) {
				try {
					IPath fileIPath = new Path(filePath);
					IResource resource = project.findMember(fileIPath);
					while (!fileIPath.isEmpty() && resource == null) {
						fileIPath = fileIPath.removeFirstSegments(1);
						resource = project.findMember(fileIPath);
					}
					if (resource == null) {
						continue;
					}

					ISVNResource projectResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
					String url = projectResource.getUrl().toString();

					if (url.endsWith(filePath)) {
						return resource;
					}
				} catch (Exception e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianCvsUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
		return null;
	}*/

	public RevisionInfo getLocalRevision(IResource resource) throws CoreException {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}

		// check if project is associated with Subversive Team provider, 
		// if we don't test it asRepositoryResource will throw RuntimeException
		RepositoryProvider provider = RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
		if (provider == null) {
			return null;
		}

		IRepositoryResource svnResource = SVNRemoteStorage.instance().asRepositoryResource(resource);
		try {
			String mimeTypeProp = SVNUtility.getPropertyForNotConnected(resource, SVNProperty.BuiltIn.MIME_TYPE);
			boolean isBinary = (mimeTypeProp != null && !mimeTypeProp.startsWith("text"));

			return new RevisionInfo(svnResource.getUrl(), Long.toString(svnResource.getRevision()), isBinary);
		} catch (SVNConnectorException e) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"Cannot determine SVN information for resource [" + resource + "]", e));
		}
	}
	/*
	private String getEditorId(IWorkbench workbench, ISVNRemoteFile file) {
		IEditorRegistry registry = workbench.getEditorRegistry();
		String filename = file.getName();
		IEditorDescriptor descriptor = registry.getDefaultEditor(filename);
		String id;
		if (descriptor == null) {
			descriptor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		}
		if (descriptor == null) {
			id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			id = descriptor.getId();
		}
		return id;
	}*/

}