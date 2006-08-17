/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.CRSUtilities;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.vfny.geoserver.global.CoverageStoreInfo;

/**
 * A collection of utilties for dealing with GeotTools Format.
 * 
 * @author Richard Gould, Refractions Research, Inc.
 * @author cholmesny
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
 *         modification)
 * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
 *         modification)
 * @version $Id: CoverageStoreUtils.java,v 1.12 2004/09/21 21:14:48 cholmesny
 *          Exp $
 */
public final class CoverageStoreUtils {
	private final static Logger LOGGER = Logger
			.getLogger(CoverageStoreUtils.class.toString());
	public final static Format[] formats = GridFormatFinder.getFormatArray();

	private CoverageStoreUtils() {
	}

	public static Format acquireFormat(String type, ServletContext sc)
			throws IOException {
		Format[] formats = GridFormatFinder.getFormatArray();
		Format format = null;
		final int length = formats.length;
		for (int i = 0; i < length; i++) {
			if (formats[i].getName().equals(type)) {
				format = formats[i];
				break;
			}
		}

		if (format == null) {
			throw new IOException("Cannot handle format: " + type);
		} else {
			return format;
		}
	}

	public static Map getParams(Map m, ServletContext sc) {
		String baseDir = sc.getRealPath("/");
		return Collections.synchronizedMap(getParams(m, baseDir));
	}

	/**
	 * Get Connect params.
	 */
	public static Map getParams(Map m, String baseDir) {
		return Collections.synchronizedMap(CoverageStoreInfo.getParams(m, baseDir));
	}

	/**
	 * Utility method for finding Params
	 * 
	 * @param factory
	 *            DOCUMENT ME!
	 * @param key
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static ParameterValue find(Format format, String key) {
		return find(format.getReadParameters(), key);
	}

	/**
	 * Utility methods for find param by key
	 * 
	 * @param params
	 *            DOCUMENT ME!
	 * @param key
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static ParameterValue find(ParameterValueGroup params, String key) {
		List list = params.values();
		Iterator it = list.iterator();
		ParameterDescriptor descr;
		ParameterValue val;
		while (it.hasNext()) {
			val = (ParameterValue) it.next();
			descr = (ParameterDescriptor) val.getDescriptor();
			if (key.equalsIgnoreCase(descr.getName().toString())) {
				return val;
			}
		}

		return null;
	}

	/**
	 * When loading from DTO use the params to locate factory.
	 * 
	 * <p>
	 * bleck
	 * </p>
	 * 
	 * @param params
	 * 
	 * @return
	 */
	public static Format aquireFactory(Map params, String type) {
		final Format[] formats = GridFormatFinder.getFormatArray();
		Format format = null;
		final int length = formats.length;
		for (int i = 0; i < length; i++) {
			format = formats[i];
			if (format.getName().equals(type))
				return format;
		}

		return null;
	}

	/**
	 * After user has selected Description can aquire Format based on
	 * description.
	 * 
	 * @param description
	 * 
	 * @return
	 */
	public static Format aquireFactory(String description) {
		Format[] formats = GridFormatFinder.getFormatArray();
		Format format = null;
		final int length = formats.length;
		for (int i = 0; i < length; i++) {
			format = formats[i];
			if (format.getDescription().equals(description))
				return format;
		}

		return null;
	}

	/**
	 * Returns the descriptions for the available DataFormats.
	 * 
	 * <p>
	 * Arrrg! Put these in the select box.
	 * </p>
	 * 
	 * @return Descriptions for user to choose from
	 */
	public static List listDataFormatsDescriptions() {
		List list = new ArrayList();
		Format[] formats = GridFormatFinder.getFormatArray();
		final int length = formats.length;
		for (int i = 0; i < length; i++) {
			if (!list.contains(formats[i].getDescription())) {
				list.add(formats[i].getDescription());
			}
		}

		return Collections.synchronizedList(list);
	}

	public static List listDataFormats() {
		List list = new ArrayList();
		Format[] formats = GridFormatFinder.getFormatArray();
		final int length = formats.length;
		for (int i = 0; i < length; i++) {
			if (!list.contains(formats[i])) {
				list.add(formats[i]);
			}
		}

		return Collections.synchronizedList(list);
	}

	public static Map defaultParams(String description) {
		return Collections
				.synchronizedMap(defaultParams(aquireFactory(description)));
	}

	public static Map defaultParams(Format factory) {
		Map defaults = new HashMap();
		ParameterValueGroup params = factory.getReadParameters();

		if (params != null) {
			List list = params.values();
			Iterator it = list.iterator();
			ParameterDescriptor descr = null;
			ParameterValue val = null;
			String key;
			Object value;
			while (it.hasNext()) {
				val = (ParameterValue) it.next();
				descr = (ParameterDescriptor) val.getDescriptor();

				key = descr.getName().toString();
				value = null;

				if (val.getValue() != null) {
					// Required params may have nice sample values
					//
					if ("values_palette".equalsIgnoreCase(key))
						value = val.getValue();
					else
						value = val.getValue().toString();
				}
				if (value == null) {
					// or not
					value = "";
				}
				if (value != null) {
					defaults.put(key, value);
				}
			}
		}

		return Collections.synchronizedMap(defaults);
	}

	/**
	 * Convert map to real values based on factory Params.
	 * 
	 * @param factory
	 * @param params
	 * 
	 * @return Map with real values that may be acceptable to GDSFactory
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public static Map toParams(GridFormatFactorySpi factory, Map params)
			throws IOException {
		final Map map = new HashMap(params.size());

		final ParameterValueGroup info = factory.createFormat()
				.getReadParameters();
		String key;
		Object value;

		// Convert Params into the kind of Map we actually need
		for (Iterator i = params.keySet().iterator(); i.hasNext();) {
			key = (String) i.next();
			value = find(info, key).getValue();

			if (value != null) {
				map.put(key, value);
			}
		}

		return Collections.synchronizedMap(map);
	}

	/**
	 * Retrieve a WGS84 lon,lat envelope from the provided one.
	 * 
	 * @param sourceCRS
	 * @param targetEnvelope
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws FactoryException
	 * @throws TransformException
	 */
	public static GeneralEnvelope getWGS84LonLatEnvelope(
			GeneralEnvelope envelope) throws IndexOutOfBoundsException,
			FactoryException, TransformException {

		final CoordinateReferenceSystem sourceCRS = envelope
		.getCoordinateReferenceSystem();
		////
		//
		// Do we need to transform?
		//
		////
		if(CRSUtilities.equalsIgnoreMetadata(sourceCRS,DefaultGeographicCRS.WGS84))
			return new GeneralEnvelope(envelope);
		
		
		////
		//
		//transform
		//
		////
		final CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;
		final MathTransform mathTransform = CRS.transform(sourceCRS, targetCRS);
		final GeneralEnvelope targetEnvelope;
		if (!mathTransform.isIdentity())
			targetEnvelope = CRSUtilities.transform(mathTransform, envelope);
		else
			targetEnvelope = new GeneralEnvelope(envelope);
		targetEnvelope.setCoordinateReferenceSystem(targetCRS);

		return targetEnvelope;
	}

	

//	/**
//	 * Get a generic envelope and retrieve a lon,lat envelope.
//	 * 
//	 * @param sourceCRS
//	 * @param envelope
//	 * @return
//	 * @throws IndexOutOfBoundsException
//	 * @throws MismatchedDimensionException
//	 * @throws NoSuchAuthorityCodeException
//	 */
//	public static GeneralEnvelope adjustEnvelopeLongitudeFirst(
//			final CoordinateReferenceSystem sourceCRS, GeneralEnvelope envelope)
//			throws IndexOutOfBoundsException, MismatchedDimensionException,
//			NoSuchAuthorityCodeException {
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Is Lon first?
//		//
//		// /////////////////////////////////////////////////////////////////////
//		final CoordinateReferenceSystem crs2D;
//		try {
//			crs2D = CRSUtilities.getCRS2D(sourceCRS);
//		} catch (TransformException e) {
//			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
//			return null;
//		}
//		final CoordinateSystem sourceCS = crs2D.getCoordinateSystem();
//		final boolean lonFirst = !GridGeometry2D.swapXY(sourceCS);
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Creating a new envelope lon,lat
//		//
//		// /////////////////////////////////////////////////////////////////////
//		final GeneralEnvelope lonLatEnvelope = lonFirst ? new GeneralEnvelope(
//				envelope) : new GeneralEnvelope(new double[] {
//				envelope.getLowerCorner().getOrdinate(1),
//				envelope.getLowerCorner().getOrdinate(0) }, new double[] {
//				envelope.getUpperCorner().getOrdinate(1),
//				envelope.getUpperCorner().getOrdinate(0) });
//		lonLatEnvelope.setCoordinateReferenceSystem(crs2D);
//		return lonLatEnvelope;
//	}
}