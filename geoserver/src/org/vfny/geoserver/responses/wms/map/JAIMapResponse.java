/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.responses.wms.map;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.DefaultMapContext;
import org.geotools.renderer.Renderer;
import org.geotools.renderer.j2d.StyledMapRenderer;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.vfny.geoserver.WmsException;
import org.vfny.geoserver.global.FeatureTypeInfo;
import org.vfny.geoserver.global.GeoServer;
import org.vfny.geoserver.requests.wms.GetMapRequest;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;


/**
 * Generates a map using the geotools jai rendering classes.  Currently does a
 * fairly poor job of taking advantage of the streaming architecture, but I'm
 * not sure there's a better way to handle it.
 *
 * @author Chris Holmes, TOPP
 * @version $Id: JAIMapResponse.java,v 1.16 2004/04/13 03:16:39 groldan Exp $
 */
public class JAIMapResponse extends GetMapDelegate {
    /** A logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.vfny.geoserver.responses.wms.map");

    /** The formats supported by this map delegate. */
    private static List supportedFormats = null;

    /** The image generated by the execute method. */
    private BufferedImage image;

    /** setted in execute() from the requested output format, it's holded just
     * to be sure that method has been called before getContentType() thus 
     * supporting the workflow contract of the request processing*/
    private String format = null;

    public JAIMapResponse() {
    }

    /**
     * Evaluates if this Map producer can generate the map format specified by
     * <code>mapFormat</code>
     *
     * @param mapFormat the mime type of the output map format requiered
     *
     * @return true if class can produce a map in the passed format
     */
    public boolean canProduce(String mapFormat) {
        return getSupportedFormats().contains(mapFormat);
    }

    /**
     * The formats this delegate supports. Includes those formats supported by
     * the Java ImageIO extension, mostly: <i>png, x-portable-graymap, jpeg,
     * jpeg2000, x-png, tiff, vnd.wap.wbmp, x-portable-pixmap,
     * x-portable-bitmap, bmp and x-portable-anymap</i>, but the specific ones
     * will depend on the platform and JAI version. At leas JPEG and PNG will
     * generally work.
     *
     * @return The list of the supported formats, as returned by the Java
     *         ImageIO extension.
     */
    public List getSupportedFormats() {
        if (supportedFormats == null) {
            StyledMapRenderer renderer = null;

            try {
                renderer = new StyledMapRenderer(null);
            } catch (NoClassDefFoundError ncdfe) {
                supportedFormats = Collections.EMPTY_LIST;

                //this will occur if JAI is not present, so please do not
                //delete, or we get really nasty messages on getCaps for wms.
            } catch (ExceptionInInitializerError eiie) {
                supportedFormats = Collections.EMPTY_LIST;

                //TODO: This is a weird one, it seems to be caused by a null
                //pointer in units?  Uncomment and investigate further, must run
                //a jvm withough JAI installed, and call wms/GetCaps.
                //this will occur if JAI is not present, so please do not
                //delete, or we get really nasty messages on getCaps for wms.
            }

            if (renderer == null) {
                supportedFormats = Collections.EMPTY_LIST;
            } else {
                String[] mimeTypes = ImageIO.getWriterMIMETypes();
                supportedFormats = Arrays.asList(mimeTypes);

                if (LOGGER.isLoggable(Level.CONFIG)) {
                    StringBuffer sb = new StringBuffer(
                            "Supported JAIMapResponse's MIME Types: [");

                    for (Iterator it = supportedFormats.iterator();
                            it.hasNext();) {
                        sb.append(it.next());

                        if (it.hasNext()) {
                            sb.append(", ");
                        }
                    }

                    sb.append("]");
                    LOGGER.config(sb.toString());
                }
            }
        }

        return supportedFormats;
    }

    /**
     * Writes the image to the client.
     *
     * @param out The output stream to write to.
     *
     * @throws org.vfny.geoserver.ServiceException DOCUMENT ME!
     * @throws java.io.IOException DOCUMENT ME!
     */
    public void writeTo(OutputStream out)
        throws org.vfny.geoserver.ServiceException, java.io.IOException {
        formatImageOutputStream(format, image, out);
    }

    /**
     * Transforms the rendered image into the appropriate format, streaming to
     * the output stream.
     *
     * @param format The name of the format
     * @param image The image to be formatted.
     * @param outStream The stream to write to.
     *
     * @throws WmsException
     * @throws IOException DOCUMENT ME!
     */
    public void formatImageOutputStream(String format, BufferedImage image,
        OutputStream outStream) throws WmsException, IOException {
        if (format.equalsIgnoreCase("jpeg")) {
            format = "image/jpeg";
        }

        Iterator it = ImageIO.getImageWritersByMIMEType(format);

        if (!it.hasNext()) {
            throw new WmsException( //WMSException.WMSCODE_INVALIDFORMAT,
                "Format not supported: " + format);
        }

        ImageWriter writer = (ImageWriter) it.next();
        ImageOutputStream ioutstream = null;

        ioutstream = ImageIO.createImageOutputStream(outStream);
        writer.setOutput(ioutstream);
        writer.write(image);
        writer.dispose();
        ioutstream.close();
    }

    /**
     * Halts the loading.  Right now unimplemented.
     *
     * @param gs DOCUMENT ME!
     */
    public void abort(GeoServer gs) {
    }

    /**
     * Gets the content type.  This is set by the request, should only be
     * called after execute.  GetMapResponse should handle this though.
     *
     * @param gs server configuration
     *
     * @return The mime type that this response will generate.
     *
     * @throws java.lang.IllegalStateException if <code>execute()</code> has not
     * been previously called
     */
    public String getContentType(GeoServer gs)
        throws java.lang.IllegalStateException {
        //Return a default?  Format is not set until execute is called...
        return format;
    }

    /**
     * returns the content encoding for the output data (null for this class)
     * @retun <code>null</code> since no special encoding is performed while
     * wrtting to the output stream 
     */
    public String getContentEncoding() {
        return null;
    }

    /**
     * Performs the execute request using geotools rendering.
     *
     * @param requestedLayers The information on the types requested.
     * @param resultLayers The results of the queries to generate maps with.
     * @param styles The styles to be used on the results.
     *
     * @throws WmsException For any problems.
     *
     * @task TODO: Update to feature streaming and latest api, Map is
     *       deprecated.
     */
    protected void execute(FeatureTypeInfo[] requestedLayers,
        Query []queries, Style[] styles)
        throws WmsException {
        GetMapRequest request = getRequest();
        this.format = request.getFormat();

        int width = request.getWidth();
        int height = request.getHeight();

        //GR: just remove this bunch of code when switch to literenderer
        //it is here just to adapt the code to this method's signature change
        int nLayers = requestedLayers.length;
        FeatureResults[] resultLayers = new FeatureResults[nLayers];
        try {
			for(int i = 0; i < nLayers; i++)
			{
				FeatureSource fSource = requestedLayers[i].getFeatureSource();
				resultLayers[i] = fSource.getFeatures(queries[i]);
			}
		} catch (IOException e) {
			throw new WmsException(e, "Executing requests: " +
					e.getMessage(), getClass().getName() + 
					"::execute(FeatureTypeInfo[], Query[], Style[])");
		}
		// /GR
        try {
            LOGGER.fine("setting up map");

            DefaultMapContext map = new DefaultMapContext();
            Style[] layerstyle = null;
            StyleBuilder sb = new StyleBuilder();

            for (int i = 0; i < requestedLayers.length; i++) {
                Style style = styles[i];
                FeatureCollection fc = resultLayers[i].collection();
                map.addLayer(fc, style);
            }

            LOGGER.fine("map setup");

            //Renderer renderer = new LiteRenderer();
            BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Envelope env = request.getBbox();

            //LOGGER.fine("setting up renderer");
            java.awt.Graphics g = image.getGraphics();
            g.setColor(request.getBgColor());

            if (!request.isTransparent()) {
                g.fillRect(0, 0, width, height);
            }

            StyledMapRenderer renderer = new StyledMapRenderer(null);

            synchronized (renderer) {
                renderer.setMapContext(map);
                renderer.paint((Graphics2D) image.getGraphics(),
                    new java.awt.Rectangle(width, height),
                    new AffineTransform(), false);

                LOGGER.fine("called renderer");
            }

            map = null;
            this.image = image;
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new WmsException(null, "Internal error : " + exp.getMessage());
        }
    }
}
