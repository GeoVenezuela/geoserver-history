/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wfs.v1_1_0.impl;

import java.math.BigInteger;

import net.opengis.wfs.v1_1_0.GetGmlObjectType;
import net.opengis.wfs.v1_1_0.WFSPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Get Gml Object Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wfs.v1_1_0.impl.GetGmlObjectTypeImpl#getGmlObjectId <em>Gml Object Id</em>}</li>
 *   <li>{@link net.opengis.wfs.v1_1_0.impl.GetGmlObjectTypeImpl#getOutputFormat <em>Output Format</em>}</li>
 *   <li>{@link net.opengis.wfs.v1_1_0.impl.GetGmlObjectTypeImpl#getTraverseXlinkDepth <em>Traverse Xlink Depth</em>}</li>
 *   <li>{@link net.opengis.wfs.v1_1_0.impl.GetGmlObjectTypeImpl#getTraverseXlinkExpiry <em>Traverse Xlink Expiry</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GetGmlObjectTypeImpl extends BaseRequestTypeImpl implements GetGmlObjectType {
	/**
	 * The default value of the '{@link #getGmlObjectId() <em>Gml Object Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGmlObjectId()
	 * @generated
	 * @ordered
	 */
	protected static final Object GML_OBJECT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getGmlObjectId() <em>Gml Object Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGmlObjectId()
	 * @generated
	 * @ordered
	 */
	protected Object gmlObjectId = GML_OBJECT_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getOutputFormat() <em>Output Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputFormat()
	 * @generated
	 * @ordered
	 */
	protected static final String OUTPUT_FORMAT_EDEFAULT = "GML3";

	/**
	 * The cached value of the '{@link #getOutputFormat() <em>Output Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputFormat()
	 * @generated
	 * @ordered
	 */
	protected String outputFormat = OUTPUT_FORMAT_EDEFAULT;

	/**
	 * This is true if the Output Format attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean outputFormatESet = false;

	/**
	 * The default value of the '{@link #getTraverseXlinkDepth() <em>Traverse Xlink Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTraverseXlinkDepth()
	 * @generated
	 * @ordered
	 */
	protected static final String TRAVERSE_XLINK_DEPTH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTraverseXlinkDepth() <em>Traverse Xlink Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTraverseXlinkDepth()
	 * @generated
	 * @ordered
	 */
	protected String traverseXlinkDepth = TRAVERSE_XLINK_DEPTH_EDEFAULT;

	/**
	 * The default value of the '{@link #getTraverseXlinkExpiry() <em>Traverse Xlink Expiry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTraverseXlinkExpiry()
	 * @generated
	 * @ordered
	 */
	protected static final BigInteger TRAVERSE_XLINK_EXPIRY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTraverseXlinkExpiry() <em>Traverse Xlink Expiry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTraverseXlinkExpiry()
	 * @generated
	 * @ordered
	 */
	protected BigInteger traverseXlinkExpiry = TRAVERSE_XLINK_EXPIRY_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GetGmlObjectTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return WFSPackage.eINSTANCE.getGetGmlObjectType();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getGmlObjectId() {
		return gmlObjectId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGmlObjectId(Object newGmlObjectId) {
		Object oldGmlObjectId = gmlObjectId;
		gmlObjectId = newGmlObjectId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WFSPackage.GET_GML_OBJECT_TYPE__GML_OBJECT_ID, oldGmlObjectId, gmlObjectId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOutputFormat(String newOutputFormat) {
		String oldOutputFormat = outputFormat;
		outputFormat = newOutputFormat;
		boolean oldOutputFormatESet = outputFormatESet;
		outputFormatESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WFSPackage.GET_GML_OBJECT_TYPE__OUTPUT_FORMAT, oldOutputFormat, outputFormat, !oldOutputFormatESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetOutputFormat() {
		String oldOutputFormat = outputFormat;
		boolean oldOutputFormatESet = outputFormatESet;
		outputFormat = OUTPUT_FORMAT_EDEFAULT;
		outputFormatESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, WFSPackage.GET_GML_OBJECT_TYPE__OUTPUT_FORMAT, oldOutputFormat, OUTPUT_FORMAT_EDEFAULT, oldOutputFormatESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetOutputFormat() {
		return outputFormatESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTraverseXlinkDepth() {
		return traverseXlinkDepth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTraverseXlinkDepth(String newTraverseXlinkDepth) {
		String oldTraverseXlinkDepth = traverseXlinkDepth;
		traverseXlinkDepth = newTraverseXlinkDepth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_DEPTH, oldTraverseXlinkDepth, traverseXlinkDepth));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigInteger getTraverseXlinkExpiry() {
		return traverseXlinkExpiry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTraverseXlinkExpiry(BigInteger newTraverseXlinkExpiry) {
		BigInteger oldTraverseXlinkExpiry = traverseXlinkExpiry;
		traverseXlinkExpiry = newTraverseXlinkExpiry;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_EXPIRY, oldTraverseXlinkExpiry, traverseXlinkExpiry));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
		switch (eDerivedStructuralFeatureID(eFeature)) {
			case WFSPackage.GET_GML_OBJECT_TYPE__HANDLE:
				return getHandle();
			case WFSPackage.GET_GML_OBJECT_TYPE__SERVICE:
				return getService();
			case WFSPackage.GET_GML_OBJECT_TYPE__VERSION:
				return getVersion();
			case WFSPackage.GET_GML_OBJECT_TYPE__GML_OBJECT_ID:
				return getGmlObjectId();
			case WFSPackage.GET_GML_OBJECT_TYPE__OUTPUT_FORMAT:
				return getOutputFormat();
			case WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_DEPTH:
				return getTraverseXlinkDepth();
			case WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_EXPIRY:
				return getTraverseXlinkExpiry();
		}
		return eDynamicGet(eFeature, resolve);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eSet(EStructuralFeature eFeature, Object newValue) {
		switch (eDerivedStructuralFeatureID(eFeature)) {
			case WFSPackage.GET_GML_OBJECT_TYPE__HANDLE:
				setHandle((String)newValue);
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__SERVICE:
				setService((String)newValue);
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__VERSION:
				setVersion((String)newValue);
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__GML_OBJECT_ID:
				setGmlObjectId((Object)newValue);
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__OUTPUT_FORMAT:
				setOutputFormat((String)newValue);
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_DEPTH:
				setTraverseXlinkDepth((String)newValue);
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_EXPIRY:
				setTraverseXlinkExpiry((BigInteger)newValue);
				return;
		}
		eDynamicSet(eFeature, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eUnset(EStructuralFeature eFeature) {
		switch (eDerivedStructuralFeatureID(eFeature)) {
			case WFSPackage.GET_GML_OBJECT_TYPE__HANDLE:
				setHandle(HANDLE_EDEFAULT);
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__SERVICE:
				unsetService();
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__VERSION:
				unsetVersion();
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__GML_OBJECT_ID:
				setGmlObjectId(GML_OBJECT_ID_EDEFAULT);
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__OUTPUT_FORMAT:
				unsetOutputFormat();
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_DEPTH:
				setTraverseXlinkDepth(TRAVERSE_XLINK_DEPTH_EDEFAULT);
				return;
			case WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_EXPIRY:
				setTraverseXlinkExpiry(TRAVERSE_XLINK_EXPIRY_EDEFAULT);
				return;
		}
		eDynamicUnset(eFeature);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean eIsSet(EStructuralFeature eFeature) {
		switch (eDerivedStructuralFeatureID(eFeature)) {
			case WFSPackage.GET_GML_OBJECT_TYPE__HANDLE:
				return HANDLE_EDEFAULT == null ? handle != null : !HANDLE_EDEFAULT.equals(handle);
			case WFSPackage.GET_GML_OBJECT_TYPE__SERVICE:
				return isSetService();
			case WFSPackage.GET_GML_OBJECT_TYPE__VERSION:
				return isSetVersion();
			case WFSPackage.GET_GML_OBJECT_TYPE__GML_OBJECT_ID:
				return GML_OBJECT_ID_EDEFAULT == null ? gmlObjectId != null : !GML_OBJECT_ID_EDEFAULT.equals(gmlObjectId);
			case WFSPackage.GET_GML_OBJECT_TYPE__OUTPUT_FORMAT:
				return isSetOutputFormat();
			case WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_DEPTH:
				return TRAVERSE_XLINK_DEPTH_EDEFAULT == null ? traverseXlinkDepth != null : !TRAVERSE_XLINK_DEPTH_EDEFAULT.equals(traverseXlinkDepth);
			case WFSPackage.GET_GML_OBJECT_TYPE__TRAVERSE_XLINK_EXPIRY:
				return TRAVERSE_XLINK_EXPIRY_EDEFAULT == null ? traverseXlinkExpiry != null : !TRAVERSE_XLINK_EXPIRY_EDEFAULT.equals(traverseXlinkExpiry);
		}
		return eDynamicIsSet(eFeature);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (gmlObjectId: ");
		result.append(gmlObjectId);
		result.append(", outputFormat: ");
		if (outputFormatESet) result.append(outputFormat); else result.append("<unset>");
		result.append(", traverseXlinkDepth: ");
		result.append(traverseXlinkDepth);
		result.append(", traverseXlinkExpiry: ");
		result.append(traverseXlinkExpiry);
		result.append(')');
		return result.toString();
	}

} //GetGmlObjectTypeImpl
