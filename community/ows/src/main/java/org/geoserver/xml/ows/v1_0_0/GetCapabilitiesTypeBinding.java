package org.geoserver.xml.ows.v1_0_0;


import org.geotools.xml.*;

import net.opengis.ows.v1_0_0.AcceptFormatsType;
import net.opengis.ows.v1_0_0.AcceptVersionsType;
import net.opengis.ows.v1_0_0.GetCapabilitiesType;
import net.opengis.ows.v1_0_0.OWSFactory;		
import net.opengis.ows.v1_0_0.SectionsType;

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ows:GetCapabilitiesType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="GetCapabilitiesType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;XML encoded GetCapabilities operation request. This operation allows clients to retrieve service metadata about a specific service instance. In this XML encoding, no "request" parameter is included, since the element name specifies the specific operation. This base type shall be extended by each specific OWS to include the additional required "service" attribute, with the correct value for that OWS. &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element minOccurs="0" name="AcceptVersions" type="ows:AcceptVersionsType"&gt;
 *              &lt;annotation&gt;
 *                  &lt;documentation&gt;When omitted, server shall return latest supported version. &lt;/documentation&gt;
 *              &lt;/annotation&gt;
 *          &lt;/element&gt;
 *          &lt;element minOccurs="0" name="Sections" type="ows:SectionsType"&gt;
 *              &lt;annotation&gt;
 *                  &lt;documentation&gt;When omitted or not supported by server, server shall return complete service metadata (Capabilities) document. &lt;/documentation&gt;
 *              &lt;/annotation&gt;
 *          &lt;/element&gt;
 *          &lt;element minOccurs="0" name="AcceptFormats" type="ows:AcceptFormatsType"&gt;
 *              &lt;annotation&gt;
 *                  &lt;documentation&gt;When omitted or not supported by server, server shall return service metadata document using the MIME type "text/xml". &lt;/documentation&gt;
 *              &lt;/annotation&gt;
 *          &lt;/element&gt;
 *      &lt;/sequence&gt;
 *      &lt;attribute name="updateSequence" type="ows:UpdateSequenceType" use="optional"&gt;
 *          &lt;annotation&gt;
 *              &lt;documentation&gt;When omitted or not supported by server, server shall return latest complete service metadata document. &lt;/documentation&gt;
 *          &lt;/annotation&gt;
 *      &lt;/attribute&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class GetCapabilitiesTypeBinding extends AbstractComplexBinding {

	OWSFactory owsfactory;		
	public GetCapabilitiesTypeBinding( OWSFactory owsfactory ) {
		this.owsfactory = owsfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OWS.GETCAPABILITIESTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return null;
	}
	
	/**
	 * <!-- begin-user-doc -->
     * @param value an instance of {@link GetCapabilitiesType} (possibly a subclass) if
     * a binding for a specific service's GetCapabilities request used {@link Binding#BEFORE} 
     * {@link #getExecutionMode() execution mode}, and thus relies on this binding to fill
     * the common properties. <code>null</code> otherwise.
     * 
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		final GetCapabilitiesType getCapabilities;

        if(value == null){
            getCapabilities = owsfactory.createGetCapabilitiesType();
        }else{
            getCapabilities = (GetCapabilitiesType)value;
        }
	
		getCapabilities.setAcceptVersions( 
			(AcceptVersionsType) node.getChildValue( AcceptVersionsType.class )
		);
		getCapabilities.setSections( 
			(SectionsType) node.getChildValue( SectionsType.class ) 
		);
		getCapabilities.setAcceptFormats(
			(AcceptFormatsType) node.getChildValue( AcceptFormatsType.class )	
		);
		getCapabilities.setUpdateSequence( (String) node.getAttributeValue( "updateSequence" ) );
		
		return getCapabilities;
	}

}