/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.kvp;

import net.opengis.ows.v1_0_0.AcceptVersionsType;
import net.opengis.ows.v1_0_0.OWSFactory;
import org.geoserver.ows.KvpParser;
import org.geoserver.ows.util.KvpUtils;


/**
 * Parses a kvp of the form "acceptVersions=version1,version2,...,versionN" into
 * an instance of {@link net.opengis.ows.v1_0_0.AcceptVersionsType}.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class AcceptVersionsKvpParser extends KvpParser {
    public AcceptVersionsKvpParser() {
        super("acceptversions", AcceptVersionsType.class);
    }

    public Object parse(String value) throws Exception {
        AcceptVersionsType acceptVersions = OWSFactory.eINSTANCE
            .createAcceptVersionsType();
        acceptVersions.getVersion().addAll(KvpUtils.readFlat(value, ","));

        return acceptVersions;
    }
}
