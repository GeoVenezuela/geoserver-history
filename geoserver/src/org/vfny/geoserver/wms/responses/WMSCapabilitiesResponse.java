/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.vfny.geoserver.Request;
import org.vfny.geoserver.Response;
import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.global.GeoServer;
import org.vfny.geoserver.global.Service;
import org.vfny.geoserver.util.requests.CapabilitiesRequest;
import org.vfny.geoserver.wms.WmsException;
import org.vfny.geoserver.wms.responses.helpers.WMSCapsTransformer;


/**
 * Processes a WMS GetCapabilities request.
 * <p>
 * The response of a GetCapabilities request is general information about the
 * service itself and specific information about the available maps.
 * </p>
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: WMSCapabilitiesResponse.java,v 1.9 2004/09/05 17:19:05 cholmesny Exp $
 */
public class WMSCapabilitiesResponse implements Response {
    /** package's logger */
    private static final Logger LOGGER = Logger.getLogger(WMSCapabilitiesResponse.class.getPackage()
                                                                                       .getName());

    /**
     * Byte array holding the raw content of the capabilities document,
     * generated in <code>execute()</code>
     */
    private byte[] rawResponse;

    /**
     * DOCUMENT ME!
     *
     * @param request DOCUMENT ME!
     *
     * @throws ServiceException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws WmsException DOCUMENT ME!
     */
    public void execute(Request request) throws ServiceException {
        if (!(request instanceof CapabilitiesRequest)) {
            throw new IllegalArgumentException("Not a GetCapabilities Request");
        }

        WMSCapsTransformer transformer = new WMSCapsTransformer(request
                .getSchemaBaseUrl());

       // if (request.getWFS().getGeoServer().isVerbose()) {
            transformer.setIndentation(2);
       // }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            transformer.transform(request, out);
        } catch (TransformerException e) {
            throw new WmsException(e);
        }

        this.rawResponse = out.toByteArray();
    }

    /**
     * Returns the fixed capabilities MIME type  (application/vnd.ogc.wms_xml)
     * as specified in whe WMS spec, version 1.1.1, section 6.5.3, table 3.
     *
     * @param gs DOCUMENT ME!
     *
     * @return the capabilities document MIME type.
     *
     * @throws IllegalStateException if the response was not yet produced.
     */
    public String getContentType(GeoServer gs) throws IllegalStateException {
        if (rawResponse == null) {
            throw new IllegalStateException(
                "execute() not called or not succeed.");
        }

        return WMSCapsTransformer.WMS_CAPS_MIME;
    }

    /**
     * Just returns <code>null</code>, since no special encoding is applyed to
     * the output data.
     *
     * @return <code>null</code>
     */
    public String getContentEncoding() {
        return null;
    }

    /**
     * Writes the capabilities document generated in <code>execute()</code> to
     * the given output stream.
     *
     * @param out the capabilities document destination
     *
     * @throws ServiceException never, since the whole content was aquired in
     *         <code>execute()</code>
     * @throws IOException if it is thrown while writing to <code>out</code>
     * @throws IllegalStateException if <code>execute()</code> was not
     *         called/succeed before this method is called.
     */
    public void writeTo(OutputStream out) throws ServiceException, IOException {
        if (rawResponse == null) {
            throw new IllegalStateException("");
        }

        out.write(rawResponse);
    }

    /**
     * Does nothing, since no processing is done after <code>execute()</code>
     * has returned.
     *
     * @param gs the service instance
     */
    public void abort(Service gs) {
    }

	/* (non-Javadoc)
	 * @see org.vfny.geoserver.Response#getContentDisposition()
	 */
	public String getContentDisposition() {
		// TODO Auto-generated method stub
		return null;
	}
}
