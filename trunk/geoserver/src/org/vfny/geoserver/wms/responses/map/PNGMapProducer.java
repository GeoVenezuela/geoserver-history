/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.map;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.vfny.geoserver.wms.WmsException;
import org.vfny.geoserver.wms.responses.DefaultRasterMapProducer;
import org.vfny.geoserver.wms.responses.map.png.PngEncoder;
import org.vfny.geoserver.wms.responses.map.png.PngEncoderB;


/**
 * Handles a GetMap request that spects a map in GIF format.
 *
 * @author Didier Richard
 * @version $Id
 */
public class PNGMapProducer extends DefaultRasterMapProducer {
	
	public PNGMapProducer(String format)
	{
		super(format);
	}
	
    /**
     * Transforms the rendered image into the appropriate format, streaming to
     * the output stream.
     *
     * @param format The name of the format
     * @param image The image to be formatted.
     * @param outStream The stream to write to.
     *
     * @throws WmsException not really.
     * @throws IOException if encoding to <code>outStream</code> fails.
     */
    public void formatImageOutputStream(String format, BufferedImage image,
        OutputStream outStream) throws WmsException, IOException 
	{        
        PngEncoderB png =  new PngEncoderB( image, PngEncoder.ENCODE_ALPHA,	0, 1 ); // filter (0), and compression (1)
        byte[] pngbytes = png.pngEncode();	
        outStream.write( pngbytes );		 
        outStream.flush();
    }
}
