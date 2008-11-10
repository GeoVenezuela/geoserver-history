package org.geoserver.vfny.global;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.data.test.MockData;
import org.geoserver.test.GeoServerTestSupport;
import org.vfny.geoserver.global.Config;

import com.vividsolutions.jts.geom.Envelope;

public class TolerantStartupTest extends GeoServerTestSupport {
    
    @Override
    public MockData buildTestData() throws Exception {
        MockData md = new MockData();
        
        QName name = MockData.BASIC_POLYGONS;
        URL properties = MockData.class.getResource(name.getLocalPart() + ".properties");
        String styleName = name.getLocalPart();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(MockData.KEY_STYLE, styleName);
        props.put(MockData.KEY_SRS_HANDLINGS, ProjectionPolicy.REPROJECT_TO_DECLARED.getCode());
        props.put(MockData.KEY_SRS_NUMBER, "900913");
        Envelope env = new Envelope(-13000000, -7500000, 2800000, 6400000);
        props.put(MockData.KEY_NATIVE_ENVELOPE, env);
        md.addPropertiesType(name, properties, props);
        
        md.addWellKnownTypes(new QName[] {MockData.BUILDINGS});
        
        return md;
    }
    
    @Override
    protected String getLogConfiguration() {
        return "/DEFAULT_LOGGING.properties";
    }
    
    public void testContextStartup() {
        Config config = (Config) applicationContext.getBean("config");
        assertNotNull(config.getData().getFeaturesTypes().get(getLayerId(MockData.BUILDINGS)));        
        assertNull(config.getData().getFeaturesTypes().get(getLayerId(MockData.BASIC_POLYGONS)));
    }

}
