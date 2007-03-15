/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wfs.requests.readers;

import org.vfny.geoserver.Request;
import org.vfny.geoserver.util.requests.CapabilitiesHandler;
import org.vfny.geoserver.util.requests.readers.XmlRequestReader;
import org.vfny.geoserver.wfs.WfsException;
import org.vfny.geoserver.wfs.servlets.WFService;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;
import java.io.IOException;
import java.io.Reader;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * reads a GetCapabilities request from an XML stream
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @version $Id: CapabilitiesXmlReader.java,v 1.8 2004/02/13 19:30:39 dmzwiers Exp $
 *
 * @task TODO: see if it must be refactored to read WMS GetCapabilities too
 */
public class CapabilitiesXmlReader extends XmlRequestReader {
    /**
             * Constructs a new reader.
             *
             * @param service The WFS service handling the request.
             */
    public CapabilitiesXmlReader(WFService service) {
        super(service);
    }

    /**
     * Reads the Capabilities XML request into a CapabilitiesRequest
     * object.
     *
     * @param reader The plain POST text from the client.
     * @param req DOCUMENT ME!
     *
     * @return The read CapabilitiesRequest object.
     *
     * @throws WfsException For any problems reading the request
     */
    public Request read(Reader reader, HttpServletRequest req)
        throws WfsException {
        InputSource requestSource = new InputSource(reader);

        // instantiante parsers and content handlers
        CapabilitiesHandler currentRequest = new CapabilitiesHandler(getServiceRef());

        // read in XML file and parse to content handler
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ParserAdapter adapter = new ParserAdapter(parser.getParser());

            adapter.setContentHandler(currentRequest);
            adapter.parse(requestSource);
            LOGGER.fine("just parsed: " + requestSource);
        } catch (SAXException e) {
            throw new WfsException(e, "XML capabilities request parsing error", getClass().getName());
        } catch (IOException e) {
            throw new WfsException(e, "XML capabilities request input error", getClass().getName());
        } catch (ParserConfigurationException e) {
            throw new WfsException(e, "Some sort of issue creating parser", getClass().getName());
        }

        Request r = currentRequest.getRequest(req);

        return r;
    }
}
