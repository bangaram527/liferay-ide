/*******************************************************************************
 * Copyright (c) 2000-2014 Liferay, Inc. All rights reserved.
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

package com.liferay.ide.server.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plugin life cycle
 *
 * @auther Greg Amerson
 */
public class LiferayServerUIPlugin extends AbstractUIPlugin
{

    public static final String IMG_WIZ_RUNTIME = "imgWizRuntime"; //$NON-NLS-1$

    // The plugin ID
    public static final String PLUGIN_ID = "com.liferay.ide.server.ui"; //$NON-NLS-1$

    // base url for icons
    private static URL ICON_BASE_URL;

    // The shared instance
    private static LiferayServerUIPlugin plugin;

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static LiferayServerUIPlugin getDefault()
    {
        return plugin;
    }

    /**
     * Return the image with the given key from the image registry.
     *
     * @param key
     *            java.lang.String
     * @return org.eclipse.jface.parts.IImage
     */
    public static ImageDescriptor getImageDescriptor( String key )
    {
        try
        {
            getDefault().getImageRegistry();
            return (ImageDescriptor) getDefault().imageDescriptors.get( key );
        }
        catch( Exception e )
        {
            return null;
        }
    }

    public static void logError( Exception ex )
    {
        getDefault().getLog().log( createErrorStatus( ex ) );
    }

    public static IStatus createErrorStatus( Exception ex )
    {
        return new Status( IStatus.ERROR, PLUGIN_ID, ex.getMessage(), ex );
    }

    public static void logError( String msg, Exception ex )
    {
        getDefault().getLog().log( createErrorStatus( msg, ex ) );
    }

    public static IStatus createErrorStatus( String msg )
    {
        return new Status( IStatus.ERROR, PLUGIN_ID, msg, null );
    }

    public static IStatus createErrorStatus( String msg, Exception ex )
    {
        return new Status( IStatus.ERROR, PLUGIN_ID, msg, ex );
    }

    protected Map<String, ImageDescriptor> imageDescriptors = new HashMap<String, ImageDescriptor>();

    /**
     * The constructor
     */
    public LiferayServerUIPlugin()
    {

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext ) fe
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }

    /**
     * Register an image with the registry.
     *
     * @param key
     *            java.lang.String
     * @param partialURL
     *            java.lang.String
     */
    private void registerImage( ImageRegistry registry, String key, String partialURL )
    {
        if( ICON_BASE_URL == null )
        {
            String pathSuffix = "icons/"; //$NON-NLS-1$

            ICON_BASE_URL = plugin.getBundle().getEntry( pathSuffix );
        }

        try
        {
            ImageDescriptor id = ImageDescriptor.createFromURL( new URL( ICON_BASE_URL, partialURL ) );

            registry.put( key, id );

            imageDescriptors.put( key, id );
        }
        catch( Exception e )
        {
            plugin.getLog().log( new Status( IStatus.ERROR, PLUGIN_ID, e.getMessage() ) );
        }
    }

    protected ImageRegistry createImageRegistry()
    {
        final ImageRegistry registry = new ImageRegistry();

        final String[] pluginTypes = new String[]
        {
            "portlet", "hook", "ext", "layouttpl", "theme", "web" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        };

        for (String type : pluginTypes)
        {
            registerImage( registry, type, "/icons/e16/" + type + ".png" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        registerImage( registry, IMG_WIZ_RUNTIME, "wizban/liferay_wiz.png" ); //$NON-NLS-1$

        return registry;
    }

}
