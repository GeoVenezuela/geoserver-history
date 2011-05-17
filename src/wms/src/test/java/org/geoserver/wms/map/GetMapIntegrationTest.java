package org.geoserver.wms.map;

import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;

import org.geoserver.data.test.MockData;
import org.geoserver.wms.WMSTestSupport;
import org.geotools.image.test.ImageAssert;

import com.mockrunner.mock.web.MockHttpServletResponse;

public class GetMapIntegrationTest extends WMSTestSupport {

    @Override
    protected void populateDataDirectory(MockData dataDirectory) throws Exception {
        super.populateDataDirectory(dataDirectory);
        dataDirectory.addStyle("indexed",
                GetMapIntegrationTest.class.getResource("indexed.sld"));
        dataDirectory.addCoverage(new QName(MockData.SF_URI, "indexed", MockData.SF_PREFIX),
                GetMapIntegrationTest.class.getResource("indexed.tif"), "tif", "indexed");
        
        // this also adds the raster style
        dataDirectory.addWcs10Coverages();
        
        dataDirectory.addCoverage(new QName(MockData.SF_URI, "mosaic", MockData.SF_PREFIX), 
                getClass().getResource("../raster-filter-test.zip"), null, "raster");
    }
    
    
    public void testIndexed() throws Exception {
    	MockHttpServletResponse response = getAsServletResponse("wms?LAYERS=sf:indexed&STYLES=&FORMAT=image/png&SERVICE=WMS&VERSION=1.1.1" +
    			"&REQUEST=GetMap&SRS=EPSG:4326&BBOX=100,78,104,80&WIDTH=300&HEIGHT=150");
    	
    	assertEquals("image/png", response.getContentType());
    	
    	RenderedImage image = ImageIO.read(getBinaryInputStream(response));
    	ImageAssert.assertEquals(new File("src/test/resources/org/geoserver/wms/map/indexed-expected.png"), image, 0);
    }
    
    public void testIndexedBlackBG() throws Exception {
    	MockHttpServletResponse response = getAsServletResponse("wms?bgcolor=0x000000&LAYERS=sf:indexed&STYLES=&FORMAT=image/png&SERVICE=WMS&VERSION=1.1.1" +
    			"&REQUEST=GetMap&SRS=EPSG:4326&BBOX=100,78,104,80&WIDTH=300&HEIGHT=150&transparent=false");
    	
    	assertEquals("image/png", response.getContentType());
    	
    	RenderedImage image = ImageIO.read(getBinaryInputStream(response));
    	ImageAssert.assertEquals(new File("src/test/resources/org/geoserver/wms/map/indexed-bg-expected.png"), image, 0);
    }
    
    public void testRasterFilterRed() throws Exception {
        MockHttpServletResponse response = getAsServletResponse("wms?bgcolor=0x000000&LAYERS=sf:mosaic&STYLES=&FORMAT=image/png&SERVICE=WMS&VERSION=1.1.1" +
        "&REQUEST=GetMap&SRS=EPSG:4326&BBOX=0,0,1,1&WIDTH=150&HEIGHT=150&transparent=false&CQL_FILTER=location like 'red%25'");
        
        assertEquals("image/png", response.getContentType());
        
        // check we got the 
        RenderedImage image = ImageIO.read(getBinaryInputStream(response));
        int[] pixel = new int[3];
        image.getData().getPixel(0, 0, pixel);
        assertEquals(255, pixel[0]);
        assertEquals(0, pixel[1]);
        assertEquals(0, pixel[2]);
    }
    
    public void testRasterFilterGreen() throws Exception {
        MockHttpServletResponse response = getAsServletResponse("wms?bgcolor=0x000000&LAYERS=sf:mosaic&STYLES=&FORMAT=image/png&SERVICE=WMS&VERSION=1.1.1" +
        "&REQUEST=GetMap&SRS=EPSG:4326&BBOX=0,0,1,1&WIDTH=150&HEIGHT=150&transparent=false&CQL_FILTER=location like 'green%25'");
        
        assertEquals("image/png", response.getContentType());
        
        RenderedImage image = ImageIO.read(getBinaryInputStream(response));
        int[] pixel = new int[3];
        image.getData().getPixel(0, 0, pixel);
        assertEquals(0, pixel[0]);
        assertEquals(255, pixel[1]);
        assertEquals(0, pixel[2]);
    }
}