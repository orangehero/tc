package com.keba.tracecompass.jitter.ui;

import java.util.TreeMap;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

public class SystemJitterColumnLabelProvider extends CellLabelProvider implements ILabelProvider {

	@Override
	public void update(ViewerCell cell) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof TreeMap<?,?>) return "Jitter UOS.Intr-Task";
		else if (element instanceof Double) return element.toString();
		else if (element instanceof Integer) return element.toString();
		return "unknown";
	}

}
