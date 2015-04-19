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

import java.util.TreeMap;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

public class JitterIntervalList {

	private TreeMap<Long, JitterIntervalNodeList> jitterFreq;
	private String Name;
	
	public JitterIntervalList(String n) {
		jitterFreq = new TreeMap<Long, JitterIntervalNodeList>();
		Name = n;
	}
	
	public String getName () {
		return Name;
	}
	
	public void clear() {
		jitterFreq.clear();
	}
	
	public void addJitterEntry (ITmfTimestamp beginTs, ITmfTimestamp endTs) {
		long jitter = endTs.getDelta(beginTs).getValue();
		JitterIntervalNodeList il = jitterFreq.get(jitter);
		
		/* Add new list in case it didn't already exist */
		if (il==null) {
			il = new JitterIntervalNodeList(jitter);
			jitterFreq.put(jitter, il);
		}
		
		il.addIntervalNode(new JitterIntervalNode(beginTs, endTs));
	}
	
	public int getFrequency(long jitter) {
		int freq = 0;
		JitterIntervalNodeList il = jitterFreq.get(jitter);
		
		if (il != null) {
			freq = il.size();
		}
		return freq;
	}
	
	public Object [] getJitterFrequencies () {
		return jitterFreq.keySet().toArray();
	}
	
	public Object [] getJitterIntervals () {
		return jitterFreq.values().toArray();
	}
}
