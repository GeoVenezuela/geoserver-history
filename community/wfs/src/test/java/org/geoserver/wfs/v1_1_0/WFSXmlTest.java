package org.geoserver.wfs.v1_1_0;

import net.opengis.ows.v1_0_0.GetCapabilitiesType;

import org.geoserver.data.DefaultGeoServerCatalog;
import org.geoserver.data.GeoServerCatalog;
import org.geoserver.data.GeoServerResolveAdapterFactoryFinder;
import org.geoserver.wfs.xml.v1_1_0.WFS;
import org.geoserver.wfs.xml.v1_1_0.WFSConfiguration;
import org.geotools.catalog.adaptable.ResolveAdapterFactoryFinder;
import org.geotools.xml.Configuration;
import org.geotools.xml.test.XMLTestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WFSXmlTest extends XMLTestSupport {

	
	protected Configuration createConfiguration() {
		GeoServerCatalog catalog = new DefaultGeoServerCatalog( 
			new GeoServerResolveAdapterFactoryFinder()
		);
		
		return new WFSConfiguration( catalog );
	}
	
	public void test() throws Exception {
		Element root = document.createElementNS( 
			WFS.GETCAPABILITIES.getNamespaceURI(), WFS.GETCAPABILITIES.getLocalPart()
		);
		root.setAttribute( "version", "1.1.0" );
		
		document.appendChild( root );
			
		GetCapabilitiesType getCapabilities = (GetCapabilitiesType) parse();
		assertNotNull( getCapabilities );
	}

}
