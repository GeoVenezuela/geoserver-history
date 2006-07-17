/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wfs.servlets;

import java.util.Map;

import javax.servlet.ServletContext;

import org.vfny.geoserver.Response;
import org.vfny.geoserver.global.GeoServer;
import org.vfny.geoserver.global.WFS;
import org.vfny.geoserver.util.requests.readers.KvpRequestReader;
import org.vfny.geoserver.util.requests.readers.XmlRequestReader;
import org.vfny.geoserver.wfs.requests.readers.LockKvpReader;
import org.vfny.geoserver.wfs.requests.readers.LockXmlReader;
import org.vfny.geoserver.wfs.responses.LockResponse;


/**
 * Implements the WFS Lock interface, which performs insert, update and delete
 * functions on the dataset. This servlet accepts a Lock request and returns a
 * LockResponse xml element.
 *
 * @author Chris Holmes, TOPP
 * @version $Id: Lock.java,v 1.6 2004/02/09 23:29:46 dmzwiers Exp $
 */
public class Lock extends WFService {
    
	public Lock(WFS wfs) {
		super("LockFeature",wfs);
	}

	/**
     * DOCUMENT ME!
     *	
     * @return DOCUMENT ME!
     */
    protected Response getResponseHandler() {
    		return new LockResponse(getWFS().getGeoServer());
    }

    /**
     * DOCUMENT ME!
     *
     * @param params DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected KvpRequestReader getKvpReader(Map params) {
        return new LockKvpReader(params,this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected XmlRequestReader getXmlRequestReader() {
        return new LockXmlReader(this);
    }
}
