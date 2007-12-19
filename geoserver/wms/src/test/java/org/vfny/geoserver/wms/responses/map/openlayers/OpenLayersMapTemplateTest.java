/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.map.openlayers;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import junit.framework.TestCase;
import org.geoserver.data.test.MockData;
import org.geoserver.wms.WMSTestSupport;
import org.vfny.geoserver.wms.WMSMapContext;
import org.vfny.geoserver.wms.requests.GetMapRequest;
import org.vfny.geoserver.wms.responses.map.openlayers.OpenLayersMapProducer;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class OpenLayersMapTemplateTest extends WMSTestSupport {
    public void test() throws Exception {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(OpenLayersMapProducer.class, "");
        cfg.setObjectWrapper(new BeansWrapper());

        Template template = cfg.getTemplate("OpenLayersMapTemplate.ftl");
        assertNotNull(template);

        GetMapRequest request = createGetMapRequest(MockData.BASIC_POLYGONS);
        WMSMapContext mapContext = new WMSMapContext();
        mapContext.addLayer(createMapLayer(MockData.BASIC_POLYGONS));
        mapContext.setRequest(request);
        mapContext.setMapWidth(256);
        mapContext.setMapHeight(256);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HashMap map = new HashMap();
        map.put("context", mapContext);
        map.put("request", mapContext.getRequest());
        map.put("maxResolution", new Double(0.0005)); // just a random number
        map.put("baseUrl", "http://localhost:8080/geoserver/wms");
        map.put("parameters", new ArrayList());
        map.put("layerName", "layer");
        map.put("units", "degrees");
        template.process(map, new OutputStreamWriter(output));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setExpandEntityReferences(false);

        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        docBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                    StringReader reader = new StringReader(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    InputSource source = new InputSource(reader);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);

                    return source;
                }
            });

        Document document = docBuilder.parse(new ByteArrayInputStream(output.toByteArray()));
        assertNotNull(document);

        assertEquals("html", document.getDocumentElement().getNodeName());
    }
}
