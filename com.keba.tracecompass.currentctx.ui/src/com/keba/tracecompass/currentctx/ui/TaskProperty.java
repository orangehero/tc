package com.keba.tracecompass.currentctx.ui;

public class TaskProperty {
	
	private String property;
	private String value;
	
	public TaskProperty(String prop, String val) {
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
