package com.keba.tracecompass.jitter.ui;

public class TaskProperties {
	
	private String property;
	private String value;
	
	public TaskProperties(String prop, String val) {
		property = prop;
		value = val;
	}
	
	public void setProperty(String prop) {
		property = prop;
	}
	
	public void setValue(String val) {
		value = val;
	}
	
	public String getProperty() {
		return property;
	}
	
	public String getValue() {
		return value;
	}

}
