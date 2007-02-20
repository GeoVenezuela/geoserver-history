/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.featureInfo;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;


/**
 * Generates a FeatureInfoResponse of type text. This simply reports the
 * attributes of the feature requested as a text string. This class just
 * performs the writeTo, the GetFeatureInfoDelegate and abstract feature info
 * class handle the rest.
 *
 * @author James Macgill, PSU
 * @version $Id: TextFeatureInfoResponse.java,v 1.3 2004/07/19 22:31:40 jmacgill Exp $
 */
public class TextFeatureInfoResponse extends AbstractFeatureInfoResponse {
    /**
             *
             */
    public TextFeatureInfoResponse() {
        format = "text/plain";
        supportedFormats = Collections.singletonList("text/plain");
    }

    /**
     * Returns any extra headers that this service might want to set in
     * the HTTP response object.
     *
     * @see org.vfny.geoserver.Response#getResponseHeaders()
     */
    public HashMap getResponseHeaders() {
        return new HashMap();
    }

    /**
     * Writes the feature information to the client in text/plain
     * format.
     *
     * @param out The output stream to write to.
     */
    public void writeTo(OutputStream out)
        throws org.vfny.geoserver.ServiceException, java.io.IOException {
        Charset charSet = getRequest().getGeoServer().getCharSet();
        OutputStreamWriter osw = new OutputStreamWriter(out, charSet);

        // getRequest().getGeoServer().getCharSet());
        PrintWriter writer = new PrintWriter(osw);

        // DJB: this is to limit the number of features read - as per the spec
        // 7.3.3.7 FEATURE_COUNT
        int featuresPrinted = 0; // how many features we've actually printed
                                 // so far!

        int maxfeatures = getRequest().getFeatureCount(); // will default to 1
                                                          // if not specified
                                                          // in the request

        FeatureReader reader = null;

        try {
            final int size = results.size();
            FeatureResults fr;
            Feature f;

            FeatureType schema;
            AttributeType[] types;

            for (int i = 0; i < size; i++) // for each layer queried
             {
                fr = (FeatureResults) results.get(i);
                reader = fr.reader();

                if (reader.hasNext() && (featuresPrinted < maxfeatures)) // if this layer has a hit and we're going to print it
                 {
                    writer.println("Results for FeatureType '"
                        + reader.getFeatureType().getTypeName() + "':");
                }

                while (reader.hasNext()) {
                    f = reader.next();
                    schema = f.getFeatureType();
                    types = schema.getAttributeTypes();

                    if (featuresPrinted < maxfeatures) {
                        writer.println("--------------------------------------------");

                        for (int j = 0; j < types.length; j++) // for each
                                                               // column in the
                                                               // featuretype
                         {
                            if (Geometry.class.isAssignableFrom(types[j].getType())) {
                                // writer.println(types[j].getName() + " =
                                // [GEOMETRY]");

                                // DJB: changed this to print out WKT - its very
                                // nice for users
                                // Geometry g = (Geometry)
                                // f.getAttribute(types[j].getName());
                                // writer.println(types[j].getName() + " =
                                // [GEOMETRY] = "+g.toText() );

                                // DJB: decided that all the geometry info was
                                // too much - they should use GML version if
                                // they want those details
                                Geometry g = (Geometry) f.getAttribute(types[j].getName());
                                writer.println(types[j].getName() + " = [GEOMETRY ("
                                    + g.getGeometryType() + ") with " + g.getNumPoints()
                                    + " points]");
                            } else {
                                writer.println(types[j].getName() + " = "
                                    + f.getAttribute(types[j].getName()));
                            }
                        }

                        writer.println("--------------------------------------------");
                        featuresPrinted++;
                    }
                }
            }
        } catch (IllegalAttributeException ife) {
            writer.println("Unable to generate information " + ife);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (featuresPrinted == 0) {
            writer.println("no features were found");
        }

        writer.flush();
    }

    public String getContentDisposition() {
        // TODO Auto-generated method stub
        return null;
    }
}
