/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
/* Copyright (c) 2004 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import org.vfny.geoserver.config.validation.ValidationConfig;
import org.vfny.geoserver.global.GeoServer;
import org.vfny.geoserver.global.GeoServerPlugIn;


/**
 * ConfigPlugIn purpose.
 * 
 * <p>
 * Used to set-up config's memory model.   REQUIRED  Pre-condition must be run
 * after GeoServerPlugIn
 * </p>
 * 
 * <p></p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @version $Id: ConfigPlugIn.java,v 1.5 2004/02/07 01:30:05 jive Exp $
 *
 * @see org.vfny.geoserver.global.GeoServerPlugIn
 */
public class ConfigPlugIn implements PlugIn {
    /**
     * Implement destroy.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @see org.apache.struts.action.PlugIn#destroy()
     */
    public void destroy() {
        // does nothing.
    }

    /**
     * Implement init.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @param arg0
     * @param arg1
     *
     * @throws ServletException
     *
     * @see org.apache.struts.action.PlugIn#init(org.apache.struts.action.ActionServlet,
     *      org.apache.struts.config.ModuleConfig)
     */
    public void init(ActionServlet arg0, ModuleConfig arg1)
        throws ServletException {
        ServletContext sc = arg0.getServletContext();
        GeoServer gs = (GeoServer) sc.getAttribute(GeoServer.WEB_CONTAINER_KEY);

        if (gs == null) {
            GeoServerPlugIn gspi = new GeoServerPlugIn();
            gspi.init(arg0, arg1);
            gs = (GeoServer) sc.getAttribute(GeoServer.WEB_CONTAINER_KEY);

            if (gs == null) {
                throw new ServletException(
                    "GeoServerPlugIn Failed. Thus ConfigPlugIn cannot run.");
            }
        }

        sc.setAttribute(WMSConfig.CONFIG_KEY, new WMSConfig(gs.toWMSDTO()));
        sc.setAttribute(WFSConfig.CONFIG_KEY, new WFSConfig(gs.toWFSDTO()));
        sc.setAttribute(GlobalConfig.CONFIG_KEY,
            new GlobalConfig(gs.toGeoServerDTO()));
        sc.setAttribute(DataConfig.CONFIG_KEY, new DataConfig(gs.toDataDTO()));
        ValidationConfig vc = new ValidationConfig(gs.toPlugInDTO(), gs.toTestSuiteDTO());
        sc.setAttribute(ValidationConfig.CONFIG_KEY, vc);
    }
}
