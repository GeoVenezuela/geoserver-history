package org.geoserver.wfs.v1_1;

import java.util.StringTokenizer;

import junit.framework.Test;

import org.geoserver.data.test.MockData;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.WFSTestSupport;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WFSReprojectionTest extends WFSTestSupport {
    private static final String TARGET_CRS_CODE = "EPSG:900913";
    MathTransform tx;
    
    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new WFSReprojectionTest());
    }
    
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
    
        CoordinateReferenceSystem epsgTarget = CRS.decode(TARGET_CRS_CODE);
        CoordinateReferenceSystem epsg32615 = CRS.decode("urn:x-ogc:def:crs:EPSG:6.11.2:32615");
        
        tx = CRS.findMathTransform(epsg32615, epsgTarget);
        WFSInfo wfs = getWFS();
        wfs.setFeatureBounding(true);
        getGeoServer().save( wfs );
    }
    
    public void testGetFeatureGet() throws Exception {
        
        Document dom1 = getAsDOM("wfs?request=getfeature&service=wfs&version=1.0.0&typename=" + 
            MockData.POLYGONS.getLocalPart());
        print(dom1);
        Document dom2 = getAsDOM("wfs?request=getfeature&service=wfs&version=1.0.0&typename=" + 
            MockData.POLYGONS.getLocalPart() + "&srsName=" + TARGET_CRS_CODE);
        print(dom2);
        
        runTest(dom1,dom2);
    }
    
    public void testGetFeaturePost() throws Exception {
        String xml = "<wfs:GetFeature " + "service=\"WFS\" "
        + "version=\"1.0.0\" "
        + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
        + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
        + "xmlns:wfs=\"http://www.opengis.net/wfs\" " + "> "
        + "<wfs:Query typeName=\"" + 
            MockData.POLYGONS.getPrefix() + ":" + MockData.POLYGONS.getLocalPart() + "\"> "
        + "<wfs:PropertyName>cgf:polygonProperty</wfs:PropertyName> "
        + "</wfs:Query> " + "</wfs:GetFeature>";
        
        Document dom1 = postAsDOM("wfs", xml);
//        print(dom1);
        
        xml = "<wfs:GetFeature " + "service=\"WFS\" "
        + "version=\"1.0.0\" "
        + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
        + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
        + "xmlns:wfs=\"http://www.opengis.net/wfs\" " + "> "
        + "<wfs:Query srsName=\"" + TARGET_CRS_CODE + "\" typeName=\"" + 
            MockData.POLYGONS.getPrefix() + ":" + MockData.POLYGONS.getLocalPart() + "\"> "
        + "<wfs:PropertyName>cgf:polygonProperty</wfs:PropertyName> "
        + "</wfs:Query> " + "</wfs:GetFeature>";
        Document dom2 = postAsDOM("wfs", xml);
//        print(dom2);
        
        runTest(dom1, dom2);
    }
    
    public void testGetFeatureWithProjectedBoxGet() throws Exception {
        WFSInfo wfs = getWFS();
        boolean oldFeatureBounding = wfs.isFeatureBounding();
        wfs.setFeatureBounding(true);
        getGeoServer().save(wfs);
            
        try {
            String q = "wfs?request=getfeature&service=wfs&version=1.1&typeName=" + 
                MockData.POLYGONS.getLocalPart();
            Document dom = getAsDOM( q );
    //        print(dom);
            Element envelope = getFirstElementByTagName(dom, "gml:Envelope" );
            String lc = getFirstElementByTagName(envelope, "gml:lowerCorner" )
                .getFirstChild().getNodeValue();
            String uc = getFirstElementByTagName(envelope, "gml:upperCorner" )
                .getFirstChild().getNodeValue();
            double[] c = new double[]{
                Double.parseDouble(lc.split( " " )[0]), Double.parseDouble(lc.split( " " )[1]),
                Double.parseDouble(uc.split( " " )[0]), Double.parseDouble(uc.split( " " )[1]) 
            };
            double[] cr = new double[4];
            tx.transform(c, 0, cr, 0, 2);
            
            q += "&bbox=" + cr[0] + "," + cr[1] + "," + cr[2] + "," + cr[3] + "," + TARGET_CRS_CODE;
            dom = getAsDOM( q );
            
            assertEquals( 1, dom.getElementsByTagName( MockData.POLYGONS.getPrefix() + ":" + MockData.POLYGONS.getLocalPart()).getLength() );
        }
        finally {
            wfs.setFeatureBounding(oldFeatureBounding);
            getGeoServer().save( wfs );
        }
    }
    
    public void testGetFeatureWithProjectedBoxPost() throws Exception {
        WFSInfo wfs = getWFS();
        boolean oldFeatureBounding = wfs.isFeatureBounding();
        wfs.setFeatureBounding(true);
        getGeoServer().save(wfs);
        
        try {
            String q = "wfs?request=getfeature&service=wfs&version=1.1&typeName=" + 
                MockData.POLYGONS.getLocalPart();
            Document dom = getAsDOM( q );
            Element envelope = getFirstElementByTagName(dom, "gml:Envelope" );
            String lc = getFirstElementByTagName(envelope, "gml:lowerCorner" )
                .getFirstChild().getNodeValue();
            String uc = getFirstElementByTagName(envelope, "gml:upperCorner" )
                .getFirstChild().getNodeValue();
            double[] c = new double[]{
                Double.parseDouble(lc.split( " " )[0]), Double.parseDouble(lc.split( " " )[1]),
                Double.parseDouble(uc.split( " " )[0]), Double.parseDouble(uc.split( " " )[1]) 
            };
            double[] cr = new double[4];
            tx.transform(c, 0, cr, 0, 2);
            
            String xml = "<wfs:GetFeature service=\"WFS\" version=\"1.1.0\""
                + " xmlns:" + MockData.POLYGONS.getPrefix() + "=\"" + MockData.POLYGONS.getNamespaceURI() + "\""
                + " xmlns:ogc=\"http://www.opengis.net/ogc\" "
                + " xmlns:gml=\"http://www.opengis.net/gml\" "
                + " xmlns:wfs=\"http://www.opengis.net/wfs\" " + "> "
                + "<wfs:Query typeName=\"" + MockData.POLYGONS.getPrefix() + ":" + MockData.POLYGONS.getLocalPart() + "\">"
                + "<wfs:PropertyName>cgf:polygonProperty</wfs:PropertyName> "
                + "<ogc:Filter>" 
                +  "<ogc:BBOX>"
                +   "<ogc:PropertyName>polygonProperty</ogc:PropertyName>" 
                +   "<gml:Envelope srsName=\"" + TARGET_CRS_CODE + "\">"
                +      "<gml:lowerCorner>" + cr[0] + " " + cr[1] + "</gml:lowerCorner>"
                +      "<gml:upperCorner>" + cr[2] + " " + cr[3] + "</gml:upperCorner>"
                +   "</gml:Envelope>"  
                +  "</ogc:BBOX>" 
                + "</ogc:Filter>"
                + "</wfs:Query> " + "</wfs:GetFeature>";
            
            dom = postAsDOM( "wfs", xml );
            
            assertEquals( 1, dom.getElementsByTagName( MockData.POLYGONS.getPrefix() + ":" + MockData.POLYGONS.getLocalPart()).getLength() );
        }
        finally {
            wfs.setFeatureBounding(oldFeatureBounding);
            getGeoServer().save( wfs );
        }
    }
    
    private void runTest( Document dom1, Document dom2 ) throws Exception {
        Element box = getFirstElementByTagName(dom1.getDocumentElement(), "gml:Box");
        Element coordinates = getFirstElementByTagName(box, "gml:coordinates");
        double[] d1 = coordinates(coordinates.getFirstChild().getNodeValue());
        
        box = getFirstElementByTagName(dom2.getDocumentElement(), "gml:Box");
        coordinates = getFirstElementByTagName(box, "gml:coordinates");
        double[] d2 = coordinates(coordinates.getFirstChild().getNodeValue());
    
        double[] d3 = new double[d1.length];
        tx.transform(d1, 0, d3, 0, d1.length/2);
        
        for ( int i = 0; i < d2.length; i++ ) {
            assertEquals( d2[i], d3[i], 0.001 );
        }
    }
    
    private double[] coordinates(String string) {
        StringTokenizer st = new StringTokenizer(string, " ");
        double[] coordinates = new double[st.countTokens()*2];
        int i = 0;
        while(st.hasMoreTokens()) {
            String tuple = st.nextToken();
            coordinates[i++] = Double.parseDouble(tuple.split(",")[0]);
            coordinates[i++] = Double.parseDouble(tuple.split(",")[1]);
        }
        
        return coordinates;
    }
    
    public void testFilterReprojection() throws Exception {
        String xml = "<wfs:GetFeature " + "service='WFS' "
        + "version='1.0.0' "
        + "xmlns:cdf='http://www.opengis.net/cite/data' "
        + "xmlns:ogc='http://www.opengis.net/ogc' "
        + "xmlns:wfs='http://www.opengis.net/wfs' " + "> "
        + "<wfs:Query typeName='" + MockData.POLYGONS.getPrefix() + ":" + MockData.POLYGONS.getLocalPart() + "' "
        +    " srsName='" + TARGET_CRS_CODE + "'> "
      +    "> "
        +    "<wfs:PropertyName>cgf:polygonProperty</wfs:PropertyName> "
        +    "<ogc:Filter>"
        +     "<ogc:Intersects>"
        +      "<ogc:PropertyName>polygonProperty</ogc:PropertyName>"
        +        "<gml:Point xmlns:gml='http://www.opengis.net/gml'>"
        +          "<gml:coordinates decimal='.' cs=',' ts=' '>-1.035246176730227E7,504135.14926478104</gml:coordinates>"
        +        "</gml:Point>"
        +     "</ogc:Intersects>"
        +    "</ogc:Filter>"
        + "</wfs:Query> " + "</wfs:GetFeature>";
        
        Document dom = postAsDOM("wfs",xml);
        assertEquals( 1, dom.getElementsByTagName( MockData.POLYGONS.getPrefix() + ":" + MockData.POLYGONS.getLocalPart()).getLength() );
        
    }
}