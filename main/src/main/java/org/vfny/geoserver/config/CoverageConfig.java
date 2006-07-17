/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.config;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.units.Unit;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.InternationalString;
import org.vfny.geoserver.global.ConfigurationException;
import org.vfny.geoserver.global.CoverageDimension;
import org.vfny.geoserver.global.MetaDataLink;
import org.vfny.geoserver.global.dto.CoverageInfoDTO;
import org.vfny.geoserver.util.CoverageStoreUtils;

/**
 * User interface Coverage staging area.
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
 *         modification)
 * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
 *         modification)
 * @version $Id: FeatureTypeConfig.java,v 1.20 2004/03/09 10:59:56 jive Exp $
 */
public class CoverageConfig {

	/**
	 * 
	 * @uml.property name="formatId" multiplicity="(0 1)"
	 */
	private String formatId;

	/**
	 * 
	 * @uml.property name="name" multiplicity="(0 1)"
	 */
	private String name;

	/**
	 * 
	 */
	private String wmsPath;

	/**
	 * 
	 * @uml.property name="label" multiplicity="(0 1)"
	 */
	private String label;

	/**
	 * 
	 * @uml.property name="description" multiplicity="(0 1)"
	 */
	private String description;

	/**
	 * 
	 * @uml.property name="metadataLink"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private MetaDataLink metadataLink;

	/**
	 * 
	 * @uml.property name="dirName" multiplicity="(0 1)"
	 */
	private String dirName;

	/**
	 * 
	 * @uml.property name="keywords"
	 * @uml.associationEnd elementType="java.lang.String" multiplicity="(0 -1)"
	 */
	private List keywords;

	/**
	 * 
	 * @uml.property name="envelope"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private GeneralEnvelope envelope;

	/**
	 * 
	 * @uml.property name="lonLatWGS84Envelope"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private GeneralEnvelope lonLatWGS84Envelope;

	/**
	 * 
	 * @uml.property name="grid"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private GridGeometry grid;

	/**
	 * 
	 * @uml.property name="dimensions"
	 * @uml.associationEnd multiplicity="(0 -1)"
	 */
	private CoverageDimension[] dimensions;

	/**
	 * 
	 * @uml.property name="dimentionNames"
	 * @uml.associationEnd multiplicity="(0 -1)"
	 */
	private InternationalString[] dimentionNames;

	/**
	 * 
	 * @uml.property name="requestCRSs"
	 * @uml.associationEnd elementType="java.lang.String" multiplicity="(0 -1)"
	 */
	private List requestCRSs;

	/**
	 * 
	 * @uml.property name="responseCRSs"
	 * @uml.associationEnd elementType="java.lang.String" multiplicity="(0 -1)"
	 */
	private List responseCRSs;

	/**
	 * 
	 * @uml.property name="nativeFormat" multiplicity="(0 1)"
	 */
	private String nativeFormat;

	/**
	 * 
	 * @uml.property name="supportedFormats"
	 * @uml.associationEnd elementType="java.lang.String" multiplicity="(0 -1)"
	 */
	private List supportedFormats;

	/**
	 * 
	 * @uml.property name="defaultInterpolationMethod" multiplicity="(0 1)"
	 */
	private String defaultInterpolationMethod;

	/**
	 * 
	 * @uml.property name="interpolationMethods"
	 * @uml.associationEnd elementType="java.lang.String" multiplicity="(0 -1)"
	 */
	private List interpolationMethods;

	/**
	 * 
	 * @uml.property name="srsName" multiplicity="(0 1)"
	 */
	private String srsName;

	/**
	 * 
	 */
	private String srsWKT;

	/**
	 * 
	 * @uml.property name="crs"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private CoordinateReferenceSystem crs;

	/**
	 * The default style name.
	 * 
	 * @uml.property name="defaultStyle" multiplicity="(0 1)"
	 */
	private String defaultStyle;

	/**
	 * String representation of connection parameter keys
	 */
	private List paramKeys;

	/**
	 * String representation of connection parameter values
	 */
	private List paramValues;

	/**
	 * Help text for Params if available
	 */
	private ArrayList paramHelp;

	/**
	 * Package visible constructor for test cases
	 */
	CoverageConfig() {
	}

	/**
	 * Creating a coverage config from gridcoverages information
	 * 
	 * @param formatId
	 * @param format
	 * @param gc
	 * @throws ConfigurationException
	 */
	public CoverageConfig(String formatId, Format format, GridCoverage2D gc,
			HttpServletRequest request) throws ConfigurationException {
		if ((formatId == null) || (formatId.length() == 0)) {
			throw new IllegalArgumentException(
					"formatId is required for CoverageConfig");
		}
		if (format == null) {
			throw new ConfigurationException(new StringBuffer(
					"Cannot handle format: ").append(formatId).toString());
		}
		this.formatId = formatId;
		crs = gc.getCoordinateReferenceSystem();
		srsName = (crs != null && !crs.getIdentifiers().isEmpty() ? crs
				.getIdentifiers().toArray()[0].toString() : crs.getName()
				.toString());
		srsWKT = (crs != null ? crs.toWKT() : "UNKNOWN");
		envelope = (GeneralEnvelope) gc.getEnvelope();
		try {
			lonLatWGS84Envelope = CoverageStoreUtils
					.getWGS84LonLatEnvelope(envelope);
		} catch (IndexOutOfBoundsException e) {
			final ConfigurationException newEx = new ConfigurationException(
					new StringBuffer("Converting Envelope to Lat-Lon WGS84: ")
							.append(e.toString()).toString());
			newEx.initCause(e);
			throw newEx;
		} catch (FactoryException e) {
			final ConfigurationException newEx = new ConfigurationException(
					new StringBuffer("Converting Envelope to Lat-Lon WGS84: ")
							.append(e.toString()).toString());
			newEx.initCause(e);
			throw newEx;
		} catch (TransformException e) {
			final ConfigurationException newEx = new ConfigurationException(
					new StringBuffer("Converting Envelope to Lat-Lon WGS84: ")
							.append(e.toString()).toString());
			newEx.initCause(e);
			throw newEx;
		}

		grid = gc.getGridGeometry();
		try {
			dimensions = parseCoverageDimesions(gc.getSampleDimensions());
		} catch (UnsupportedEncodingException e) {
			final ConfigurationException newEx = new ConfigurationException(
					new StringBuffer("Coverage dimensions: ").append(
							e.toString()).toString());
			newEx.initCause(e);
			throw newEx;
		}
		dimentionNames = gc.getDimensionNames();

		final DataConfig config = ConfigRequests.getDataConfig(request);
		StringBuffer cvName = new StringBuffer(gc.getName().toString());
		int count = 0;
		StringBuffer key;
		Map coverages;
		Set cvKeySet;
		boolean key_exists;
		String cvKey;
		Iterator it;
		while (true) {
			key = new StringBuffer(gc.getName().toString());
			if (count > 0)
				key.append("[").append(count).append("]");

			coverages = config.getCoverages();
			cvKeySet = coverages.keySet();
			key_exists = /* cvKeySet.contains(key.toString()) */false;
			for (it = cvKeySet.iterator(); it.hasNext();) {
				cvKey = ((String) it.next()).toLowerCase();
				if (cvKey.endsWith(key.toString().toLowerCase())) {
					key_exists = true;
				}
			}
			if (!key_exists) {
				cvName = key;
				break;
			} else {
				count++;
			}
		}
		name = cvName.toString();
		wmsPath = "/";
		label = new StringBuffer(name).append(" is a ").append(
				format.getDescription()).toString();
		description = new StringBuffer("Generated from ").append(formatId)
				.toString();
		metadataLink = new MetaDataLink();
		metadataLink.setAbout(format.getDocURL());
		metadataLink.setMetadataType("other");
		keywords = new ArrayList(10);
		keywords.add("WCS");
		keywords.add(formatId);
		keywords.add(name);
		nativeFormat = format.getName();
		dirName = new StringBuffer(formatId).append("_").append(name)
				.toString();
		requestCRSs = new ArrayList(10);
		if (gc.getCoordinateReferenceSystem2D().getIdentifiers() != null
				&& !gc.getCoordinateReferenceSystem2D().getIdentifiers()
						.isEmpty())
			requestCRSs.add(((Identifier) gc.getCoordinateReferenceSystem2D()
					.getIdentifiers().toArray()[0]).toString());
		responseCRSs = new ArrayList(10);

		if (gc.getCoordinateReferenceSystem2D().getIdentifiers() != null
				&& !gc.getCoordinateReferenceSystem2D().getIdentifiers()
						.isEmpty())
			responseCRSs.add(((Identifier) gc.getCoordinateReferenceSystem2D()
					.getIdentifiers().toArray()[0]).toString());
		supportedFormats = new ArrayList(10);
		final List formats = CoverageStoreUtils.listDataFormats();
		String fName;
		Format fTmp;
		for (Iterator i = formats.iterator(); i.hasNext();) {
			fTmp = (Format) i.next();
			fName = fTmp.getName();
			if (fName.equalsIgnoreCase("WorldImage")) {
				/*
				 * final String[] formatNames = ImageIO.getReaderFormatNames();
				 * final int length = formatNames.length; for (int f=0; f<length;
				 * f++) { // TODO check if coverage can encode Format
				 * supportedFormats.add(formatNames[f]); }
				 */
				// TODO check if coverage can encode Format
				supportedFormats.add("GIF");
				supportedFormats.add("PNG");
				supportedFormats.add("JPEG");
				supportedFormats.add("TIFF");
			} else if (fName.toLowerCase().startsWith("geotiff")) {
				// TODO check if coverage can encode Format
				supportedFormats.add("GeoTIFF");
			} else {
				// TODO check if coverage can encode Format
				supportedFormats.add(fName);
			}
		}
		defaultInterpolationMethod = "nearest neighbor"; // TODO make me
		// parametric
		interpolationMethods = new ArrayList(10);
		interpolationMethods.add("nearest neighbor");
		interpolationMethods.add("bilinear");
		interpolationMethods.add("bicubic");
		interpolationMethods.add("bicubic_2");
		defaultStyle = "raster";

		/**
		 * ReadParameters ...
		 */
		final DataConfig dataConfig = getDataConfig(request);
		final CoverageStoreConfig cvConfig = dataConfig.getDataFormat(formatId);
		if (cvConfig == null) {
			// something is horribly wrong no FormatID selected!
			// The JSP needs to not include us if there is no
			// selected Format
			//
			throw new RuntimeException(
					"selectedDataFormatId required in Session");
		}

		// Retrieve connection params
		final Format factory = cvConfig.getFactory();
		ParameterValueGroup params = factory.getReadParameters();

		if (params != null && params.values().size() > 0) {
			paramKeys = new ArrayList(params.values().size());
			paramValues = new ArrayList(params.values().size());
			paramHelp = new ArrayList(params.values().size());

			List list = params.values();
			it = list.iterator();
			ParameterDescriptor descr = null;
			ParameterValue val = null;
			while (it.hasNext()) {
				val = (ParameterValue) it.next();
				if (val != null) {
					descr = (ParameterDescriptor) val.getDescriptor();
					String _key = descr.getName().toString();

					if ("namespace".equals(_key)) {
						// skip namespace as it is *magic* and
						// appears to be an entry used in all dataformats?
						//
						continue;
					}

					Object value = cvConfig.getParameters().get(_key);
					String text = "";

					if (value == null) {
						text = null;
					} else if (value instanceof String) {
						text = (String) value;
					} else {
						text = value.toString();
					}

					paramKeys.add(_key);
					paramValues.add((text != null) ? text : "");
					paramHelp.add(_key);
				}
			}
		}
	}

	/**
	 * @param sampleDimensions
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private CoverageDimension[] parseCoverageDimesions(
			GridSampleDimension[] sampleDimensions)
			throws UnsupportedEncodingException {
		final int length = sampleDimensions.length;
		CoverageDimension[] dims = new CoverageDimension[length];

		for (int i = 0; i < length; i++) {
			dims[i] = new CoverageDimension();
			dims[i].setName(sampleDimensions[i].getDescription().toString(
					Locale.getDefault()));
			StringBuffer label = new StringBuffer("GridSampleDimension"
					.intern());
			final Unit uom = sampleDimensions[i].getUnits();
			if (uom != null) {

				label.append("(".intern());
				parseUom(label, uom);
				label.append(")".intern());
			}
			label.append("[".intern());
			label.append(sampleDimensions[i].getMinimumValue());
			label.append(",".intern());
			label.append(sampleDimensions[i].getMaximumValue());
			label.append("]".intern());
			dims[i].setDescription(label.toString());
			dims[i].setRange(sampleDimensions[i].getRange());
			double[] nTemp = sampleDimensions[i].getNoDataValues();
			if (nTemp != null) {
				final int ntLength = nTemp.length;
				Double[] nulls = new Double[ntLength];
				for (int nd = 0; nd < ntLength; nd++) {
					nulls[nd] = new Double(nTemp[nd]);
				}
				dims[i].setNullValues(nulls);
			}
		}

		return dims;
	}

	/**
	 * This method tries to put in order problems with 16 bits characters.
	 * 
	 * @param label2
	 * @param uom
	 */
	private void parseUom(StringBuffer label2, Unit uom) {

		String uomString = uom.toString();
		uomString = uomString.replaceAll("�", "^2");
		uomString = uomString.replaceAll("�", "^3");
		uomString = uomString.replaceAll("�", "A");
		uomString = uomString.replaceAll("�", "");
		label2.append(uomString);

	}

	public CoverageConfig(CoverageInfoDTO dto) {
		if (dto == null) {
			throw new NullPointerException("Non null CoverageInfoDTO required");
		}

		formatId = dto.getFormatId();
		name = dto.getName();
		wmsPath = dto.getWmsPath();
		label = dto.getLabel();
		description = dto.getDescription();
		metadataLink = dto.getMetadataLink();
		keywords = dto.getKeywords();
		crs = dto.getCrs();
		srsName = dto.getSrsName();
		srsWKT = dto.getSrsWKT();
		envelope = dto.getEnvelope();
		lonLatWGS84Envelope = dto.getLonLatWGS84Envelope();
		grid = dto.getGrid();
		dimensions = dto.getDimensions();
		dimentionNames = dto.getDimensionNames();
		nativeFormat = dto.getNativeFormat();
		dirName = dto.getDirName();
		requestCRSs = dto.getRequestCRSs();
		responseCRSs = dto.getResponseCRSs();
		supportedFormats = dto.getSupportedFormats();
		defaultInterpolationMethod = dto.getDefaultInterpolationMethod();
		interpolationMethods = dto.getInterpolationMethods();
		defaultStyle = dto.getDefaultStyle();
		paramHelp = dto.getParamHelp();
		paramKeys = dto.getParamKeys();
		paramValues = dto.getParamValues();
	}

	public CoverageInfoDTO toDTO() {
		CoverageInfoDTO c = new CoverageInfoDTO();
		c.setFormatId(formatId);
		c.setName(name);
		c.setWmsPath(wmsPath);
		c.setLabel(label);
		c.setDescription(description);
		c.setMetadataLink(metadataLink);
		c.setKeywords(keywords);
		c.setCrs(crs);
		c.setSrsName(srsName);
		c.setSrsWKT(srsWKT);
		c.setEnvelope(envelope);
		c.setLonLatWGS84Envelope(lonLatWGS84Envelope);
		c.setGrid(grid);
		c.setDimensions(dimensions);
		c.setDimensionNames(dimentionNames);
		c.setNativeFormat(nativeFormat);
		c.setDirName(dirName);
		c.setRequestCRSs(requestCRSs);
		c.setResponseCRSs(responseCRSs);
		c.setSupportedFormats(supportedFormats);
		c.setDefaultInterpolationMethod(defaultInterpolationMethod);
		c.setInterpolationMethods(interpolationMethods);
		c.setDefaultStyle(defaultStyle);
		c.setParamHelp(paramHelp);
		c.setParamKeys(paramKeys);
		c.setParamValues(paramValues);

		return c;
	}

	/**
	 * Access Catalog Configuration Model from the WebContainer.
	 * 
	 * @param request
	 * 
	 * @return Configuration model for Catalog information.
	 */
	protected DataConfig getDataConfig(HttpServletRequest request) {
		return (DataConfig) request.getSession().getServletContext()
				.getAttribute(DataConfig.CONFIG_KEY);
	}

	public String getKey() {
		return getFormatId() + DataConfig.SEPARATOR + getName();
	}

	public String toString() {
		return "CoverageConfig[name: " + name + " dewcription: " + description
				+ " srsName: " + srsName + "]";
	}

	/**
	 * @return Returns the defaultInterpolationMethod.
	 * 
	 * @uml.property name="defaultInterpolationMethod"
	 */
	public String getDefaultInterpolationMethod() {
		return defaultInterpolationMethod;
	}

	/**
	 * @param defaultInterpolationMethod
	 *            The defaultInterpolationMethod to set.
	 * 
	 * @uml.property name="defaultInterpolationMethod"
	 */
	public void setDefaultInterpolationMethod(String defaultInterpolationMethod) {
		this.defaultInterpolationMethod = defaultInterpolationMethod;
	}

	/**
	 * @return Returns the description.
	 * 
	 * @uml.property name="description"
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 * 
	 * @uml.property name="description"
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the dirName.
	 * 
	 * @uml.property name="dirName"
	 */
	public String getDirName() {
		return dirName;
	}

	/**
	 * @param dirName
	 *            The dirName to set.
	 * 
	 * @uml.property name="dirName"
	 */
	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	/**
	 * @return Returns the envelope.
	 * 
	 * @uml.property name="envelope"
	 */
	public GeneralEnvelope getEnvelope() {
		return envelope;
	}

	/**
	 * @param envelope
	 *            The envelope to set.
	 * 
	 * @uml.property name="envelope"
	 */
	public void setEnvelope(GeneralEnvelope envelope) {
		this.envelope = envelope;
	}

	/**
	 * @return Returns the formatId.
	 * 
	 * @uml.property name="formatId"
	 */
	public String getFormatId() {
		return formatId;
	}

	/**
	 * @param formatId
	 *            The formatId to set.
	 * 
	 * @uml.property name="formatId"
	 */
	public void setFormatId(String formatId) {
		this.formatId = formatId;
	}

	/**
	 * @return Returns the interpolationMethods.
	 * 
	 * @uml.property name="interpolationMethods"
	 */
	public List getInterpolationMethods() {
		return interpolationMethods;
	}

	/**
	 * @param interpolationMethods
	 *            The interpolationMethods to set.
	 * 
	 * @uml.property name="interpolationMethods"
	 */
	public void setInterpolationMethods(List interpolationMethods) {
		this.interpolationMethods = interpolationMethods;
	}

	/**
	 * @return Returns the keywords.
	 * 
	 * @uml.property name="keywords"
	 */
	public List getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords
	 *            The keywords to set.
	 * 
	 * @uml.property name="keywords"
	 */
	public void setKeywords(List keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return Returns the label.
	 * 
	 * @uml.property name="label"
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            The label to set.
	 * 
	 * @uml.property name="label"
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return Returns the metadataLink.
	 * 
	 * @uml.property name="metadataLink"
	 */
	public MetaDataLink getMetadataLink() {
		return metadataLink;
	}

	/**
	 * @param metadataLink
	 *            The metadataLink to set.
	 * 
	 * @uml.property name="metadataLink"
	 */
	public void setMetadataLink(MetaDataLink metadataLink) {
		this.metadataLink = metadataLink;
	}

	/**
	 * @return Returns the name.
	 * 
	 * @uml.property name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 * 
	 * @uml.property name="name"
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the nativeFormat.
	 * 
	 * @uml.property name="nativeFormat"
	 */
	public String getNativeFormat() {
		return nativeFormat;
	}

	/**
	 * @param nativeFormat
	 *            The nativeFormat to set.
	 * 
	 * @uml.property name="nativeFormat"
	 */
	public void setNativeFormat(String nativeFormat) {
		this.nativeFormat = nativeFormat;
	}

	/**
	 * @return Returns the requestCRSs.
	 * 
	 * @uml.property name="requestCRSs"
	 */
	public List getRequestCRSs() {
		return requestCRSs;
	}

	/**
	 * @param requestCRSs
	 *            The requestCRSs to set.
	 * 
	 * @uml.property name="requestCRSs"
	 */
	public void setRequestCRSs(List requestCRSs) {
		this.requestCRSs = requestCRSs;
	}

	/**
	 * @return Returns the responseCRSs.
	 * 
	 * @uml.property name="responseCRSs"
	 */
	public List getResponseCRSs() {
		return responseCRSs;
	}

	/**
	 * @param responseCRSs
	 *            The responseCRSs to set.
	 * 
	 * @uml.property name="responseCRSs"
	 */
	public void setResponseCRSs(List responseCRSs) {
		this.responseCRSs = responseCRSs;
	}

	/**
	 * @return Returns the srsName.
	 * 
	 * @uml.property name="srsName"
	 */
	public String getSrsName() {
		return srsName;
	}

	/**
	 * @param srsName
	 *            The srsName to set.
	 * 
	 * @uml.property name="srsName"
	 */
	public void setSrsName(String srsName) {
		this.srsName = srsName;
	}

	/**
	 * @return Returns the supportedFormats.
	 * 
	 * @uml.property name="supportedFormats"
	 */
	public List getSupportedFormats() {
		return supportedFormats;
	}

	/**
	 * @param supportedFormats
	 *            The supportedFormats to set.
	 * 
	 * @uml.property name="supportedFormats"
	 */
	public void setSupportedFormats(List supportedFormats) {
		this.supportedFormats = supportedFormats;
	}

	/**
	 * 
	 * @uml.property name="crs"
	 */
	public CoordinateReferenceSystem getCrs() {
		return crs;
	}

	/**
	 * 
	 * @uml.property name="crs"
	 */
	public void setCrs(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}

	/**
	 * 
	 * @uml.property name="grid"
	 */
	public GridGeometry getGrid() {
		return grid;
	}

	/**
	 * 
	 * @uml.property name="grid"
	 */
	public void setGrid(GridGeometry grid) {
		this.grid = grid;
	}

	/**
	 * 
	 * @uml.property name="dimentionNames"
	 */
	public InternationalString[] getDimentionNames() {
		return dimentionNames;
	}

	/**
	 * 
	 * @uml.property name="dimentionNames"
	 */
	public void setDimentionNames(InternationalString[] dimentionNames) {
		this.dimentionNames = dimentionNames;
	}

	/**
	 * @return Returns the dimensions.
	 * 
	 * @uml.property name="dimensions"
	 */
	public CoverageDimension[] getDimensions() {
		return dimensions;
	}

	/**
	 * @param dimensions
	 *            The dimensions to set.
	 * 
	 * @uml.property name="dimensions"
	 */
	public void setDimensions(CoverageDimension[] dimensions) {
		this.dimensions = dimensions;
	}

	public String getDefaultStyle() {
		return defaultStyle;
	}

	public void setDefaultStyle(String defaultStyle) {
		this.defaultStyle = defaultStyle;
	}

	public String getSrsWKT() {
		return srsWKT;
	}

	public void setSrsWKT(String srsWKT) {
		this.srsWKT = srsWKT;
	}

	public GeneralEnvelope getLonLatWGS84Envelope() {
		return lonLatWGS84Envelope;
	}

	public String getWmsPath() {
		return wmsPath;
	}

	public void setWmsPath(String wmsPath) {
		this.wmsPath = wmsPath;
	}

	/**
	 * @return Returns the paramHelp.
	 */
	public ArrayList getParamHelp() {
		return paramHelp;
	}

	/**
	 * @param paramHelp
	 *            The paramHelp to set.
	 */
	public void setParamHelp(ArrayList paramHelp) {
		this.paramHelp = paramHelp;
	}

	/**
	 * @return Returns the paramKeys.
	 */
	public List getParamKeys() {
		return paramKeys;
	}

	/**
	 * @param paramKeys
	 *            The paramKeys to set.
	 */
	public void setParamKeys(List paramKeys) {
		this.paramKeys = paramKeys;
	}

	/**
	 * @return Returns the paramValues.
	 */
	public List getParamValues() {
		return paramValues;
	}

	/**
	 * @param paramValues
	 *            The paramValues to set.
	 */
	public void setParamValues(List paramValues) {
		this.paramValues = paramValues;
	}
}
