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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.xml.TmfFilterContentHandler;
import org.eclipse.tracecompass.tmf.core.filter.xml.TmfFilterXMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class for saving and loading of interval filter settings to/from file.
 *
 * @version 1.0
 * @author orangehero
 *
 */
public class IntervalFilterXML {

	private static final String INTERVAL_FILTER_SETTINGS_TAG = "INTERVAL_FILTER_SETTINGS"; //$NON-NLS-1$
	private static final String INTERVAL_FILTER_SETTING_TAG = "INTERVAL_FILTER_SETTING"; //$NON-NLS-1$
	private static final String FILTER_1ST_TAG = "FILTERBEGIN"; //$NON-NLS-1$
	private static final String FILTER_2ND_TAG = "FILTEREND"; //$NON-NLS-1$
	private static final String FILTER_NAME_TAG = "FILTERNAME"; //$NON-NLS-1$
	
	/**
     * Saves the given interval Filters to file.
     *
     * @param pathName
     *            A file name with path
     * @param intervalFilterSettings
     *            An array of interval filter settings to save.
     */
	public static void save(String pathName, IntervalFilterSetting[] intervalFilterSettings) {
		try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement(INTERVAL_FILTER_SETTINGS_TAG);
            document.appendChild(rootElement);

            for (IntervalFilterSetting ivalFilterSetting : intervalFilterSettings) {
                Element ivalFilterElement = document.createElement(INTERVAL_FILTER_SETTING_TAG);
                rootElement.appendChild(ivalFilterElement);
                
                String name = ivalFilterSetting.name;
                if (name != null) {
                	Element filterElement = document.createElement(FILTER_NAME_TAG);
                    ivalFilterElement.appendChild(filterElement);
                    //Element element = document.createElement(treenode.getNodeName());
                    filterElement.setAttribute(TmfFilterNode.NAME_ATTR, name);
                }

                ITmfFilterTreeNode filter1 = ivalFilterSetting.beginFilter;
                if (filter1 != null) {
                    Element filterElement = document.createElement(FILTER_1ST_TAG);
                    ivalFilterElement.appendChild(filterElement);
                    TmfFilterXMLWriter.buildXMLTree(document, filter1, filterElement);
                }
                
                ITmfFilterTreeNode filter2 = ivalFilterSetting.endFilter;
                if (filter2 != null) {
                    Element filterElement = document.createElement(FILTER_2ND_TAG);
                    ivalFilterElement.appendChild(filterElement);
                    TmfFilterXMLWriter.buildXMLTree(document, filter2, filterElement);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(pathName));
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException e) {
            //Activator.getDefault().logError("Error saving color xml file: " + pathName, e); //$NON-NLS-1$
        }
	}
	
	/**
     * Loads interval filter sttings from file and returns it in an array.
     *
     * @param pathName
     *            A file name with path
     *
     * @return An array of interval filter settings loaded from a file.
     */
    public static IntervalFilterSetting[] load(String pathName) {
        if (new File(pathName).canRead()) {        
	        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	        parserFactory.setNamespaceAware(true);
	        IntervalFilterSettingsContentHandler handler = new IntervalFilterSettingsContentHandler();
	        try {
	            XMLReader saxReader = parserFactory.newSAXParser().getXMLReader();
	            saxReader.setContentHandler(handler);
	            saxReader.parse(pathName);
	            return handler.ivalFilterSettings.toArray(new IntervalFilterSetting[0]);
	        } catch (ParserConfigurationException | SAXException | IOException e) {
	            //Activator.getDefault().logError("Error loading color xml file: " + pathName, e); //$NON-NLS-1$
	        }
        }
        return new IntervalFilterSetting[0];
    }
    
    private static class IntervalFilterSettingsContentHandler extends DefaultHandler {
    	
    	public List<IntervalFilterSetting> ivalFilterSettings = new ArrayList<>(0);
    	private ITmfFilterTreeNode fBeginFilter;
    	private ITmfFilterTreeNode fEndFilter;
    	private String fName;
    	private TmfFilterContentHandler filterContentHandler;
    	
    	@Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    		if (localName.equals(INTERVAL_FILTER_SETTINGS_TAG)) {
    			ivalFilterSettings = new ArrayList<>();
    		} else if (localName.equals(INTERVAL_FILTER_SETTING_TAG)) {
    			fBeginFilter = null;
    			fEndFilter = null;
    			fName = null;
    		} else if (localName.equals(FILTER_NAME_TAG)) {
    			fName = attributes.getValue(TmfFilterNode.NAME_ATTR);
    		} else if (localName.equals(FILTER_1ST_TAG) || localName.equals(FILTER_2ND_TAG)) {
    			filterContentHandler = new TmfFilterContentHandler();
    		} else {
    			if (filterContentHandler != null) {
                    filterContentHandler.startElement(uri, localName, qName, attributes);
                }
    		}
    	}
    	
    	@Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
    		if (localName.equals(INTERVAL_FILTER_SETTINGS_TAG)) {
    			/* nothing to do */
    		} else if (localName.equals(INTERVAL_FILTER_SETTING_TAG)) {
    			IntervalFilterSetting ivalFilterSetting = new IntervalFilterSetting();
    			ivalFilterSetting.beginFilter = fBeginFilter;
    			ivalFilterSetting.endFilter = fEndFilter;
    			ivalFilterSetting.name = fName;
    			ivalFilterSettings.add(ivalFilterSetting);
    		} else if (localName.equals(FILTER_NAME_TAG)) {
    			/* nothing to do */
    		} else if (localName.equals(FILTER_1ST_TAG)) {
    			fBeginFilter = filterContentHandler.getTree();
                filterContentHandler = null;
    		} else if (localName.equals(FILTER_1ST_TAG)) {
    			fEndFilter = filterContentHandler.getTree();
                filterContentHandler = null;
    		} else if (filterContentHandler != null) {
                filterContentHandler.endElement(uri, localName, qName);
            }
    	}
    }
}
