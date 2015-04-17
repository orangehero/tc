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

import java.util.HashMap;

public class JitterRootNode {
	
	private class MinMaxStruct {
		public double minY = Double.MAX_VALUE;
		public double maxY = -Double.MAX_VALUE;
	}
	
	private HashMap<String, JitterIntervalList> jitterDiagrams;
	private HashMap<String, MinMaxStruct> jitterDiagramsMinMax;
	
	JitterRootNode () {
		jitterDiagrams = new HashMap<String, JitterIntervalList>();
		jitterDiagramsMinMax = new HashMap<String, MinMaxStruct>();
	}
	
	public void createNewJitterDiagram(String Name) {
		if (!jitterDiagrams.containsKey(Name)) {
			jitterDiagrams.put(Name, new JitterIntervalList(Name));
		}
		if (!jitterDiagramsMinMax.containsKey(Name)) {
			jitterDiagramsMinMax.put(Name, new MinMaxStruct());
		}
	}
	
	public void cleanJitterEntries() {
		jitterDiagrams.clear();
		jitterDiagramsMinMax.clear();
	}
	
	public void cleanJitterDiagram(String Name) {
		JitterIntervalList il = jitterDiagrams.get(Name);
		if (il != null) {
			il.clear();
		}
		MinMaxStruct mms = jitterDiagramsMinMax.get(Name);
		if (mms != null) {
			mms.minY = Double.MAX_VALUE;
			mms.maxY = -Double.MAX_VALUE;
		}
	}

	public boolean addJitterEntry(String Name, double beginTs, double endTs, boolean createDiagram) {
		JitterIntervalList il = jitterDiagrams.get(Name);
		if (il == null && createDiagram) {
			createNewJitterDiagram(Name);
			il = jitterDiagrams.get(Name);
		}
		if (il != null) {
			il.addJitterEntry(beginTs, endTs);
			return true;
		} 
		return false;
	}
	
	private double[] toArray (Object[] list) {
        double[] d = new double[list.length];
        for (int i = 0; i < list.length; ++i) {
        	if (list[i] instanceof Integer) {
        		int val = (int) list[i];
        		d[i] = val;
        	} else if (list[i] instanceof Double) {
        		d[i] = (double) list[i];
        	}
        }

        return d;
    }
	
	public double [] getYValues (String Name) {
		JitterIntervalList il = jitterDiagrams.get(Name);
		MinMaxStruct mms = jitterDiagramsMinMax.get(Name);
		if (il != null && mms != null) {
			Object [] freqList = il.getJitterFrequencies();
			double[] d = new double[freqList.length];
			
			for (int i = 0; i < freqList.length; ++i) {
				double jitter = (Double)freqList[i];
				d[i] = il.getFrequency(jitter);
				mms.minY = Math.min(mms.minY, d[i]);
				mms.maxY = Math.max(mms.maxY, d[i]);
			}
			return d;
		}
		return null;
	}
	
	public double [] getXValues (String Name) {
		JitterIntervalList il = jitterDiagrams.get(Name);
		if (il != null) {
			Object [] freqList = il.getJitterFrequencies();
			return toArray(freqList);
		}
		
		return null;
	}
	
	public double getYMin(String Name) {
		MinMaxStruct mms = jitterDiagramsMinMax.get(Name);
		if (mms != null) {
			return mms.minY;
		}
		return Double.MAX_VALUE;
	}

	public double getYMax(String Name) {
		MinMaxStruct mms = jitterDiagramsMinMax.get(Name);
		if (mms != null) {
			return mms.maxY;
		}
		return -Double.MAX_VALUE;
	}
	
	public Object [] getContentNodes() {
		return jitterDiagrams.values().toArray();
	}
	
	public Object [] getKeyNodes() {
		return jitterDiagrams.keySet().toArray();
	}
	
}
