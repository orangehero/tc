/*******************************************************************************
 * Copyright (c) 2015 
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   orangehero - Initial API and implementation
 *******************************************************************************/

package com.keba.tracecompass.jitter.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class SystemJitterTreeContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {

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
