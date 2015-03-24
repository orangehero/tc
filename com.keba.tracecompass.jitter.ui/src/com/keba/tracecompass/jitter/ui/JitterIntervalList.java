package com.keba.tracecompass.jitter.ui;

import java.util.TreeMap;

public class JitterIntervalList {

	private TreeMap<Double, JitterIntervalNodeList> jitterFreq;
	private String Name;
	
	public JitterIntervalList(String n) {
		jitterFreq = new TreeMap<Double, JitterIntervalNodeList>();
		Name = n;
	}
	
	public String getName () {
		return Name;
	}
	
	public void clear() {
		jitterFreq.clear();
	}
	
	public void addJitterEntry (double beginTs, double endTs) {
		double jitter = endTs - beginTs;
		JitterIntervalNodeList il = jitterFreq.get(jitter);
		
		/* Add new list in case it didn't already exist */
		if (il==null) {
			il = new JitterIntervalNodeList(jitter);
			jitterFreq.put(jitter, il);
		}
		
		il.addIntervalNode(new JitterIntervalNode(beginTs, endTs));
	}
	
	public int getFrequency(double jitter) {
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
