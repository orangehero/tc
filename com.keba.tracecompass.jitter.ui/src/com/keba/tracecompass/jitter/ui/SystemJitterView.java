package com.keba.tracecompass.jitter.ui;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.swtchart.Chart;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.Range;

public class SystemJitterView extends TmfView {

    private static final String SERIES_NAME = "Series";
    private static final String Y_AXIS_TITLE = "Frequency";
    private static final String X_AXIS_TITLE = "Jitter";
    private static final String VIEW_ID = "com.keba.tracecompass.jitter.ui.view";
    private Chart chart;
    private TreeViewer treeViewer;
    private CTabFolder tabs;
    private ITmfTrace currentTrace;
    private JitterRootNode jitnode;
    private TmfSignalThrottler throttler;
	
    public class TmfChartTimeStampFormat extends SimpleDateFormat {
        private static final long serialVersionUID = 1L;
        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        	Long time = date.getTime();
            toAppendTo.append(time.toString());
            return toAppendTo;
        }
    }
    
	public SystemJitterView(String viewName) {
		super(viewName);
		throttler = new TmfSignalThrottler(this, 200);
	}	
	
	public SystemJitterView() {
		super(VIEW_ID);
		throttler = new TmfSignalThrottler(this, 200);
	}

	@Override
	public void createPartControl(Composite parent) {
		tabs = new CTabFolder(parent, SWT.BORDER);
		CTabItem item1 = new CTabItem(tabs,  SWT.BORDER);
		item1.setText("System Jitter Graph");
		CTabItem item2 = new CTabItem(tabs,  SWT.BORDER);
		item2.setText("Jitter Bookmarks");
		
		chart = new Chart(tabs, SWT.BORDER);
        chart.getTitle().setVisible(false);
        chart.getAxisSet().getXAxis(0).getTitle().setText(X_AXIS_TITLE);
        chart.getAxisSet().getYAxis(0).getTitle().setText(Y_AXIS_TITLE);
        chart.getAxisSet().getXAxis(0).getTick().setFormat(new TmfChartTimeStampFormat());
        chart.getSeriesSet().createSeries(SeriesType.LINE, SERIES_NAME);
        chart.getLegend().setVisible(false);
        
        treeViewer = new TreeViewer(tabs, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        treeViewer.setContentProvider(new SystemJitterTreeContentProvider());
        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.setUseHashlookup(true);
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				Object o = sel.getFirstElement();
				if (o instanceof JitterIntervalNode) {
					JitterIntervalNode jin = (JitterIntervalNode)o;
					TmfTimestamp beginTs = new TmfTimestamp((long) jin.getStartTs(), ITmfTimestamp.NANOSECOND_SCALE);
			        TmfTimestamp endTs = new TmfTimestamp((long) jin.getEndTs(), ITmfTimestamp.NANOSECOND_SCALE);
			        TmfTimeSynchSignal signal = new TmfTimeSynchSignal(this, beginTs, endTs);
			        throttler.queue(signal);
				}
			}
		});
        
        final TreeViewerColumn treeColumn1 = new TreeViewerColumn(treeViewer, 0);
        treeColumn1.getColumn().setText("Name");
        treeColumn1.getColumn().setWidth(200);
        treeColumn1.getColumn().setToolTipText("Tooltip");
        treeColumn1.setLabelProvider(new ColumnLabelProvider() {
        	@Override
        	public String getText(Object element) {
        		if (element instanceof JitterIntervalList) {
        			return ((JitterIntervalList)element).getName();
        		} else if (element instanceof JitterIntervalNodeList) {
        			return Double.toString(((JitterIntervalNodeList)element).getJitter());
        		}
        		return "";
        	}
        });
        
        final TreeViewerColumn treeColumn2 = new TreeViewerColumn(treeViewer, 0);
        treeColumn2.getColumn().setText("Start Timestamp");
        treeColumn2.getColumn().setWidth(100);
        treeColumn2.getColumn().setToolTipText("Tooltip");
        treeColumn2.setLabelProvider(new ColumnLabelProvider() {
        	@Override
        	public String getText(Object element) {
        		if (element instanceof JitterIntervalNode) {
        			double sts = ((JitterIntervalNode)element).getStartTs();
        			return Double.toString(sts);
        		}
        		return "";
        	}
        });
        
        final TreeViewerColumn treeColumn3 = new TreeViewerColumn(treeViewer, 0);
        treeColumn3.getColumn().setText("End Timestamp");
        treeColumn3.getColumn().setWidth(100);
        treeColumn3.getColumn().setToolTipText("Tooltip");
        treeColumn3.setLabelProvider(new ColumnLabelProvider() {
        	@Override
        	public String getText(Object element) {
        		if (element instanceof JitterIntervalNode) {
        			double ets = ((JitterIntervalNode)element).getEndTs();
        			return Double.toString(ets);
        		}
        		return "";
        	}
        });
        
        
        item1.setControl(chart);
        item2.setControl(treeViewer.getControl());
        tabs.setSelection(item1);
        
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace trace = traceManager.getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
        
        jitnode = new JitterRootNode();
        jitnode.createNewJitterDiagram("UOS.Intr-Task Jitter");
	}

	@Override
	public void setFocus() {
		chart.setFocus();
	}
	
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        // Called when the time stamp preference is changed
        chart.getAxisSet().getXAxis(0).getTick().setFormat(new TmfChartTimeStampFormat());
        chart.redraw();
    }
	
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        // Don't populate the view again if we're already showing this trace
        if (currentTrace == signal.getTrace()) {
            return;
        }
        currentTrace = signal.getTrace();
        jitnode.cleanJitterEntries("UOS.Intr-Task Jitter");
        

        // Create the request to get data from the trace

        TmfEventRequest req = new TmfEventRequest(TmfEvent.class,
                TmfTimeRange.ETERNITY, 0, ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.BACKGROUND) {

        	private double lastUosIntrTs = 0.0;
            private double lastTimerIntTs = 0.0;
            private double minFreq   = Double.MAX_VALUE;
            private double maxFreq   = -Double.MAX_VALUE;
            private boolean timerInterruptOccurred = false;

        	
            @Override
            public void handleData(ITmfEvent data) {
                // Called for each event
                super.handleData(data);
                
                if (data.getType().getName().equals("softirq_raise")) {
                	ITmfEventField action = data.getContent().getField("action");
                	if (action != null) {
                		String sact = (String)action.getValue();
                		if (sact.equals("TIMER]")) {
                			// We just found the timer interrupt.
                			double ts = (double) data.getTimestamp().getValue();
                			if (lastTimerIntTs != 0.0) {
                				// do something statistical
                			}
                			lastTimerIntTs = ts;
                			timerInterruptOccurred = true;
                		}
                	}
                }
                /* sched_switch to UOS.Intr-Task
                 * only relevant if a timerinterrupt has occurred before */
                else if (timerInterruptOccurred && data.getType().getName().equals("sched_switch")) {
                	ITmfEventField next_comm = data.getContent().getField("next_comm");
                	if (next_comm != null) {
                		String spc = (String)next_comm.getValue();
                		if (spc.equals("UOS.Intr-Task")) {
                			double ts = (double) data.getTimestamp().getValue();
                			/* first occurrence of event ... */
                			if (lastUosIntrTs != 0.0) {
                				jitnode.addJitterEntry("UOS.Intr-Task Jitter", lastUosIntrTs, ts);
                			}
                			lastUosIntrTs = ts;
                			timerInterruptOccurred = false; /* reset timer interrupt occurrence */
                		}
                	}
                }
            }

            @Override
            public void handleSuccess() {
                // Request successful, not more data available
                super.handleSuccess();
                lastUosIntrTs = 0.0;
                timerInterruptOccurred = false;
                
                final double x[] = jitnode.getXValues("UOS.Intr-Task Jitter");
                final double y[] = jitnode.getYValues("UOS.Intr-Task Jitter");
                minFreq = jitnode.getYMin();
                maxFreq = jitnode.getYMax();

                // This part needs to run on the UI thread since it updates the chart SWT control
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {

                        chart.getSeriesSet().getSeries()[0].setXSeries(x);
                        chart.getSeriesSet().getSeries()[0].setYSeries(y);

                        // Set the new range
                        if (x.length>0 && y.length>0) {
                            chart.getAxisSet().getXAxis(0).setRange(new Range(0, x[x.length - 1]));
                            chart.getAxisSet().getYAxis(0).setRange(new Range(minFreq, maxFreq));
                        } else {
                            chart.getAxisSet().getXAxis(0).setRange(new Range(0, 1));
                            chart.getAxisSet().getYAxis(0).setRange(new Range(0, 1));
                        }
                        chart.getAxisSet().adjustRange();
                        
                        chart.redraw();
                        
                        treeViewer.setInput(jitnode);
                    }

                });
            }
            
            @Override
            public void handleFailure() {
                // Request failed, not more data available
                super.handleFailure();
                lastUosIntrTs = 0.0;
                timerInterruptOccurred = false;
            }
        };
        ITmfTrace trace = signal.getTrace();
        trace.sendRequest(req);
    }
    
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
    	Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	treeViewer.setInput(null);
            	
            	chart.getSeriesSet().getSeries()[0].setXSeries(new double[0]);
                chart.getSeriesSet().getSeries()[0].setYSeries(new double[0]);
                chart.redraw();
            }
    	});
    }

}
