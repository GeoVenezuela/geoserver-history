/* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v1_1_0;

import net.opengis.wfs.WFSFactory;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.opengis.net/wfs:GetGmlObjectType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="GetGmlObjectType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *              A GetGmlObjectType element contains exactly one GmlObjectId.
 *              The value of the gml:id attribute on that GmlObjectId is used
 *              as a unique key to retrieve the complex element with a
 *              gml:id attribute with the same value.
 *           &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="wfs:BaseRequestType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element ref="ogc:GmlObjectId"/&gt;
 *              &lt;/xsd:sequence&gt;
 *              &lt;xsd:attribute default="GML3" name="outputFormat"
 *                  type="xsd:string" use="optional"/&gt;
 *              &lt;xsd:attribute name="traverseXlinkDepth" type="xsd:string" use="required"&gt;
 *                  &lt;xsd:annotation&gt;
 *                      &lt;xsd:documentation&gt;
 *                       This attribute indicates the depth to which nested
 *                       property XLink linking element locator attribute
 *                       (href) XLinks are traversed and resolved if possible.
 *                       A value of "1" indicates that one linking element
 *                       locator attribute (href) XLink will be traversed
 *                       and the referenced element returned if possible, but
 *                       nested property XLink linking element locator attribute
 *                       (href) XLinks in the returned element are not traversed.
 *                       A value of "*" indicates that all nested property XLink
 *                       linking element locator attribute (href) XLinks will be
 *                       traversed and the referenced elements returned if
 *                       possible.  The range of valid values for this attribute
 *                       consists of positive integers plus "*".
 *                    &lt;/xsd:documentation&gt;
 *                  &lt;/xsd:annotation&gt;
 *              &lt;/xsd:attribute&gt;
 *              &lt;xsd:attribute name="traverseXlinkExpiry"
 *                  type="xsd:positiveInteger" use="optional"&gt;
 *                  &lt;xsd:annotation&gt;
 *                      &lt;xsd:documentation&gt;
 *                       The traverseXlinkExpiry attribute value is specified
 *                       in minutes.  It indicates how long a Web Feature Service
 *                       should wait to receive a response to a nested GetGmlObject
 *                       request.
 *                    &lt;/xsd:documentation&gt;
 *                  &lt;/xsd:annotation&gt;
 *              &lt;/xsd:attribute&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class GetGmlObjectTypeBinding extends AbstractComplexBinding {
    WFSFactory wfsfactory;

    public GetGmlObjectTypeBinding(WFSFactory wfsfactory) {
        this.wfsfactory = wfsfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return WFS.GETGMLOBJECTTYPE;
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
