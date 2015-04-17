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

public class JitterIntervalNode {
	
	private double startTs;
	private double endTs;
	
	JitterIntervalNode (double sts, double ets) {
		startTs = sts;
		endTs = ets;
	}
	
	public double getStartTs() {
		return startTs;
	}
	
	public double getEndTs() {
		return endTs;
	}
	
	public double getJitterInterval () {
	   return endTs - startTs;
	}
	
}
