package com.keba.tracecompass.jitter.ui;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelThreadInformationProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

public class TaskPropertiesView extends TmfView {

	private final class InputForTableViewer implements Runnable {
		@Override
		public void run() {
			tableviewer.setInput(ltp.toArray());
		}
	}

	private static final String VIEW_ID = "Current Task Properties";
	private ITmfTrace currentTrace;
	private TableViewer tableviewer;
	private ArrayList<TaskProperties> ltp;

	public TaskPropertiesView() {
		super(VIEW_ID);
		ltp = new ArrayList<TaskProperties>();
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
				if (element instanceof TaskProperties) {
					TaskProperties p = (TaskProperties) element;
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
				if (element instanceof TaskProperties) {
					TaskProperties p = (TaskProperties) element;
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
		ltp.clear();
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

		ltp.clear();
		Display.getDefault().asyncExec(new InputForTableViewer());
	}

	@TmfSignalHandler
	public void synchToTime(final TmfTimeSynchSignal signal) {
		if (signal.getSource() == this || currentTrace == null) {
			return;
		}

		ltp.clear();
		final long beginTime = signal.getBeginTime()
				.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

		for (ITmfTrace trace : TmfTraceManager.getTraceSet(currentTrace)) {

			Integer eventCpu = null;
			KernelAnalysis kernelAnalysis = TmfTraceUtils
					.getAnalysisModuleOfClass(trace, KernelAnalysis.class,
							KernelAnalysis.ID);
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

			for (int cpuNr = 0; cpuNr < 32; cpuNr++) {
				Integer threadId = KernelThreadInformationProvider
						.getThreadOnCpu(kernelAnalysis, cpuNr, beginTime);
				if (threadId != null) {
					String threadName = KernelThreadInformationProvider
							.getExecutableName(kernelAnalysis, threadId);
					Integer threadPrio = KernelThreadInformationProvider
							.getThreadPrio(kernelAnalysis, threadId, beginTime);

					if (eventCpu != null && eventCpu.equals(cpuNr)) {
						ltp.add(new TaskProperties("==> Current Thread <==",
								threadId.toString()));
					} else {
						ltp.add(new TaskProperties("Current Thread", threadId
								.toString()));
					}
					ltp.add(new TaskProperties("  Current CPU", Integer
							.toString(cpuNr)));
					ltp.add(new TaskProperties("  Current ThreadName",
							threadName));

					ltp.add(new TaskProperties("  Current Prio", Long
							.toString(threadPrio)));

					ltp.add(new TaskProperties("", ""));

				}
			}
		}

		TmfTraceManager traceManager = TmfTraceManager.getInstance();
		TmfTraceContext traceContext = traceManager.getCurrentTraceContext();
		TmfTimeRange winRange = traceContext.getWindowRange();
		TmfTimeRange selRange = traceContext.getSelectionRange();

		long beginTS = selRange.getStartTime().getValue();
		long endTS = selRange.getEndTime().getValue();
		
		ITmfTimestamp deltaTS;

		/* no selection, take window range */
		if (beginTS == endTS) {
			deltaTS = winRange.getEndTime().getDelta(winRange.getStartTime());
		} else {
			deltaTS = selRange.getEndTime().getDelta(selRange.getStartTime());
		}

		ltp.add(new TaskProperties("Time range", deltaTS.toString()));

		Display.getDefault().asyncExec(new InputForTableViewer());
	}

}
