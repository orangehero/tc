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
