/*******************************************************************************
 * Copyright (c) 2015 
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   orangehero - Initial API and implementation
 *******************************************************************************/

package com.keba.tracecompass.currentctx.ui;

public class TaskProperty {
	
	private String property;
	private String value;
	
	public TaskProperty(String prop, String val) {
		property = prop;
		value = val;
	}
	
	public void setProperty(String prop) {
		property = prop;
	}
	
	public void setValue(String val) {
		value = val;
	}
	
	public String getProperty() {
		return property;
	}
	
	public String getValue() {
		return value;
	}

}
