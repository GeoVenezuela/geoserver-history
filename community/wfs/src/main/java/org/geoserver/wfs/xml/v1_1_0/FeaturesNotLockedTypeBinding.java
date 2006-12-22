package org.geoserver.wfs.xml.v1_1_0;


import javax.xml.namespace.QName;

import net.opengis.wfs.FeaturesNotLockedType;
import net.opengis.wfs.WFSFactory;

import org.geoserver.ows.ComplexEMFBinding;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

/**
 * Binding object for the type http://www.opengis.net/wfs:FeaturesNotLockedType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="FeaturesNotLockedType"&gt;
 *      &lt;xsd:sequence maxOccurs="unbounded"&gt;
 *          &lt;xsd:element ref="ogc:FeatureId"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class FeaturesNotLockedTypeBinding extends ComplexEMFBinding {

	WFSFactory wfsfactory;		
	public FeaturesNotLockedTypeBinding( WFSFactory wfsfactory ) {
		this.wfsfactory = wfsfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return WFS.FEATURESNOTLOCKEDTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return FeaturesNotLockedType.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: implement
		return null;
	}

}