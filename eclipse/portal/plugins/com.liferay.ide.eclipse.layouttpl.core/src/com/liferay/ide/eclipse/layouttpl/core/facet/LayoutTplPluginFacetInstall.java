/*******************************************************************************
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *******************************************************************************/

package com.liferay.ide.eclipse.layouttpl.core.facet;

import com.liferay.ide.eclipse.core.util.FileUtil;
import com.liferay.ide.eclipse.project.core.facet.IPluginFacetConstants;
import com.liferay.ide.eclipse.project.core.facet.PluginFacetInstall;
import com.liferay.ide.eclipse.sdk.ISDKConstants;
import com.liferay.ide.eclipse.sdk.SDK;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.common.componentcore.datamodel.FacetInstallDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

/**
 * @author Greg Amerson
 */
public class LayoutTplPluginFacetInstall extends PluginFacetInstall {

	public static final IProjectFacet LIFERAY_LAYOUTTPL_PLUGIN_FACET =
		ProjectFacetsManager.getProjectFacet(IPluginFacetConstants.LIFERAY_LAYOUTTPL_PLUGIN_FACET_ID);

	@Override
	public void execute(IProject project, IProjectFacetVersion fv, Object config, IProgressMonitor monitor)
		throws CoreException {
		
		super.execute(project, fv, config, monitor);

		IDataModel model = (IDataModel) config;
		
		IDataModel masterModel = (IDataModel) model.getProperty(FacetInstallDataModelProvider.MASTER_PROJECT_DM);
		
		if (masterModel != null && masterModel.getBooleanProperty(CREATE_PROJECT_OPERATION)) {
			// get the template zip for layouttpl and extract into the project
			SDK sdk = getSDK();

			String layoutTplName = this.masterModel.getStringProperty(LAYOUTTPL_NAME);
			
			String displayName = this.masterModel.getStringProperty(DISPLAY_NAME);

			IPath newLayoutTplPath = sdk.createNewLayoutTpl(layoutTplName, displayName, getRuntimeLocation());
			
			processNewFiles(newLayoutTplPath.append(layoutTplName + ISDKConstants.LAYOUTTPL_PLUGIN_PROJECT_SUFFIX), false);
			
			// cleanup portlet files
			FileUtil.deleteDir(newLayoutTplPath.toFile(), true);

			removeUnneededClasspathEntries();

			this.project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
		else {
			setupDefaultOutputLocation();
		}
	}

	protected void removeUnneededClasspathEntries() {
		IFacetedProjectWorkingCopy facetedProject = getFacetedProject();
		IJavaProject javaProject = JavaCore.create(facetedProject.getProject());

		try {
			IClasspathEntry[] existingClasspath = javaProject.getRawClasspath();
			List<IClasspathEntry> newClasspath = new ArrayList<IClasspathEntry>();

			for (IClasspathEntry entry : existingClasspath) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					continue;
				}
				else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					String path = entry.getPath().toPortableString();
					if (path.contains("org.eclipse.jdt.launching.JRE_CONTAINER") ||
						path.contains("org.eclipse.jst.j2ee.internal.web.container") ||
						path.contains("org.eclipse.jst.j2ee.internal.module.container")) {
						continue;
					}
				}

				newClasspath.add(entry);
			}

			javaProject.setRawClasspath(newClasspath.toArray(new IClasspathEntry[0]), null);

			IResource sourceFolder =
				javaProject.getProject().findMember(IPluginFacetConstants.PORTLET_PLUGIN_SDK_SOURCE_FOLDER);

			if (sourceFolder.exists()) {
				sourceFolder.delete(true, null);
			}
		}
		catch (Exception e) {

		}
	}

	@Override
	protected boolean shouldInstallPluginLibraryDelegate() {
		return false;
	}

}