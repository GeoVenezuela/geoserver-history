/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.restconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.vfny.geoserver.config.WMSConfig;

import org.geoserver.rest.MapResource;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.rest.format.FreemarkerFormat;
import org.geoserver.rest.format.MapJSONFormat;
import org.geoserver.rest.format.MapXMLFormat;
import org.restlet.data.MediaType;

/**
 * Restlet for Style resources
 *
 * @author David Winslow <dwinslow@openplans.org> , The Open Planning Project
 */
class LayerGroupListResource extends MapResource {
    private WMSConfig myWMSConfig;

    public LayerGroupListResource(){
        super(null,null,null);
    }

    public LayerGroupListResource(Context context, Request request, Response response,
        WMSConfig wmsConfig) {
        super(context, request, response);
        myWMSConfig = wmsConfig;
    }

    public void setWMSConfig(WMSConfig c){
        myWMSConfig = c;
    }

    public WMSConfig getWMSConfig(){
        return myWMSConfig;
    }

    @Override
    protected List<DataFormat> createSupportedFormats(Request request,
            Response response) {
        List l = new ArrayList();
        l.add(new FreemarkerFormat("HTMLTemplates/layergroups.ftl", getClass(), MediaType.TEXT_HTML));
        l.add(new MapJSONFormat());
        l.add(new MapXMLFormat("layergroups"));

        return l;
    }

    public Map getMap() {
        Map context = new HashMap();
        Map layerGroups = myWMSConfig.getBaseMapLayers();
        List layerNames = new ArrayList();

        if (layerGroups != null) {
            Iterator it = layerGroups.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                // addition.add(entry.getKey());
                // addition.put("members", Arrays.asList(entry.getValue().toString().split(",")));
                layerNames.add(entry.getKey());
            }

            context.put("layers", layerNames);
        }

        return context;
    }
}
