/* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v1_0_0;

import java.io.InputStream;

import javax.xml.namespace.QName;

import org.geoserver.ows.XmlRequestReader;
import org.geotools.util.Version;
import org.geotools.xml.Parser;


public class WfsXmlReader extends XmlRequestReader {
    /**
     * Xml Configuration
     */
    WFSConfiguration configuration;
    
    public WfsXmlReader(String element, WFSConfiguration configuration) {
        super(new QName(WFS.NAMESPACE, element), new Version("1.0.0"));
    }

    public Object read(InputStream input) throws Exception {
        Parser parser = new Parser(configuration);
        return parser.parse(input);
    }
}
