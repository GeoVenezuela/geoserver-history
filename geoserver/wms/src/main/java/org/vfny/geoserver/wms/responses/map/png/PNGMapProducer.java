/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.map.png;

import org.geotools.image.ImageWorker;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.wms.WmsException;
import org.vfny.geoserver.wms.responses.DefaultRasterMapProducer;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;


/**
 * Handles a GetMap request that spects a map in GIF format.
 *
 * @author Simone Giannecchini
 * @author Didier Richard
 * @version $Id
 */
public final class PNGMapProducer extends DefaultRasterMapProducer {
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(PNGMapProducer.class.getPackage().getName());

    /** PNG Native Acceleration Mode */
    private Boolean PNGNativeAcc;

    public PNGMapProducer(String format, WMS wms) {
        super(format, wms);
        this.PNGNativeAcc = wms.getGeoServer().getPNGNativeAcceleration();
    }

    /**
     * Transforms the rendered image into the appropriate format,
     * streaming to the output stream.
     * @param image
     *            The image to be formatted.
     * @param outStream
     *            The stream to write to.
     *
     * @throws WmsException
     *             not really.
     * @throws IOException
     *             if encoding to <code>outStream</code> fails.
     */
    public void formatImageOutputStream(BufferedImage image, OutputStream outStream)
        throws WmsException, IOException {
        // /////////////////////////////////////////////////////////////////
        //
        // Reformatting this image for png
        //
        // /////////////////////////////////////////////////////////////////
        new ImageWorker(image).writePNG(outStream, "FILTERED", 0.5f, PNGNativeAcc.booleanValue(), image.getColorModel() instanceof IndexColorModel);
    }

    public String getContentDisposition() {
        // can be null
        return null;
    }
}
