/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.config;

import org.w3c.dom.*;
import java.util.*;


/**
 * default configuration for services
 *
 * @author Gabriel Rold�n
 * @author Chris Holmes
 * @version $Id: ServiceConfig.java,v 1.9.4.3 2003/11/11 02:41:40 cholmesny Exp $
 */
public abstract class ServiceConfig extends BasicConfig {
    /** DOCUMENT ME! */
    private boolean enabled;

    /** DOCUMENT ME! */
    private String serviceType;

    /** DOCUMENT ME! */
    private String onlineResource;

    /** DOCUMENT ME! */
    protected String URL;

    public ServiceConfig(Element serviceRoot) throws ConfigurationException {
        super(serviceRoot);
        this.serviceType = getAttribute(serviceRoot, "type", true);
        this.enabled = getBooleanAttribute(serviceRoot, "enabled", true);
        this.onlineResource = getChildText(serviceRoot, "onlineResource", true);

        //this.URL = getChildText(serviceRoot, "URL");
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getOnlineResource() {
        return onlineResource;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getURL() {
        return URL;
    }

    /**
     * Gets the base schema url.
     *
     * @return The url to use as the base for schema locations.
     *
     * @deprecated Use GlobalConfig.getSchemaBaseUrl()
     */
    public String getSchemaBaseUrl() {
        return GlobalConfig.getInstance().getSchemaBaseUrl();
    }
}
