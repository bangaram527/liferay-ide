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

package com.liferay.ide.eclipse.server.core;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.Version;

/**
 * @author Greg Amerson
 */
public interface IPortalConstants {

	public static final IEclipsePreferences defaultPrefs = new DefaultScope().getNode(PortalServerCorePlugin.PLUGIN_ID);

	String DEFAULT_RUNTIME_TYPE_ID = "com.liferay.ide.eclipse.server.tomcat.runtime.60";

	String INSTALLABLE_UPDATE_URL = defaultPrefs.get("installable.update.url", "");

	Version LEAST_SUPPORTED_VERSION = new Version(6, 0, 2);

	String LIFERAY_DISPLAY_XML_FILE = "liferay-display.xml";

	String LIFERAY_HOOK_XML_FILE = "liferay-hook.xml";

	String LIFERAY_PORTLET_XML_FILE = "liferay-portlet.xml";

	String LIFERAY_SERVICE_BUILDER_XML_FILE = "service.xml";

	String PORTLET_XML_FILE = "portlet.xml";

	String WEB_XML_FILE = "web.xml";

}