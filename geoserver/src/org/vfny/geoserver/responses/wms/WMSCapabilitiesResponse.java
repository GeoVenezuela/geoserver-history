/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.responses.wms;

import org.vfny.geoserver.global.Service;
import org.vfny.geoserver.responses.CapabilitiesResponse;
import org.vfny.geoserver.responses.CapabilitiesResponseHandler;
import org.vfny.geoserver.responses.ResponseHandler;
import org.xml.sax.ContentHandler;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Rold�n
 * @version $Id: WMSCapabilitiesResponse.java,v 1.7 2004/02/09 23:11:36 dmzwiers Exp $
 */
public class WMSCapabilitiesResponse extends CapabilitiesResponse {
    /**
     * Retrieves the GeoServer's Global Web Map Service.
     *
     * @return Web Map Service
     */
    protected Service getGlobalService() {
        //return GeoServer.getInstance().getWMS();
        return request.getWMS();
    }

    /**
     * DOCUMENT ME!
     *
     * @param contentHandler DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected ResponseHandler getResponseHandler(ContentHandler contentHandler) {
        CapabilitiesResponseHandler cr = new WmsCapabilitiesResponseHandler(contentHandler,
                request);
        cr.setPrettyPrint(true, request.getWFS().getGeoServer().isVerbose());

        return cr;
    }
    
    public void abort(Service gs) {
    }
}
