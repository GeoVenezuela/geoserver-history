/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.requests.readers.wfs;

import org.vfny.geoserver.requests.*;
import org.vfny.geoserver.requests.readers.*;
import org.vfny.geoserver.requests.wfs.*;
import java.util.*;


/**
 * This utility reads in a DescribeFeatureType KVP request and turns it into an
 * appropriate internal DescribeRequest object.
 *
 * @author Rob Hranac, TOPP
 * @author Gabriel Rold�n
 * @version $Id: DescribeKvpReader.java,v 1.1.2.1 2003/11/04 22:46:52 cholmesny Exp $
 */
public class DescribeKvpReader extends KvpRequestReader {
    /**
     * Constructor with raw request string.  Calls parent.
     *
     * @param kvPairs the key/value pairs containing DESCRIBE
     */
    public DescribeKvpReader(Map kvPairs) {
        super(kvPairs);
    }

    /**
     * Returns a list of requested feature types..
     *
     * @return DescribeRequest request object.
     */
    public Request getRequest() {
        DescribeRequest currentRequest = new DescribeRequest();
        currentRequest.setVersion(getValue("VERSION"));
        currentRequest.setRequest(getValue("REQUEST"));
        currentRequest.setOutputFormat(getValue("OUTPUTFORMAT"));
        currentRequest.setFeatureTypes(readFlat(getValue("TYPENAME"),
                INNER_DELIMETER));

        return currentRequest;
    }
}
