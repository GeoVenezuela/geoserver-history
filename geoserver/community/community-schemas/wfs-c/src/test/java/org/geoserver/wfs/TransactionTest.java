/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import org.w3c.dom.Document;


/**
 * This test must be run with the server configured with the wfs 1.0 cite
 * configuration, with data initialized.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class TransactionTest extends WFSTestSupport {
    public void testDelete() throws Exception {
        if (skipDisabled()) {
            return; // FIXME: this test is disabled by default
        }
       // 1. do a getFeature
        String getFeature = "<wfs:GetFeature " + "service=\"WFS\" " + "version=\"1.0.0\" "
            + "xmlns:cgf=\"http://www.opengis.net/cite/geometry\" "
            + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
            + "xmlns:wfs=\"http://www.opengis.net/wfs\" " + "> "
            + "<wfs:Query typeName=\"cgf:Points\"> "
            + "<ogc:PropertyName>cite:id</ogc:PropertyName> " + "</wfs:Query> "
            + "</wfs:GetFeature>";

        Document dom = postAsDOM("wfs", getFeature);
        assertEquals(1, dom.getElementsByTagName("gml:featureMember").getLength());

        // perform a delete
        String delete = "<wfs:Transaction service=\"WFS\" version=\"1.0.0\" "
            + "xmlns:cgf=\"http://www.opengis.net/cite/geometry\" "
            + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
            + "xmlns:wfs=\"http://www.opengis.net/wfs\"> "
            + "<wfs:Delete typeName=\"cgf:Points\"> " + "<ogc:Filter> "
            + "<ogc:PropertyIsEqualTo> " + "<ogc:PropertyName>cgf:id</ogc:PropertyName> "
            + "<ogc:Literal>t0000</ogc:Literal> " + "</ogc:PropertyIsEqualTo> " + "</ogc:Filter> "
            + "</wfs:Delete> " + "</wfs:Transaction>";

        dom = postAsDOM("wfs", delete);
        assertEquals("WFS_TransactionResponse", dom.getDocumentElement().getLocalName());
        assertEquals(1, dom.getElementsByTagName("wfs:SUCCESS").getLength());

        // do another get feature
        dom = postAsDOM("wfs", getFeature);

        assertEquals(0, dom.getElementsByTagName("gml:featureMember").getLength());
    }

    public void testInsert() throws Exception {
        if (skipDisabled()) {
            return; // FIXME: this test is disabled by default
        }
        // 1. do a getFeature
        String getFeature = "<wfs:GetFeature " + "service=\"WFS\" " + "version=\"1.0.0\" "
            + "xmlns:cgf=\"http://www.opengis.net/cite/geometry\" "
            + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
            + "xmlns:wfs=\"http://www.opengis.net/wfs\" " + "> "
            + "<wfs:Query typeName=\"cgf:Lines\"> "
            + "<ogc:PropertyName>cite:id</ogc:PropertyName> " + "</wfs:Query> "
            + "</wfs:GetFeature>";

        Document dom = postAsDOM("wfs", getFeature);
        assertEquals(1, dom.getElementsByTagName("gml:featureMember").getLength());

        // perform an insert
        String insert = "<wfs:Transaction service=\"WFS\" version=\"1.0.0\" "
            + "xmlns:cgf=\"http://www.opengis.net/cite/geometry\" "
            + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
            + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
            + "xmlns:gml=\"http://www.opengis.net/gml\"> " + "<wfs:Insert > " + "<cgf:Lines>"
            + "<cgf:lineStringProperty>" + "<gml:LineString>"
            + "<gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"
            + "494475.71056415,5433016.8189323 494982.70115662,5435041.95096618"
            + "</gml:coordinates>" + "</gml:LineString>" + "</cgf:lineStringProperty>"
            + "<cgf:id>t0002</cgf:id>" + "</cgf:Lines>" + "</wfs:Insert>" + "</wfs:Transaction>";

        dom = postAsDOM("wfs", insert);
        assertTrue(dom.getElementsByTagName("wfs:SUCCESS").getLength() != 0);
        assertTrue(dom.getElementsByTagName("wfs:InsertResult").getLength() != 0);

        // do another get feature
        dom = postAsDOM("wfs", getFeature);
        assertEquals(2, dom.getElementsByTagName("gml:featureMember").getLength());
    }

    public void testUpdate() throws Exception {
        if (skipDisabled()) {
            return; // FIXME: this test is disabled by default
        }
        // 1. do a getFeature
        String getFeature = "<wfs:GetFeature " + "service=\"WFS\" " + "version=\"1.0.0\" "
            + "xmlns:cgf=\"http://www.opengis.net/cite/geometry\" "
            + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
            + "xmlns:wfs=\"http://www.opengis.net/wfs\" " + "> "
            + "<wfs:Query typeName=\"cgf:Polygons\"> "
            + "<ogc:PropertyName>cite:id</ogc:PropertyName> " + "</wfs:Query> "
            + "</wfs:GetFeature>";

        Document dom = postAsDOM("wfs", getFeature);
        assertEquals(1, dom.getElementsByTagName("gml:featureMember").getLength());
        assertEquals("t0002",
            dom.getElementsByTagName("cgf:id").item(0).getFirstChild().getNodeValue());

        // perform an update
        String insert = "<wfs:Transaction service=\"WFS\" version=\"1.0.0\" "
            + "xmlns:cgf=\"http://www.opengis.net/cite/geometry\" "
            + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
            + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
            + "xmlns:gml=\"http://www.opengis.net/gml\"> "
            + "<wfs:Update typeName=\"cgf:Polygons\" > " + "<wfs:Property>"
            + "<wfs:Name>id</wfs:Name>" + "<wfs:Value>t0003</wfs:Value>" + "</wfs:Property>"
            + "<ogc:Filter>" + "<ogc:PropertyIsEqualTo>"
            + "<ogc:PropertyName>id</ogc:PropertyName>" + "<ogc:Literal>t0002</ogc:Literal>"
            + "</ogc:PropertyIsEqualTo>" + "</ogc:Filter>" + "</wfs:Update>" + "</wfs:Transaction>";

        dom = postAsDOM("wfs", insert);

        // do another get feature
        dom = postAsDOM("wfs", getFeature);
        assertEquals("t0003",
            dom.getElementsByTagName("cgf:id").item(0).getFirstChild().getNodeValue());
    }

    public void testInsertWithBoundedBy() throws Exception {
        String xml = "<wfs:Transaction service=\"WFS\" version=\"1.0.0\" "
            + " xmlns:wfs=\"http://www.opengis.net/wfs\" "
            + " xmlns:gml=\"http://www.opengis.net/gml\" "
            + " xmlns:cite=\"http://www.opengeospatial.org/cite\">" + "<wfs:Insert>"
            + " <cite:BasicPolygons>" + "<gml:boundedBy>" + "<gml:Box>"
            + "<gml:coordinates cs=\",\" decimal=\".\" ts=\" \">-2,-1 2,6</gml:coordinates>"
            + "</gml:Box>" + "</gml:boundedBy>" + "  <cite:the_geom>" + "<gml:MultiPolygon>"
            + "<gml:polygonMember>" + "<gml:Polygon>" + "<gml:outerBoundaryIs>"
            + "<gml:LinearRing>"
            + "<gml:coordinates cs=\",\" decimal=\".\" ts=\" \">-1,0 0,1 1,0 0,-1 -1,0</gml:coordinates>"
            + "</gml:LinearRing>" + "</gml:outerBoundaryIs>" + "</gml:Polygon>"
            + "</gml:polygonMember>" + "</gml:MultiPolygon>" + "  </cite:the_geom>"
            + "  <cite:ID>foo</cite:ID>" + " </cite:BasicPolygons>" + "</wfs:Insert>"
            + "</wfs:Transaction>";

        Document dom = postAsDOM("wfs", xml);

        assertEquals("wfs:WFS_TransactionResponse", dom.getDocumentElement().getNodeName());
        assertTrue(dom.getElementsByTagName("ogc:FeatureId").getLength() > 0);
        assertTrue(dom.getElementsByTagName("wfs:SUCCESS").getLength() > 0);
    }
}
