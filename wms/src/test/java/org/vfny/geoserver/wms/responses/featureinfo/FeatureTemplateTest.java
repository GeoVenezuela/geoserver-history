package org.vfny.geoserver.wms.responses.featureinfo;

import java.util.Iterator;

import org.geoserver.data.test.MockData;
import org.geoserver.wms.WMSTestSupport;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.vfny.geoserver.wms.responses.featureInfo.FeatureTemplate;
import org.vfny.geoserver.wms.responses.featureinfo.dummy.Dummy;

public class FeatureTemplateTest extends WMSTestSupport {

    public void testWithDate() throws Exception {

        FeatureSource source = getFeatureSource( MockData.PRIMITIVEGEOFEATURE );
        FeatureCollection fc = source.getFeatures();
        Iterator i = fc.iterator();
        try {
            Feature f = (Feature) i.next();
            
            FeatureTemplate template = new FeatureTemplate();
            try {
                template.description( f );    
            }
            catch ( Exception e ) {
                e.printStackTrace();
                fail("template threw exception on null value");
            }
            
        }
        finally {
            fc.close( i );
        }
      
    }
    
    public void testWithNull() throws Exception {
        
        FeatureSource source = getFeatureSource( MockData.BASIC_POLYGONS );
        FeatureCollection fc = source.getFeatures();
        Iterator i = fc.iterator();
        try {
            Feature f = (Feature) i.next();
            
            FeatureTemplate template = new FeatureTemplate();
            template.description( f );
            
            //set a value to null
            f.setAttribute(1,null);
            try {
                template.description( f );    
            }
            catch ( Exception e ) {
                e.printStackTrace();
                fail("template threw exception on null value");
            }
            
        }
        finally {
            fc.close( i );
        }
      
    }
    
    public void testAlternateLookup() throws Exception {
        
        FeatureSource source = getFeatureSource( MockData.PRIMITIVEGEOFEATURE );
        FeatureCollection fc = source.getFeatures();
        Feature f = fc.features().next();
        
        FeatureTemplate template = new FeatureTemplate();
        String result = template.template(f, "dummy.ftl", Dummy.class );
        
        assertEquals( "dummy", result );
    }
}
