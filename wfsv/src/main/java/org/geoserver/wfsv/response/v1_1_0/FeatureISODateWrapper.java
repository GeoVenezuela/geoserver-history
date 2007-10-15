/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfsv.response.v1_1_0;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
* A subclass of FeatureWrapper that outputs a ISO8601 compliant date string.
* 
* @author Arne Kepp,  The Open Planning Project
*
*/
public class FeatureISODateWrapper extends org.geoserver.template.FeatureWrapper {
    /**
     * Wrapper to make it possible to subclass for different date formats,
     * or other behaviors.
     * 
     * @param o could be an instance of Date (a special case)
     * @return the formated date as a String, or the object
     */
    protected Object wrapValue(Object o) {
    	if ( o instanceof Date ) { 
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    		return sdf.format((Date) o);
    	} else { 
    		return o; 
    	}
    }
}
