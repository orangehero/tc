package com.keba.tracecompass.jitter.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskPropertiesProvider {
	
	private HashMap<String, ArrayList<TaskProperty>> properties;
	
	public TaskPropertiesProvider() {
		properties = new HashMap<>();
	}
	
	public void clearall() {
		properties.clear();
	}
	
	public void clearCategory(String category) {
		properties.remove(category);
	}
	
	public Object [] getContent () {
		ArrayList<TaskProperty> ltp = new ArrayList<>();
		for (Map.Entry<String, ArrayList<TaskProperty>> entry : properties.entrySet()) {
			ltp.addAll(entry.getValue());
		}
		return ltp.toArray();
	}
	
	public void addProperty(String category, TaskProperty tp) {
		ArrayList<TaskProperty> ltp = properties.get(category);
		if (ltp == null) {
			ltp = new ArrayList<>();
			properties.put(category, ltp);
		}
		ltp.add(tp);
	}
}
