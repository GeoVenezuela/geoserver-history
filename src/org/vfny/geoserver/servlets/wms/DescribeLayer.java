/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.servlets.wms;

import java.util.Map;

import org.vfny.geoserver.requests.readers.KvpRequestReader;
import org.vfny.geoserver.requests.readers.XmlRequestReader;
import org.vfny.geoserver.requests.readers.wms.DescribeLayerKvpRequestReader;
import org.vfny.geoserver.responses.Response;
import org.vfny.geoserver.responses.wms.DescribeLayerResponse;
import org.vfny.geoserver.servlets.WMService;


/**
 * Provides the artifacts to manage a WMS DescribeLayer request
 * following the {@link org.vfny.geoserver.servlet.AbstractService}'s workflow.
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class DescribeLayer extends WMService {
	
    /**
     * Creates and returns a response handler to encode
     * the list of requested layers into a DescribeLayer
     * document.
     *
     * @return a <code>DescribeLayerResponse</code>
     */
    protected Response getResponseHandler() {
        return new DescribeLayerResponse();
    }

    /**
     * Builds a KVP reader to parse the parameters of
     * a DescribeLayer request.
     *
     * @param params the request kvp parameters
     *
     * @return a new DescribeLayerKvpRequestReader
     */
    protected KvpRequestReader getKvpReader(Map params) {
        return new DescribeLayerKvpRequestReader(params);
    }

    /**
     * Throws an UnsupportedOperationException allways, since
     * there are no standard XML encoding for DescribeLayer requests.
     *
     * @return none
     * @throws UnsupportedOperationException allways, since
     * there are no standard XML encoding for DescribeLayer requests.
     */
    protected XmlRequestReader getXmlRequestReader() {
    	throw new UnsupportedOperationException(
    			"There are no standard XML encoding for DescribeLayer requests");
    }
}
