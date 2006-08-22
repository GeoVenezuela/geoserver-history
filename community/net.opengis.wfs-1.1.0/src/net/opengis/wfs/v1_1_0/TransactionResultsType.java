/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wfs.v1_1_0;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Transaction Results Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *             The TransactionResults element may be used to report exception
 *             codes and messages for all actions of a transaction that failed
 *             to complete successfully.
 *          
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wfs.v1_1_0.TransactionResultsType#getAction <em>Action</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wfs.v1_1_0.WFSPackage#getTransactionResultsType()
 * @model extendedMetaData="name='TransactionResultsType' kind='elementOnly'"
 * @generated
 */
public interface TransactionResultsType extends EObject {
	/**
	 * Returns the value of the '<em><b>Action</b></em>' containment reference list.
	 * The list contents are of type {@link net.opengis.wfs.v1_1_0.ActionType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * 
	 *                   The Action element reports an exception code
	 *                   and exception message indicating why the
	 *                   corresponding action of a transaction request
	 *                   failed.
	 *                
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Action</em>' containment reference list.
	 * @see net.opengis.wfs.v1_1_0.WFSPackage#getTransactionResultsType_Action()
	 * @model type="net.opengis.wfs.v1_1_0.ActionType" containment="true" resolveProxies="false"
	 *        extendedMetaData="kind='element' name='Action' namespace='##targetNamespace'"
	 * @generated
	 */
	EList getAction();

} // TransactionResultsType
