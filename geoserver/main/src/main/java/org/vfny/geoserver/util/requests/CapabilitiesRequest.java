/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.util.requests;

import org.vfny.geoserver.Request;
import org.vfny.geoserver.servlets.AbstractService;


/**
 * This class enforces a standard interface for GetCapabilities requests.
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @version $Id: CapabilitiesRequest.java,v 1.4 2004/01/31 00:27:25 jive Exp $
 */
public class CapabilitiesRequest extends Request {
    /**
         * Creates a new capabilities request object.
         *
         * @param serviceType The id of the service being handled.
         * @param service The service handing the request.
         */
    public CapabilitiesRequest(String serviceType, AbstractService service) {
        super(serviceType, "GetCapabilities", service);
    }

    /**
     * Returns a string representation of this CapabilitiesRequest.
     *
     * @return a string of with the service and version.
     */
    public String toString() {
        return "GetCapabilities [service: " + service + ", version: " + version + "]";
    }

    /**
     * Override of equals.  Just calls super.equals, since there are no
     * extra fields here that aren't in Request.
     *
     * @param o the object to test against.
     *
     * @return <tt>true</tt> if o is equal to this request.
     */
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
