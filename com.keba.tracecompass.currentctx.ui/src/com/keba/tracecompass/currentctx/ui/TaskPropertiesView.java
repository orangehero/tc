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

package com.keba.tracecompass.currentctx.ui;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelThreadInformationProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

public class TaskPropertiesView extends TmfView {

	private final class InputForTableViewer implements Runnable {
		@Override
		public void run() {
			tableviewer.setInput(tpp.getPrecedenceContent/*getContent*/());
		}
	}

	private static final String VIEW_ID = "Current Task Properties";
	private ITmfTrace currentTrace;
	private TableViewer tableviewer;
	private TaskPropertiesProvider tpp;

	public TaskPropertiesView() {
		super(VIEW_ID);
		tpp = new TaskPropertiesProvider();
		tpp.addCategory("Window Range", 2);
		tpp.addCategory("Time Range", 1);
		tpp.addCategory("Tasks", 0);
		
		
	}

	/*
	 * public TaskPropertiesView(String viewName) { super(viewName); }
	 */

	@Override
	public void createPartControl(Composite parent) {
		tableviewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tableviewer.setContentProvider(ArrayContentProvider.getInstance());

		// Column property
		TableViewerColumn colProperty = new TableViewerColumn(tableviewer,
				SWT.NONE);
		colProperty.getColumn().setWidth(150);
		colProperty.getColumn().setText("Property");
		colProperty.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof TaskProperty) {
					TaskProperty p = (TaskProperty) element;
					return p.getProperty();
				}
				return "unknown";
			}
		});

		// Column value
		TableViewerColumn valProperty = new TableViewerColumn(tableviewer,
				SWT.NONE);
		valProperty.getColumn().setWidth(200);
		valProperty.getColumn().setText("Value");
		valProperty.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof TaskProperty) {
					TaskProperty p = (TaskProperty) element;
					return p.getValue();
				}
				return "unknown";
			}
		});

		final Table tab = tableviewer.getTable();
		tab.setHeaderVisible(true);
		tab.setLinesVisible(true);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@TmfSignalHandler
	public void traceOpened(final TmfTraceOpenedSignal signal) {
		// Don't populate the view again if we're already showing this trace
		if (currentTrace == signal.getTrace()) {
			return;
		} else {
			currentTrace = signal.getTrace();
		}
	}

	@TmfSignalHandler
	public void traceClosed(final TmfTraceClosedSignal signal) {
		currentTrace = null;
		/* clear current entries */
		tpp.clearProperties();
		Display.getDefault().asyncExec(new InputForTableViewer());
	}

	@TmfSignalHandler
	public void traceSelected(final TmfTraceSelectedSignal signal) {
		// Don't populate the view again if we're already showing this trace
		if (currentTrace == signal.getTrace()) {
			return;
		} else {
			currentTrace = signal.getTrace();
		}

		tpp.clearProperties();
		Display.getDefault().asyncExec(new InputForTableViewer());
	}

	@TmfSignalHandler 
	public void catchWindowTimeRange(TmfWindowRangeUpdatedSignal signal) {
		if (signal.getSource() == this || currentTrace == null) {
			return;
		}
		
		tpp.clearPropertiesOfCategory("Window Range");
		ITmfTimestamp beginTS = signal.getCurrentRange().getStartTime();
		ITmfTimestamp endTS = signal.getCurrentRange().getEndTime();
		
		tpp.addProperty("Window Range", new TaskProperty("Window Range", endTS.getDelta(beginTS).toString()));
		
		Display.getDefault().asyncExec(new InputForTableViewer());
	}
	
	@TmfSignalHandler
	public void catchSelectionTimeRange(final TmfSelectionRangeUpdatedSignal signal) {
		if (signal.getSource() == this || currentTrace == null) {
			return;
		}

		tpp.clearPropertiesOfCategory("Time Range");
		tpp.clearPropertiesOfCategory("Tasks");
		
		final ITmfTimestamp beginTS = signal.getBeginTime();
		final ITmfTimestamp endTS = signal.getEndTime();

		for (ITmfTrace trace : TmfTraceManager.getTraceSet(currentTrace)) {

			Integer eventCpu = null;
			KernelAnalysisModule kernelAnalysis = TmfTraceUtils
					.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class,
							KernelAnalysisModule.ID);
			if (kernelAnalysis == null)
				continue;

			ITmfContext ctx = trace.seekEvent(signal.getBeginTime());
			if (ctx != null) {
				ITmfEvent event = trace.getNext(ctx);
				Object cpuObj = TmfTraceUtils
						.resolveEventAspectOfClassForEvent(trace,
								TmfCpuAspect.class, event);
				if (cpuObj != null) {
					eventCpu = (Integer) cpuObj;
				}
			}
			
			final long beginTime = beginTS.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

			ITmfStateSystem ssq = kernelAnalysis.getStateSystem();
			try {
				
				int cpusQuark = ssq.getQuarkAbsolute(Attributes.CPUS);
	            List<Integer> cpuQuarks = ssq.getSubAttributes(cpusQuark, false);
	            for (Integer cpuQuark : cpuQuarks) {
	            	int threadQuark = ssq.getQuarkRelative(cpuQuark, Attributes.CURRENT_THREAD);
					ITmfStateInterval ival = ssq.querySingleState(beginTS.getValue(), threadQuark);
					if (ival.getStateValue().isNull()) {
						continue;
					}
					
					int threadId = ival.getStateValue().unboxInt();
					String threadName;
					if (threadId==0) {
						threadName = "idle";
					} else {
						threadName = KernelThreadInformationProvider
								.getExecutableName(kernelAnalysis, threadId);
					}
					String cpuName = ssq.getAttributeName(cpuQuark);
					Integer threadPrio = KernelThreadInformationProvider
							.getThreadPrio(kernelAnalysis, threadId, beginTime);
					if (eventCpu != null && eventCpu.toString().equals(cpuName)) {
						tpp.addProperty("Tasks", new TaskProperty("==> Thread Name <==", threadName));
					} else {
						tpp.addProperty("Tasks", new TaskProperty("  Thread Name", threadName));
					}
					tpp.addProperty("Tasks", new TaskProperty("  Thread Id", Integer.toString(threadId)));
					tpp.addProperty("Tasks", new TaskProperty("  CPU", cpuName));
					tpp.addProperty("Tasks", new TaskProperty("  Prio", Long
							.toString(threadPrio)));

					tpp.addProperty("Tasks", new TaskProperty("", ""));
					
				}
			} catch (AttributeNotFoundException | StateSystemDisposedException e) {
				// do nothing
			}
			
		}
		
		TaskProperty tp = null;
		if (beginTS.equals(endTS)) {
			tp = new TaskProperty("Time stamp", beginTS.toString());
		} else {
			tp = new TaskProperty("Time range", endTS.getDelta(beginTS).toString());
		}
		tpp.addProperty("Time Range", tp);

		Display.getDefault().asyncExec(new InputForTableViewer());
	}

}
