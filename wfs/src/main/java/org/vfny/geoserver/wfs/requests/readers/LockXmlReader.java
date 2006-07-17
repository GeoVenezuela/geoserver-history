/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wfs.requests.readers;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.filter.FilterFilter;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.vfny.geoserver.Request;
import org.vfny.geoserver.util.requests.readers.XmlRequestReader;
import org.vfny.geoserver.wfs.WfsException;
import org.vfny.geoserver.wfs.requests.LockHandler;
import org.vfny.geoserver.wfs.servlets.WFService;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;


/**
 * reads in a Lock WFS request from an XML stream
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @version $Id: LockXmlReader.java,v 1.8 2004/02/13 19:30:38 dmzwiers Exp $
 */
public class LockXmlReader extends XmlRequestReader {
    /**
     * Creates a new LockXmlReader object.
     * 
     * @param service The WFS service handling the request.
     */
    public LockXmlReader(WFService service) {
    		super(service);
    }

    /**
     * Reads the Lock XML request into a LockRequest object.
     *
     * @param reader The plain POST text from the client.
     *
     * @return The read LockRequest object.
     *
     * @throws WfsException For any problems reading the request.
     */
    public Request read(Reader reader, HttpServletRequest req) throws WfsException {
        // translate string into a proper SAX input source
        InputSource requestSource = new InputSource(reader);

        // instantiante parsers and content handlers
        LockHandler contentHandler = 
        		new LockHandler((WFService)getServiceRef());
        FilterFilter filterParser = new FilterFilter(contentHandler, null);
        GMLFilterGeometry geometryFilter = new GMLFilterGeometry(filterParser);
        GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);

        // read in XML file and parse to content handler
        try {
            LOGGER.finest("about to create parser");

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ParserAdapter adapter = new ParserAdapter(parser.getParser());
            LOGGER.finest("setting the content handler");
            LOGGER.finest("content handler = " + documentFilter);
            adapter.setContentHandler(documentFilter);
            LOGGER.finest("about to parse");
            LOGGER.finest("calling parse on " + requestSource);
            adapter.parse(requestSource);
            LOGGER.fine("just parsed: " + requestSource);
        } catch (SAXException e) {
            e.printStackTrace(System.out);
            throw new WfsException(e,
                "XML getFeature request SAX parsing error",
                XmlRequestReader.class.getName());
        } catch (IOException e) {
            throw new WfsException(e, "XML get feature request input error",
                XmlRequestReader.class.getName());
        } catch (ParserConfigurationException e) {
            throw new WfsException(e, "Some sort of issue creating parser",
                XmlRequestReader.class.getName());
        }

        Request r = contentHandler.getRequest(req);
        return r;
    }
}
