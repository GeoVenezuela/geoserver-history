/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.global;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.FeatureSource;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Represents a FeatureTypeInfo, its user config and autodefined information.
 *
 * @author Gabriel Rold�n
 * @author Chris Holmes
 * @author dzwiers
 * @version $Id: FeatureTypeInfo.java,v 1.1.2.8 2004/01/09 02:36:13 jive Exp $
 */
public class FeatureTypeInfo extends GlobalLayerSupertype {
    /** Default constant */
    private static final int DEFAULT_NUM_DECIMALS = 8;

	/** The DTO instane which hold this instance's data */
	private FeatureTypeInfoDTO ftc;
	
	/** ref to parent set of datastores. */
	private Data data;
	
	/**
	 * FeatureTypeInfo constructor.
	 * <p>
	 * Generates a new object from the data provided.
	 * </p>
	 * @param config FeatureTypeInfoDTO The data to populate this class with.
	 * @param data Data a reference for future use to get at DataStoreInfo instances
	 * @throws ConfigurationException
	 */
    public FeatureTypeInfo(FeatureTypeInfoDTO config, Data data)throws ConfigurationException{
    	ftc = config;
    	this.data = data;
    }

	/**
	 * toDTO purpose.
	 * <p>
	 * This method is package visible only, and returns a reference to the GeoServerDTO. This method is unsafe, and should only be used with extreme caution.
	 * </p>
	 * @return FeatureTypeInfoDTO the generated object
	 */
	Object toDTO(){
		return ftc;
	}
	
	/**
	 * getNumDecimals purpose.
	 * <p>
	 * The default number of decimals allowed in the data.
	 * </p>
	 * @return int the default number of decimals allowed in the data.
	 */
    public int getNumDecimals() {
        return ftc.getNumDecimals();
    }

    /**
     * getSchema purpose.
     * <p>
     * Generates a real FeatureType and returns it!
     * Access geotools2 FeatureType
     * </p>
     * @return FeatureType
     */
    public FeatureType getSchema() {
    	try{
        	return getSchema(ftc.getSchema());
    	}catch(Exception e){
    		return null;
    	}
    }

    /**
     * getDataStore purpose.
     * <p>
	 * gets the string of the path to the schema file.  This is set during
     * feature reading, the schema file should be in the same folder as the
     * feature type info, with the name schema.xml.  This function does not
     * guarantee that the schema file actually exists, it just gives the
     * location where it _should_ be located.
     * </p>
     * @return DataStoreInfo the requested DataStoreInfo if it was found.
     * @see Data#getDataStoreInfo(String)
     */
    public DataStoreInfo getDataStore() {
        return data.getDataStoreInfo(ftc.getDataStoreId());
    }

    /**
     * Indicates if this FeatureTypeInfo is enabled.  For now just gets whether the
     * backing datastore is enabled.
     *
     * @return <tt>true</tt> if this FeatureTypeInfo is enabled.
     *
     * @task REVISIT: Consider adding more fine grained control to config
     *       files, so users can indicate specifically if they want the
     *       featureTypes enabled, instead of just relying on if the datastore
     *       is.
     */
    public boolean isEnabled() {
        return (getDataStore() != null) && (getDataStore().isEnabled());
    }

    /**
     * getPrefix purpose.
     * <p>
     * returns the namespace prefix for this FeatureTypeInfo
     * </p>
     * @return String the namespace prefix.
     */
    public String getPrefix() {
		return getDataStore().getNameSpace().getPrefix();
    }

    /**
     * Gets the namespace for this featureType.  This isn't _really_ necessary,
     * but I'm putting it in in case we change namespaces,  letting
     * FeatureTypes set their own namespaces instead of being dependant on
     * datasources.  This method will allow us to make that change more easily
     * in the future.
     *
     * @return NameSpace the namespace specified for the specified DataStoreInfo (by ID)
     *
     * @throws IllegalStateException THrown when disabled.
     */
    public NameSpace getNameSpace() {
        if (!isEnabled()) {
            throw new IllegalStateException("This featureType is not "
                + "enabled");
        }

        return getDataStore().getNameSpace();
    }

    /**
     * overrides getName to return full type name with namespace prefix
     *
     * @return String the FeatureTypeInfo name - should be unique for the parent Data instance.
     */
    public String getName() {
        return ":"+ftc.getName();
    }

    /**
     * Convenience method for those who just want to report the name of the
     * featureType instead of requiring the full name for look up.  If
     * allowShort is true then just the localName, with no prefix, will be
     * returned if the dataStore is not enabled.  If allow short is false then
     * a full getName will be returned, with potentially bad results.
     *
     * @param allowShort does nothing
     *
     * @return String getName()
     * @see getName()
     */
    public String getName(boolean allowShort) {
        if (allowShort && (!isEnabled() || (getDataStore() == null))) {
            return getShortName();
        } else {
            return getName();
        }
    }

    /**
     * Same as getName()
     * 
     * @return String getName()
     * @see getName()
     */
    public String getShortName() {
        return ftc.getName();
    }

    /**
     * getFeatureSource purpose.
     * <p>
     * Returns a real FeatureSource.
     * </p>
     * @return FeatureSource the feature source represented by this info class
     * @throws IOException when an error occurs.
     */
    public FeatureSource getFeatureSource() throws IOException {
        if (!isEnabled() || (getDataStore().getDataStore() == null)) {
            throw new IOException("featureType: " + getName(true)
                + " does not have a properly configured " + "datastore");
        }

        FeatureSource realSource = getRealFeatureSource();
        FeatureSource mappedSource =
            GeoServerFeatureLocking.create(realSource, getSchema(), ftc.getDefinitionQuery());

        return mappedSource;
    }

	/**
	 * getRealFeatureSource purpose.
	 * <p>
	 * Returns a real FeatureSource. Used by getFeatureSource()
	 * </p>
	 * @return FeatureSource the feature source represented by this info class
	 * @see getFeatureSource()
	 */
    private FeatureSource getRealFeatureSource()
        throws NoSuchElementException, IllegalStateException, IOException {
        FeatureSource realSource = getDataStore().getDataStore().getFeatureSource(ftc.getName());

        return realSource;
    }

    /**
     * getBoundingBox purpose.
     * <p>
     * The feature source bounds.
     * </p>
     * @return Envelope the feature source bounds.
     * @throws IOException when an error occurs
     */
    public Envelope getBoundingBox() throws IOException {
		FeatureSource source = getRealFeatureSource();
		return source.getBounds();
    }

    /**
     * getDefinitionQuery purpose.
     * <p>
     * Returns the definition query for this feature source
     * </p>
     * @return Filter the definition query
     */
    public Filter getDefinitionQuery() {
        return ftc.getDefinitionQuery();
    }

    /**
     * getLatLongBoundingBox purpose.
     * <p>
     * The feature source lat/long bounds.
     * </p>
     * @return Envelope the feature source lat/long bounds.
     * @throws IOException when an error occurs
     */
    public Envelope getLatLongBoundingBox() throws IOException {
		if(ftc.getLatLongBBox() == null)
			return getBoundingBox();
        return ftc.getLatLongBBox();
    }

    /**
     * getSRS purpose.
     * <p>
     * Proprietary identifier number
     * </p>
     * @return int the SRS number.
     */
    public String getSRS() {
        return ftc.getSRS()+"";
    }


    /**
     * creates a FeatureTypeInfo schema from the list of defined exposed
     * attributes, or the full schema if no exposed attributes were defined
     *
     * @param attsElem a parsed DOM
     *
     * @return A complete FeatureType
     *
     * @throws ConfigurationException For an invalid DOM tree
     * @throws IOException When IO fails
     *
     * @task TODO: if the default geometry attribute was not declared as
     *       exposed should we expose it anyway? I think yes.
     */
    private FeatureType getSchema(Element attsElem)
        throws ConfigurationException, IOException {
        NodeList exposedAttributes = null;
        FeatureType schema = getRealFeatureSource().getSchema();
        FeatureType filteredSchema = null;

        if (attsElem != null) {
            exposedAttributes = attsElem.getElementsByTagName("attribute");
        }

        if ((exposedAttributes == null) || (exposedAttributes.getLength() == 0)) {
            return schema;
        }

        int attCount = exposedAttributes.getLength();
        AttributeType[] attributes = new AttributeType[attCount];
        Element attElem;
        String attName;

        for (int i = 0; i < attCount; i++) {
            attElem = (Element) exposedAttributes.item(i);
            attName = getAttribute(attElem, "name", true);
            attributes[i] = schema.getAttributeType(attName);

            if (attributes[i] == null) {
                throw new ConfigurationException("the FeatureTypeConfig " + getName()
                    + " does not contains the configured attribute " + attName
                    + ". Check your catalog configuration");
            }
        }

        try {
            filteredSchema = FeatureTypeFactory.newFeatureType(attributes,
                    getName());
        } catch (SchemaException ex) {
        } catch (FactoryConfigurationError ex) {
        }

        return filteredSchema;
    }
    
    /**
     * getAttribute purpose.
     * <p>
     * XLM helper method.
     * </p>
     * @param elem The element to work on. 
     * @param attName The attribute name to find
     * @param mandatory true is an exception is be thrown when the attr is not found.
     * @return String the Attr value
     * @throws ConfigurationException thrown when an error occurs.
     */
	protected String getAttribute(Element elem, String attName,
		boolean mandatory) throws ConfigurationException {
		Attr att = elem.getAttributeNode(attName);

		String value = null;

		if (att != null) {
			value = att.getValue();
		}

		if (mandatory) {
			if (att == null) {
				throw new ConfigurationException("element "
					+ elem.getNodeName()
					+ " does not contains an attribute named " + attName);
			} else if ("".equals(value)) {
				throw new ConfigurationException("attribute " + attName
					+ "in element " + elem.getNodeName() + " is empty");
			}
		}

		return value;
	}
    
    private FeatureType getSchema(String schema) throws ConfigurationException{
    	try{
    		return getSchema(loadConfig(new StringReader(schema)));
    	}catch(IOException e){
    		throw new ConfigurationException("",e);
    	}
    }

	/**
	 * loadConfig purpose.
	 * <p>
	 * Parses the specified file into a DOM tree.
	 * </p>
	 * @param configFile The file to parse int a DOM tree.
	 * @return the resulting DOM tree
	 * @throws ConfigException
	 */
	public static Element loadConfig(Reader fis)
		throws ConfigurationException {
		try {
			InputSource in = new InputSource(fis);
			DocumentBuilderFactory dfactory = DocumentBuilderFactory
				.newInstance();
			/*set as optimizations and hacks for geoserver schema config files
			 * @HACK should make documents ALL namespace friendly, and validated. Some documents are XML fragments.
			 * @TODO change the following config for the parser and modify config files to avoid XML fragmentation.
			 */
			dfactory.setNamespaceAware(false);
			dfactory.setValidating(false);
			dfactory.setIgnoringComments(true);
			dfactory.setCoalescing(true);
			dfactory.setIgnoringElementContentWhitespace(true);

			Document serviceDoc = dfactory.newDocumentBuilder().parse(in);
			Element configElem = serviceDoc.getDocumentElement();

			return configElem;
		} catch (IOException ioe) {
			String message = "problem reading file " + "due to: "
				+ ioe.getMessage();
			LOGGER.warning(message);
			throw new ConfigurationException(message, ioe);
		} catch (ParserConfigurationException pce) {
			String message = "trouble with parser to read org.vfny.geoserver.config.org.vfny.geoserver.config.xml, make sure class"
				+ "path is correct, reading file " ;
			LOGGER.warning(message);
			throw new ConfigurationException(message, pce);
		} catch (SAXException saxe) {
			String message = "trouble parsing XML "  + ": "
				+ saxe.getMessage();
			LOGGER.warning(message);
			throw new ConfigurationException(message, saxe);
		}
	}

    /**
     * here we must make the transformation. Crhis: do you know how to do it? I
     * don't know.  Ask martin or geotools devel.  This will be better when
     * our geometries actually have their srs objects.  And I think that we
     * may need some MS Access database, not sure, but I saw some stuff about
     * that on the list.  Hopefully they'll do it all in java soon.  I'm sorta
     * tempted to just have users define for now.
     *
     * @param fromSrId 
     * @param bbox Envelope
     *
     * @return Envelope
     */
    private static Envelope getLatLongBBox(String fromSrId, Envelope bbox) {
        return bbox;
    }

	/**
	 * getAbstract purpose.
	 * <p>
	 * returns the FeatureTypeInfo abstract
	 * </p>
	 * @return String the FeatureTypeInfo abstract
	 */
	public String getAbstract() {
		return ftc.getAbstract();
	}

	/**
	 * getKeywords purpose.
	 * <p>
	 * returns the FeatureTypeInfo keywords
	 * </p>
	 * @return List the FeatureTypeInfo keywords
	 */
	public List getKeywords() {
		return ftc.getKeywords();
	}

	/**
	 * getTitle purpose.
	 * <p>
	 * returns the FeatureTypeInfo title
	 * </p>
	 * @return String the FeatureTypeInfo title
	 */
	public String getTitle() {
		return ftc.getTitle();
	}
}
