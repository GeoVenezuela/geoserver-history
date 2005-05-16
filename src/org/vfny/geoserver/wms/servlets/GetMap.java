/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.servlets;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.vfny.geoserver.Request;
import org.vfny.geoserver.Response;
import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.util.requests.readers.KvpRequestReader;
import org.vfny.geoserver.util.requests.readers.XmlRequestReader;
import org.vfny.geoserver.wms.requests.GetMapKvpReader;
import org.vfny.geoserver.wms.requests.GetMapXmlReader;
import org.vfny.geoserver.wms.responses.GetMapResponse;


/**
 * WMS service wich returns request and response handlers to manage a GetMap
 * request
 *
 * @author Gabriel Rold?n
 * @version $Id: GetMap.java,v 1.7 2004/03/30 11:12:40 cholmesny Exp $
 */
public class GetMap extends WMService {
    /**
     * Creates a new GetMap object.
     */
    public GetMap() {
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
	{
    	//DJB: added post support
    	Request serviceRequest = null;
    	this.curRequest = request;

        if (!isServiceEnabled(request)) 
        {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        
        //we need to construct an approriate serviceRequest from the GetMap XML POST.
        try{
        	 GetMapXmlReader xmlPostReader = new GetMapXmlReader();
        	 Reader xml =  request.getReader();
        	 serviceRequest= xmlPostReader.read(xml,request);
        }
        catch (ServiceException se) 
		{
            sendError(response, se);
            return;
        } 
        catch (Throwable e) 
		{
            sendError(response, e);
            return;
        }
        
        doService(request, response, serviceRequest);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Response getResponseHandler() {
        return new GetMapResponse();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    protected XmlRequestReader getXmlRequestReader() {
        /**
         * @todo Implement this org.vfny.geoserver.servlets.AbstractService
         *       abstract method
         */
        throw new java.lang.UnsupportedOperationException(
            "Method getXmlRequestReader() not yet implemented.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param params DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected KvpRequestReader getKvpReader(Map params) {
        return new GetMapKvpReader(params);
    }
}
