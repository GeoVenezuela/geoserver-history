/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wfs.servlets;

import javax.servlet.http.HttpServletRequest;

import org.vfny.geoserver.ExceptionHandler;
import org.vfny.geoserver.global.WFS;
import org.vfny.geoserver.servlets.AbstractService;
import org.vfny.geoserver.wfs.WfsExceptionHandler;


/**
 * Base servlet for all Web Feature Server requests.
 * 
 * <p>
 * Subclasses should supply the handler, request and response mapping for the
 * service they implement.
 * </p>
 *
 * @author Gabriel Rold?n
 * @version $Id: WFService.java,v 1.6 2004/02/17 22:42:32 dmzwiers Exp $
 */
abstract public class WFService extends AbstractService {
	
	
    /**
	 * Constructor for WFS service.
	 * 
	 * @param request The service request being made (GetCaps,GetFeature,...)
	 * @param wfs The WFS service reference.
	 */
    public WFService(String request, WFS wfs) {
    		super("WFS",request,wfs);
    }
    
    /**
     * @return The wfs service ref.
     */
    public WFS getWFS() {
    		return (WFS) getServiceRef();
    }
    
    /**
     * Sets the wfs service ref.
     * @param wfs
     */
    public void setWFS(WFS wfs) {
    		setServiceRef(wfs);
    }
    
    /**
     * a Web Feature ServiceConfig exception handler
     *
     * @return an instance of WfsExceptionHandler
     */
    protected ExceptionHandler getExceptionHandler() {
        return WfsExceptionHandler.getInstance();
    }
    
    protected boolean isServiceEnabled(HttpServletRequest req){
    		return getWFS().isEnabled();
    	}
}
