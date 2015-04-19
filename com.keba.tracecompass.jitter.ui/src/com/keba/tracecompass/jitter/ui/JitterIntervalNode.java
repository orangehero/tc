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

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

public class JitterIntervalNode {
	
	private ITmfTimestamp startTs;
	private ITmfTimestamp endTs;
	
	JitterIntervalNode (ITmfTimestamp sts, ITmfTimestamp ets) {
		startTs = sts;
		endTs = ets;
	}
	
	public ITmfTimestamp getStartTs() {
		return startTs;
	}
	
	public ITmfTimestamp getEndTs() {
		return endTs;
	}
	
	public ITmfTimestamp getJitterInterval () {
	   return endTs.getDelta(startTs);
	}
	
}
