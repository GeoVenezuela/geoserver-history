/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wfs;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Get Capabilities Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *             This type defines the GetCapabilities operation.  In response
 *             to a GetCapabilities request, a Web Feature Service must
 *             generate a capabilities XML document that validates against
 *             the schemas defined in WFS-capabilities.xsd.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wfs.GetCapabilitiesType#getService <em>Service</em>}</li>
 *   <li>{@link net.opengis.wfs.GetCapabilitiesType#getVersion <em>Version</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wfs.WFSPackage#getGetCapabilitiesType()
 * @model extendedMetaData="name='GetCapabilitiesType' kind='empty'"
 * @generated
 */
public interface GetCapabilitiesType extends EObject{
	/**
	 * Returns the value of the '<em><b>Service</b></em>' attribute.
	 * The default value is <code>"WFS"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Service</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Service</em>' attribute.
	 * @see #isSetService()
	 * @see #unsetService()
	 * @see #setService(String)
	 * @see net.opengis.wfs.WFSPackage#getGetCapabilitiesType_Service()
	 * @model default="WFS" unique="false" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
	 *        extendedMetaData="kind='attribute' name='service'"
	 * @generated
	 */
	String getService();

	/**
	 * Sets the value of the '{@link net.opengis.wfs.GetCapabilitiesType#getService <em>Service</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Service</em>' attribute.
	 * @see #isSetService()
	 * @see #unsetService()
	 * @see #getService()
	 * @generated
	 */
	void setService(String value);

	/**
	 * Unsets the value of the '{@link net.opengis.wfs.GetCapabilitiesType#getService <em>Service</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetService()
	 * @see #getService()
	 * @see #setService(String)
	 * @generated
	 */
	void unsetService();

	/**
	 * Returns whether the value of the '{@link net.opengis.wfs.GetCapabilitiesType#getService <em>Service</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Service</em>' attribute is set.
	 * @see #unsetService()
	 * @see #getService()
	 * @see #setService(String)
	 * @generated
	 */
	boolean isSetService();

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * The default value is <code>"1.0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Version</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #isSetVersion()
	 * @see #unsetVersion()
	 * @see #setVersion(String)
	 * @see net.opengis.wfs.WFSPackage#getGetCapabilitiesType_Version()
	 * @model default="1.0.0" unique="false" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='version'"
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link net.opengis.wfs.GetCapabilitiesType#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #isSetVersion()
	 * @see #unsetVersion()
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

	/**
	 * Unsets the value of the '{@link net.opengis.wfs.GetCapabilitiesType#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetVersion()
	 * @see #getVersion()
	 * @see #setVersion(String)
	 * @generated
	 */
	void unsetVersion();

	/**
	 * Returns whether the value of the '{@link net.opengis.wfs.GetCapabilitiesType#getVersion <em>Version</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Version</em>' attribute is set.
	 * @see #unsetVersion()
	 * @see #getVersion()
	 * @see #setVersion(String)
	 * @generated
	 */
	boolean isSetVersion();

} // GetCapabilitiesType
