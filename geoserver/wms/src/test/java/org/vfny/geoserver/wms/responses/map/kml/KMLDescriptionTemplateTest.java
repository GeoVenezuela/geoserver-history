/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.map.kml;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;
import junit.framework.TestCase;
import org.geoserver.template.FeatureWrapper;
import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeature;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.w3c.dom.Document;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class KMLDescriptionTemplateTest extends TestCase {
    public void testTemplate() throws Exception {
        Configuration cfg = new Configuration();
        cfg.setObjectWrapper(new FeatureWrapper());
        cfg.setClassForTemplateLoading(KMLWriter.class, "");

        Template template = cfg.getTemplate("kmlPlacemarkDescription.ftl");
        assertNotNull(template);

        //create some data
        GeometryFactory gf = new GeometryFactory();
        FeatureType featureType = DataUtilities.createType("testType",
                "string:String,int:Integer,double:Double,geom:Point");

        DefaultFeature f = new DefaultFeature((DefaultFeatureType) featureType,
                new Object[] {
                    "three", new Integer(3), new Double(3.3), gf.createPoint(new Coordinate(3, 3))
                }, "fid.3") {
            };

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        template.process(f, new OutputStreamWriter(output));

        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(new ByteArrayInputStream(output.toByteArray()));

        assertNotNull(document);

        assertEquals("table", document.getDocumentElement().getNodeName());
    }
}
