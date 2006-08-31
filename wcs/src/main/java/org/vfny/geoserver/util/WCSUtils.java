package org.vfny.geoserver.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.media.jai.Interpolation;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.operation.Crop;
import org.geotools.coverage.processing.operation.Interpolate;
import org.geotools.coverage.processing.operation.Resample;
import org.geotools.coverage.processing.operation.SelectSampleDimension;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.resources.CRSUtilities;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridRange;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.vfny.geoserver.global.CoverageInfo;
import org.vfny.geoserver.wcs.WcsException;
import org.vfny.geoserver.wcs.requests.CoverageRequest;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 *
 */
public class WCSUtils {
	private final static Hints LENIENT_HINT = new Hints(
			Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
	
	private static final Logger LOGGER = Logger
	.getLogger("org.vfny.geoserver.util");
	
	private final static SelectSampleDimension bandSelectFactory = new SelectSampleDimension();
	
	private final static Crop cropFactory = new Crop();
	
	private final static Interpolate interpolateFactory = new Interpolate();
	
	private final static Resample resampleFactory = new Resample();
	
	static {
		
		// ///////////////////////////////////////////////////////////////////
		//
		//
		// ///////////////////////////////////////////////////////////////////
		final DefaultProcessor processor = new DefaultProcessor(LENIENT_HINT);
		bandSelectParams 	= processor.getOperation("SelectSampleDimension").getParameters();
		cropParams 			= processor.getOperation("CoverageCrop").getParameters();
		interpolateParams 	= processor.getOperation("Interpolate").getParameters();
		resampleParams 		= processor.getOperation("Resample").getParameters();
	}
	
	private final static ParameterValueGroup bandSelectParams;
	
	private final static ParameterValueGroup cropParams;
	
	private final static ParameterValueGroup interpolateParams;
	
	private final static ParameterValueGroup resampleParams;
	
	private final static Hints hints = new Hints(new HashMap(5));
	
	/**
	 * GetCroppedCoverage
	 * 
	 * @param request CoverageRequest
	 * @param meta CoverageInfo
	 * @param coverage GridCoverage
	 * @return GridCoverage2D
	 * @throws WcsException
	 * @throws IOException
	 * @throws IndexOutOfBoundsException
	 * @throws FactoryException
	 * @throws TransformException
	 */
	public static GridCoverage2D getCroppedCoverage(CoverageRequest request,
			CoverageInfo meta, GridCoverage coverage) throws WcsException,
			IOException, IndexOutOfBoundsException, FactoryException,
			TransformException {
		
		// ///////////////////////////////////////////////////////////////////
		//
		// HINTS
		//
		// ///////////////////////////////////////////////////////////////////
		hints.add(LENIENT_HINT);
		/*if (java2dHints != null)
		 this.hints.add(java2dHints);*/
		
		// This is the final Response CRS
		final String responseCRS = request.getResponseCRS();
		// - first check if the responseCRS is present on the Coverage
		// ResponseCRSs list
		if (!meta.getResponseCRSs().contains(responseCRS)) {
			throw new WcsException(
			"This Coverage does not support the Response CRS requested.");
		}
		// - then create the Coordinate Reference System
		final CoordinateReferenceSystem targetCRS = CRS.decode(responseCRS,
				true);
		// This is the CRS of the requested Envelope
		final String requestCRS = request.getCRS();
		// - first check if the requestCRS is present on the Coverage
		// RequestCRSs list
		if (!meta.getResponseCRSs().contains(requestCRS)) {
			throw new WcsException(
			"This Coverage does not support the CRS requested.");
		}
		// - then create the Coordinate Reference System
		final CoordinateReferenceSystem sourceCRS = CRS.decode(requestCRS);
		// This is the CRS of the Coverage Envelope
		final CoordinateReferenceSystem cvCRS = ((GeneralEnvelope) coverage
				.getEnvelope()).getCoordinateReferenceSystem();
		final MathTransform GCCRSTodeviceCRSTransformdeviceCRSToGCCRSTransform = CRS
		.transform(cvCRS, sourceCRS, true);
		final MathTransform deviceCRSToGCCRSTransform = GCCRSTodeviceCRSTransformdeviceCRSToGCCRSTransform
		.inverse();
		
		com.vividsolutions.jts.geom.Envelope envelope = request.getEnvelope();
		GeneralEnvelope destinationEnvelope;
		final boolean lonFirst = sourceCRS.getCoordinateSystem().getAxis(0)
		.getDirection().absolute().equals(AxisDirection.EAST);
		// the envelope we are provided with is lon,lat always
		if (!lonFirst)
			destinationEnvelope = new GeneralEnvelope(new double[] {
					envelope.getMinY(), envelope.getMinX() }, new double[] {
					envelope.getMaxY(), envelope.getMaxX() });
		else
			destinationEnvelope = new GeneralEnvelope(new double[] {
					envelope.getMinX(), envelope.getMinY() }, new double[] {
					envelope.getMaxX(), envelope.getMaxY() });
		destinationEnvelope.setCoordinateReferenceSystem(sourceCRS);
		
		// this is the destination envelope in the coverage crs
		final GeneralEnvelope destinationEnvelopeInSourceCRS = (!deviceCRSToGCCRSTransform
				.isIdentity()) ? CRSUtilities.transform(
						deviceCRSToGCCRSTransform, destinationEnvelope)
						: new GeneralEnvelope(destinationEnvelope);
						destinationEnvelopeInSourceCRS.setCoordinateReferenceSystem(cvCRS);
						
						/**
						 * Band Select
						 */
						Coverage bandSelectedCoverage = bandSelect(request.getParameters().keySet(), coverage);
						
						/**
						 * Crop
						 */
						final GridCoverage2D croppedGridCoverage = crop(
								bandSelectedCoverage,
								(GeneralEnvelope) coverage.getEnvelope(), 
								cvCRS, 
								destinationEnvelopeInSourceCRS);
						
						/**
						 * Scale
						 */
						GridCoverage2D subCoverage = croppedGridCoverage;
						if (request.getGridLow() != null && request.getGridHigh() != null) {
							final int[] lowers = new int[] {
									request.getGridLow()[0].intValue(),
									request.getGridLow()[1].intValue() };
							final int[] highers = new int[] {
									request.getGridHigh()[0].intValue(),
									request.getGridHigh()[1].intValue() };
							// new grid range
							final GeneralGridRange newGridrange = new GeneralGridRange(lowers,
									highers);
							
							subCoverage = scale(croppedGridCoverage, newGridrange, coverage, cvCRS);
						}
						
						/**
						 * Reproject
						 */
						subCoverage = reproject(
								subCoverage,
								sourceCRS, 
								targetCRS);
						
						/**
						 * Interpolate (if necessary)
						 */
						final String interp_requested = request.getInterpolation();
						if (interp_requested != null) {
							int interp_type = -1;
							
							if (interp_requested.equalsIgnoreCase("nearest_neighbor"))
								interp_type = Interpolation.INTERP_NEAREST;
							else if (interp_requested.equalsIgnoreCase("bilinear"))
								interp_type = Interpolation.INTERP_BILINEAR;
							else if (interp_requested.equalsIgnoreCase("bicubic"))
								interp_type = Interpolation.INTERP_BICUBIC;
							else if (interp_requested.equalsIgnoreCase("bicubic_2"))
								interp_type = Interpolation.INTERP_BICUBIC_2;
							else
								throw new WcsException(
								"Unrecognized interpolation type. Allowed values are: nearest_neighbor, bilinear, bicubic, bicubic_2");
							
							subCoverage = interpolate(subCoverage, Interpolation.getInstance(interp_type));
						}
						
						return subCoverage;
	}
	
	/**
	 * REPROJECT
	 * 
	 * @param coverage GridCoverage2D
	 * @param sourceCRS CoordinateReferenceSystem
	 * @param targetCRS CoordinateReferenceSystem
	 * @return GridCoverage2D
	 * @throws WcsException
	 */
	public static GridCoverage2D reproject(
			GridCoverage2D coverage,
			final CoordinateReferenceSystem sourceCRS,
			final CoordinateReferenceSystem targetCRS
	) throws WcsException {
		/////////////////////////////////////////////////////////////////////
		//
		//REPROJECT
		//
		//
		/////////////////////////////////////////////////////////////////////
		if (!sourceCRS.equals(targetCRS)) {
			/*Operations.DEFAULT.resample(
			 coverage, 
			 targetCRS, 
			 null, 
			 Interpolation.getInstance(Interpolation.INTERP_NEAREST))*/
			final ParameterValueGroup param = (ParameterValueGroup) resampleParams.clone();
			param.parameter("Source").setValue(coverage);
			param.parameter("CoordinateReferenceSystem").setValue(targetCRS);
			param.parameter("GridGeometry").setValue(null);
			param.parameter("InterpolationType").setValue(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
			
			coverage = (GridCoverage2D) resampleFactory.doOperation(param, hints);
		}
		
		return coverage;
	}
	
	/**
	 * INTERPOLATE
	 * 
	 * @param coverage GridCoverage2D
	 * @param interpolation Interpolation
	 * @return GridCoverage2D
	 * @throws WcsException
	 */
	public static GridCoverage2D interpolate(
			GridCoverage2D coverage,			
			final Interpolation interpolation
	) throws WcsException {
		/////////////////////////////////////////////////////////////////////
		//
		//INTERPOLATE
		//
		//
		/////////////////////////////////////////////////////////////////////
		if (interpolation != null) {
			/*Operations.DEFAULT.interpolate(coverage, interpolation)*/
			final ParameterValueGroup param = (ParameterValueGroup) interpolateParams.clone();
			param.parameter("Source").setValue(coverage);
			param.parameter("Type").setValue(interpolation);
			
			coverage = (GridCoverage2D) interpolateFactory.doOperation(param, hints);
		}
		
		return coverage;
	}
	
	/**
	 * SCALE to the needed resolution
	 * Let me now scale down to the EXACT needed resolution. This step does
	 * not prevent from having loaded an overview of the original image
	 * based on the requested scale.
	 * 
	 * @param coverage GridCoverage2D
	 * @param newGridRange GridRange
	 * @param sourceCoverage GridCoverage
	 * @param sourceCRS CoordinateReferenceSystem
	 * @return GridCoverage2D
	 */
	public static GridCoverage2D scale(
			final GridCoverage2D coverage,
			final GridRange newGridRange,
			final GridCoverage sourceCoverage, 
			final CoordinateReferenceSystem sourceCRS) {
		// ///////////////////////////////////////////////////////////////////
		//
		// SCALE to the needed resolution
		// Let me now scale down to the EXACT needed resolution. This step does
		// not prevent from having loaded an overview of the original image
		// based on the requested scale.
		//
		// ///////////////////////////////////////////////////////////////////
		GridGeometry2D scaledGridGeometry = new GridGeometry2D(newGridRange, sourceCoverage.getEnvelope());
		
		/*Operations.DEFAULT.resample(
		 coverage, 
		 sourceCRS, 
		 scaledGridGeometry,
		 Interpolation.getInstance(Interpolation.INTERP_NEAREST));*/
		final ParameterValueGroup param = (ParameterValueGroup) resampleParams.clone();
		param.parameter("Source").setValue(coverage);
		param.parameter("CoordinateReferenceSystem").setValue(sourceCRS);
		param.parameter("GridGeometry").setValue(scaledGridGeometry);
		param.parameter("InterpolationType").setValue(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
		
		final GridCoverage2D scaledGridCoverage = (GridCoverage2D) resampleFactory.doOperation(param, hints);
		
		return scaledGridCoverage;
	}
	
	/**
	 * CROP
	 * 
	 * @param coverage Coverage
	 * @param sourceEnvelope GeneralEnvelope
	 * @param sourceCRS CoordinateReferenceSystem
	 * @param destinationEnvelopeInSourceCRS GeneralEnvelope
	 * @return GridCoverage2D
	 * @throws WcsException
	 */
	public static GridCoverage2D crop(
			final Coverage coverage,
			final GeneralEnvelope sourceEnvelope, 
			final CoordinateReferenceSystem sourceCRS, 
			final GeneralEnvelope destinationEnvelopeInSourceCRS) 
	throws WcsException {
		// ///////////////////////////////////////////////////////////////////
		//
		// CROP
		//
		//
		// ///////////////////////////////////////////////////////////////////
		final GridCoverage2D croppedGridCoverage;
		// intersect the envelopes
		final GeneralEnvelope intersectionEnvelope = new GeneralEnvelope(
				destinationEnvelopeInSourceCRS);
		intersectionEnvelope.setCoordinateReferenceSystem(sourceCRS);
		intersectionEnvelope.intersect((GeneralEnvelope) sourceEnvelope);
		// dow we have something to show?
		if (intersectionEnvelope.isEmpty())
			throw new WcsException(
			"The Intersection is null. Check the requested BBOX!");
		if (!intersectionEnvelope.equals((GeneralEnvelope) sourceEnvelope)) {
			// get the cropped grid geometry
			// final GridGeometry2D cropGridGeometry = getCroppedGridGeometry(
			// intersectionEnvelope, gridCoverage);
			
			/*Operations.DEFAULT.crop(coverage, intersectionEnvelope)*/
			final ParameterValueGroup param = (ParameterValueGroup) cropParams.clone();
			param.parameter("Source").setValue(coverage);
			param.parameter("Envelope").setValue(intersectionEnvelope);
			
			croppedGridCoverage = (GridCoverage2D) cropFactory.doOperation(param, hints);
		} else
			croppedGridCoverage = (GridCoverage2D) coverage;
		
		// prefetch to be faster afterwards.
		// This step is important since at this stage we might be loading tiles
		// from disk
		croppedGridCoverage.prefetch(intersectionEnvelope.toRectangle2D());
		return croppedGridCoverage;
	}
	
	/**
	 * BAND SELECT
	 * 
	 * @param params Set
	 * @param coverage GridCoverage
	 * @return Coverage
	 */
	public static Coverage bandSelect(final Set params, final GridCoverage coverage) {
		// ///////////////////////////////////////////////////////////////////
		//
		// BAND SELECT
		//
		//
		// ///////////////////////////////////////////////////////////////////
		final int numDimensions = coverage.getNumSampleDimensions();
		final Map dims = new HashMap();
		final ArrayList selectedBands = new ArrayList();
		
		for (int d = 0; d < numDimensions; d++) {
			dims.put(coverage.getSampleDimension(d).getDescription().toString(
					Locale.getDefault()).toUpperCase(), new Integer(d));
		}
		
		if (!params.isEmpty()) {
			for (Iterator p = params.iterator(); p.hasNext();) {
				final String param = (String) p.next();
				if (dims.containsKey(param)) {
					selectedBands.add(dims.get(param));
				}
			}
		}
		
		final int length = selectedBands.size();
		final int[] bands = new int[length];
		for (int b = 0; b < length; b++) {
			bands[b] = ((Integer) selectedBands.get(b)).intValue();
		}
		
		Coverage bandSelectedCoverage;
		if (bands != null && bands.length > 0) {
			/*Operations.DEFAULT.selectSampleDimension(coverage, bands)*/
			final ParameterValueGroup param = (ParameterValueGroup) bandSelectParams.clone();
			param.parameter("Source").setValue(coverage);
			param.parameter("SampleDimensions").setValue(bands);
			
			bandSelectedCoverage = bandSelectFactory.doOperation(param, hints); 
		} else
			bandSelectedCoverage = coverage;
		return bandSelectedCoverage;
	}
}