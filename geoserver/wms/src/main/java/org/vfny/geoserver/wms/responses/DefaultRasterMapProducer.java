/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.VolatileImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.TiledImage;
import javax.media.jai.operator.BandMergeDescriptor;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.renderer.lite.StreamingRenderer;
import org.vfny.geoserver.config.WMSConfig;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.wms.RasterMapProducer;
import org.vfny.geoserver.wms.WmsException;
import org.vfny.geoserver.wms.responses.palette.CustomPaletteBuilder;
import org.vfny.geoserver.wms.responses.palette.InverseColorMapOp;

/**
 * Abstract base class for GetMapProducers that relies in LiteRenderer for
 * creating the raster map and then outputs it in the format they specializes
 * in.
 * <p>
 * This class does the job of producing a BufferedImage using geotools
 * LiteRenderer, so it should be enough for a subclass to implement
 * {@linkPlain #formatImageOutputStream(String, BufferedImage, OutputStream)}
 * </p>
 * <p>
 * Generates a map using the geotools jai rendering classes. Uses the Lite
 * renderer, loading the data on the fly, which is quite nice. Thanks Andrea and
 * Gabriel. The word is that we should eventually switch over to
 * StyledMapRenderer and do some fancy stuff with caching layers, but I think we
 * are a ways off with its maturity to try that yet. So Lite treats us quite
 * well, as it is stateless and therefor loads up nice and fast.
 * </p>
 * <p>
 * </p>
 * 
 * @author Chris Holmes, TOPP
 * @author Simone Giannecchini, GeoSolutions
 */
public abstract class DefaultRasterMapProducer extends
		AbstractRasterMapProducer implements RasterMapProducer {
	public final static Interpolation NN_INTERPOLATION = new InterpolationNearest();

	public final static Interpolation BIL_INTERPOLATION = new InterpolationBilinear();

	public final static Interpolation BIC_INTERPOLATION = new InterpolationBicubic2(
			0);

	/** A logger for this class. */
	private static final Logger LOGGER = Logger
			.getLogger("org.vfny.geoserver.responses.wms.map");

	/** Which format to encode the image in if one is not supplied */
	private static final String DEFAULT_MAP_FORMAT = "image/png";

	/** WMS Service configuration */
	private WMS wms;

	/**
	 * 
	 */
	public DefaultRasterMapProducer() {
		this(DEFAULT_MAP_FORMAT, null);
	}

	/**
	 * 
	 */
	public DefaultRasterMapProducer(WMS wms) {
		this(DEFAULT_MAP_FORMAT, wms);
	}

	/**
	 * 
	 */
	public DefaultRasterMapProducer(String outputFormat, WMS wms) {
		this(outputFormat, outputFormat, wms);
	}

	/**
	 * 
	 */
	public DefaultRasterMapProducer(String outputFormat, String mime, WMS wms) {
		super(outputFormat, mime);
		this.wms = wms;
	}

	/**
	 * Writes the image to the client.
	 * 
	 * @param out
	 *            The output stream to write to.
	 */
	public void writeTo(OutputStream out)
			throws org.vfny.geoserver.ServiceException, java.io.IOException {
		formatImageOutputStream(this.image, out);
	}

	/**
	 * Performs the execute request using geotools rendering.
	 * 
	 * @param map
	 *            The information on the types requested.
	 * 
	 * @throws WmsException
	 *             For any problems.
	 */
	public void produceMap() throws WmsException {
		final int width = mapContext.getMapWidth();
		final int height = mapContext.getMapHeight();

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine(new StringBuffer("setting up ").append(width).append(
					"x").append(height).append(" image").toString());
		}

		final InverseColorMapOp paletteInverter = mapContext
				.getPaletteInverter();
		final RenderedImage preparedImage = prepareImage(width, height,
				paletteInverter != null ? paletteInverter.getIcm() : null);
		final Graphics2D graphic;

		if (preparedImage instanceof BufferedImage) {
			graphic = ((BufferedImage) preparedImage).createGraphics();
		} else if (preparedImage instanceof TiledImage) {
			graphic = ((TiledImage) preparedImage).createGraphics();
		} else if (preparedImage instanceof VolatileImage) {
			graphic = ((VolatileImage) preparedImage).createGraphics();
		} else {
			throw new WmsException("Unrecognized back-end image type");
		}

		Map hintsMap = new HashMap();

		// if (preparedImage.getColorModel() instanceof IndexColorModel) {
		// hintsMap.put(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_OFF);
		// // This can be added, but it increases the image size significantly
		// // for png's... hum...
		// // we should really expose these settings to the users
		// // hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING,
		// // RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// hintsMap.put(RenderingHints.KEY_DITHERING,
		// RenderingHints.VALUE_DITHER_DISABLE);
		// } else {
		hintsMap.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// }

		// turn off/on interpolation rendering hint
		if ((wms != null)
				&& WMSConfig.INT_NEAREST.equals(wms.getAllowInterpolation())) {
			hintsMap.put(JAI.KEY_INTERPOLATION, NN_INTERPOLATION);
		} else if ((wms != null)
				&& WMSConfig.INT_BIlINEAR.equals(wms.getAllowInterpolation())) {
			hintsMap.put(JAI.KEY_INTERPOLATION, BIL_INTERPOLATION);
		} else if ((wms != null)
				&& WMSConfig.INT_BICUBIC.equals(wms.getAllowInterpolation())) {
			hintsMap.put(JAI.KEY_INTERPOLATION, BIC_INTERPOLATION);
		}

		// make sure the hints are set before we start rendering the map
		graphic.setRenderingHints(hintsMap);

		if (!mapContext.isTransparent()) {
			graphic.setColor(mapContext.getBgColor());
			graphic.fillRect(0, 0, width, height);
		} else {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("setting to transparent");
			}

			int type = AlphaComposite.SRC;
			graphic.setComposite(AlphaComposite.getInstance(type));

			Color c = new Color(mapContext.getBgColor().getRed(), mapContext
					.getBgColor().getGreen(),
					mapContext.getBgColor().getBlue(), 0);
			graphic.setBackground(mapContext.getBgColor());
			graphic.setColor(c);
			graphic.fillRect(0, 0, width, height);
			type = AlphaComposite.SRC_OVER;
			graphic.setComposite(AlphaComposite.getInstance(type));
		}

		Rectangle paintArea = new Rectangle(width, height);
		RenderingHints hints = new RenderingHints(hintsMap);
		renderer = new StreamingRenderer();
		renderer.setContext(mapContext);
		renderer.setJava2DHints(hints);

		// we already do everything that the optimized data loading does...
		// if we set it to true then it does it all twice...
		Map rendererParams = new HashMap();
		rendererParams.put("optimizedDataLoadingEnabled", new Boolean(true));
		rendererParams.put("renderingBuffer", new Integer(mapContext
				.getBuffer()));
		renderer.setRendererHints(rendererParams);

		final ReferencedEnvelope dataArea = mapContext.getAreaOfInterest();

		if (this.abortRequested) {
			graphic.dispose();

			return;
		}

		renderer.paint(graphic, paintArea, dataArea);
		// uncomment this to have a black border drawn around the "tile"
		// graphic.setColor(Color.BLACK);
		// graphic.drawRect(0, 0, (int) paintArea.getWidth(), (int) paintArea.getHeight());
		graphic.dispose();

		if (!this.abortRequested) {
			this.image = preparedImage;
		}
	}

	/**
	 * Sets up a {@link BufferedImage#TYPE_4BYTE_ABGR} if the paletteInverter is
	 * not provided, or a indexed image otherwise. Subclasses may override this
	 * method should they need a special kind of image
	 * 
	 * @param width
	 * @param height
	 * @param paletteInverter
	 * @return
	 */
	protected RenderedImage prepareImage(int width, int height,
			IndexColorModel palette) {
		// if (paletteInverter != null) {
		// WritableRaster raster =
		// Raster.createInterleavedRaster(paletteInverter.getTransferType(),
		// width, height, 1, null);
		//
		// return new BufferedImage(paletteInverter, raster, false, null);
		// }

		return new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
	}

	/**
	 * @param originalImage
	 * @return
	 */
	protected RenderedImage forceIndexed8Bitmask(RenderedImage originalImage) {
		// /////////////////////////////////////////////////////////////////
		//
		// Check what we need to do depending on the color model of the image we
		// are working on.
		//
		// /////////////////////////////////////////////////////////////////
		final ColorModel cm = originalImage.getColorModel();
		final boolean dataTypeByte = originalImage.getSampleModel()
				.getDataType() == DataBuffer.TYPE_BYTE;
		RenderedImage image;

		// /////////////////////////////////////////////////////////////////
		//
		// IndexColorModel and DataBuffer.TYPE_BYTE
		//
		// ///
		//
		// If we got an image whose color model is already indexed on 8 bits
		// paletteInverter,we have to check if it is bitmask or not.
		//
		// /////////////////////////////////////////////////////////////////
		if ((cm instanceof IndexColorModel) && dataTypeByte) {
			final IndexColorModel icm = (IndexColorModel) cm;

			if (icm.getTransparency() != Transparency.TRANSLUCENT) {
				// //
				//
				// The image is indexed on 8 bits and the color model is either
				// opaque or bitmask. WE do not have to do anything.
				//
				// //
				image = originalImage;
			} else {
				// //
				//
				// The image is indexed on 8 bits and the color model is
				// Translucent, we have to perform some color operations in
				// order to convert it to bitmask.
				//
				// //
				image = new ImageWorker(originalImage)
						.forceBitmaskIndexColorModel().getRenderedImage();
			}
		} else {
			// /////////////////////////////////////////////////////////////////
			//
			// NOT IndexColorModel and DataBuffer.TYPE_BYTE
			//
			// ///
			//
			// We got an image that needs to be converted.
			//
			// /////////////////////////////////////////////////////////////////
			final InverseColorMapOp invColorMap = this.getMapContext()
					.getPaletteInverter();
			if (invColorMap != null) {


				// make me parametric which means make me work with other image
				// types
				image = invColorMap.filterRenderedImage(originalImage);
			} else {
				// //
				//
				// We do not have a paletteInverter, let's create one that is as
				// good as
				// possible.
				//
				// //
				// make sure we start from a componentcolormodel.
				image = new ImageWorker(originalImage)
						.forceComponentColorModel().getRenderedImage();

				if (originalImage.getColorModel().hasAlpha()) {
					// //
					//
					// We want to use the CustomPaletteBuilder but to do so we
					// have first to reduce the image to either opaque or
					// bitmask because otherwise the CustomPaletteBuilder will
					// fail to address transparency.
					//
					// //
					// I am exploiting the clamping property of the JAI
					// MultiplyCOnst operation.
					// TODO make this code parametric since people might want to
					// use a different transparency threshold. Right now we are
					// thresholding the transparency band using a fixed
					// threshold of 255, which means that anything that was not
					// transparent will become opaque.
					final RenderedImage alpha = new ImageWorker(originalImage)
							.retainLastBand().multiplyConst(
									new double[] { 255.0 }).retainFirstBand()
							.getRenderedImage();

					final int numBands = originalImage.getSampleModel()
							.getNumBands();
					originalImage = new ImageWorker(originalImage).retainBands(
							numBands - 1).getRenderedImage();

					final ImageLayout layout = new ImageLayout();

					if (numBands == 4) {
						layout.setColorModel(new ComponentColorModel(ColorSpace
								.getInstance(ColorSpace.CS_sRGB), true, false,
								Transparency.BITMASK, DataBuffer.TYPE_BYTE));
					} else {
						layout.setColorModel(new ComponentColorModel(ColorSpace
								.getInstance(ColorSpace.CS_GRAY), true, false,
								Transparency.BITMASK, DataBuffer.TYPE_BYTE));
					}

					image = BandMergeDescriptor.create(originalImage, alpha,
							new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout))
							.getNewRendering();
				} else {
					// //
					//
					// Everything is fine
					//
					// //
					image = originalImage;
				}

				// //
				//
				// Build the paletteInverter doing some good subsampling.
				//
				// //
				final int subsx = (int) Math.pow(2, image.getWidth() / 256);
				final int subsy = (int) Math.pow(2, image.getHeight() / 256);
				image = new CustomPaletteBuilder(image, 255, subsx, subsy)
						.buildPalette().getIndexedImage();
			}
		}

		return image;
	}
}
