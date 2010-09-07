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

package com.atlassian.connector.eclipse.ui.team;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RepositoryInfo implements Comparable<RepositoryInfo> {
	private final String scmPath;

	private final String name;

	private final ITeamResourceConnector connector;

	public RepositoryInfo(@NotNull String scmPath, @Nullable String name, @NotNull ITeamResourceConnector connector) {
		this.scmPath = scmPath;
		this.name = name;
		this.connector = connector;
	}

	@NotNull
	public String getScmPath() {
		return scmPath;
	}

	@Nullable
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return (name != null ? name + ": " : "") + scmPath;
	}

	@NotNull
	public ITeamResourceConnector getTeamResourceConnector() {
		return connector;
	}

	public int compareTo(RepositoryInfo other) {
		return this.scmPath.compareTo(other.getScmPath());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scmPath == null) ? 0 : scmPath.hashCode());
		result = prime * result + ((connector == null) ? 0 : connector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Decide if we want to compare connector and name also
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RepositoryInfo other = (RepositoryInfo) obj;
		if (scmPath == null) {
			if (other.scmPath != null) {
				return false;
			}
		} else if (!scmPath.equals(other.scmPath)) {
			return false;
		}

		if (connector == null) {
			if (other.connector != null) {
				return false;
			}
		} else if (!connector.equals(other.connector)) {
			return false;
		}
		return true;
	}
}