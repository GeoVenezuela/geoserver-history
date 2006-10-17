/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wfs.requests;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.vfny.geoserver.servlets.AbstractService;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * Uses SAX to extact a DescribeFeatureType query from and incoming GetFeature
 * request XML stream.
 * 
 * <p>
 * Note that this Handler extension ignores Filters completely and must be
 * chained as a parent to the PredicateFilter method in order to recognize
 * them.  If it is not chained, it will still generate valid queries, but with
 * no filtering whatsoever.
 * </p>
 *
 * @author Rob Hranac, TOPP
 * @version $Id: DescribeHandler.java,v 1.7 2004/02/13 19:30:39 dmzwiers Exp $
 */
public class DescribeHandler extends XMLFilterImpl implements ContentHandler {
    /** Class logger */
    private static Logger LOGGER = Logger.getLogger(
            "org.vfny.geoserver.requests.wfs");

    /** Internal GetCapabilities request for construction. */
    private DescribeRequest request = null;

    /** Local variable to track current tag */
    private String currentTag = "";
    
    /** Collects string chunks in {@link #characters(char[], int, int)} 
     * callback to be handled at the beggining of {@link #endElement(String, String, String)}
     */
    private StringBuffer characters = new StringBuffer();

    /**
     * Creates a new describe request handler.
     * 
     * @param request The describe request.
     */
    public DescribeHandler(DescribeRequest request) {
    		this.request = request;
    }
    
    /**
     * Returns the GetCapabilities request.
     *
     * @return GetCapabilities request.
     */
    public DescribeRequest getRequest(HttpServletRequest req) {
    		request.setHttpServletRequest(req);
        return request;
    }

    /* ***********************************************************************
     *  Standard SAX content handler methods                                 *
     * ***********************************************************************/

    /**
     * Notes the start of the element and sets the current tag.
     *
     * @param namespaceURI URI for namespace appended to element.
     * @param localName Local name of element.
     * @param rawName Raw name of element.
     * @param atts Element attributes.
     *
     * @throws SAXException For standard SAX errors.
     */
    public void startElement(String namespaceURI, String localName,
        String rawName, Attributes atts) throws SAXException {
        LOGGER.finest("found start element: " + localName);
        characters.setLength(0);
        currentTag = localName;

        if (currentTag.equals("DescribeFeatureType")) {
            for (int i = 0; i < atts.getLength(); i++) {
                if (atts.getLocalName(i).equals("outputFormat")) {
                    LOGGER.finest("found outputFormat: " + atts.getValue(i));
                    request.setOutputFormat(atts.getValue(i));
                }
            }
        }
    }

    /**
     * Notes the end of the element and sets the current tag.
     *
     * @param namespaceURI URI for namespace appended to element.
     * @param localName Local name of element.
     * @param rawName Raw name of element.
     *
     * @throws SAXException For standard SAX errors.
     */
    public void endElement(String namespaceURI, String localName, String rawName)
        throws SAXException {
        LOGGER.finest("found end element: " + localName);
        handleCharacters();
        currentTag = "";
    }

    /**
     * Checks if inside type name and adds to feature type list, if so.
     *
     * @param ch URI for namespace appended to element.
     * @param start Local name of element.
     * @param length Raw name of element.
     *
     * @throws SAXException For standard SAX errors.
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
    	characters.append(ch, start, length);
    }
    
    /**
     * Handles the string chunks collected in {@link #characters}.
     */
    private void handleCharacters(){
    	if(characters.length() == 0){
    		return;
    	}
        if (currentTag.equals("TypeName")) {
            request.addFeatureType(characters.toString());
            LOGGER.finest("added type name: " + characters);
            characters.setLength(0);
        }	
    }
}
