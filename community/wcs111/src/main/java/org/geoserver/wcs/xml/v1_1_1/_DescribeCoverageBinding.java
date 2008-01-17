package org.geoserver.wcs.xml.v1_1_1;


import javax.xml.namespace.QName;

import net.opengis.wcs.v1_1_1.DescribeCoverageType;
import net.opengis.wcs.v1_1_1.Wcs111Factory;

import org.geotools.xml.AbstractComplexEMFBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

/**
 * Binding object for the type http://www.opengis.net/wcs/1.1.1:_DescribeCoverage.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="_DescribeCoverage"&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="wcs:RequestBaseType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element maxOccurs="unbounded" ref="wcs:Identifier"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;Unordered list of identifiers of desired coverages. A client can obtain identifiers by a prior GetCapabilities request, or from a third-party source. &lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *              &lt;/sequence&gt;
 *          &lt;/extension&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class _DescribeCoverageBinding extends AbstractComplexEMFBinding {
    
    public _DescribeCoverageBinding() {
        super(Wcs111Factory.eINSTANCE);
    }

	/**
	 * @generated
	 */
	public QName getTarget() {
		return WCS._DescribeCoverage;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return DescribeCoverageType.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: implement and remove call to super
		return super.parse(instance,node,value);
	}

}