/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.featureInfo;

import org.geotools.data.FeatureResults;
import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.global.FeatureTypeInfo;
import org.vfny.geoserver.global.Service;
import org.vfny.geoserver.global.WFS;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.servlets.AbstractService;
import org.vfny.geoserver.wfs.requests.FeatureRequest;
import org.vfny.geoserver.wfs.responses.GML2FeatureResponseDelegate;
import org.vfny.geoserver.wfs.responses.GetFeatureResults;
import org.vfny.geoserver.wfs.servlets.WFService;
import org.vfny.geoserver.wms.requests.GetFeatureInfoRequest;
import org.vfny.geoserver.wms.servlets.WMService;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * A GetFeatureInfo response handler specialized in producing GML data for a
 * GetFeatureInfo request.
 *
 * <p>
 * This class does not deals directly with GML encoding. Instead, it works by
 * taking the FeatureResults produced in <code>execute()</code> and constructs
 * a <code>GetFeaturesResult</code> wich is passed to a
 * <code>GML2FeatureResponseDelegate</code>, as if it where the result of a
 * GetFeature WFS request.
 * </p>
 *
 * @author Gabriel Roldan, Axios Engineering
 */
public class GmlFeatureInfoResponse extends AbstractFeatureInfoResponse {
    /**
     * The MIME type of the format this response produces:
     * <code>"application/vnd.ogc.gml"</code>
     */
    private static final String FORMAT = "application/vnd.ogc.gml";

    /**
     * Default constructor, sets up the supported output format string.
     */
    public GmlFeatureInfoResponse() {
        super.supportedFormats = Collections.singletonList(FORMAT);
    }

    /**
     * Returns any extra headers that this service might want to set in the HTTP
     * response object.
     *
     * @see org.vfny.geoserver.Response#getResponseHeaders()
     */
    public HashMap getResponseHeaders() {
        return new HashMap();
    }

    /**
     * Takes the <code>FeatureResult</code>s generated by the
     * <code>execute</code> method in the superclass and constructs a
     * <code>GetFeaturesResult</code> wich is passed to a
     * <code>GML2FeatureResponseDelegate</code>.
     *
     * @param out
     *            DOCUMENT ME!
     *
     * @throws ServiceException
     *             DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     */
    public void writeTo(OutputStream out) throws ServiceException, IOException {
        GetFeatureInfoRequest fInfoReq = (GetFeatureInfoRequest) getRequest();
        WMS wms = (WMS) fInfoReq.getServiceRef().getServiceRef();
        FeatureRequest freq = new FeatureRequest(new MockWFService(fInfoReq.getRequest(),
                    wms.getWFS()));
        freq.setHttpServletRequest(fInfoReq.getHttpServletRequest());

        freq.setRequest("GETFEATURE");
        freq.setHandle("GetFeatureInfo");
        freq.setMaxFeatures(fInfoReq.getFeatureCount());

        List queries = null;
        freq.setQueries(queries);

        GetFeatureResults getFeatureResults = new GetFeatureResults(freq);
        FeatureTypeInfo finfo;
        FeatureResults fresults;
        int i = 0;

        for (Iterator it = results.iterator(); it.hasNext(); i++) {
            fresults = (FeatureResults) it.next();
            finfo = (FeatureTypeInfo) metas.get(i);
            getFeatureResults.addFeatures(finfo, fresults);

            // TODO: Do we want to reproject the geometries here? Or leave them
            // in their native projection?
        }

        GML2FeatureResponseDelegate encoder = new GML2FeatureResponseDelegate();
        encoder.prepare("GML2", getFeatureResults);
        encoder.encode(out);
    }

    public String getContentDisposition() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Crude hack to make the FeatureRequest, expecting a WFService, work
     * anyways. In fact FeatureRequest does not use anything specific from
     * WFService and it's happy with whatever service has been provided to it...
     * but that's a knowledge you can get only inspecting its code...
     *
     * @author aaime
     *
     */
    private static class MockWFService extends WFService {
        public MockWFService(String request, WFS wfs) {
            super(request, wfs);
        }
    }
}
