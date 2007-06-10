package org.vfny.geoserver.wms.responses.map.georss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.geoserver.data.test.MockData;
import org.geoserver.wms.WMSTestSupport;
import org.geotools.data.Query;
import org.vfny.geoserver.wms.WMSMapContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AtomGeoRSSTransformerTest extends WMSTestSupport {

    WMSMapContext map;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        map = new WMSMapContext( createGetMapRequest( MockData.BASIC_POLYGONS ) );
        map.addLayer( createMapLayer( MockData.BASIC_POLYGONS ));
    }
    
    public void testLatLong() throws Exception {
        AtomGeoRSSTransformer tx = new AtomGeoRSSTransformer();
        tx.setGeometryEncoding( AtomGeoRSSTransformer.GeometryEncoding.LATLONG );
        tx.setIndentation(2);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        tx.transform( map, output );
        
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(new ByteArrayInputStream(output.toByteArray()));

        Element element = document.getDocumentElement();
        assertEquals("feed", element.getNodeName());
        
        NodeList entries = element.getElementsByTagName("entry");
        
        int n = getFeatureSource(MockData.BASIC_POLYGONS).getCount(Query.ALL);
        
        assertEquals(n,entries.getLength());
        
        for ( int i = 0; i < entries.getLength(); i++ ) {
            Element entry = (Element) entries.item( i );
            assertEquals( 1, entry.getElementsByTagName("geo:lat").getLength());
            assertEquals( 1, entry.getElementsByTagName("geo:long").getLength());
        }
    }
    
    public void testSimple() throws Exception {
        AtomGeoRSSTransformer tx = new AtomGeoRSSTransformer();
        tx.setGeometryEncoding( AtomGeoRSSTransformer.GeometryEncoding.SIMPLE );
        tx.setIndentation(2);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        tx.transform( map, output );
        
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(new ByteArrayInputStream(output.toByteArray()));

        Element element = document.getDocumentElement();
        assertEquals("feed", element.getNodeName());
        
        NodeList entries = element.getElementsByTagName("entry");
        
        int n = getFeatureSource(MockData.BASIC_POLYGONS).getCount(Query.ALL);
        
        assertEquals(n,entries.getLength());
        
        for ( int i = 0; i < entries.getLength(); i++ ) {
            Element entry = (Element) entries.item( i );
            assertEquals( 1, entry.getElementsByTagName("georss:where").getLength());
            assertEquals( 1, entry.getElementsByTagName("georss:polygon").getLength());
        }
    }
}
