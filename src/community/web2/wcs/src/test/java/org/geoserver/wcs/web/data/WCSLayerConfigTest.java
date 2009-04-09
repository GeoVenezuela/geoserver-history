package org.geoserver.wcs.web.data;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.wcs.web.GeoServerWicketCoverageTestSupport;
import org.geoserver.wcs.web.publish.WCSLayerConfig;
import org.geoserver.web.FormTestPage;

public class WCSLayerConfigTest extends GeoServerWicketCoverageTestSupport{
    public void testValues() {
        
        login();
        FormTestPage page = new FormTestPage(new FormTestPage.ComponentBuilder() {
        
            public Component buildComponent(String id) {
                CoverageInfo info = getCatalog().getResources(CoverageInfo.class).get(0);
                LayerInfo layer = getCatalog().getLayerByName(info.getName());
                return new WCSLayerConfig(id, new Model(layer));
            }
        });
        
        tester.startPage(page);
        tester.assertRenderedPage(FormTestPage.class);
    }
}
