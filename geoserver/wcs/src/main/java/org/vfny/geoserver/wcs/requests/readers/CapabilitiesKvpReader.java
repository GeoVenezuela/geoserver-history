/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wcs.requests.readers;

import org.vfny.geoserver.Request;
import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.util.requests.CapabilitiesRequest;
import org.vfny.geoserver.util.requests.readers.KvpRequestReader;
import org.vfny.geoserver.wcs.requests.WCSCapabilitiesRequest;
import org.vfny.geoserver.wcs.servlets.WCService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;


/**
 * This utility reads in a GetCapabilities KVP request and turns it into an
 * appropriate internal CapabilitiesRequest object, upon request.
 *
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last modification)
 * @version $Id$
 */
public class CapabilitiesKvpReader extends KvpRequestReader {
    /**
     * Constructor with raw request string.  Calls parent.
     *
     * @param kvPairs The raw string of a capabilities kvp request.
     */
    public CapabilitiesKvpReader(Map kvPairs, WCService service) {
        super(kvPairs, service);
    }

    /**
     * Get Capabilities request.
     *
     * @param request the servlet request to get the GeoServer object from
     *
     * @return Capabilities request.
     *
     * @throws ServiceException DOCUMENT ME!
     */
    public Request getRequest(HttpServletRequest request)
        throws ServiceException {
        CapabilitiesRequest currentRequest = new WCSCapabilitiesRequest(service);
        currentRequest.setHttpServletRequest(request);
        currentRequest.setVersion(getValue("VERSION"));

        //service is set in the constructor
        //currentRequest.setService(getValue("SERVICE"));
        return currentRequest;
    }
}
