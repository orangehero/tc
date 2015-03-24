package com.keba.tracecompass.jitter.ui;

import java.util.HashMap;

public class JitterRootNode {
	
	private HashMap<String, JitterIntervalList> jitterDiagrams;
    private double minY   = Double.MAX_VALUE;
    private double maxY   = -Double.MAX_VALUE;
	
	JitterRootNode () {
		jitterDiagrams = new HashMap<String, JitterIntervalList>();
	}
	
	public void createNewJitterDiagram(String Name) {
		if (!jitterDiagrams.containsKey(Name)) {
			JitterIntervalList il = new JitterIntervalList(Name);
			jitterDiagrams.put(Name, il);
		}
	}
	
	public void cleanJitterEntries(String Name) {
		JitterIntervalList il = jitterDiagrams.get(Name);
		if (il != null) {
			il.clear();
		}
	}

	public boolean addJitterEntry(String Name, double beginTs, double endTs) {
		JitterIntervalList il = jitterDiagrams.get(Name);
		if (il != null) {
			il.addJitterEntry(beginTs, endTs);
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
		if (il != null) {
			Object [] freqList = il.getJitterFrequencies();
			double[] d = new double[freqList.length];
			
			for (int i = 0; i < freqList.length; ++i) {
				double jitter = (Double)freqList[i];
				d[i] = il.getFrequency(jitter);
				minY = Math.min(minY, d[i]);
				maxY = Math.max(maxY, d[i]);
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
	
	public double getYMin() {
		return minY;
	}

	public double getYMax() {
		return maxY;
	}
	
	public Object [] getContentNodes() {
		return jitterDiagrams.values().toArray();
	}
	
}
