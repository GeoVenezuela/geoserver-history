/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.map.georss;

import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.wms.GetMapProducer;
import org.vfny.geoserver.wms.WMSMapContext;
import org.vfny.geoserver.wms.WmsException;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.TransformerException;


public class RSSGeoRSSMapProducer implements GetMapProducer {
    /** format name */
    public static String FORMAT = "rss";

    /** mime type */
    public static String MIME_TYPE = "application/rss+xml";

    /**
     * current map context
     */
    WMSMapContext map;

    public String getContentType() throws IllegalStateException {
        //return MIME_TYPE;
        return "application/xml";
    }

    public void produceMap() throws WmsException {
		
	}
    
    public void writeTo(OutputStream out) throws ServiceException, IOException {
        RSSGeoRSSTransformer tx = new RSSGeoRSSTransformer();

        try {
            tx.transform(map, out);
        } catch (TransformerException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    public void abort() {
    }

    public String getContentDisposition() {
        return "inline; filename=geoserver.xml";
    }

	public WMSMapContext getMapContext() {
		return map;
	}
	
	public void setMapContext(WMSMapContext mapContext) {
		this.map = mapContext;
	}

	public String getOutputFormat() {
		return FORMAT;
	}
	
	public void setOutputFormat(String format) {
		throw new UnsupportedOperationException();
	}
    
	public void setContentType(String mime) {
		throw new UnsupportedOperationException();
	}
}
