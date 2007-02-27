/* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v1_0_0;

import net.opengis.wfs.GetFeatureWithLockType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.WfsFactory;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import java.math.BigInteger;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.opengis.net/wfs:GetFeatureWithLockType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="GetFeatureWithLockType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *              A GetFeatureWithLock request operates identically to a
 *              GetFeature request expect that it attempts to lock the
 *              feature instances in the result set and includes a lock
 *              identifier in its response to a client.  A lock identifier
 *              is an identifier generated by a Web Feature Service that
 *              a client application can use, in subsequent operations,
 *              to reference the locked set of feature instances.
 *           &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element maxOccurs="unbounded" ref="wfs:Query"/&gt;
 *      &lt;/xsd:sequence&gt;
 *      &lt;xsd:attribute fixed="1.0.0" name="version" type="xsd:string" use="required"/&gt;
 *      &lt;xsd:attribute fixed="WFS" name="service" type="xsd:string" use="required"/&gt;
 *      &lt;xsd:attribute name="handle" type="xsd:string" use="optional"/&gt;
 *      &lt;xsd:attribute name="expiry" type="xsd:positiveInteger" use="optional"/&gt;
 *      &lt;xsd:attribute default="GML2" name="outputFormat" type="xsd:string" use="optional"/&gt;
 *      &lt;xsd:attribute name="maxFeatures" type="xsd:positiveInteger" use="optional"/&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class GetFeatureWithLockTypeBinding extends AbstractComplexBinding {
    WfsFactory wfsfactory;

    public GetFeatureWithLockTypeBinding(WfsFactory wfsfactory) {
        this.wfsfactory = wfsfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return WFS.GETFEATUREWITHLOCKTYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return GetFeatureWithLockType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        GetFeatureWithLockType getFeatureWithLock = wfsfactory
            .createGetFeatureWithLockType();

        WFSBindingUtils.service(getFeatureWithLock, node);
        WFSBindingUtils.version(getFeatureWithLock, node);
        WFSBindingUtils.outputFormat(getFeatureWithLock, node, "GML2");

        if (node.getAttributeValue("handle") != null) {
            getFeatureWithLock.setHandle((String) node.getAttributeValue("handle"));
        }

        //get the max features
        BigInteger maxFeatures = WFSBindingUtils.asBigInteger((Number) node.getAttributeValue(
                    "maxFeatures"));

        if (maxFeatures != null) {
            getFeatureWithLock.setMaxFeatures(maxFeatures);
        }

        //get the lock expiry
        BigInteger expiry = WFSBindingUtils.asBigInteger((Number) node.getAttributeValue("expiry"));

        if (expiry != null) {
            getFeatureWithLock.setExpiry(expiry);
        }

        //queries
        getFeatureWithLock.getQuery().addAll(node.getChildValues(QueryType.class));

        return getFeatureWithLock;
    }
}
