/* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.kvp;

import net.opengis.ows.v1_0_0.AcceptFormatsType;
import net.opengis.ows.v1_0_0.OWSFactory;
import org.geoserver.ows.KvpParser;
import org.geoserver.ows.util.KvpUtils;
import java.util.Iterator;
import java.util.List;


/**
 * Parses a kvp of the form "acceptFormats=format1,format2,...,formatN" into
 * an instance of {@link net.opengis.ows.v1_0_0.AcceptFormatsType}.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class AcceptFormatsKvpParser extends KvpParser {
    public AcceptFormatsKvpParser() {
        super("acceptFormats", AcceptFormatsType.class);
    }

    public Object parse(String value) throws Exception {
        List values = KvpUtils.readFlat(value);

        AcceptFormatsType acceptFormats = OWSFactory.eINSTANCE.createAcceptFormatsType();

        for (Iterator v = values.iterator(); v.hasNext();) {
            acceptFormats.getOutputFormat().add(v.next());
        }

        return acceptFormats;
    }
}
