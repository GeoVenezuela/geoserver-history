/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.util.requests;

import org.vfny.geoserver.servlets.AbstractService;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;


/**
 * A SAX content handler that acquires a GetCapabilities request from an
 * incoming XML stream.
 *
 * @author Rob Hranac, TOPP
 * @version $Id: CapabilitiesHandler.java,v 1.9 2004/02/13 19:30:39 dmzwiers Exp $
 */
public class CapabilitiesHandler extends XMLFilterImpl implements ContentHandler {
    /** Class logger */
    private static Logger LOGGER = Logger.getLogger("org.vfny.geoserver.requests");

    /** Service **/
    private AbstractService service;

    /** Internal Capabilities request for construction. */
    private CapabilitiesRequest request = null;

    /**
     * Creates a new CapabilitiesHandler
     * @param service this is the AbstractService Handling the Request
     */
    public CapabilitiesHandler(AbstractService service) {
        this.service = service;
        request = new CapabilitiesRequest("WFS", service);
    }

    /**
    * Returns the GetCapabilities request.
    *
    * @return GetCapabilities request.
    */
    public CapabilitiesRequest getRequest(HttpServletRequest req) {
        request.setHttpServletRequest(req);

        return request;
    }

    /* ***********************************************************************
     *  Standard SAX content handler methods                                 *
     * ***********************************************************************/

    /**
     * Notes the start of the element and sets version and service tags, as
     * required.
     *
     * @param namespaceURI URI for namespace appended to element.
     * @param localName Local name of element.
     * @param rawName Raw name of element.
     * @param atts Element attributes.
     *
     * @throws SAXException For any standard SAX errors.
     */
    public void startElement(String namespaceURI, String localName, String rawName, Attributes atts)
        throws SAXException {
        if (localName.equals("GetCapabilities")) {
            LOGGER.finer("found capabilities start.");

            for (int i = 0, n = atts.getLength(); i < n; i++) {
                if (atts.getLocalName(i).equals("version")) {
                    request.setVersion(atts.getValue(i));
                } else if (atts.getLocalName(i).equals("service")) {
                    request.setService(atts.getValue(i));
                }
            }
        }
    }
}
