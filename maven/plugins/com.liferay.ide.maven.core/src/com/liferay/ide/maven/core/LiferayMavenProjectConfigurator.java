/*******************************************************************************
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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
package com.liferay.ide.maven.core;

import com.liferay.ide.project.core.facet.IPluginFacetConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.markers.IMavenMarkerManager;
import org.eclipse.m2e.core.internal.markers.MavenProblemInfo;
import org.eclipse.m2e.core.internal.markers.SourceLocation;
import org.eclipse.m2e.core.internal.markers.SourceLocationHelper;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;


/**
 * @author Gregory Amerson
 */
@SuppressWarnings( "restriction" )
public class LiferayMavenProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator
{

    private IMavenMarkerManager mavenMarkerManager;

    public LiferayMavenProjectConfigurator()
    {
        super();

        this.mavenMarkerManager = MavenPluginActivator.getDefault().getMavenMarkerManager();
    }

    @Override
    public void configure( ProjectConfigurationRequest request, IProgressMonitor monitor ) throws CoreException
    {
        final IProject project = request.getProject();
        final MavenProject mavenProject = request.getMavenProject();
        final Xpp3Dom liferayMavenPluginConfig = LiferayMavenUtil.getLiferayMavenPluginConfig( mavenProject );

        if( ! shouldConfigure( liferayMavenPluginConfig ) )
        {
            return;
        }

        removeLiferayMavenMarkers( project );

        String pluginType =
            LiferayMavenUtil.getLiferayMavenPluginConfig( mavenProject, ILiferayMavenConstants.PLUGIN_CONFIG_PLUGIN_TYPE );

        if( pluginType == null )
        {
            pluginType = ILiferayMavenConstants.DEFAULT_PLUGIN_TYPE;
        }

        final IFacetedProject facetedProject = ProjectFacetsManager.create( request.getProject(), false, monitor );

        if( shouldInstallNewLiferayFacet( facetedProject ) )
        {
            installNewLiferayFacet( facetedProject, pluginType, monitor );
        }

        findLiferayMavenPluginProblems( project, mavenProject );
    }

    public void configureClasspath( IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor )
        throws CoreException
    {
    }

    public void configureRawClasspath(
        ProjectConfigurationRequest request, IClasspathDescriptor classpath, IProgressMonitor monitor )
        throws CoreException
    {
    }

    private Plugin findLiferayMavenPlugin( MavenProject mavenProject )
    {
        Plugin retval = null;

        if( mavenProject != null )
        {
            retval = mavenProject.getPlugin( ILiferayMavenConstants.LIFERAY_MAVEN_PLUGIN_KEY );

            if( retval == null )
            {
                retval = findLiferayMavenPlugin( mavenProject.getParent() );
            }
        }

        return retval;
    }

    private void findLiferayMavenPluginProblems( IProject project, MavenProject mavenProject )
    {
        // first check to make sure that the AppServer* properties are available and pointed to valid location
        final Plugin liferayMavenPlugin = LiferayMavenUtil.getLiferayMavenPlugin( mavenProject );

        if( liferayMavenPlugin != null )
        {
            List<MavenProblemInfo> errors = new ArrayList<MavenProblemInfo>();

            Xpp3Dom config = (Xpp3Dom) liferayMavenPlugin.getConfiguration();

            Xpp3Dom appServerDeployDirNode = config.getChild( ILiferayMavenConstants.PLUGIN_CONFIG_APP_SERVER_DEPLOY_DIR );

            if( appServerDeployDirNode != null )
            {
                String appServerDeployDir = appServerDeployDirNode.getValue();

                if( !new File( appServerDeployDir ).exists() )
                {
                    SourceLocation location =
                        SourceLocationHelper.findLocation(
                            liferayMavenPlugin, ILiferayMavenConstants.PLUGIN_CONFIG_APP_SERVER_DEPLOY_DIR );
                    errors.add( new MavenProblemInfo(
                        "Invalid value for property " + ILiferayMavenConstants.PLUGIN_CONFIG_APP_SERVER_DEPLOY_DIR + ": " + appServerDeployDir, IMarker.SEVERITY_ERROR, location ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            try
            {
                this.markerManager.addErrorMarkers(
                    project.getFile( IMavenConstants.POM_FILE_NAME ),
                    ILiferayMavenConstants.LIFERAY_MAVEN_MARKER_CONFIGURATION_ERROR_ID, errors );
            }
            catch( CoreException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private IProjectFacetVersion getLiferayProjectFacet( IFacetedProject facetedProject )
    {
        IProjectFacetVersion retval = null;

        if( facetedProject != null )
        {
            for( IProjectFacetVersion fv : facetedProject.getProjectFacets() )
            {
                if( fv.getProjectFacet().getId().contains( "liferay." ) ) //$NON-NLS-1$
                {
                    retval = fv;
                    break;
                }
            }
        }

        return retval;
    }

    private Action getNewLiferayFacetInstallAction( String pluginType )
    {
        Action retval = null;
        IProjectFacetVersion newFacet = null;
        IDataModelProvider dataModel = null;

        if( ILiferayMavenConstants.PORTLET_PLUGIN_TYPE.equals( pluginType ) )
        {
            newFacet = IPluginFacetConstants.LIFERAY_PORTLET_PROJECT_FACET.getDefaultVersion();
            dataModel = new MavenPortletPluginFacetInstallProvider();
        }
        else if( ILiferayMavenConstants.HOOK_PLUGIN_TYPE.equals( pluginType ) )
        {
            newFacet = IPluginFacetConstants.LIFERAY_HOOK_PROJECT_FACET.getDefaultVersion();
            dataModel = new MavenHookPluginFacetInstallProvider();
        }
        //TODO handle EXT
//        else if( EXT_PLUGIN_TYPE.equals( pluginType ) )
//        {
//            newLiferayFacetToInstall = IPluginFacetConstants.LIFERAY_EXT_PROJECT_FACET.getDefaultVersion();
//            liferayFacetInstallProvider = new MavenExtPluginFacetInstallProvider();
//        }
        else if( ILiferayMavenConstants.LAYOUTTPL_PLUGIN_TYPE.equals( pluginType ) )
        {
            newFacet = IPluginFacetConstants.LIFERAY_LAYOUTTPL_PROJECT_FACET.getDefaultVersion();
            dataModel = new MavenLayoutTplPluginFacetInstallProvider();
        }
        else if( ILiferayMavenConstants.THEME_PLUGIN_TYPE.equals( pluginType ) )
        {
            newFacet = IPluginFacetConstants.LIFERAY_THEME_PROJECT_FACET.getDefaultVersion();
            dataModel = new MavenThemePluginFacetInstallProvider();
        }

        if( newFacet != null )
        {
            final IDataModel config = DataModelFactory.createDataModel( dataModel );
            retval = new Action( Action.Type.INSTALL, newFacet, config );
        }

        return retval;
    }

    private void installNewLiferayFacet( IFacetedProject facetedProject, String pluginType, IProgressMonitor monitor )
    {
        final Action action = getNewLiferayFacetInstallAction( pluginType );

        if( action != null )
        {
            try
            {
                facetedProject.modify( Collections.singleton( action ), monitor );
            }
            catch ( Exception e )
            {
                LiferayMavenCore.logError( "Unable to install liferay facet " + action.getProjectFacetVersion(), e ); //$NON-NLS-1$
            }
        }
    }

    private boolean loadParentHierarchy( IMavenProjectFacade facade, IProgressMonitor monitor ) throws CoreException
    {
        boolean loadedParent = false;
        MavenProject mavenProject = facade.getMavenProject();

        try
        {
            if( mavenProject.getModel().getParent() == null || mavenProject.getParent() != null )
            {
                // If the getParent() method is called without error,
                // we can assume the project has been fully loaded, no need to continue.
                return false;
            }
        }
        catch( IllegalStateException e )
        {
            // The parent can not be loaded properly
        }

        MavenExecutionRequest request = null;

        while( mavenProject != null && mavenProject.getModel().getParent() != null )
        {
            if( monitor.isCanceled() )
            {
                break;
            }

            if( request == null )
            {
                request = projectManager.createExecutionRequest( facade, monitor );
            }

            MavenProject parentProject = maven.resolveParentProject( request, mavenProject, monitor );

            if( parentProject != null )
           {
                mavenProject.setParent( parentProject );
                loadedParent = true;
            }

            mavenProject = parentProject;
        }

        return loadedParent;
    }

    private void removeLiferayMavenMarkers( IProject project ) throws CoreException
    {
        this.mavenMarkerManager.deleteMarkers(
            project, ILiferayMavenConstants.LIFERAY_MAVEN_MARKER_CONFIGURATION_ERROR_ID );
    }

    private boolean shouldConfigure( Xpp3Dom config )
    {
        return config != null;
    }

    private boolean shouldInstallNewLiferayFacet( IFacetedProject facetedProject )
    {
        return getLiferayProjectFacet( facetedProject ) == null;
    }

}


