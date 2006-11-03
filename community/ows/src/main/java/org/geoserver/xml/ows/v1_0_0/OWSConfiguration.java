package org.geoserver.xml.ows.v1_0_0;

import net.opengis.ows.v1_0_0.OWSFactory;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geotools.xlink.XLINKConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

/**
 * Parser configuration for ows schema.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class OWSConfiguration extends Configuration {

	/**
	 * Creates a new configuration, adding the dependency on {@link OWSConfiguration}.
	 */
	public OWSConfiguration() {
		super();
		
		addDependency( new XLINKConfiguration() );
	}

	/**
	 * @return {@link OWS#NAMESPACE}, http://www.opengis.net/ows
	 */
	public String getNamespaceURI() {
		return OWS.NAMESPACE;
	}

	/**
	 * @return the owsAll.xsd file of the ows schema.
	 */
	public String getSchemaFileURL() {
		return getSchemaLocationResolver().resolveSchemaLocation( null, getNamespaceURI(), "owsAll.xsd" );	
	}

	/**
	 * @return A new instance of {@link OWSBindingConfiguration}.
	 */
	public BindingConfiguration getBindingConfiguration() {
		return new OWSBindingConfiguration();
	}

	/**
	 * @return A new instance of {@link OWSSchemaLocationResolver}.
	 */
	public XSDSchemaLocationResolver getSchemaLocationResolver() {
		return new OWSSchemaLocationResolver();
	}
	
	  /**
     * Configures the ows context.
     * <p>
     * The following factories are registered:
     * <ul>
     * <li>{@link OWSFactory}
     * </ul>
     * </p>
     */
	protected void configureContext(MutablePicoContainer container) {
		super.configureContext( container );
		
		container.registerComponentInstance( OWSFactory.eINSTANCE );
	}
	

}
