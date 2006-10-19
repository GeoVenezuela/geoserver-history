package org.geoserver.wfs.xml.v1_1_0;

import java.net.MalformedURLException;
import java.net.URL;

import net.opengis.wfs.WFSFactory;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geoserver.data.GeoServerCatalog;
import org.geoserver.wfs.xml.FeatureTypeSchema;
import org.geoserver.wfs.xml.WFSHandlerFactory;
import org.geoserver.xml.ows.v1_0_0.OWSConfiguration;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

public class WFSConfiguration extends Configuration {

	GeoServerCatalog catalog;
	
	public WFSConfiguration( GeoServerCatalog catalog ) {
		super();
		
		this.catalog = catalog;
		
		addDependency( new OGCConfiguration() );
		addDependency( new GMLConfiguration() );
		addDependency( new OWSConfiguration() );
	}
	
	public String getNamespaceURI() {
		return WFS.NAMESPACE;
	}

	public URL getSchemaFileURL() throws MalformedURLException {
		return new URL( 
			getSchemaLocationResolver().resolveSchemaLocation( null, WFS.NAMESPACE, "wfs.xsd" )		
		); 
	}

	public BindingConfiguration getBindingConfiguration() {
		return new WFSBindingConfiguration();
	}

	public XSDSchemaLocationResolver getSchemaLocationResolver() {
		return new WFSSchemaLocationResolver();
	}

	public void configureContext(MutablePicoContainer context) {
		super.configureContext( context );
		
		context.registerComponentInstance( WFSFactory.eINSTANCE );
		context.registerComponentInstance( new WFSHandlerFactory( catalog, FeatureTypeSchema.GML3.class ) );
	}

}
