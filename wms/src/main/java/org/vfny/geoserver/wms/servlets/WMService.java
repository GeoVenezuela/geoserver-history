/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.servlets;

import org.vfny.geoserver.ExceptionHandler;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.servlets.AbstractService;
import org.vfny.geoserver.util.Requests;
import org.vfny.geoserver.wms.WmsExceptionHandler;
import javax.servlet.http.HttpServletRequest;


/**
 * Base servlet for all Web Map Server requests.
 * 
 * <p>
 * Subclasses should supply the handler, request and response mapping for the
 * service they implement.
 * </p>
 *
 * @author Gabriel Rold?n
 * @version $Id: WMService.java,v 1.6 2004/02/17 22:42:32 dmzwiers Exp $
 */
abstract public class WMService extends AbstractService {
	
	
	 /**
 	 * Constructor for WMS service.
 	 * 
 	 * @param request The service request being made (GetCaps,GetFeature,...)
 	 * @param wms The WMS service reference.
 	 */
     public WMService(String request, WMS wms) {
     		super("WMS",request,wms);
     }
     
     /**
      * @return The wms service ref.
      */
     public WMS getWMS() {
     		return (WMS) getServiceRef();
     }
     
     /**
      * Sets the wms service ref.
      * @param wms
      */
     public void setWMS(WMS wms) {
     		setServiceRef(wms);
     }
     
     
     /**
     * returns a Web Map ServiceConfig exception handler
     *
     * @return WmsExceptionHandler
     */
    protected ExceptionHandler getExceptionHandler() {
        return WmsExceptionHandler.getInstance();
    }
    
    protected boolean isServiceEnabled(HttpServletRequest req){
    	return getWMS().isEnabled();
    }
}
