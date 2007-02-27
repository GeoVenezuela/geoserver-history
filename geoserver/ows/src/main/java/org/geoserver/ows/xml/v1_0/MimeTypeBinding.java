/* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.ows.xml.v1_0;

import net.opengis.ows.OwsFactory;
import org.geotools.xml.AbstractSimpleBinding;
import org.geotools.xml.InstanceComponent;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.opengis.net/ows:MimeType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;simpleType name="MimeType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;XML encoded identifier of a standard MIME type, possibly a parameterized MIME type. &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;restriction base="string"&gt;
 *          &lt;pattern value="(application|audio|image|text|video|message|multipart|model)/.+(;\s*.+=.+)*"/&gt;
 *      &lt;/restriction&gt;
 *  &lt;/simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class MimeTypeBinding extends AbstractSimpleBinding {
    OwsFactory owsfactory;

    public MimeTypeBinding(OwsFactory owsfactory) {
        this.owsfactory = owsfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OWS.MIMETYPE;
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
    public Object parse(InstanceComponent instance, Object value)
        throws Exception {
        //TODO: implement
        return null;
    }
}
