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

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.views.filter.FilterDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.Range;

public class SystemJitterView extends TmfView {

	private static final String PLUGIN_ID = "com.keba.tracecompass.jitter.ui";
	private static final String VIEW_ID = "com.keba.tracecompass.jitter.ui.view";
	
	private static final Image ADD_IMAGE = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "/icons/add_button.gif").createImage();
	private static final Image DELETE_IMAGE = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "/icons/delete_button.gif").createImage();
	private static final Image EXPORT_IMAGE = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "/icons/export_button.gif").createImage();
	private static final Image IMPORT_IMAGE = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "/icons/import_button.gif").createImage();
	
    private static final String Y_AXIS_TITLE = "Frequency";
    private static final String X_AXIS_TITLE = "Jitter";
    private static final int TAB_INTERVAL_INDEX = 1;
    private static final int TAB_FILTER_INDEX = 2;
    
    private Shell fShell;
    private Chart fChart;
    private TreeViewer fIntervalTreeViewer;
    private TableViewer fIntervalFilterTableViewer;
    private CTabFolder fTabs;
    private ITmfTrace fCurrentTrace;
    private JitterRootNode fjitterNodes;
    private TmfSignalThrottler fThrottler;

    private Action addFilterAction;
    private Action deleteFilterAction;
    private Action importFilterAction;
    private Action exportFilterAction;
    
    private List<IntervalFilterSetting> fIntervalSettings;
    
    private class TmfChartTimeStampFormat extends SimpleDateFormat {
        private static final long serialVersionUID = 1L;
        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        	Long time = date.getTime();
            toAppendTo.append(time.toString());
            return toAppendTo;
        }
    }
    
    private class AddFilterAction extends Action {
    	@Override
    	public void run() {
    		if (fTabs.getSelectionIndex() != TAB_FILTER_INDEX) {
    			return;
    		}
    		fIntervalSettings.add(new IntervalFilterSetting());
    		fIntervalFilterTableViewer.setInput(fIntervalSettings.toArray());
    	}
    };
    
    private class DeleteFilterAction extends Action {
    	@Override
    	public void run() {
    		if (fTabs.getSelectionIndex() != TAB_FILTER_INDEX) {
    			return;
    		}
    		int selectionIndex = 0;
    		StructuredSelection sel = (StructuredSelection)fIntervalFilterTableViewer.getSelection();
    		Iterator<?> it = sel.iterator(); 
    		while (it.hasNext()) {
    			IntervalFilterSetting ifs = (IntervalFilterSetting)it.next();
    			selectionIndex = fIntervalSettings.indexOf(ifs);
    			fIntervalSettings.remove(ifs);
    		}
    		fIntervalFilterTableViewer.setInput(fIntervalSettings.toArray());
    		int iValSettingsSize = fIntervalSettings.size();
    		if (iValSettingsSize > 0) {
	    		if (selectionIndex >= iValSettingsSize) {
	    			selectionIndex = iValSettingsSize - 1;
	    		}
				fIntervalFilterTableViewer.setSelection(new StructuredSelection(
						fIntervalFilterTableViewer.getElementAt(selectionIndex)),
						true);
    		}
    	}
    };
    
    private class ImportFilterAction extends Action {
    	@Override
    	public void run() {
    		FileDialog fileDialog = new FileDialog(fShell, SWT.OPEN);
            fileDialog.setFilterExtensions(new String[] {"*.xml"}); //$NON-NLS-1$
            String pathName = fileDialog.open();
            if (pathName != null) {
            	 IntervalFilterSetting[] ivalSettings = IntervalFilterXML.load(pathName);
            	 fIntervalSettings.clear();
            	 for (IntervalFilterSetting e : ivalSettings) {
            		 fIntervalSettings.add(e);
            	 }
            	 fIntervalFilterTableViewer.setInput(fIntervalSettings.toArray());
            }
    	}
    };
    
    private class ExportFilterAction extends Action {
    	@Override
    	public void run() {
    		FileDialog fileDialog = new FileDialog(fShell, SWT.SAVE);
            fileDialog.setFilterExtensions(new String[] {"*.xml"}); //$NON-NLS-1$
            fileDialog.setOverwrite(true);
            String pathName = fileDialog.open();
            if (pathName != null) {
            	IntervalFilterXML.save(pathName, fIntervalSettings.toArray(new IntervalFilterSetting[0]));//fColorSettings.toArray(new ColorSetting[0]));
            }
    	}
    };
    
    private class SetEventFilterListener implements Listener {
    	@Override
		public void handleEvent(Event event) {
			Point pt = new Point(event.x, event.y);
			TableItem item = fIntervalFilterTableViewer.getTable().getItem(pt);
			if (item != null) {
				Object o = item.getData();
				if (o instanceof IntervalFilterSetting) {
					IntervalFilterSetting ival = (IntervalFilterSetting)o;
					int index = TAB_INTERVAL_INDEX;
					Rectangle rect1 = item.getBounds(index);
					if (rect1.contains(pt)) {
						FilterDialog dialog = new FilterDialog(fShell);
						dialog.setFilter(ival.beginFilter);
			            dialog.open();
			            if (dialog.getReturnCode() == Window.OK) {
			            	ival.beginFilter = dialog.getFilter();
			            	if (ival.beginFilter != null) {
			            		item.setText(index, ival.beginFilter.toString());
			            	}
			            }
					}
					Rectangle rect2 = item.getBounds(TAB_FILTER_INDEX);
					if (rect2.contains(pt)) {
						FilterDialog dialog = new FilterDialog(fShell);
						dialog.setFilter(ival.endFilter);
			            dialog.open();
			            if (dialog.getReturnCode() == Window.OK) {
			            	ival.endFilter = dialog.getFilter();
			            	if (ival.endFilter != null) {
			            		item.setText(TAB_FILTER_INDEX, ival.endFilter.toString());
			            	}
			            }
					}
				}
			}
		}
    }
    
	public SystemJitterView(String viewName) {
		super(viewName);
		fThrottler = new TmfSignalThrottler(this, 200);
		fIntervalSettings = new ArrayList<>();
	}	
	
	public SystemJitterView() {
		super(VIEW_ID);
		fThrottler = new TmfSignalThrottler(this, 200);
		fIntervalSettings = new ArrayList<>();
	}
	
	/* create the tree of intervals. */
	private void createIntervalTreeViewer() {
		fIntervalTreeViewer = new TreeViewer(fTabs, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        fIntervalTreeViewer.setContentProvider(new SystemJitterTreeContentProvider());
        fIntervalTreeViewer.getTree().setHeaderVisible(true);
        fIntervalTreeViewer.setUseHashlookup(true);
        fIntervalTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				Object o = sel.getFirstElement();
				if (o instanceof JitterIntervalNode) {
					JitterIntervalNode jin = (JitterIntervalNode)o;
			        TmfSelectionRangeUpdatedSignal signal = new TmfSelectionRangeUpdatedSignal(this, jin.getStartTs(), jin.getEndTs());
			        fThrottler.queue(signal);
				}
			}
		});
        
        final TreeViewerColumn treeColumn1 = new TreeViewerColumn(fIntervalTreeViewer, 0);
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
        
        final TreeViewerColumn treeColumn2 = new TreeViewerColumn(fIntervalTreeViewer, 0);
        treeColumn2.getColumn().setText("Start Timestamp");
        treeColumn2.getColumn().setWidth(100);
        treeColumn2.getColumn().setToolTipText("Tooltip");
        treeColumn2.setLabelProvider(new ColumnLabelProvider() {
        	@Override
        	public String getText(Object element) {
        		if (element instanceof JitterIntervalNode) {
        			return ((JitterIntervalNode)element).getStartTs().toString();
        		}
        		return "";
        	}
        });
        
        final TreeViewerColumn treeColumn3 = new TreeViewerColumn(fIntervalTreeViewer, 0);
        treeColumn3.getColumn().setText("End Timestamp");
        treeColumn3.getColumn().setWidth(100);
        treeColumn3.getColumn().setToolTipText("Tooltip");
        treeColumn3.setLabelProvider(new ColumnLabelProvider() {
        	@Override
        	public String getText(Object element) {
        		if (element instanceof JitterIntervalNode) {
        			return ((JitterIntervalNode)element).getEndTs().toString();
        		}
        		return "";
        	}
        });
	}
	
	private void createIntervalFilterTable() {
		fIntervalFilterTableViewer = new TableViewer(fTabs, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		fIntervalFilterTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		fIntervalFilterTableViewer.getTable().setHeaderVisible(true);
		
		TableViewerFocusCellManager tvfcm = new TableViewerFocusCellManager(fIntervalFilterTableViewer, new FocusCellOwnerDrawHighlighter(fIntervalFilterTableViewer));
		TableViewerEditor.create(fIntervalFilterTableViewer, tvfcm, new ColumnViewerEditorActivationStrategy(fIntervalFilterTableViewer){
		    protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {  
		        return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION;
		    }
		}, ColumnViewerEditor.DEFAULT);
		
		final String [] columnProperties = {"Name", "1st event filter", "2nd event filter"};
		fIntervalFilterTableViewer.setColumnProperties(columnProperties);
		
		CellEditor[] editors = {new TextCellEditor(fIntervalFilterTableViewer.getTable()), null, null};
		fIntervalFilterTableViewer.setCellEditors(editors);
		fIntervalFilterTableViewer.setCellModifier(new ICellModifier() {
			
			@Override
			public void modify(Object element, String property, Object value) {
				if (element != null && element instanceof TableItem) {
					IntervalFilterSetting ival = (IntervalFilterSetting)((TableItem)element).getData();
					ival.name = (String)value;
				}
				fIntervalFilterTableViewer.refresh();
			}
			
			@Override
			public Object getValue(Object element, String property) {
				if (element != null && element instanceof IntervalFilterSetting) {
					IntervalFilterSetting ival = (IntervalFilterSetting)element;
					return ival.name;
				}
				return "";
			}
			
			@Override
			public boolean canModify(Object element, String property) {
				if (property.equals(columnProperties[0])) return true;
				return false;
			}
		});
		
		fIntervalFilterTableViewer.getTable().addListener(SWT.MouseDoubleClick, new SetEventFilterListener());
		
		TableViewerColumn colName = new TableViewerColumn(fIntervalFilterTableViewer, SWT.NONE);
		colName.getColumn().setWidth(200);
		colName.getColumn().setText(columnProperties[0]);
		colName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element != null && element instanceof IntervalFilterSetting) {
					IntervalFilterSetting ival = (IntervalFilterSetting)element;
					return (ival.name == null || ival.name.equals("")) ? "Set name ..." : ival.name;
				} 
				
				return "";
			}
		});
		
		TableViewerColumn colBeginFilter = new TableViewerColumn(fIntervalFilterTableViewer, SWT.NONE);
		colBeginFilter.getColumn().setWidth(200);
		colBeginFilter.getColumn().setText(columnProperties[TAB_INTERVAL_INDEX]);
		colBeginFilter.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element != null && element instanceof IntervalFilterSetting) {
					IntervalFilterSetting ival = (IntervalFilterSetting)element;
					return (ival.beginFilter == null) ? "No filter set." : ival.beginFilter.toString();
				} 
				
				return "";
			}
		});
		
		TableViewerColumn colEndFilter = new TableViewerColumn(fIntervalFilterTableViewer, SWT.NONE);
		colEndFilter.getColumn().setWidth(200);
		colEndFilter.getColumn().setText(columnProperties[TAB_FILTER_INDEX]);
		colEndFilter.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element != null && element instanceof IntervalFilterSetting) {
					IntervalFilterSetting ival = (IntervalFilterSetting)element;
					return (ival.endFilter == null) ? "No filter set." : ival.beginFilter.toString();
				} 
				
				return "";
			}
		});
	}
	
	/**
	 * Create the action bar for adding, importing, exporting filters. 
	 * */
	private void createActionBar () {
		addFilterAction = new AddFilterAction();
        addFilterAction.setImageDescriptor(ImageDescriptor.createFromImage(ADD_IMAGE));
        addFilterAction.setToolTipText("Add new filter");
        
        deleteFilterAction = new DeleteFilterAction();
        deleteFilterAction.setImageDescriptor(ImageDescriptor.createFromImage(DELETE_IMAGE));
        deleteFilterAction.setToolTipText("Delete filter");
        
        importFilterAction = new ImportFilterAction();
        importFilterAction.setImageDescriptor(ImageDescriptor.createFromImage(IMPORT_IMAGE));
        importFilterAction.setToolTipText("Import filters from File");
        
        exportFilterAction = new ExportFilterAction();
        exportFilterAction.setImageDescriptor(ImageDescriptor.createFromImage(EXPORT_IMAGE));
        exportFilterAction.setToolTipText("Export filters to File");

        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(addFilterAction);
        manager.add(deleteFilterAction);
        manager.add(importFilterAction);
        manager.add(exportFilterAction);
	}

	@Override
	public void createPartControl(Composite parent) {
		fShell = parent.getShell();
	
		fTabs = new CTabFolder(parent, SWT.BORDER);
		CTabItem item1 = new CTabItem(fTabs,  SWT.BORDER);
		item1.setText("System Jitter Graph");
		CTabItem item2 = new CTabItem(fTabs,  SWT.BORDER);
		item2.setText("Jitter Bookmarks");
		CTabItem item3 = new CTabItem(fTabs,  SWT.BORDER);
		item3.setText("Filter");
		
		fChart = new Chart(fTabs, SWT.BORDER);
        fChart.getTitle().setVisible(false);
        fChart.getAxisSet().getXAxis(0).getTitle().setText(X_AXIS_TITLE);
        fChart.getAxisSet().getYAxis(0).getTitle().setText(Y_AXIS_TITLE);
        fChart.getAxisSet().getXAxis(0).getTick().setFormat(new TmfChartTimeStampFormat());
        fChart.getLegend().setVisible(false);
        
        createIntervalTreeViewer();
        
        createIntervalFilterTable();
        
        item1.setControl(fChart);
        item2.setControl(fIntervalTreeViewer.getControl());
        item3.setControl(fIntervalFilterTableViewer.getControl());
        fTabs.setSelection(item1);
        
        createActionBar();
        
        fjitterNodes = new JitterRootNode();
        
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace trace = traceManager.getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
	}

	@Override
	public void setFocus() {
		fChart.setFocus();
	}
	
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        // Called when the time stamp preference is changed
        fChart.getAxisSet().getXAxis(0).getTick().setFormat(new TmfChartTimeStampFormat());
        fChart.redraw();
    }
	
    @TmfSignalHandler
    public void traceOpened(final TmfTraceOpenedSignal signal) {
        System.out.println("XXX Update Trace upon traceOpened!");
        setSelectedTrace(signal.getTrace());
    }
    
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
    	System.out.println("XXX Update Trace upon traceSelected!");
    	setSelectedTrace(signal.getTrace());
    }
    
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
    	Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	if (fIntervalTreeViewer.getControl().isDisposed()) {
            		return;
            	}
            	
            	fIntervalTreeViewer.setInput(null);
            	
            	fChart.getSeriesSet().getSeries()[0].setXSeries(new double[0]);
                fChart.getSeriesSet().getSeries()[0].setYSeries(new double[0]);
                fChart.redraw();
            }
    	});
    }
    
    private void setSelectedTrace(ITmfTrace trace) {
    	// Don't populate the view again if we're already showing this trace
        if (fCurrentTrace == trace) {
        	System.out.println("XXX Trace already selected!");
            return;
        }
        fCurrentTrace = trace;
        fjitterNodes.cleanJitterEntries();
        for (IntervalFilterSetting ival : fIntervalSettings) {
        	ival.matchBeginTS = null;
        }
        
        // Create the request to get data from the trace
        TmfEventRequest req = new TmfEventRequest(TmfEvent.class,
                TmfTimeRange.ETERNITY, 0, ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.BACKGROUND) {

        	
            @Override
            public void handleData(ITmfEvent data) {
                super.handleData(data);
                
                for (IntervalFilterSetting ival : fIntervalSettings) {
                	if (ival.beginFilter != null && ival.beginFilter.matches(data)) {
                		ITmfTimestamp ts = data.getTimestamp();
                		if (ival.endFilter == null && ival.matchBeginTS != null) {
                				fjitterNodes.addJitterEntry(ival.name, ival.matchBeginTS, ts, true);
                		}
                		ival.matchBeginTS = ts;
                	}
                	if (ival.endFilter != null && ival.endFilter.matches(data)) {
                		// add to fjitterNodes
                		ITmfTimestamp ts = data.getTimestamp();
                		/* Record only if a beginFilter was matched */
                		if (ival.matchBeginTS != null) {
                			fjitterNodes.addJitterEntry(ival.name, ival.matchBeginTS, ts, false);
                		}
                		ival.matchBeginTS = null;
                	}
                }
            }

            @Override
            public void handleSuccess() {
                // Request successful, not more data available
                super.handleSuccess();

                // This part needs to run on the UI thread since it updates the chart SWT control
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                    	Object [] keys = fjitterNodes.getKeyNodes();
                    	double xAxisRangeMax = Double.MAX_VALUE;
                    	
                    	for (ISeries s : fChart.getSeriesSet().getSeries()) {
                    		fChart.getSeriesSet().deleteSeries(s.getId());
                    	}

                    	for (int idx = 0; idx < keys.length; idx++) {
                    		final double x[] = fjitterNodes.getXValues(keys[idx].toString());
                            final double y[] = fjitterNodes.getYValues(keys[idx].toString());
                            
                            if (x.length == 0 || y.length == 0) {
                            	continue;
                            }
                            
                            xAxisRangeMax = Math.min(xAxisRangeMax, x[x.length - 1]);
                            
                            fChart.getSeriesSet().createSeries(SeriesType.LINE, keys[idx].toString());
	                        fChart.getSeriesSet().getSeries()[idx].setXSeries(x);
	                        fChart.getSeriesSet().getSeries()[idx].setYSeries(y);
                    	}
                    	// Set the new range
                        if (xAxisRangeMax  != Double.MAX_VALUE && xAxisRangeMax > 0) {
                            fChart.getAxisSet().getXAxis(0).setRange(new Range(0, xAxisRangeMax));
							fChart.getAxisSet()
									.getYAxis(0)
									.setRange(
											new Range(
													fjitterNodes.getYMin(keys[0]
															.toString()),
													fjitterNodes.getYMax(keys[0]
															.toString())));
                        } else {
                            fChart.getAxisSet().getXAxis(0).setRange(new Range(0, 1));
                            fChart.getAxisSet().getYAxis(0).setRange(new Range(0, 1));
                        }
                        fChart.getAxisSet().adjustRange();
                        
                        fChart.redraw();
                        
                        fIntervalTreeViewer.setInput(fjitterNodes);
                    }

                });
            }
            
            @Override
            public void handleFailure() {
                // Request failed, not more data available
                super.handleFailure();
            }
        };
        trace.sendRequest(req);
    }

}
