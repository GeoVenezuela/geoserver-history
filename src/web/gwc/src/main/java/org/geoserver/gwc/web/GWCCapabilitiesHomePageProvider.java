/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.geoserver.web.CapabilitiesHomePageLinkProvider;
import org.geoserver.web.CapabilitiesHomePagePanel;
import org.geoserver.web.CapabilitiesHomePagePanel.CapsInfo;
import org.geoserver.web.GeoServerApplication;
import org.geotools.util.Version;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Implements the {@link CapabilitiesHomePageLinkProvider} extension point to contribute links to
 * GetCapabilities and other service description documents supported by GeoWebCache.
 * 
 * @author Gabriel Roldan
 * @see CapabilitiesHomePagePanel
 */
public class GWCCapabilitiesHomePageProvider implements CapabilitiesHomePageLinkProvider {

    /**
     * Adds capabilities links for WMS-C, WMTS and TMS, as long as they're available.
     * 
     * @see org.geoserver.web.CapabilitiesHomePageLinkProvider#getCapabilitiesComponent
     * @see CapabilitiesHomePagePanel
     */
    public Component getCapabilitiesComponent(final String id) {

        List<CapsInfo> gwcCaps = new ArrayList<CapsInfo>();

        GeoServerApplication app = GeoServerApplication.get();
        try {
            app.getBean("gwcServiceWMS");
            gwcCaps.add(new CapsInfo("WMS-C", new Version("1.1.1"),
                    "../gwc/service/wms?request=GetCapabilities&version=1.1.1&tiled=true"));
        } catch (NoSuchBeanDefinitionException e) {
            // service not found, ignore exception
        }
        try {
            app.getBean("gwcServiceWMTS");
            gwcCaps.add(new CapsInfo("WMTS", new Version("1.0.0"),
                    "../gwc/service/wmts?REQUEST=GetCapabilities"));
        } catch (NoSuchBeanDefinitionException e) {
            // service not found, ignore exception
        }
        try {
            app.getBean("gwcServiceTMS");
            gwcCaps.add(new CapsInfo("TMS", new Version("1.0.0"), "../gwc/service/tms/1.0.0"));
        } catch (NoSuchBeanDefinitionException e) {
            // service not found, ignore exception
        }

        return new CapabilitiesHomePagePanel(id, gwcCaps);
    }
}
