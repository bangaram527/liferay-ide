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
 * Contributors:
 *               Kamesh Sampath - initial implementation
 *******************************************************************************/
/**
 * 
 */

package com.liferay.ide.portlet.core.model.lfrportlet.common;

import com.liferay.ide.core.model.xml.internal.ChoiceValueBinding;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author kamesh
 */
@GenerateImpl
public interface ISimple extends Element {

	ElementType TYPE = new ElementType( ISimple.class );

	// *** PropertyKey ***

	@Label( standard = "Property Key" )
	@XmlBinding( path = "property-key" )
	@Enablement( expr = "${SimpleTriggerValue=='0'}" )
	@DefaultValue( text = "PROPERTY_KEY" )
	@CustomXmlValueBinding( impl = ChoiceValueBinding.class, params = { "property-key", "property-key",
		"cron-trigger-value" } )
	ValueProperty PROP_PROPERTY_KEY = new ValueProperty( TYPE, "PropertyKey" );

	Value<String> getPropertyKey();

	void setPropertyKey( String value );

	// *** Simple Trigger Value ***

	@Label( standard = "Simple Trigger Value" )
	@Type( base = Integer.class )
	@XmlBinding( path = "simple-trigger-value" )
	@DefaultValue( text = "0" )
	@Enablement( expr = "${PropertyKey=='PROPERTY_KEY'}" )
	@CustomXmlValueBinding( impl = ChoiceValueBinding.class, params = { "simple-trigger-value", "property-key",
		"cron-trigger-value" } )
	ValueProperty PROP_SIMPLE_TRIGGER_VALUE = new ValueProperty( TYPE, "SimpleTriggerValue" );

	Value<Integer> getSimpleTriggerValue();

	void setSimpleTriggerValue( Integer value );

	void setSimpleTriggerValue( String value );

	// *** TimeUnit ***

	@Type( base = TimeUnit.class )
	@Label( standard = "Version" )
	@DefaultValue( text = "second" )
	@Required
	@XmlBinding( path = "time-unit" )
	ValueProperty PROP_TIME_UNIT = new ValueProperty( TYPE, "TimeUnit" );

	Value<TimeUnit> getTimeUnit();

	void setTimeUnit( TimeUnit timeUnit );

	void setTimeUnit( String timeUnit );

}
