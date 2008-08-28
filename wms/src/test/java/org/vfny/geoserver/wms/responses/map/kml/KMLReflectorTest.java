package org.vfny.geoserver.wms.responses.map.kml;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import junit.framework.Test;

import static org.custommonkey.xmlunit.XMLAssert.*;

import org.custommonkey.xmlunit.XMLAssert;
import org.geoserver.data.test.MockData;
import org.geoserver.wms.WMSTestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Some functional tests for kml reflector
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 */
public class KMLReflectorTest extends WMSTestSupport {

    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new KMLReflectorTest());
    }

    public void testFake(){}
    
    public void _testWmsRepeatedLayerWithNonStandardStyleAndCqlFiler() throws Exception {
        final String layerName = MockData.BASIC_POLYGONS.getPrefix() + ":"
                + MockData.BASIC_POLYGONS.getLocalPart();

        String requestUrl = "kml/wms?layers=" + layerName + "," + layerName
                + "&styles=Default,Default&cql_filter=att1<10;att1>1000";
        Document dom = getAsDOM(requestUrl);

        assertEquals("kml", dom.getDocumentElement().getLocalName());

        NodeList folders = dom.getDocumentElement().getElementsByTagName("Folder");
        assertEquals(1, folders.getLength());
        Element folder = (Element) folders.item(0);

        NodeList netLinks = folder.getElementsByTagName("NetworkLink");
        assertEquals(2, netLinks.getLength());

        assertXpathEvaluatesTo(layerName, "kml/Folder/NetworkLink[1]/name", dom);
        assertXpathEvaluatesTo(layerName, "kml/Folder/NetworkLink[2]/name", dom);

        XPath xpath = XPathFactory.newInstance().newXPath();

        String url1 = xpath.compile("/kml/Folder/NetworkLink[1]/Url/href").evaluate(dom);
        String url2 = xpath.compile("/kml/Folder/NetworkLink[2]/Url/href").evaluate(dom);

        assertNotNull(url1);
        assertNotNull(url2);

        Map<String, String> kvp1 = toKvp(url1);
        Map<String, String> kvp2 = toKvp(url2);

        assertEquals(layerName, kvp1.get("LAYERS"));
        assertEquals(layerName, kvp2.get("LAYERS"));

        assertEquals("Default", kvp1.get("STYLES"));
        assertEquals("Default", kvp2.get("STYLES"));

        assertEquals("att1<10", kvp1.get("CQL_FILTER"));
        assertEquals("att1>1000", kvp2.get("CQL_FILTER"));
    }

    /**
     * Creates a key/value pair map from the cgi parameters in the provided url
     * 
     * @param url
     *            an url where all the cgi parameter values are url encoded
     * @return a map with the key value pairs from the url with all the
     *         parameter names in upper case
     */
    private Map<String, String> toKvp(String url) {
        if (url.indexOf('?') > 0) {
            url = url.substring(url.indexOf('?') + 1);
        }
        Map<String, String> kvpMap = new HashMap<String, String>();

        String[] tuples = url.split("&");
        for (String tuple : tuples) {
            String[] kvp = tuple.split("=");
            String key = kvp[0].toUpperCase();
            String value = kvp.length > 1 ? kvp[1] : null;
            if (value != null) {
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            kvpMap.put(key, value);
        }

        return kvpMap;
    }
}
