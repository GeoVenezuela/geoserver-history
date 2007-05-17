/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.action;

import com.vividsolutions.jts.geom.Envelope;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.vfny.geoserver.global.CoverageInfo;
import org.vfny.geoserver.global.Data;
import org.vfny.geoserver.global.FeatureTypeInfo;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.util.requests.CapabilitiesRequest;
import org.vfny.geoserver.wms.servlets.Capabilities;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <b>MapPreviewAction</b><br> Sep 26, 2005<br>
 * <b>Purpose:</b><br>
 * Gathers up all the FeatureTypes in use and returns the informaion to the
 * .jsp .<br>
 * It will also generate requests to the WMS "openlayers" output format. <br>
 * This will communicate to a .jsp and return it three arrays of strings that contain:<br>
 * - The Featuretype's name<br>
 * - The DataStore name of the FeatureType<br>
 * - The bounding box of the FeatureType<br>
 * To change what data is output to the .jsp, you must change <b>struts-config.xml</b>.<br>
 * Look for the line:<br>
 * &lt;form-bean <br>
 * name="mapPreviewForm"<br>
 *
 * @author Brent Owens (The Open Planning Project)
 * @version
 */
public class MapPreviewAction extends GeoServerAction {
    /* (non-Javadoc)
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        ArrayList dsList = new ArrayList();
        ArrayList ftList = new ArrayList();
        ArrayList bboxList = new ArrayList();
        ArrayList srsList = new ArrayList();
        ArrayList ftnsList = new ArrayList();
        ArrayList widthList = new ArrayList();
        ArrayList heightList = new ArrayList();

        // 1) get the capabilities info so we can find out our feature types
        WMS wms = getWMS(request);
        Capabilities caps = new Capabilities(wms);
        CapabilitiesRequest capRequest = new CapabilitiesRequest("WMS", caps);
        capRequest.setHttpServletRequest(request);

        Data catalog = wms.getData();
        List ftypes = new ArrayList(catalog.getFeatureTypeInfos().values());
        Collections.sort(ftypes, new FeatureTypeInfoNameComparator());

        List ctypes = new ArrayList(catalog.getCoverageInfos().values());
        Collections.sort(ctypes, new CoverageInfoNameComparator());

        // 2) delete any existing generated files in the generation directory
        ServletContext sc = request.getSession().getServletContext();

        //File rootDir =  GeoserverDataDirectory.getGeoserverDataDirectory(sc);
        //File previewDir = new File(sc.getRealPath("data/generated"));
        if (sc.getRealPath("preview") == null) {
            //There's a problem here, since we can't get a real path for the "preview" directory.
            //On tomcat this happens when "unpackWars=false" is set for this context.
            throw new RuntimeException(
                "Couldn't populate preview directory...is the war running in unpacked mode?");
        }

        File previewDir = new File(sc.getRealPath("preview"));

        //File previewDir = new File(rootDir, "data/generated");
        if (!previewDir.exists()) {
            previewDir.mkdirs();
        }

        // We need to create a 4326 CRS for comparison to layer's crs
        CoordinateReferenceSystem latLonCrs = null;

        try { // get the CRS object for lat/lon 4326
            latLonCrs = CRS.decode("EPSG:" + 4326);
        } catch (NoSuchAuthorityCodeException e) {
            String msg = "Error looking up SRS for EPSG: " + 4326 + ":" + e.getLocalizedMessage();
            LOGGER.warning(msg);
        } catch (FactoryException e) {
            String msg = "Error looking up SRS for EPSG: " + 4326 + ":" + e.getLocalizedMessage();
            LOGGER.warning(msg);
        }

        // 3) Go through each *FeatureType* and collect information && write out config files
        for (Iterator it = ftypes.iterator(); it.hasNext();) {
            FeatureTypeInfo layer = (FeatureTypeInfo) it.next();

            if (!layer.isEnabled() || layer.isGeometryless()) {
                continue; // if it isn't enabled, move to the next layer
            }

            CoordinateReferenceSystem layerCrs = layer.getDeclaredCRS();

            /* A quick and efficient way to grab the bounding box is to get it
             * from the featuretype info where the lat/lon bbox is loaded
             * from the DTO. We do this with layer.getLatLongBoundingBox().
             * We need to reproject the bounding box from lat/lon to the layer crs
             * for display
             */
            Envelope orig_bbox = layer.getLatLongBoundingBox();

            if ((orig_bbox.getWidth() == 0) || (orig_bbox.getHeight() == 0)) {
                orig_bbox.expandBy(0.1);
            }

            ReferencedEnvelope bbox = new ReferencedEnvelope(orig_bbox, latLonCrs);

            if (!CRS.equalsIgnoreMetadata(layerCrs, latLonCrs)) {
                try { // reproject the bbox to the layer crs
                    bbox = bbox.transform(layerCrs, true);
                } catch (TransformException e) {
                    e.printStackTrace();
                } catch (FactoryException e) {
                    e.printStackTrace();
                }
            }

            // we now have a bounding box in the same CRS as the layer

            // prepare strings for web output
            ftList.add(layer.getNameSpace().getPrefix() + "_"
                + layer.getFeatureType().getTypeName()); // FeatureType name
            ftnsList.add(layer.getNameSpace().getPrefix() + ":"
                + layer.getFeatureType().getTypeName());
            dsList.add(layer.getDataStoreInfo().getId()); // DataStore info
                                                          // bounding box of the FeatureType

            bboxList.add(bbox.getMinX() + "," + bbox.getMinY() + "," + bbox.getMaxX() + ","
                + bbox.getMaxY());
            srsList.add("EPSG:" + layer.getSRS());

            int[] imageBox = getMapWidthHeight(bbox);
            widthList.add(String.valueOf(imageBox[0]));
            heightList.add(String.valueOf(imageBox[1]));
        }

        // 3.5) Go through each *Coverage* and collect its info
        for (Iterator it = ctypes.iterator(); it.hasNext();) {
            CoverageInfo layer = (CoverageInfo) it.next();

            // upper right corner? lower left corner? Who knows?! Better naming conventions needed guys.
            double[] lowerLeft = layer.getEnvelope().getLowerCorner().getCoordinates();
            double[] upperRight = layer.getEnvelope().getUpperCorner().getCoordinates();
            Envelope bbox = new Envelope(lowerLeft[0], upperRight[0], lowerLeft[1], upperRight[1]);

            if (layer.isEnabled()) {
                // prepare strings for web output
                String shortLayerName = layer.getName().split(":")[1]; // we don't want the namespace prefix

                ftList.add(layer.getNameSpace().getPrefix() + "_" + shortLayerName); // Coverage name
                ftnsList.add(layer.getNameSpace().getPrefix() + ":" + shortLayerName);

                dsList.add(layer.getFormatInfo().getId()); // DataStore info
                                                           // bounding box of the Coverage

                bboxList.add(bbox.getMinX() + "," + bbox.getMinY() + "," + bbox.getMaxX() + ","
                    + bbox.getMaxY());
                srsList.add(layer.getSrsName());

                int[] imageBox = getMapWidthHeight(bbox);
                widthList.add(String.valueOf(imageBox[0]));
                heightList.add(String.valueOf(imageBox[1]));
            }
        }

        // 4) send off gathered information to the .jsp
        DynaActionForm myForm = (DynaActionForm) form;

        myForm.set("DSNameList", dsList.toArray(new String[dsList.size()]));
        myForm.set("FTNameList", ftList.toArray(new String[ftList.size()]));
        myForm.set("BBoxList", bboxList.toArray(new String[bboxList.size()]));
        myForm.set("SRSList", srsList.toArray(new String[srsList.size()]));
        myForm.set("WidthList", widthList.toArray(new String[widthList.size()]));
        myForm.set("HeightList", heightList.toArray(new String[heightList.size()]));
        myForm.set("FTNamespaceList", ftnsList.toArray(new String[ftnsList.size()]));

        return mapping.findForward("success");
    }

    private int[] getMapWidthHeight(Envelope bbox) {
        int width;
        int height;
        double ratio = bbox.getHeight() / bbox.getWidth();

        if (ratio < 1) {
            width = 750;
            height = (int) Math.round(750 * ratio);
        } else {
            width = (int) Math.round(550 / ratio);
            height = 550;
        }

        // make sure we reach some minimal dimensions (300 pixels is more or less 
        // the height of the zoom bar)
        if (width < 300) {
            width = 300;
        }

        if (height < 300) {
            height = 300;
        }

        // add 50 pixels horizontally to account for the zoom bar
        return new int[] { width + 50, height };
    }

    private static class FeatureTypeInfoNameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FeatureTypeInfo ft1 = (FeatureTypeInfo) o1;
            FeatureTypeInfo ft2 = (FeatureTypeInfo) o2;
            String ft1Name = ft1.getNameSpace().getPrefix() + ft1.getName();
            String ft2Name = ft2.getNameSpace().getPrefix() + ft2.getName();

            return ft1Name.compareTo(ft2Name);
        }
    }

    private static class CoverageInfoNameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            CoverageInfo c1 = (CoverageInfo) o1;
            CoverageInfo c2 = (CoverageInfo) o2;
            String ft1Name = c1.getNameSpace().getPrefix() + c1.getName();
            String ft2Name = c2.getNameSpace().getPrefix() + c2.getName();

            return ft1Name.compareTo(ft2Name);
        }
    }
}
