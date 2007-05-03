/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.map.kml;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.map.MapLayer;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.vfny.geoserver.util.Requests;
import org.vfny.geoserver.wms.WMSMapContext;
import org.vfny.geoserver.wms.requests.GetMapRequest;
import org.xml.sax.ContentHandler;


/**
 * Tranforms a feature colleciton to a kml "Document" element which contains a
 * "Folder" element consisting of "GroundOverlay" elements.
 * <p>
 * Usage:
 * <pre>
 *  <code>
 *  //have a reference to a map context and output stream
 *  WMSMapContext context = ...
 *  OutputStream output = ...;
 *
 *  KMLRasterTransformer tx = new KMLRasterTransformer( context );
 *  for ( int i = 0; i < context.getLayerCount(); i++ ) {
 *    MapLayer mapLayer = mapConext.getMapLayer( i );
 *
 *    //transform
 *    tx.transform( mapLayer, output );
 *  }
 *  </code>
 * </pre>
 * </p>
 * <p>
 * The inline parameter {@link #setInline(boolean)} controls wether the images
 * for the request are refernces "inline" as local images, or remoteley as wms
 * requests.
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class KMLRasterTransformer extends TransformerBase {
    /**
     * The map context
     */
    final WMSMapContext mapContext;

    /**
     * Flag controlling wether images are refernces inline or as remote wms calls.
     */
    boolean inline = false;

    public KMLRasterTransformer(WMSMapContext mapContext) {
        this.mapContext = mapContext;

        setNamespaceDeclarationEnabled(false);
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    public Translator createTranslator(ContentHandler handler) {
        return new KMLRasterTranslator(handler);
    }

    class KMLRasterTranslator extends TranslatorSupport {
        public KMLRasterTranslator(ContentHandler handler) {
            super(handler, null, null);
        }

        public void encode(Object o) throws IllegalArgumentException {
            MapLayer mapLayer = (MapLayer) o;
            int mapLayerOrder = mapContext.indexOf(mapLayer);

            start("Document");
            element("name", mapLayer.getTitle());

            //start the folder naming it 'layer_<mapLayerOrder>', this is 
            // necessary for a GroundOverlay
            start("Folder");
            element("name", "layer_" + mapLayerOrder);
            element("description", mapLayer.getTitle());

            start("GroundOverlay");
            //element( "name", feature.getID() );
            element("name", mapLayer.getTitle());
            element("drawOrder", Integer.toString(mapLayerOrder));

            //encode the icon
            start("Icon");

            encodeHref(mapLayer);

            element("viewRefreshMode", "never");
            element("viewBoundScale", "0.75");
            end("Icon");

            //encde the bounding box
            Envelope box = mapContext.getRequest().getBbox();
            start("LatLonBox");
            element("north", Double.toString(box.getMaxY()));
            element("south", Double.toString(box.getMinY()));
            element("east", Double.toString(box.getMaxX()));
            element("west", Double.toString(box.getMinX()));
            end("LatLonBox");

            end("GroundOverlay");

            end("Folder");

            end("Document");
        }

        protected void encodeHref(MapLayer mapLayer) {
            if (inline) {
                //inline means reference the image "inline" as in kmz
                // use the mapLayerOrder
                int mapLayerOrder = mapContext.indexOf(mapLayer);
                element("href", "layer_" + mapLayerOrder + ".png");
            } else {
                //reference the image as a remote wms call
                GetMapRequest request = mapContext.getRequest();
                String baseUrl = Requests.getBaseUrl(request.getHttpServletRequest(),
                        request.getGeoServer());

                StringBuffer href = new StringBuffer(baseUrl).append("wms?");

                Envelope box = request.getBbox();
                href.append("bbox=").append(box.getMinX()).append(",").append(box.getMinY())
                    .append(",").append(box.getMaxX()).append(",").append(box.getMaxY());

                href.append("&layers=").append(mapLayer.getTitle());
                href.append("&styles=").append(mapLayer.getStyle().getName());
                href.append("&format=image/png");
                href.append("&height=").append(mapContext.getMapHeight()).append("&width=")
                    .append(mapContext.getMapWidth());
                href.append("&srs=EPSG:4326");
                href.append("&transparent=true&");
                ;

                element("href", href.toString());
            }
        }
    }
}
