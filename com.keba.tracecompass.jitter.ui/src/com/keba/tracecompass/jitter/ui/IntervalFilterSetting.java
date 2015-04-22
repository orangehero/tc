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

package com.keba.tracecompass.jitter.ui;

import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

public class IntervalFilterSetting {
	
	public ITmfFilterTreeNode beginFilter;
	public ITmfFilterTreeNode endFilter;
	public String name;
	public ITmfTimestamp matchBeginTS;
	
	public IntervalFilterSetting() {
		name = "";
		beginFilter = null;
		endFilter = null;
		matchBeginTS = null;
	}
}
