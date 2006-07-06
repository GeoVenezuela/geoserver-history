/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.global.xml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.xml.serialize.LineSeparator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.filter.FilterDOMParser;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.NameFactory;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.util.InternationalString;
import org.vfny.geoserver.global.ConfigurationException;
import org.vfny.geoserver.global.CoverageDimension;
import org.vfny.geoserver.global.GeoServer;
import org.vfny.geoserver.global.GeoserverDataDirectory;
import org.vfny.geoserver.global.MetaDataLink;
import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
import org.vfny.geoserver.global.dto.ContactDTO;
import org.vfny.geoserver.global.dto.CoverageInfoDTO;
import org.vfny.geoserver.global.dto.CoverageStoreInfoDTO;
import org.vfny.geoserver.global.dto.DataDTO;
import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
import org.vfny.geoserver.global.dto.GeoServerDTO;
import org.vfny.geoserver.global.dto.LegendURLDTO;
import org.vfny.geoserver.global.dto.NameSpaceInfoDTO;
import org.vfny.geoserver.global.dto.ServiceDTO;
import org.vfny.geoserver.global.dto.StyleDTO;
import org.vfny.geoserver.global.dto.WCSDTO;
import org.vfny.geoserver.global.dto.WFSDTO;
import org.vfny.geoserver.global.dto.WMSDTO;
import org.vfny.geoserver.util.CoverageStoreUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * XMLConfigReader purpose.
 * 
 * <p>
 * Description of XMLConfigReader Static class to load a configuration
 * org.vfny.geoserver.global.dto
 * </p>
 * 
 * <p>
 * Example Use:
 * 
 * <pre><code>
 * ModelConfig m = XMLConfigReader.load(new File(&quot;/conf/&quot;));
 * </code></pre>
 * 
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
 *         modification)
 * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
 *         modification)
 * @version $Id: XMLConfigReader.java,v 1.43 2004/09/13 16:04:26 cholmesny Exp $
 */
public class XMLConfigReader {
	/** Used internally to create log information to detect errors. */
	private static final Logger LOGGER = Logger
			.getLogger("org.vfny.geoserver.global");

	/** The root directory from which the configuration is loaded. */
	private File root;

	/** Is set to true after the model is loaded into memory. */
	private boolean initialized = false;

	private WMSDTO wms;

	private WFSDTO wfs;

	private WCSDTO wcs;

	private GeoServerDTO geoServer;

	private DataDTO data;

	/** the servlet context * */
	ServletContext context;

	/**
	 * XMLConfigReader constructor.
	 * 
	 * <p>
	 * Should never be called.
	 * </p>
	 */
	protected XMLConfigReader(ServletContext context) {
		this.context = context;
		wms = new WMSDTO();
		wfs = new WFSDTO();
		wcs = new WCSDTO();
		geoServer = new GeoServerDTO();
		data = new DataDTO();
		root = new File(".");
	}

	/**
	 * <p>
	 * This method loads the config files from the specified directory into a
	 * ModelConfig. If the path is incorrect, or the directory is formed
	 * correctly, a ConfigException will be thrown and/or null returned. <br>
	 * <br>
	 * The config directory is as follows:<br>
	 * 
	 * <ul>
	 * <li> ./WEB-INF/catalog.xml </li>
	 * <li> ./WEB-INF/services.xml </li>
	 * <li> ./data/featuretypes/ /info.xml </li>
	 * <li> ./data/featuretypes/ /schema.xml </li>
	 * </ul>
	 * </p>
	 * 
	 * @param root
	 *            A directory which contains the config files.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	public XMLConfigReader(File root, ServletContext context)
			throws ConfigurationException {
		this.root = root;
		this.context = context;
		wms = new WMSDTO();
		wfs = new WFSDTO();
		wcs = new WCSDTO();
		geoServer = new GeoServerDTO();
		data = new DataDTO();
		load();
		initialized = true;

	}

	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * load purpose.
	 * 
	 * <p>
	 * Main load routine, sets up file handles for various other portions of the
	 * load procedure.
	 * </p>
	 * 
	 * @throws ConfigurationException
	 */
	protected void load() throws ConfigurationException {
		root = ReaderUtils.checkFile(root, true);
		File configDir;

		// Doing some trys here for either being in the webapp, with data and
		// web-inf defined
		// or in a true data_dir, with the catalog and service in the same root
		// dir.
		try {
			configDir = ReaderUtils.checkFile(new File(root, "WEB-INF/"), true);
		} catch (ConfigurationException confE) {
			// no WEB-INF, so we're in a data_dir, use as root.
			configDir = root;
		}
		File configFile = ReaderUtils.checkFile(new File(configDir,
				"services.xml"), false);

		loadServices(configFile);

		File catalogFile = ReaderUtils.checkFile(new File(configDir,
				"catalog.xml"), false);
		File featureTypeDir = GeoserverDataDirectory.findConfigDir(root,
				"featureTypes/");
		File styleDir = GeoserverDataDirectory.findConfigDir(root, "styles/");
		File coverageDir = GeoserverDataDirectory.findConfigDir(root,
				"coverages/");

		loadCatalog(catalogFile, featureTypeDir, styleDir, coverageDir);

		// Future additions
		// validationDir = ReaderUtils.initFile(new
		// File(dataDir,"validation/"),true);
		// loadValidation(validationDir);
	}

	/**
	 * loadServices purpose.
	 * 
	 * <p>
	 * loads services.xml into memory with the assistance of other class
	 * methods.
	 * </p>
	 * 
	 * @param configFile
	 *            services.xml
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected void loadServices(File configFile) throws ConfigurationException {
		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config(new StringBuffer("Loading configuration file: ")
					.append(configFile).toString());
		}

		Element configElem = null;

		try {
			FileReader fr = new FileReader(configFile);
			configElem = ReaderUtils.loadConfig(fr);
			fr.close();
		} catch (FileNotFoundException e) {
			throw new ConfigurationException(e);
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}

		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config("parsing configuration documents");
		}

		Element elem = (Element) configElem.getElementsByTagName("global")
				.item(0);
		loadGlobal(elem);

		NodeList configuredServices = configElem
				.getElementsByTagName("service");
		int nServices = configuredServices.getLength();

		for (int i = 0; i < nServices; i++) {
			elem = (Element) configuredServices.item(i);

			String serviceType = elem.getAttribute("type");

			if ("WCS".equalsIgnoreCase(serviceType)) {
				loadWCS(elem);
			} else if ("WFS".equalsIgnoreCase(serviceType)) {
				loadWFS(elem);
			} else if ("WMS".equalsIgnoreCase(serviceType)) {
				loadWMS(elem);
			} else if ("Z39.50".equalsIgnoreCase(serviceType)) {
				// ...
			} else {
				throw new ConfigurationException("Unknown service type: "
						+ serviceType);
			}
		}
	}

	/**
	 * loadCatalog purpose.
	 * 
	 * <p>
	 * loads catalog.xml into memory with the assistance of other class methods.
	 * </p>
	 * 
	 * @param catalogFile
	 *            catalog.xml
	 * @param featureTypeDir
	 *            the directory containing the info.xml files for the
	 *            featuretypes.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected void loadCatalog(File catalogFile, File featureTypeDir,
			File styleDir, File coverageDir) throws ConfigurationException {

		Element catalogElem = null;

		try {
			if (LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(new StringBuffer("Loading configuration file: ")
						.append(catalogFile).toString());
			}
			FileReader fr = new FileReader(catalogFile);
			catalogElem = ReaderUtils.loadConfig(fr);
			fr.close();
		} catch (FileNotFoundException e) {
			throw new ConfigurationException(e);
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}

		data.setNameSpaces(loadNameSpaces(ReaderUtils.getChildElement(
				catalogElem, "namespaces", true)));
		setDefaultNS();
		data.setFormats(loadFormats(ReaderUtils.getChildElement(catalogElem,
				"formats", true)));
		data.setDataStores(loadDataStores(ReaderUtils.getChildElement(
				catalogElem, "datastores", true)));

		data.setStyles(loadStyles(ReaderUtils.getChildElement(catalogElem,
				"styles", false), styleDir));
		// new File(featureTypeDir.getParentFile(), "styles")));

		// must be last
		data.setFeaturesTypes(loadFeatureTypes(featureTypeDir));
		data.setCoverages(loadCoverages(coverageDir));
	}

	/**
	 * setDefaultNS purpose.
	 * 
	 * <p>
	 * Finds and sets the default namespace. The namespaces in catalog must
	 * already be loaded.
	 * </p>
	 */
	protected void setDefaultNS() {
		Iterator i = data.getNameSpaces().values().iterator();

		while (i.hasNext()) {
			NameSpaceInfoDTO ns = (NameSpaceInfoDTO) i.next();

			if (ns.isDefault()) {
				data.setDefaultNameSpacePrefix(ns.getPrefix());
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer(new StringBuffer(
							"set default namespace pre to ").append(
							ns.getPrefix()).toString());
				}

				return;
			}
		}
	}

	/**
	 * getLoggingLevel purpose.
	 * 
	 * <p>
	 * Parses the LoggingLevel from a DOM tree and converts the level into a
	 * Level Object.
	 * </p>
	 * 
	 * @param globalConfigElem
	 * 
	 * @return The logging Level
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected Level getLoggingLevel(Element globalConfigElem)
			throws ConfigurationException {
		Level level = Logger.getLogger("org.vfny.geoserver").getLevel();
		Element levelElem = ReaderUtils.getChildElement(globalConfigElem,
				"loggingLevel");

		if (levelElem != null) {
			String levelName = levelElem.getFirstChild().getNodeValue();

			try {
				level = Level.parse(levelName);
			} catch (IllegalArgumentException ex) {
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.warning(new StringBuffer(
							"illegal loggingLevel name: ").append(levelName)
							.toString());
				}
			}
		} else {
			if (LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER
						.config("No loggingLevel found, using default logging.properties setting");
			}
		}

		return level;
	}

	/**
	 * loadGlobal purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a GlobalData configuration.
	 * </p>
	 * 
	 * @param globalElem
	 *            A DOM tree representing a complete global configuration.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected void loadGlobal(Element globalElem) throws ConfigurationException {
		geoServer = new GeoServerDTO();
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("parsing global configuration parameters");
		}

		Level loggingLevel = getLoggingLevel(globalElem);
		geoServer.setLoggingLevel(loggingLevel);

		boolean loggingToFile = false;
		Element elem = null;
		elem = ReaderUtils.getChildElement(globalElem, "loggingToFile", false);
		if (elem != null) {
			loggingToFile = ReaderUtils.getBooleanAttribute(elem, "value",
					false, false);
		}

		String logLocation = ReaderUtils
				.getChildText(globalElem, "logLocation");
		if ((logLocation != null) && "".equals(logLocation.trim())) {
			logLocation = null;
		}

		geoServer.setLoggingToFile(loggingToFile);
		geoServer.setLogLocation(logLocation);

		// init this now so the rest of the config has correct log levels.
		try {
			GeoServer.initLogging(loggingLevel, loggingToFile, logLocation,
					context);
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}

		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config(new StringBuffer("logging level is ").append(
					loggingLevel).toString());
		}

		if (logLocation != null) {
			if (LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(new StringBuffer("logging to ").append(
						logLocation).toString());
			}
		}

		long jaiMemoryCapacity = 0;
		elem = ReaderUtils.getChildElement(globalElem, "JaiMemoryCapacity",
				false);
		if (elem != null) {
			jaiMemoryCapacity = (long) ReaderUtils.getDoubleAttribute(elem,
					"value", false);
		}

		double jaiMemoryThreshold = 0.0;
		elem = ReaderUtils.getChildElement(globalElem, "JaiMemoryThreshold",
				false);
		if (elem != null) {
			jaiMemoryThreshold = ReaderUtils.getDoubleAttribute(elem, "value",
					false);
		}

		Boolean jaiRecycling = Boolean.FALSE;
		elem = ReaderUtils.getChildElement(globalElem, "JaiRecycling", false);
		if (elem != null) {
			jaiRecycling = Boolean.valueOf(ReaderUtils.getBooleanAttribute(
					elem, "value", false, false));
		}

        Boolean imageIOCache = Boolean.FALSE;
        elem = ReaderUtils.getChildElement(globalElem, "ImageIOCache", false);
        if (elem != null) {
            imageIOCache = Boolean.valueOf(ReaderUtils.getBooleanAttribute(
                    elem, "value", false, false));
        }

		geoServer.setJaiMemoryCapacity(jaiMemoryCapacity);
		geoServer.setJaiMemoryThreshold(jaiMemoryThreshold);
		geoServer.setJaiRecycling(jaiRecycling);
        geoServer.setImageIOCache(imageIOCache);

		elem = ReaderUtils.getChildElement(globalElem, "ContactInformation");
		geoServer.setContact(loadContact(elem));

		elem = ReaderUtils.getChildElement(globalElem, "verbose", false);

		if (elem != null) {
			geoServer.setVerbose(ReaderUtils.getBooleanAttribute(elem, "value",
					false, true));
		}

		elem = ReaderUtils.getChildElement(globalElem, "maxFeatures");

		if (elem != null) {
			// if the element is pressent, it's "value" attribute is mandatory
			geoServer.setMaxFeatures(ReaderUtils.getIntAttribute(elem, "value",
					true, geoServer.getMaxFeatures()));
		}

		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config(new StringBuffer("maxFeatures is ").append(
					geoServer.getMaxFeatures()).toString());
		}
		elem = ReaderUtils.getChildElement(globalElem, "numDecimals");

		if (elem != null) {
			geoServer.setNumDecimals(ReaderUtils.getIntAttribute(elem, "value",
					true, geoServer.getNumDecimals()));
		}

		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config(new StringBuffer("numDecimals returning is ").append(
					geoServer.getNumDecimals()).toString());
		}
		elem = ReaderUtils.getChildElement(globalElem, "charSet");

		if (elem != null) {
			String chSet = ReaderUtils.getAttribute(elem, "value", true);

			try {
				Charset cs = Charset.forName(chSet);
				geoServer.setCharSet(cs);
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer(new StringBuffer("charSet: ").append(
							cs.displayName()).toString());
				}
			} catch (Exception ex) {
				if (LOGGER.isLoggable(Level.INFO)) {
					LOGGER.info(ex.getMessage());
				}
			}
		}

		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config(new StringBuffer("charSet is ").append(
					geoServer.getCharSet()).toString());
		}

		// Schema base doesn't work - this root thing is wrong. So for 1.2.0 I'm
		// just going to leave it out. The GeoServer.getSchemaBaseUrl is never
		// called, Request.getSchemaBaseUrl is used, and it always returns the
		// relative local one. This field was a hack anyways, so I don't think
		// anyone is going to miss it much - though I could be proved wrong. ch
		String schemaBaseUrl = ReaderUtils.getChildText(globalElem,
				"SchemaBaseUrl");

		if (schemaBaseUrl != null) {
			geoServer.setSchemaBaseUrl(schemaBaseUrl);
		} else {
			// This is wrong - need some key to tell the method to return based
			// on the url passed in.
			geoServer.setSchemaBaseUrl(root.toString() + "/data/capabilities/");
		}

		String adminUserName = ReaderUtils.getChildText(globalElem,
				"adminUserName");

		if (adminUserName != null) {
			geoServer.setAdminUserName(adminUserName);
		}

		String adminPassword = ReaderUtils.getChildText(globalElem,
				"adminPassword");

		if (adminPassword != null) {
			geoServer.setAdminPassword(adminPassword);
		}

		elem = ReaderUtils.getChildElement(globalElem, "verboseExceptions",
				false);

		if (elem != null) {
			geoServer.setVerboseExceptions(ReaderUtils.getBooleanAttribute(
					elem, "value", false, true));
		}
	}

	/**
	 * loadContact purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a ContactConfig
	 * </p>
	 * 
	 * @param contactInfoElement
	 *            a DOM tree to convert into a ContactConfig.
	 * 
	 * @return The resulting ContactConfig object from the DOM tree.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected ContactDTO loadContact(Element contactInfoElement)
			throws ConfigurationException {
		ContactDTO c = new ContactDTO();

		if (contactInfoElement == null) {
			return c;
		}

		Element elem;
		NodeList nodeList;
		elem = ReaderUtils.getChildElement(contactInfoElement,
				"ContactPersonPrimary");

		if (elem != null) {
			c.setContactPerson(ReaderUtils.getChildText(elem, "ContactPerson"));
			c.setContactOrganization(ReaderUtils.getChildText(elem,
					"ContactOrganization"));
		}

		c.setContactPosition(ReaderUtils.getChildText(contactInfoElement,
				"ContactPosition"));
		elem = ReaderUtils
				.getChildElement(contactInfoElement, "ContactAddress");

		if (elem != null) {
			c.setAddressType(ReaderUtils.getChildText(elem, "AddressType"));
			c.setAddress(ReaderUtils.getChildText(elem, "Address"));
			c.setAddressCity(ReaderUtils.getChildText(elem, "City"));
			c
					.setAddressState(ReaderUtils.getChildText(elem,
							"StateOrProvince"));
			c.setAddressPostalCode(ReaderUtils.getChildText(elem, "PostCode"));
			c.setAddressCountry(ReaderUtils.getChildText(elem, "Country"));
		}

		c.setContactVoice(ReaderUtils.getChildText(contactInfoElement,
				"ContactVoiceTelephone"));
		c.setContactFacsimile(ReaderUtils.getChildText(contactInfoElement,
				"ContactFacsimileTelephone"));
		c.setContactEmail(ReaderUtils.getChildText(contactInfoElement,
				"ContactElectronicMailAddress"));
		c.setOnlineResource(ReaderUtils.getChildText(contactInfoElement,
				"ContactOnlineResource"));

		return c;
	}

	/**
	 * loadWCS purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a WCS object.
	 * </p>
	 * 
	 * @param wfsElement
	 *            a DOM tree to convert into a WCS object.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 * 
	 * @see GlobalData#getBaseUrl()
	 */
	protected void loadWCS(Element wcsElement) throws ConfigurationException {
		wcs = new WCSDTO();
		wcs.setService(loadService(wcsElement));
	}

	/**
	 * loadWFS purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a WFS object.
	 * </p>
	 * 
	 * @param wfsElement
	 *            a DOM tree to convert into a WFS object.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 * 
	 * @see GlobalData#getBaseUrl()
	 */
	protected void loadWFS(Element wfsElement) throws ConfigurationException {
		wfs = new WFSDTO();

		// try {

		wfs.setFeatureBounding(ReaderUtils.getBooleanAttribute(ReaderUtils
				.getChildElement(wfsElement, "featureBounding"), "value",
				false, false));

		Element elem = ReaderUtils.getChildElement(wfsElement, "srsXmlStyle",
				false);
		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config(new StringBuffer("reading srsXmlStyle: ")
					.append(elem).toString());
		}

		if (elem != null) {
			wfs.setSrsXmlStyle(ReaderUtils.getBooleanAttribute(elem, "value",
					false, true));
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine(new StringBuffer("set srsXmlStyle to ").append(
						ReaderUtils.getBooleanAttribute(elem, "value", false,
								true)).toString());
			}
		}

		String serviceLevelValue = ReaderUtils.getChildText(wfsElement,
				"serviceLevel");
		int serviceLevel = WFSDTO.COMPLETE;

		if ((serviceLevelValue != null) && !serviceLevelValue.equals("")) {
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer(new StringBuffer("reading serviceLevel: ").append(
						serviceLevelValue).toString());
			}

			if (serviceLevelValue.equalsIgnoreCase("basic")) {
				serviceLevel = WFSDTO.BASIC;
			} else if (serviceLevelValue.equalsIgnoreCase("complete")) {
				serviceLevel = WFSDTO.COMPLETE;
			} else if (serviceLevelValue.equalsIgnoreCase("transactional")) {
				serviceLevel = WFSDTO.TRANSACTIONAL;
			} else {
				try {
					serviceLevel = Integer.parseInt(serviceLevelValue);
				} catch (NumberFormatException nfe) {
					String mesg = "Could not parse serviceLevel.  It "
							+ "should be one of Basic, Complete, or Transactional"
							+ " or else an integer value";
					throw new ConfigurationException(mesg, nfe);
				}
			}
		} else { // TODO: this should probably parse the strings as well,
			serviceLevel = ReaderUtils.getIntAttribute(ReaderUtils
					.getChildElement(wfsElement, "serviceLevel"), "value",
					false, WFSDTO.COMPLETE);
		}

		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer(new StringBuffer("setting service level to ").append(
					serviceLevel).toString());
		}
		wfs.setServiceLevel(serviceLevel);

		// get the conformance hacks attribute
		// it might not be there, in which case we just use the default value
		// (see WFSDTO.java)
		Element e = ReaderUtils.getChildElement(wfsElement,
				"citeConformanceHacks");

		if (e != null) {
			String text = ReaderUtils.getChildText(wfsElement,
					"citeConformanceHacks");
			boolean citeConformanceHacks = Boolean.valueOf(text).booleanValue(); // just
																					// get
																					// the
																					// value
																					// and
																					// parse
																					// it
			wfs.setCiteConformanceHacks(citeConformanceHacks);
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer(new StringBuffer(
						"setting citeConformanceHacks to ").append(
						citeConformanceHacks).toString());
			}
		}

		// } catch (Exception e) {
		// }
		ServiceDTO s = loadService(wfsElement);
		wfs.setService(s);
	}

	/**
	 * loadWMS purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a WMS object.
	 * </p>
	 * 
	 * @param wmsElement
	 *            a DOM tree to convert into a WMS object.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 * 
	 * @see GlobalData#getBaseUrl()
	 */
	protected void loadWMS(Element wmsElement) throws ConfigurationException {
		wms = new WMSDTO();
		wms.setService(loadService(wmsElement));

		wms.setSvgRenderer(ReaderUtils.getChildText(wmsElement, "svgRenderer"));
		wms.setSvgAntiAlias(!"false".equals(ReaderUtils.getChildText(
				wmsElement, "svgAntiAlias")));
	}

	/**
	 * loadService purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a ServiceDTO object.
	 * </p>
	 * 
	 * @param serviceRoot
	 *            a DOM tree to convert into a ServiceDTO object.
	 * 
	 * @return A complete ServiceDTO object loaded from the DOM tree provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected ServiceDTO loadService(Element serviceRoot)
			throws ConfigurationException {
		ServiceDTO s = new ServiceDTO();

		s.setName(ReaderUtils.getChildText(serviceRoot, "name", true));
		s.setTitle(ReaderUtils.getChildText(serviceRoot, "title", false));
		s.setAbstract(ReaderUtils.getChildText(serviceRoot, "abstract"));
		s.setKeywords(ReaderUtils.getKeyWords(ReaderUtils.getChildElement(
				serviceRoot, "keywords")));
		s.setMetadataLink(ReaderUtils.getMetaDataLink(ReaderUtils
				.getChildElement(serviceRoot, "metadataLink")));
		s.setFees(ReaderUtils.getChildText(serviceRoot, "fees"));
		s.setAccessConstraints(ReaderUtils.getChildText(serviceRoot,
				"accessConstraints"));
		s.setMaintainer(ReaderUtils.getChildText(serviceRoot, "maintainer"));
		s.setEnabled(ReaderUtils.getBooleanAttribute(serviceRoot, "enabled",
				false, true));

		try {
			s.setOnlineResource(new URL(ReaderUtils.getChildText(serviceRoot,
					"onlineResource", true)));
		} catch (MalformedURLException e) {
			throw new ConfigurationException(e);
		}

		return s;
	}

	/**
	 * loadNameSpaces purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a Map of NameSpaces.
	 * </p>
	 * 
	 * @param nsRoot
	 *            a DOM tree to convert into a Map of NameSpaces.
	 * 
	 * @return A complete Map of NameSpaces loaded from the DOM tree provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected Map loadNameSpaces(Element nsRoot) throws ConfigurationException {
		NodeList nsList = nsRoot.getElementsByTagName("namespace");
		Element elem;
		int nsCount = nsList.getLength();
		Map nameSpaces = new HashMap(nsCount);

		for (int i = 0; i < nsCount; i++) {
			elem = (Element) nsList.item(i);

			NameSpaceInfoDTO ns = new NameSpaceInfoDTO();
			ns.setUri(ReaderUtils.getAttribute(elem, "uri", true));
			ns.setPrefix(ReaderUtils.getAttribute(elem, "prefix", true));
			ns.setDefault(ReaderUtils.getBooleanAttribute(elem, "default",
					false, false)
					|| (nsCount == 1));
			if (LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(new StringBuffer("added namespace ").append(ns)
						.toString());
			}
			nameSpaces.put(ns.getPrefix(), ns);
		}

		return nameSpaces;
	}

	/**
	 * loadStyles purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a Map of Styles.
	 * </p>
	 * 
	 * @param stylesElem
	 *            a DOM tree to convert into a Map of Styles.
	 * @param baseDir
	 *            DOCUMENT ME!
	 * 
	 * @return A complete Map of Styles loaded from the DOM tree provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected Map loadStyles(Element stylesElem, File baseDir)
			throws ConfigurationException {
		Map styles = new HashMap();

		NodeList stylesList = null;

		if (stylesElem != null) {
			stylesList = stylesElem.getElementsByTagName("style");
		}

		if ((stylesList == null) || (stylesList.getLength() == 0)) {
			// no styles where defined, just add a default one
			StyleDTO s = new StyleDTO();
			s.setId("normal");
			s.setFilename(new File(baseDir, "normal.sld"));
			s.setDefault(true);
			styles.put("normal", s);
		}

		int styleCount = stylesList.getLength();
		Element styleElem;

		for (int i = 0; i < styleCount; i++) {
			styleElem = (Element) stylesList.item(i);

			StyleDTO s = new StyleDTO();
			s.setId(ReaderUtils.getAttribute(styleElem, "id", true));
			s.setFilename(new File(baseDir, ReaderUtils.getAttribute(styleElem,
					"filename", true)));
			s.setDefault(ReaderUtils.getBooleanAttribute(styleElem, "default",
					false, false));
			styles.put(s.getId(), s);

			if (LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(new StringBuffer("Loaded style ").append(
						s.getId()).toString());
			}
		}

		return styles;
	}

	/**
	 * loadFormats purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a Map of Formats.
	 * </p>
	 * 
	 * @param fmRoot
	 *            a DOM tree to convert into a Map of Formats.
	 * 
	 * @return A complete Map of Formats loaded from the DOM tree provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected Map loadFormats(Element fmRoot) throws ConfigurationException {
		Map formats = new HashMap();

		NodeList fmElements = fmRoot.getElementsByTagName("format");
		int fmCnt = fmElements.getLength();
		CoverageStoreInfoDTO fmConfig;
		Element fmElem;

		for (int i = 0; i < fmCnt; i++) {
			fmElem = (Element) fmElements.item(i);
			fmConfig = loadFormat(fmElem);

			if (formats.containsKey(fmConfig.getId())) {
				throw new ConfigurationException("duplicated format id: "
						+ data.getNameSpaces().get(fmConfig.getNameSpaceId()));
			}

			formats.put(fmConfig.getId(), fmConfig);
		}

		return formats;
	}

	/**
	 * loadFormat purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a CoverageStoreInfo object.
	 * </p>
	 * 
	 * @param fmElem
	 *            a DOM tree to convert into a CoverageStoreInfo object.
	 * 
	 * @return A complete CoverageStoreInfo object loaded from the DOM tree
	 *         provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected CoverageStoreInfoDTO loadFormat(Element fmElem)
			throws ConfigurationException {
		CoverageStoreInfoDTO fm = new CoverageStoreInfoDTO();

		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("creating a new FormatDTO configuration");
		}
		fm.setId(ReaderUtils.getAttribute(fmElem, "id", true));

		String namespacePrefix = ReaderUtils.getAttribute(fmElem, "namespace",
				true);

		if (data.getNameSpaces().containsKey(namespacePrefix)) {
			fm.setNameSpaceId(namespacePrefix);
		} else {
			String msg = "there is no namespace defined for coveragestore '"
					+ namespacePrefix + "'";
			throw new ConfigurationException(msg);
		}

		fm.setType(ReaderUtils.getChildText(fmElem, "type", true));
		fm.setUrl(ReaderUtils.getChildText(fmElem, "url", false));
		fm.setEnabled(ReaderUtils.getBooleanAttribute(fmElem, "enabled", false,
				true));
		fm.setTitle(ReaderUtils.getChildText(fmElem, "title", false));
		fm.setAbstract(ReaderUtils.getChildText(fmElem, "description", false));
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer(new StringBuffer("loading parameters for FormatDTO ")
					.append(fm.getId()).toString());
		}
		fm.setParameters(loadConnectionParams(ReaderUtils.getChildElement(
				fmElem, "parameters", false)));

		return fm;
	}

	/**
	 * loadDataStores purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a Map of DataStores.
	 * </p>
	 * 
	 * @param dsRoot
	 *            a DOM tree to convert into a Map of DataStores.
	 * 
	 * @return A complete Map of DataStores loaded from the DOM tree provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected Map loadDataStores(Element dsRoot) throws ConfigurationException {
		Map dataStores = new HashMap();

		NodeList dsElements = dsRoot.getElementsByTagName("datastore");
		int dsCnt = dsElements.getLength();
		DataStoreInfoDTO dsConfig;
		Element dsElem;

		for (int i = 0; i < dsCnt; i++) {
			dsElem = (Element) dsElements.item(i);
			dsConfig = loadDataStore(dsElem);

			if (dataStores.containsKey(dsConfig.getId())) {
				throw new ConfigurationException("duplicated datastore id: "
						+ data.getNameSpaces().get(dsConfig.getNameSpaceId()));
			}

			dataStores.put(dsConfig.getId(), dsConfig);
		}

		return dataStores;
	}

	/**
	 * loadDataStore purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a DataStoreInfo object.
	 * </p>
	 * 
	 * @param dsElem
	 *            a DOM tree to convert into a DataStoreInfo object.
	 * 
	 * @return A complete DataStoreInfo object loaded from the DOM tree
	 *         provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected DataStoreInfoDTO loadDataStore(Element dsElem)
			throws ConfigurationException {
		DataStoreInfoDTO ds = new DataStoreInfoDTO();

		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("creating a new DataStoreDTO configuration");
		}
		ds.setId(ReaderUtils.getAttribute(dsElem, "id", true));

		String namespacePrefix = ReaderUtils.getAttribute(dsElem, "namespace",
				true);

		if (data.getNameSpaces().containsKey(namespacePrefix)) {
			ds.setNameSpaceId(namespacePrefix);
		} else {
			String msg = "there is no namespace defined for datatasore '"
					+ namespacePrefix + "'";
			throw new ConfigurationException(msg);
		}

		ds.setEnabled(ReaderUtils.getBooleanAttribute(dsElem, "enabled", false,
				true));
		ds.setTitle(ReaderUtils.getChildText(dsElem, "title", false));
		ds.setAbstract(ReaderUtils.getChildText(dsElem, "description", false));
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer(new StringBuffer(
					"loading connection parameters for DataStoreDTO ").append(
					ds.getNameSpaceId()).toString());
		}
		ds.setConnectionParams(loadConnectionParams(ReaderUtils
				.getChildElement(dsElem, "connectionParams", true)));

		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config(new StringBuffer("Loaded datastore ").append(
					ds.getId()).toString());
		}
		return ds;
	}

	/**
	 * loadConnectionParams purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a Map of Strings which represent connection
	 * parameters.
	 * </p>
	 * 
	 * @param connElem
	 *            a DOM tree to convert into a Map of Strings which represent
	 *            connection parameters.
	 * 
	 * @return A complete Map of Strings which represent connection parameters
	 *         loaded from the DOM tree provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected Map loadConnectionParams(Element connElem)
			throws ConfigurationException {
		Map connectionParams = new HashMap();

		if (connElem != null) {
			NodeList paramElems = connElem.getElementsByTagName("parameter");
			int pCount = paramElems.getLength();
			Element param;
			String paramKey;
			String paramValue;

			for (int i = 0; i < pCount; i++) {
				param = (Element) paramElems.item(i);
				paramKey = ReaderUtils.getAttribute(param, "name", true);
				paramValue = ReaderUtils.getAttribute(param, "value", false);
				connectionParams
						.put(paramKey, paramValue.replaceAll("'", "\""));
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer(new StringBuffer("added parameter ").append(
							paramKey).append(": '").append(
							paramValue.replaceAll("'", "\"")).append("'")
							.toString());
				}
			}
		}

		return connectionParams;
	}

	/**
	 * Load map of FeatureTypeDTO instances from a directory.
	 * 
	 * <p>
	 * Expected directory structure:
	 * </p>
	 * 
	 * <ul>
	 * <li> rootDir/ </li>
	 * <li> rootDir/featureType1/info.xml - required </li>
	 * <li> rootDir/featureType1/schema.xml - optional </li>
	 * <li> rootDir/featureType2/info.xml - required </li>
	 * <li> rootDir/featureType2/schema.xml - optional </li>
	 * </ul>
	 * 
	 * <p>
	 * If a schema.xml file is not used, the information may be generated from a
	 * FeatureType using DataTransferObjectFactory.
	 * </p>
	 * 
	 * @param featureTypeRoot
	 *            Root FeatureType directory
	 * 
	 * @return Map of FeatureTypeInfoDTO by <code>dataStoreId:typeName</code>
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 */
	protected Map loadFeatureTypes(File featureTypeRoot)
			throws ConfigurationException {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest(new StringBuffer("examining: ").append(
					featureTypeRoot.getAbsolutePath()).toString());
			LOGGER.finest(new StringBuffer("is dir: ").append(
					featureTypeRoot.isDirectory()).toString());
		}

		if (!featureTypeRoot.isDirectory()) {
			throw new IllegalArgumentException(
					"featureTypeRoot must be a directoy");
		}

		File[] directories = featureTypeRoot.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		Map map = new HashMap();

		for (int i = 0, n = directories.length; i < n; i++) {
			File info = new File(directories[i], "info.xml");

			if (info.exists() && info.isFile()) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer(new StringBuffer("Info dir:").append(info)
							.toString());
				}

				FeatureTypeInfoDTO dto = loadFeature(info);
				String ftName = null;
				try { // Decode the URL of the FT. This is to catch colons
						// used in filenames
					ftName = URLDecoder.decode(dto.getKey(), "UTF-8");
					if (LOGGER.isLoggable(Level.INFO)) {
						LOGGER.info(new StringBuffer("Decoding file name: ")
								.append(ftName).toString());
					}
				} catch (UnsupportedEncodingException e) {
					throw new ConfigurationException(e);
				}
				map.put(ftName, dto);
			}
		}

		return map;
	}

	/**
	 * Load FeatureTypeInfoDTO from a directory.
	 * 
	 * <p>
	 * Expected directory structure:
	 * </p>
	 * 
	 * <ul>
	 * <li> info.xml - required </li>
	 * <li> schema.xml - optional </li>
	 * </ul>
	 * 
	 * <p>
	 * If a schema.xml file is not used, the information may be generated from a
	 * FeatureType using DataTransferObjectFactory.
	 * </p>
	 * 
	 * @param infoFile
	 *            a File to convert into a FeatureTypeInfo object. (info.xml)
	 * 
	 * @return A complete FeatureTypeInfo object loaded from the File handle
	 *         provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 * 
	 * @see loadFeaturePt2(Element)
	 */
	protected FeatureTypeInfoDTO loadFeature(File infoFile)
			throws ConfigurationException {
		if (!infoFile.exists()) {
			throw new IllegalArgumentException("Info File not found:"
					+ infoFile);
		}

		if (!infoFile.isFile()) {
			throw new IllegalArgumentException("Info file is the wrong type:"
					+ infoFile);
		}

		if (!isInfoFile(infoFile)) {
			throw new IllegalArgumentException("Info File not valid:"
					+ infoFile);
		}

		Element featureElem = null;

		try {
			if (LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(new StringBuffer("Loading configuration file: ")
						.append(infoFile).toString());
			}
			Reader reader = null;
			reader = new FileReader(infoFile);
			featureElem = ReaderUtils.loadConfig(reader);
			reader.close();
		} catch (FileNotFoundException fileNotFound) {
			throw new ConfigurationException("Could not read info file:"
					+ infoFile, fileNotFound);
		} catch (Exception erk) {
			throw new ConfigurationException("Could not parse info file:"
					+ infoFile, erk);
		}

		FeatureTypeInfoDTO dto = loadFeaturePt2(featureElem);

		File parentDir = infoFile.getParentFile();
		dto.setDirName(parentDir.getName());

		List attributeList;

		File schemaFile = new File(parentDir, "schema.xml");

		if (schemaFile.exists() && schemaFile.isFile()) {
			// attempt to load optional schema information
			//
			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(new StringBuffer("process schema file ").append(
						infoFile).toString());
			}

			try {
				loadSchema(schemaFile, dto);
			} catch (Exception badDog) {
				badDog.printStackTrace();
				attributeList = Collections.EMPTY_LIST;
			}
		} else {
			dto.setSchemaAttributes(Collections.EMPTY_LIST);
		}

		if (LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config(new StringBuffer("added featureType ").append(
					dto.getName()).toString());
		}

		return dto;
	}

	/**
	 * loadFeaturePt2 purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a FeatureTypeInfo object.
	 * </p>
	 * 
	 * @param fTypeRoot
	 *            a DOM tree to convert into a FeatureTypeInfo object.
	 * 
	 * @return A complete FeatureTypeInfo object loaded from the DOM tree
	 *         provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected FeatureTypeInfoDTO loadFeaturePt2(Element fTypeRoot)
			throws ConfigurationException {
		FeatureTypeInfoDTO ft = new FeatureTypeInfoDTO();

		ft.setName(ReaderUtils.getChildText(fTypeRoot, "name", true));
		ft.setTitle(ReaderUtils.getChildText(fTypeRoot, "title", true));
		ft.setAbstract(ReaderUtils.getChildText(fTypeRoot, "abstract"));
		ft
				.setWmsPath(ReaderUtils
						.getChildText(fTypeRoot, "wmspath"/* , true */));

		String keywords = ReaderUtils.getChildText(fTypeRoot, "keywords");

		if (keywords != null) {
			List l = new LinkedList();
			String[] ss = keywords.split(",");

			for (int i = 0; i < ss.length; i++)
				l.add(ss[i].trim());

			ft.setKeywords(l);
		}

		ft.setDataStoreId(ReaderUtils
				.getAttribute(fTypeRoot, "datastore", true));
		ft.setSRS(Integer.parseInt(ReaderUtils.getChildText(fTypeRoot, "SRS",
				true)));

		Element tmp = ReaderUtils.getChildElement(fTypeRoot, "styles");

		if (tmp != null) {
			ft.setDefaultStyle(ReaderUtils.getAttribute(tmp, "default", false));
		}

		Element cacheInfo = ReaderUtils.getChildElement(fTypeRoot, "cacheinfo");
		if (cacheInfo != null) {
			ft.setCacheMaxAge(ReaderUtils.getAttribute(cacheInfo, "maxage",
					false));// not mandatory
			ft.setCachingEnabled((new Boolean(ReaderUtils.getAttribute(
					cacheInfo, "enabled", true))).booleanValue());
		}

		// Modif C. Kolbowicz - 06/10/2004
		Element legendURL = ReaderUtils.getChildElement(fTypeRoot, "LegendURL");

		if (legendURL != null) {
			LegendURLDTO legend = new LegendURLDTO();
			legend.setWidth(Integer.parseInt(ReaderUtils.getAttribute(
					legendURL, "width", true)));
			legend.setHeight(Integer.parseInt(ReaderUtils.getAttribute(
					legendURL, "height", true)));
			legend.setFormat(ReaderUtils
					.getChildText(legendURL, "Format", true));
			legend.setOnlineResource(ReaderUtils.getAttribute(ReaderUtils
					.getChildElement(legendURL, "OnlineResource", true),
					"xlink:href", true));
			ft.setLegendURL(legend);
		}

		// -- Modif C. Kolbowicz - 06/10/2004
		ft.setLatLongBBox(loadLatLongBBox(ReaderUtils.getChildElement(
				fTypeRoot, "latLonBoundingBox")));

		Element numDecimalsElem = ReaderUtils.getChildElement(fTypeRoot,
				"numDecimals", false);

		if (numDecimalsElem != null) {
			ft.setNumDecimals(ReaderUtils.getIntAttribute(numDecimalsElem,
					"value", false, 8));
		}

		ft.setDefinitionQuery(loadDefinitionQuery(fTypeRoot));

		return ft;
	}

	/**
	 * This method loads all the coverages present under the geoserver data
	 * directory in a Map by using their respective DTOs.
	 * 
	 * @see XMLConfigReader#loadCoverage(File)
	 * @param coverageRoot
	 * @return
	 * @throws ConfigurationException
	 */
	protected Map loadCoverages(File coverageRoot)
			throws ConfigurationException {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.finest(new StringBuffer("examining: ").append(
					coverageRoot.getAbsolutePath()).toString());
			LOGGER.finest(new StringBuffer("is dir: ").append(
					coverageRoot.isDirectory()).toString());
		}

		if (!coverageRoot.isDirectory()) {
			throw new IllegalArgumentException(
					"coverageRoot must be a directoy");
		}

		File[] directories = coverageRoot.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		Map map = new HashMap();
		File info;
		CoverageInfoDTO dto;
		final int numDirectories = directories.length;
		for (int i = 0, n = numDirectories; i < n; i++) {
			info = new File(directories[i], "info.xml");

			if (info.exists() && info.isFile()) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer(new StringBuffer("Info dir:").append(info)
							.toString());
				}

				dto = loadCoverage(info);
				map.put(dto.getKey(), dto);
			}
		}

		return map;
	}

	/**
	 * This method loads the coverage information DTO from an info.xml file on
	 * the disk.
	 * 
	 * @param infoFile
	 * @return
	 * @throws ConfigurationException
	 */
	protected CoverageInfoDTO loadCoverage(File infoFile)
			throws ConfigurationException {
		if (!infoFile.exists()) {
			throw new IllegalArgumentException("Info File not found:"
					+ infoFile);
		}

		if (!infoFile.isFile()) {
			throw new IllegalArgumentException("Info file is the wrong type:"
					+ infoFile);
		}

		if (!isInfoFile(infoFile)) {
			throw new IllegalArgumentException("Info File not valid:"
					+ infoFile);
		}

		Element coverageElem = null;

		try {
			Reader reader = null;
			reader = new FileReader(infoFile);
			coverageElem = ReaderUtils.loadConfig(reader);
			reader.close();
		} catch (FileNotFoundException fileNotFound) {
			throw new ConfigurationException("Could not read info file:"
					+ infoFile, fileNotFound);
		} catch (Exception erk) {
			throw new ConfigurationException("Could not parse info file:"
					+ infoFile, erk);
		}

		// loding the DTO.
		CoverageInfoDTO dto = loadCoverageDTOFromXML(coverageElem);

		File parentDir = infoFile.getParentFile();
		dto.setDirName(parentDir.getName());

		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer(new StringBuffer("added coverageType ").append(
					dto.getName()).toString());
		}

		return dto;
	}

	/**
	 * Creation of a DTo cfron an info.xml file for a coverage.
	 * 
	 * @param coverageRoot
	 * @return
	 * @throws ConfigurationException
	 */
	protected CoverageInfoDTO loadCoverageDTOFromXML(Element coverageRoot)
			throws ConfigurationException {

		int length = 0;
		List l = null;
		int i = 0;
		String[] ss = null;
		// /////////////////////////////////////////////////////////////////////
		//
		// COVERAGEINFO DTO INITIALIZATION
		//
		// /////////////////////////////////////////////////////////////////////
		final CoverageInfoDTO cv = new CoverageInfoDTO();
		cv.setFormatId(ReaderUtils.getAttribute(coverageRoot, "format", true));
		cv.setName(ReaderUtils.getChildText(coverageRoot, "name", true));
		cv.setWmsPath(ReaderUtils
				.getChildText(coverageRoot, "wmspath"/* , true */));
		cv.setLabel(ReaderUtils.getChildText(coverageRoot, "label", true));
		cv
				.setDescription(ReaderUtils.getChildText(coverageRoot,
						"description"));

		// /////////////////////////////////////////////////////////////////////
		//
		// METADATA AND KEYORDS
		//
		// /////////////////////////////////////////////////////////////////////
		final String keywords = ReaderUtils.getChildText(coverageRoot,
				"keywords");
		if (keywords != null) {
			l = new ArrayList(10);
			ss = keywords.split(",");
			length = ss.length;
			for (i = 0; i < length; i++)
				l.add(ss[i].trim());

			cv.setKeywords(l);
		}

		cv.setMetadataLink(loadMetaDataLink(ReaderUtils.getChildElement(
				coverageRoot, "metadataLink")));
		// /////////////////////////////////////////////////////////////////////
		//
		// DEAFULT STYLE
		//
		// /////////////////////////////////////////////////////////////////////
		final Element tmp = ReaderUtils.getChildElement(coverageRoot, "styles");

		if (tmp != null) {
			cv.setDefaultStyle(ReaderUtils.getAttribute(tmp, "default", false));
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// CRS
		//
		// /////////////////////////////////////////////////////////////////////
		final Element envelope = ReaderUtils.getChildElement(coverageRoot,
				"envelope");
		cv.setSrsName(ReaderUtils.getAttribute(envelope, "srsName", true));
		final CoordinateReferenceSystem crs;
		try {
			crs = CRS.parseWKT(ReaderUtils
					.getAttribute(envelope, "crs", false).replaceAll("'", "\""));
		} catch (FactoryException e) {
			throw new ConfigurationException(e);
		} catch (ConfigurationException e) {
			throw new ConfigurationException(e);
		}
		cv.setCrs(crs);
		cv.setSrsWKT(crs.toWKT());
		
		
		
		// /////////////////////////////////////////////////////////////////////
		//
		// ENVELOPE
		//
		// /////////////////////////////////////////////////////////////////////
		GeneralEnvelope gcEnvelope = loadEnvelope(envelope, crs);
		cv.setEnvelope(gcEnvelope);
		try {
			cv.setLonLatWGS84Envelope(CoverageStoreUtils
					.getWGS84LonLatEnvelope(gcEnvelope));
		} catch (MismatchedDimensionException e) {
			throw new ConfigurationException(e);
		} catch (IndexOutOfBoundsException e) {
			throw new ConfigurationException(e);
		} catch (NoSuchAuthorityCodeException e) {
			throw new ConfigurationException(e);
		} catch (FactoryException e) {
			throw new ConfigurationException(e);
		} catch (TransformException e) {
			throw new ConfigurationException(e);
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// GRID GEOMETRY
		//
		// /////////////////////////////////////////////////////////////////////
		final Element grid = ReaderUtils.getChildElement(coverageRoot, "grid");
		cv.setGrid(loadGrid(grid, gcEnvelope, crs));

		// /////////////////////////////////////////////////////////////////////
		//
		// SAMPLE DIMENSIONS
		//
		// /////////////////////////////////////////////////////////////////////
		cv.setDimensionNames(loadDimensionNames(grid));
		final NodeList dims = coverageRoot
				.getElementsByTagName("CoverageDimension");
		cv.setDimensions(loadDimensions(dims));

		// /////////////////////////////////////////////////////////////////////
		//
		// SUPPORTED/REQUEST CRS
		//
		// /////////////////////////////////////////////////////////////////////
		final Element supportedCRSs = ReaderUtils.getChildElement(coverageRoot,
				"supportedCRSs");
		final String requestCRSs = ReaderUtils.getChildText(supportedCRSs,
				"requestCRSs");
		if (requestCRSs != null) {
			l = new LinkedList();
			ss = requestCRSs.split(",");

			length = ss.length;
			for (i = 0; i < length; i++)
				l.add(ss[i].trim());

			cv.setRequestCRSs(l);
		}
		final String responseCRSs = ReaderUtils.getChildText(supportedCRSs,
				"responseCRSs");
		if (responseCRSs != null) {
			l = new LinkedList();
			ss = responseCRSs.split(",");
			length = ss.length;
			for (i = 0; i < length; i++)
				l.add(ss[i].trim());

			cv.setResponseCRSs(l);
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// SUPPORTED FORMATS
		//
		// /////////////////////////////////////////////////////////////////////
		final Element supportedFormats = ReaderUtils.getChildElement(
				coverageRoot, "supportedFormats");
		cv.setNativeFormat(ReaderUtils.getAttribute(supportedFormats,
				"nativeFormat", true));
		final String formats = ReaderUtils.getChildText(supportedFormats,
				"formats");
		if (formats != null) {
			l = new LinkedList();
			ss = formats.split(",");
			length = ss.length;
			for (i = 0; i < length; i++)
				l.add(ss[i].trim());

			cv.setSupportedFormats(l);
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// SUPPORTED INTERPOLATIONS
		//
		// /////////////////////////////////////////////////////////////////////
		final Element supportedInterpolations = ReaderUtils.getChildElement(
				coverageRoot, "supportedInterpolations");
		cv.setDefaultInterpolationMethod(ReaderUtils.getAttribute(
				supportedInterpolations, "default", true));
		final String interpolations = ReaderUtils.getChildText(
				supportedInterpolations, "interpolationMethods");
		if (interpolations != null) {
			l = new LinkedList();
			ss = interpolations.split(",");
			length = ss.length;
			for (i = 0; i < length; i++)
				l.add(ss[i].trim());

			cv.setInterpolationMethods(l);
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// READ PARAMETERS
		//
		// /////////////////////////////////////////////////////////////////////
		cv.setParameters(loadConnectionParams(ReaderUtils.getChildElement(
				coverageRoot, "parameters", false)));

		return cv;
	}

	/**
	 * Loading the envelope for this coverage from the info.xml file.
	 * 
	 * @todo Remve usage of JTS and use GeneralEnvelope instead.
	 * @param envelopeElem
	 * @return
	 * @throws ConfigurationException
	 */
	protected GeneralEnvelope loadEnvelope(Element envelopeElem,
			CoordinateReferenceSystem crs) throws ConfigurationException {
		if (envelopeElem == null) {
			return new GeneralEnvelope(crs);
		}

		final NodeList positions = envelopeElem.getElementsByTagName("pos");
		final int numCoordinates = positions.getLength();
		final Coordinate[] coords = new Coordinate[numCoordinates];
		String values;
		double[] dd;
		for (int i = 0; i < numCoordinates; i++) {
			values = ReaderUtils.getElementText((Element) positions.item(i));

			if (values != null) {
				String[] ss = values.split(" ");
				final int length = ss.length;
				dd = new double[length];

				for (int j = 0; j < length; j++)
					dd[j] = Double.parseDouble(ss[j].trim());

				coords[i] = new Coordinate(dd[0], dd[1]);
			}
		}

		GeneralEnvelope envelope = new GeneralEnvelope(new double[] {
				coords[0].x, coords[0].y }, new double[] { coords[1].x,
				coords[1].y });
		envelope.setCoordinateReferenceSystem(crs);

		return envelope;
	}

	/**
	 * This method is in charge for loading the grid geometry for this
	 * coverage's info.xml file.
	 * 
	 * @param gridElem
	 * @param envelope
	 * @param crs
	 * @return
	 * @throws ConfigurationException
	 */
	protected GridGeometry loadGrid(Element gridElem, GeneralEnvelope envelope,
			CoordinateReferenceSystem crs) throws ConfigurationException {

		final GeneralEnvelope gcEnvelope = new GeneralEnvelope(
				new GeneralDirectPosition(envelope.getLowerCorner()
						.getOrdinate(0), envelope.getLowerCorner().getOrdinate(
						1)), new GeneralDirectPosition(envelope
						.getUpperCorner().getOrdinate(0), envelope
						.getUpperCorner().getOrdinate(1)));

		gcEnvelope.setCoordinateReferenceSystem(crs);

		if (gridElem == null) {
			// new grid range
			GeneralGridRange newGridrange = new GeneralGridRange(new int[] { 0,
					0 }, new int[] { 1, 1 });
			GridGeometry2D newGridGeometry = new GridGeometry2D(newGridrange,
					gcEnvelope);

			return newGridGeometry;
		}

		NodeList low = gridElem.getElementsByTagName("low");
		NodeList high = gridElem.getElementsByTagName("high");
		int[] lowers = null, upers = null;

		for (int i = 0; i < low.getLength(); i++) {
			String values = ReaderUtils.getElementText((Element) low.item(i));

			if (values != null) {
				String[] ss = values.split(" ");
				lowers = new int[ss.length];

				for (int j = 0; j < ss.length; j++)
					lowers[j] = Integer.parseInt(ss[j].trim());
			}
		}

		for (int i = 0; i < high.getLength(); i++) {
			String values = ReaderUtils.getElementText((Element) high.item(i));

			if (values != null) {
				String[] ss = values.split(" ");
				upers = new int[ss.length];

				for (int j = 0; j < ss.length; j++)
					upers[j] = Integer.parseInt(ss[j].trim());
			}
		}

		// new grid range
		GeneralGridRange newGridrange = new GeneralGridRange(lowers, upers);
		GridGeometry2D newGridGeometry = new GridGeometry2D(newGridrange,
				gcEnvelope);

		return newGridGeometry;
	}

	/**
	 * 
	 * @param gridElem
	 * @return
	 * @throws ConfigurationException
	 */
	protected InternationalString[] loadDimensionNames(Element gridElem)
			throws ConfigurationException {
		if (gridElem == null) {
			return null;
		}

		NodeList axisNames = gridElem.getElementsByTagName("axisName");
		InternationalString[] dimNames = new InternationalString[axisNames
				.getLength()];

		for (int i = 0; i < axisNames.getLength(); i++) {
			String values = ReaderUtils.getElementText((Element) axisNames
					.item(i));

			if (values != null) {
				dimNames[i] = NameFactory.create(values)
						.toInternationalString();
			}
		}

		return dimNames;
	}

	protected CoverageDimension[] loadDimensions(NodeList dimElems)
			throws ConfigurationException {
		CoverageDimension[] dimensions = null;
		if (dimElems != null && dimElems.getLength() > 0) {
			dimensions = new CoverageDimension[dimElems.getLength()];
			for (int dim = 0; dim < dimElems.getLength(); dim++) {
				dimensions[dim] = new CoverageDimension();
				dimensions[dim]
						.setName(ReaderUtils
								.getElementText((Element) ((Element) dimElems
										.item(dim))
										.getElementsByTagName("name").item(0)));
				dimensions[dim]
						.setDescription(ReaderUtils
								.getElementText((Element) ((Element) dimElems
										.item(dim)).getElementsByTagName(
										"description").item(0)));
				NodeList interval = ((Element) dimElems.item(dim))
						.getElementsByTagName("interval");
				double min = Double.parseDouble(ReaderUtils
						.getElementText((Element) ((Element) interval.item(0))
								.getElementsByTagName("min").item(0)));
				double max = Double.parseDouble(ReaderUtils
						.getElementText((Element) ((Element) interval.item(0))
								.getElementsByTagName("max").item(0)));
				dimensions[dim].setRange(new NumberRange(min, max));
				NodeList nullValues = ((Element) dimElems.item(dim))
						.getElementsByTagName("nullValues");
				if (nullValues != null && nullValues.getLength() > 0) {
					NodeList values = ((Element) nullValues.item(0))
							.getElementsByTagName("value");
					if (values != null) {
						Vector nulls = new Vector();
						for (int nl = 0; nl < values.getLength(); nl++) {
							nulls
									.add(new Double(ReaderUtils
											.getElementText((Element) values
													.item(nl))));
						}
						dimensions[dim].setNullValues((Double[]) nulls
								.toArray(new Double[nulls.size()]));
					}
				}
			}
		}

		return dimensions;
	}

	protected MetaDataLink loadMetaDataLink(Element metalinkRoot) {
		MetaDataLink ml = new MetaDataLink();
		try {
			ml.setAbout(ReaderUtils.getAttribute(metalinkRoot, "about", false));
			ml.setType(ReaderUtils.getAttribute(metalinkRoot, "type", false));
			ml.setMetadataType(ReaderUtils.getAttribute(metalinkRoot,
					"metadataType", false));
			ml.setContent(ReaderUtils.getElementText(metalinkRoot));
		} catch (ConfigurationException e) {
			ml = null;
		}

		return ml;
	}

	/**
	 * getKeyWords purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a List of Strings representing keywords.
	 * </p>
	 * 
	 * @param keywordsElem
	 *            a DOM tree to convert into a List of Strings representing
	 *            keywords.
	 * 
	 * @return A complete List of Strings representing keywords loaded from the
	 *         DOM tree provided.
	 */
	protected List getKeyWords(Element keywordsElem) {
		NodeList klist = keywordsElem.getElementsByTagName("keyword");
		int kCount = klist.getLength();
		List keywords = new LinkedList();
		String kword;
		Element kelem;

		for (int i = 0; i < kCount; i++) {
			kelem = (Element) klist.item(i);
			kword = ReaderUtils.getElementText(kelem);

			if (kword != null) {
				keywords.add(kword);
			}
		}

		return keywords;
	}

	/**
	 * loadLatLongBBox purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a Envelope object.
	 * </p>
	 * 
	 * @param bboxElem
	 *            a DOM tree to convert into a Envelope object.
	 * 
	 * @return A complete Envelope object loaded from the DOM tree provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected Envelope loadLatLongBBox(Element bboxElem)
			throws ConfigurationException {
		if (bboxElem == null) {
			return new Envelope();
		}

		boolean dynamic = ReaderUtils.getBooleanAttribute(bboxElem, "dynamic",
				false, true);

		if (!dynamic) {
			double minx = ReaderUtils
					.getDoubleAttribute(bboxElem, "minx", true);
			double miny = ReaderUtils
					.getDoubleAttribute(bboxElem, "miny", true);
			double maxx = ReaderUtils
					.getDoubleAttribute(bboxElem, "maxx", true);
			double maxy = ReaderUtils
					.getDoubleAttribute(bboxElem, "maxy", true);

			return new Envelope(minx, maxx, miny, maxy);
		}

		return new Envelope();
	}

	/**
	 * loadDefinitionQuery purpose.
	 * 
	 * <p>
	 * Converts a DOM tree into a Filter object.
	 * </p>
	 * 
	 * @param typeRoot
	 *            a DOM tree to convert into a Filter object.
	 * 
	 * @return A complete Filter object loaded from the DOM tree provided.
	 * 
	 * @throws ConfigurationException
	 *             When an error occurs.
	 */
	protected org.geotools.filter.Filter loadDefinitionQuery(Element typeRoot)
			throws ConfigurationException {
		Element defQNode = ReaderUtils.getChildElement(typeRoot,
				"definitionQuery", false);
		org.geotools.filter.Filter filter = null;

		if (defQNode != null) {
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER
						.finer("definitionQuery element found, looking for Filter");
			}

			Element filterNode = ReaderUtils.getChildElement(defQNode,
					"Filter", false);

			if ((filterNode != null)
					&& ((filterNode = ReaderUtils
							.getFirstChildElement(filterNode)) != null)) {
				filter = FilterDOMParser.parseFilter(filterNode);

				return filter;
			}

			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.finer("No Filter definition query found");
			}
		}

		return filter;
	}

	/**
	 * isInfoFile purpose.
	 * 
	 * <p>
	 * Used to perform safety checks on info.xml file handles.
	 * </p>
	 * 
	 * @param testFile
	 *            The file to test.
	 * 
	 * @return true if the file is an info.xml file.
	 */
	protected static boolean isInfoFile(File testFile) {
		String testName = testFile.getAbsolutePath();

		int start = testName.length() - "info.xml".length();
		int end = testName.length();

		return testName.substring(start, end).equals("info.xml");
	}

	/**
	 * Process schema File for a list of AttributeTypeInfoDTO.
	 * 
	 * <p>
	 * The provided FeatureTypeInfoDTO will be updated with the schemaBase.
	 * </p>
	 * 
	 * @param schemaFile
	 *            File containing schema definition
	 * @param dto
	 *            Schema DOM element
	 * 
	 * @throws ConfigurationException
	 */
	protected void loadSchema(File schemaFile, FeatureTypeInfoDTO dto)
			throws ConfigurationException {
		schemaFile = ReaderUtils.checkFile(schemaFile, false);

		Element elem = null;
		dto.setSchemaFile(schemaFile);

		if ((schemaFile == null)
				|| (!schemaFile.exists() || !schemaFile.canRead())) {
			System.err.println("File does not exist for schema for "
					+ dto.getName());

			return;
		}

		try {
			if (LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config(new StringBuffer("Loading configuration file: ")
						.append(schemaFile).toString());
			}
			Reader reader;
			reader = new FileReader(schemaFile);
			elem = ReaderUtils.loadConfig(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			if (LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.log(Level.FINEST, e.getMessage(), e);
			}
			throw new ConfigurationException("Could not open schema file:"
					+ schemaFile, e);
		} catch (Exception erk) {
			throw new ConfigurationException("Could not parse schema file:"
					+ schemaFile, erk);
		}

		try {
			processSchema(elem, dto);
		} catch (ConfigurationException e) {
			throw new ConfigurationException("Error occured in " + schemaFile
					+ "\n" + e.getMessage(), e);
		}
	}

	/**
	 * Process schema DOM for a list of AttributeTypeInfoDTO.
	 * 
	 * <p>
	 * The provided FeatureTypeInfoDTO will be updated with the schemaBase.
	 * </p>
	 * 
	 * @param elem
	 *            Schema DOM element
	 * @param featureTypeInfoDTO
	 * 
	 * @throws ConfigurationException
	 */
	public static void processSchema(Element elem,
			FeatureTypeInfoDTO featureTypeInfoDTO)
			throws ConfigurationException {
		ArrayList list = new ArrayList();

		featureTypeInfoDTO.setSchemaName(ReaderUtils.getAttribute(elem, "name",
				true));

		elem = ReaderUtils.getChildElement(elem, "xs:complexContent");
		elem = ReaderUtils.getChildElement(elem, "xs:extension");

		NameSpaceTranslator gml = NameSpaceTranslatorFactory.getInstance()
				.getNameSpaceTranslator("gml");
		NameSpaceElement nse = gml.getElement(ReaderUtils.getAttribute(elem,
				"base", true));
		featureTypeInfoDTO.setSchemaBase(nse.getTypeDefName());
		elem = ReaderUtils.getChildElement(elem, "xs:sequence");

		NodeList nl = elem.getElementsByTagName("xs:element");

		for (int i = 0; i < nl.getLength(); i++) {
			// one element now
			elem = (Element) nl.item(i);

			AttributeTypeInfoDTO ati = new AttributeTypeInfoDTO();
			String name = ReaderUtils.getAttribute(elem, "name", false);
			String ref = ReaderUtils.getAttribute(elem, "ref", false);
			String type = ReaderUtils.getAttribute(elem, "type", false);

			NameSpaceTranslator nst1 = NameSpaceTranslatorFactory.getInstance()
					.getNameSpaceTranslator("xs");
			NameSpaceTranslator nst2 = NameSpaceTranslatorFactory.getInstance()
					.getNameSpaceTranslator("gml");

			if ((ref != null) && (ref != "")) {
				ati.setComplex(false);
				nse = nst1.getElement(ref);

				if (nse == null) {
					nse = nst2.getElement(ref);
				}

				String tmp = nse.getTypeRefName();

				// tmp = Character.toLowerCase(tmp.charAt(0)) +
				ati.setType(tmp);
				ati.setName(tmp);
			} else {
				ati.setName(name);

				if ((type != null) && (type != "")) {
					nse = nst1.getElement(type);

					if (nse == null) {
						nse = nst2.getElement(type);
					}

					String tmp = nse.getTypeRefName();

					ati.setType(tmp);
					ati.setComplex(false);
				} else {
					Element tmp = ReaderUtils.getFirstChildElement(elem);
					OutputFormat format = new OutputFormat(tmp
							.getOwnerDocument());
					format.setLineSeparator(LineSeparator.Windows);
					format.setIndenting(true);
					format.setLineWidth(0);
					format.setPreserveSpace(true);

					StringWriter sw = new StringWriter();
					XMLSerializer serializer = new XMLSerializer(sw, format);

					try {
						serializer.asDOMSerializer();
						serializer.serialize(tmp);
					} catch (IOException e) {
						throw new ConfigurationException(e);
					}

					ati.setType(elem.toString());
					ati.setComplex(true);
				}
			}

			ati.setNillable(ReaderUtils.getBooleanAttribute(elem, "nillable",
					false, true));
			ati.setMaxOccurs(ReaderUtils.getIntAttribute(elem, "maxOccurs",
					false, 1));
			ati.setMinOccurs(ReaderUtils.getIntAttribute(elem, "minOccurs",
					false, 1));
			list.add(ati);
		}

		featureTypeInfoDTO.setSchemaAttributes(list);
	}

	/**
	 * getData purpose.
	 * 
	 * <p>
	 * Description ...
	 * </p>
	 * 
	 * @return
	 */
	public DataDTO getData() {
		return data;
	}

	/**
	 * getGeoServer purpose.
	 * 
	 * <p>
	 * Description ...
	 * </p>
	 * 
	 * @return
	 */
	public GeoServerDTO getGeoServer() {
		return geoServer;
	}

	/**
	 * getWcs purpose.
	 * 
	 * <p>
	 * Description ...
	 * </p>
	 * 
	 * @return
	 */
	public WCSDTO getWcs() {
		return wcs;
	}

	/**
	 * getWfs purpose.
	 * 
	 * <p>
	 * Description ...
	 * </p>
	 * 
	 * @return
	 */
	public WFSDTO getWfs() {
		return wfs;
	}

	/**
	 * getWms purpose.
	 * 
	 * <p>
	 * Description ...
	 * </p>
	 * 
	 * @return
	 */
	public WMSDTO getWms() {
		return wms;
	}
}
