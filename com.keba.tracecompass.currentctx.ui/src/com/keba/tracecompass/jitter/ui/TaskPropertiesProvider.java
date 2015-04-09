package com.keba.tracecompass.jitter.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class TaskPropertiesProvider {
	
	private HashMap<String, ArrayList<TaskProperty>> properties;
	private TreeMap<String, Integer> catPrecedences;
	
	public TaskPropertiesProvider() {
		properties = new HashMap<>();
		catPrecedences = new TreeMap<>();
	}
	
	public void addCategory(String category, int precedence) {
		Integer isCat = catPrecedences.get(category);
		if (isCat == null) {
			catPrecedences.put(category, precedence);
			ArrayList<TaskProperty> ltp = new ArrayList<>();
			properties.put(category, ltp);
		}
	}
	
	public void clearCategories() {
		catPrecedences.clear();
		properties.clear();
	}
	
	public void clearProperties() {
		properties.clear();
		
		for (Map.Entry<String, Integer> entry : catPrecedences.entrySet()) {
			ArrayList<TaskProperty> ltp = new ArrayList<>();
			properties.put(entry.getKey(), ltp);
		}
	}
	
	public void clearPropertiesOfCategory(String category) {
		properties.remove(category);
		ArrayList<TaskProperty> ltp = new ArrayList<>();
		properties.put(category, ltp);
	}
	
	public Object [] getPrecedenceContent () {
		ArrayList<TaskProperty> ltp = new ArrayList<>();
		
		//
		SortedSet<Map.Entry<String,Integer>> sortedEntries = new TreeSet<Map.Entry<String,Integer>>(
		        new Comparator<Map.Entry<String,Integer>>() {
					@Override
					public int compare(Entry<String, Integer> arg0, Entry<String, Integer> arg1) {
						return arg0.getValue().compareTo(arg1.getValue());
					}
		}); 
		sortedEntries.addAll(catPrecedences.entrySet());
		
		for (Map.Entry<String, Integer> entry : sortedEntries) {
			ArrayList<TaskProperty> ll = properties.get(entry.getKey());
			if (ll != null) {
				ltp.addAll(ll);
			}
		}
		
		return ltp.toArray();
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
		if (ltp != null) {
			ltp.add(tp);
		}
	}
}
