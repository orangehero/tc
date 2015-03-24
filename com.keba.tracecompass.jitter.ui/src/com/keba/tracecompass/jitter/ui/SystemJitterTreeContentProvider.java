package com.keba.tracecompass.jitter.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class SystemJitterTreeContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof JitterRootNode) {
			return ((JitterRootNode)inputElement).getContentNodes();
		}
		
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof JitterIntervalList) {
			return ((JitterIntervalList)parentElement).getJitterIntervals();
		} else if (parentElement instanceof JitterIntervalNodeList) {
			return ((JitterIntervalNodeList)parentElement).getContentNodes();
		}
		
		return null;
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof JitterRootNode) {
			return ((JitterRootNode)element).getContentNodes().length>0;
		} else if (element instanceof JitterIntervalList) {
			return ((JitterIntervalList)element).getJitterIntervals().length>0;
		} else if (element instanceof JitterIntervalNodeList) {
			return ((JitterIntervalNodeList)element).getContentNodes().length>0;
		}
		
		return false;
	}

}
