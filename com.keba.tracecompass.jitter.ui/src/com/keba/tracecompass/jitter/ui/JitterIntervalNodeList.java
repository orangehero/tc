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
