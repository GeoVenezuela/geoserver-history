/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.map.tiff;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.wms.GetMapProducer;
import org.vfny.geoserver.wms.WmsException;
import org.vfny.geoserver.wms.responses.DefaultRasterMapProducer;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;


/**
 * Map producer for producing Tiff images out of a map.
 *
 * @author Simone Giannecchini, GeoSolutions
 *
 * @since 1.4.x
 */
public final class TiffMapProducer extends DefaultRasterMapProducer {
    /** A logger for this class. */
    private static final Logger LOGGER = Logger.getLogger("org.vfny.geoserver.responses.wms.map");
    private final static ImageWriterSpi writerSPI = new TIFFImageWriterSpi();

    /**
     * Creates a {@link GetMapProducer} to encode the {@link RenderedImage}
     * generated in <code>outputFormat</code> format.
     *
     * @param outputFormat
     *            the output format.
     */
    public TiffMapProducer(String outputFormat, String mime, WMS wms) {
        super(outputFormat, mime, wms);
    }

    /**
     * Transforms the rendered image into the appropriate format, streaming to
     * the output stream.
     *
     * @param format
     *            The name of the format
     * @param image
     *            The image to be formatted.
     * @param outStream
     *            The stream to write to.
     *
     * @throws WmsException
     *             not really.
     * @throws IOException
     *             if the image writing fails.
     */
    public void formatImageOutputStream(RenderedImage image, OutputStream outStream)
        throws WmsException, IOException {
        // getting a writer
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Getting a writer for tiff");
        }

        // get a writer
        final ImageWriter writer = writerSPI.createWriterInstance();

        // getting a stream caching in memory
        final ImageOutputStream ioutstream = ImageIO.createImageOutputStream(outStream);

        // tiff
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Writing tiff image ...");
        }

        // do we want it to be 8 bits?
        if (this.format.equalsIgnoreCase("image/tiff8") || (this.mapContext.getPalette() != null)) {
            image = forceIndexed8Bitmask(image);
        }

        // write it out
        writer.setOutput(ioutstream);
        writer.write(image);
        ioutstream.close();
        writer.dispose();

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Writing tiff image done!");
        }
    }

    public String getContentDisposition() {
        // can be null
        return null;
    }
}
