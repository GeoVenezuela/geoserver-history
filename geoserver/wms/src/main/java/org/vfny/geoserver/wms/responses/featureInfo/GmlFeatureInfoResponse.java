/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.featureInfo;

import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.WfsFactory;
import org.geoserver.wfs.WFS;
import org.geoserver.wfs.WebFeatureService;
import org.geoserver.wfs.xml.GML2OutputFormat;
import org.geotools.feature.FeatureCollection;
import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.global.Data;
import org.vfny.geoserver.global.FeatureTypeInfo;
import org.vfny.geoserver.global.Service;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.servlets.AbstractService;
import org.vfny.geoserver.wms.requests.GetFeatureInfoRequest;
import org.vfny.geoserver.wms.servlets.WMService;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
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
        WFS wfs = wms.getWFS();
        Data catalog = fInfoReq.getServiceRef().getCatalog();

        FeatureCollectionType features = WfsFactory.eINSTANCE.createFeatureCollectionType();

        for (Iterator i = results.iterator(); i.hasNext();) {
            features.getFeature().add(i.next());
        }

        GML2OutputFormat format = new GML2OutputFormat(wfs, catalog);
        format.write(features, out, null);
    }

    public String getContentDisposition() {
        // TODO Auto-generated method stub
        return null;
    }
}
