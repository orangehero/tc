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

import java.util.ArrayList;

public class JitterIntervalNodeList {
	
	private ArrayList<JitterIntervalNode> intervalNodeList;
	private double jitter;
	
	JitterIntervalNodeList(double jit) {
		jitter = jit;
		intervalNodeList = new ArrayList<JitterIntervalNode>();
	}
	
	public boolean addIntervalNode(JitterIntervalNode node) {
		return intervalNodeList.add(node);
	}
	
	public int size () {
		return intervalNodeList.size();
	}
	
	public double getJitter() {
		return jitter;
	}
	
	public Object [] getContentNodes() {
		return intervalNodeList.toArray();
	}

}
