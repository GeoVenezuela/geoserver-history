/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v1_1_0;

import net.opengis.wfs.WfsFactory;
import org.geotools.xml.AbstractSimpleBinding;
import org.geotools.xml.InstanceComponent;
import org.geotools.xs.bindings.XSQNameBinding;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.opengis.net/wfs:TypeNameListType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="TypeNameListType"&gt;
 *      &lt;xsd:restriction base="wfs:Base_TypeNameListType"&gt;
 *          &lt;xsd:pattern value="((\w:)?\w(=\w)?){1,}"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                    Example typeName attribute value might be:
 *
 *                       typeName="ns1:Inwatera_1m=A, ns2:CoastL_1M=B"
 *
 *                    In this example, A is an alias for ns1:Inwatera_1m
 *                    and B is an alias for ns2:CoastL_1M.
 *                 &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:pattern&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class TypeNameListTypeBinding extends AbstractSimpleBinding {
    WfsFactory wfsfactory;
    NamespaceContext namespaceContext;

    public TypeNameListTypeBinding(WfsFactory wfsfactory, NamespaceContext namespaceContext) {
        this.wfsfactory = wfsfactory;
        this.namespaceContext = namespaceContext;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return WFS.TYPENAMELISTTYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return List.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(InstanceComponent instance, Object value)
        throws Exception {
        //TODO: implement list support in parser so that passed in value is a list
        //&lt;xsd:pattern value="((\w:)?\w(=\w)?){1,}"&gt;
        
        //GR: List support in parser implemented, casting to List directly
        List qNames = (List)value;

        return qNames;
    }
}
