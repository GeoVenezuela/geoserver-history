/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.global;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geoserver.feature.FeatureSourceUtils;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.styling.Style;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
import org.vfny.geoserver.global.dto.DataTransferObjectFactory;
import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
import org.vfny.geoserver.global.dto.LegendURLDTO;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Represents a FeatureTypeInfo, its user config and autodefined information.
 * <p>
 * This class implements {@link org.geotools.catalog.Service} interface as a
 * link to a catalog.
 * </p>
 * @author Gabriel Rold?n
 * @author Chris Holmes
 * @author dzwiers
 * @author Charles Kolbowicz
 *
 * @version $Id$
 */
public class FeatureTypeInfo extends GlobalLayerSupertype {
    /** hash table that takes a epsg# to its definition**/
    private static Hashtable SRSLookup = new Hashtable();

    /**
     * Force declared SRS
     */
    public static int FORCE = 0;
    
    /**
     * Reproject to declared SRS
     */
    public static int REPROJECT = 1;
    
    /**
     * Don't do anything, declared and actual are equal
     */
    public static int LEAVE = 2;

    /** Default constant */
    private static final int DEFAULT_NUM_DECIMALS = 8;

    /**
     * Id used to locate parent DataStoreInfo using Data Catalog.
     */
    private String dataStoreId;

    /**
     * Bounding box in Lat Long of the extent of this FeatureType.<p>Note
     * reprojection may be required to derive this value.</p>
     */
    private Envelope latLongBBox;

    /**
     * Bounding box in this FeatureType's native (or user declared) CRS.<p>Note
     * reprojection may be required to derive this value.</p>
     */
    private Envelope nativeBBox;

    /**
     * SRS number used to locate Coordidate Reference Systems
     * <p>
     * This will be used for reprojection and such like.
     * </p>
     */
    private int SRS;

    /**
     * List of AttributeTypeInfo representing the schema.xml information.
     * <p>
     * Used to define the order and manditoryness of FeatureType attributes
     * during query (re)construction.
     * </p>
     */
    private List schema;

    /** Name of elment that is an instance of schemaBase */
    private String schemaName;

    /** Base schema (usually NullType) defining manditory attribtues */
    private String schemaBase;

    /** typeName as defined by gt2 DataStore */
    private String typeName;

    /**
     *
     */
    private String wmsPath;

    /**
    * Directory where featureType is loaded from.
    *
    * This may contain metadata files.
    */
    private String dirName;

    /**
     * Abstract used to describe FeatureType
     */
    private String _abstract;

    /**
     * List of keywords for Web Register Services
     */
    private List keywords;

    /**
     * List of keywords for Web Register Services
     */
    private List metadataLinks;

    /**
     * Number of decimals used in GML output.
     */
    private int numDecimals;

    /**
     * Magic query used to limit scope of this FeatureType.
     */
    private Filter definitionQuery = null;

    /**
     * Default style used to render this FeatureType with WMS
     */
    private String defaultStyle;

    /**
     * Other WMS Styles
     */
    private ArrayList styles;

    /**
     * Title of this FeatureType as presented to End-Users.
     * <p>
     * Think of this as the display name on the off chance that typeName
     * is considered ugly.
     * </p>
     */
    private String title;

    /**
     * ref to parent set of datastores.
     * <p>
     * This backpointer to our Catalog can be used to locate our DataStore
     * using the dataStoreId.
     * </p>
     */
    private Data data;

    /**
     * MetaData used by apps to squirel information away for a rainy day.
     */
    private Map meta;

    /**
     * AttributeTypeInfo by attribute name.
     *
     * <p>
     * This will be null unless populated by schema or DTO.
     * Even if the DTO provides one this list will be lazily
     * created - so use the accessors.
     * </p>
     */
    private String xmlSchemaFrag;

    /**
     * The real geotools2 featureType cached for sanity checks.
     * <p>
     * This will be lazily created so use the accessors
     * </p>
     */
    private FeatureType ft;

    // Modif C. Kolbowicz - 07/10/2004
    /**
     * Holds value of property legendURL.
     */
    private LegendURL legendURL;

    //-- Modif C. Kolbowicz - 07/10/2004

    /** Holds the location of the file that contains schema information. */
    private File schemaFile;

    /**
     * dont use this unless you know what you're doing.  its for TemporaryFeatureTypeInfo.
     *
     */
    public FeatureTypeInfo() {
    }

    /**
     * This value is added the headers of generated maps, marking them as being both
     * "cache-able" and designating the time for which they are to remain valid.
     *  The specific header added is "Cache-Control: max-age="
     */
    private String cacheMaxAge;

    /**
     * Should we be adding the CacheControl: max-age header to outgoing maps which include this layer?
     */
    private boolean cachingEnabled;
    
    /**
     * Either force or reproject (force is the only way if native data has no native SRS)
     */
    private int srsHandling;
    
    /**
     * Maximum number of features served for this feature type in wfs requests. 0 for no limit
     */
    private int maxFeatures;
    
    /**
     * FeatureTypeInfo constructor.
     *
     * <p>
     * Generates a new object from the data provided.
     * </p>
     *
     * @param dto FeatureTypeInfoDTO The data to populate this class with.
     * @param data Data a reference for future use to get at DataStoreInfo
     *        instances
     *
     * @throws ConfigurationException
     */
    public FeatureTypeInfo(FeatureTypeInfoDTO dto, Data data)
        throws ConfigurationException {
        this.data = data;
        _abstract = dto.getAbstract();
        dataStoreId = dto.getDataStoreId();
        defaultStyle = dto.getDefaultStyle();
        styles = dto.getStyles();

        // Modif C. Kolbowicz - 07/10/2004
        if (dto.getLegendURL() != null) {
            legendURL = new LegendURL(dto.getLegendURL());
        } //-- Modif C. Kolbowicz - 07/10/2004   

        definitionQuery = dto.getDefinitionQuery();
        dirName = dto.getDirName();
        keywords = dto.getKeywords();
        metadataLinks = dto.getMetadataLinks();
        latLongBBox = dto.getLatLongBBox();
        typeName = dto.getName();
        wmsPath = dto.getWmsPath();
        numDecimals = dto.getNumDecimals();

        List tmp = dto.getSchemaAttributes();
        schema = new LinkedList();

        if ((tmp != null) && !tmp.isEmpty()) {
            Iterator i = tmp.iterator();

            while (i.hasNext())
                schema.add(new AttributeTypeInfo((AttributeTypeInfoDTO) i.next()));
        }

        schemaBase = dto.getSchemaBase();
        schemaName = dto.getSchemaName();
        schemaFile = dto.getSchemaFile();
        SRS = dto.getSRS();
        srsHandling = dto.getSRSHandling();
        nativeBBox = dto.getNativeBBox();
        title = dto.getTitle();

        cacheMaxAge = dto.getCacheMaxAge();
        cachingEnabled = dto.isCachingEnabled();
        
        maxFeatures = dto.getMaxFeatures();
    }

    /**
     * toDTO purpose.
     *
     * <p>
     * This method is package visible only, and returns a reference to the
     * GeoServerDTO. This method is unsafe, and should only be used with
     * extreme caution.
     * </p>
     *
     * @return FeatureTypeInfoDTO the generated object
     */
    public Object toDTO() {
        FeatureTypeInfoDTO dto = new FeatureTypeInfoDTO();
        dto.setAbstract(_abstract);
        dto.setDataStoreId(dataStoreId);
        dto.setDefaultStyle(defaultStyle);
        dto.setStyles(styles);

        // Modif C. Kolbowicz - 07/10/2004
        if (legendURL != null) {
            dto.setLegendURL((LegendURLDTO) legendURL.toDTO());
        } //-- Modif C. Kolbowicz - 07/10/2004

        dto.setDefinitionQuery(definitionQuery);
        dto.setDirName(dirName);
        dto.setKeywords(keywords);
        dto.setMetadataLinks(metadataLinks);
        dto.setLatLongBBox(latLongBBox);
        dto.setNativeBBox(nativeBBox);
        dto.setName(typeName);
        dto.setWmsPath(wmsPath);
        dto.setNumDecimals(numDecimals);

        List tmp = new LinkedList();
        Iterator i = schema.iterator();

        while (i.hasNext()) {
            tmp.add(((AttributeTypeInfo) i.next()).toDTO());
        }

        dto.setSchemaAttributes(tmp);
        dto.setSchemaBase(schemaBase);
        dto.setSchemaName(getSchemaName());
        dto.setSRS(SRS);
        dto.setTitle(title);

        dto.setCacheMaxAge(cacheMaxAge);
        dto.setCachingEnabled(cachingEnabled);

        return dto;
    }

    /**
     * getNumDecimals purpose.
     *
     * <p>
     * The default number of decimals allowed in the data.
     * </p>
     *
     * @return int the default number of decimals allowed in the data.
     */
    public int getNumDecimals() {
        return numDecimals;
    }

    /**
     * getDataStore purpose.
     *
     * <p>
     * gets the string of the path to the schema file.  This is set during
     * feature reading, the schema file should be in the same folder as the
     * feature type info, with the name schema.xml.  This function does not
     * guarantee that the schema file actually exists, it just gives the
     * location where it _should_ be located.
     * </p>
     *
     * @return DataStoreInfo the requested DataStoreInfo if it was found.
     *
     * @see Data#getDataStoreInfo(String)
     */
    public DataStoreInfo getDataStoreInfo() {
        return data.getDataStoreInfo(dataStoreId);
    }

    /**
     * By now just return the default style to be able to declare it in
     * WMS capabilities, but all this stuff needs to be revisited since it seems
     * currently there is no way of retrieving all the styles declared for
     * a given FeatureType.
     *
     * @return the default Style for the FeatureType
     */
    public Style getDefaultStyle() {
        return data.getStyle(defaultStyle);
    }

    public ArrayList getStyles() {
        final ArrayList realStyles = new ArrayList();
        Iterator s_IT = styles.iterator();

        while (s_IT.hasNext())
            realStyles.add(data.getStyle((String) s_IT.next()));

        return realStyles;
    }

    /**
     * Indicates if this FeatureTypeInfo is enabled.  For now just gets whether
     * the backing datastore is enabled.
     *
     * @return <tt>true</tt> if this FeatureTypeInfo is enabled.
     *
     * @task REVISIT: Consider adding more fine grained control to config
     *       files, so users can indicate specifically if they want the
     *       featureTypes enabled, instead of just relying on if the datastore
     *       is. Jody here - this should be done on a service by service basis
     *       WMS and WFS will need to decide for themselves on this one
     */
    public boolean isEnabled() {
        return (getDataStoreInfo() != null) && (getDataStoreInfo().isEnabled());
    }

    /**
     * Returns the XML prefix used for GML output of this FeatureType.
     *
     * <p>
     * Returns the namespace prefix for this FeatureTypeInfo.
     * </p>
     *
     * @return String the namespace prefix.
     */
    public String getPrefix() {
        return getDataStoreInfo().getNameSpace().getPrefix();
    }

    /**
     * Gets the namespace for this featureType.
     * <p>
     * This isn't _really_ necessary,
     * but I'm putting it in in case we change namespaces,  letting
     * FeatureTypes set their own namespaces instead of being dependant on
     * datasources.  This method will allow us to make that change more easily
     * in the future.
     *
     * @return NameSpaceInfo the namespace specified for the specified
     *         DataStoreInfo (by ID)
     *
     * @throws IllegalStateException THrown when disabled.
     */
    public NameSpaceInfo getNameSpace() {
        if (!isEnabled()) {
            throw new IllegalStateException("This featureType is not " + "enabled");
        }

        return getDataStoreInfo().getNameSpace();
    }

    /**
     * Complete xml name (namespace:element> for this FeatureType.
     *
     * This is the full type name with namespace prefix.
     *
     * @return String the FeatureTypeInfo name - should be unique for the
     *         parent Data instance.
     */
    public String getName() {
        return getPrefix() + ":" + typeName;
    }

    /**
     * getFeatureSource purpose.
     *
     * <p>
     * Returns a real FeatureSource.
     * </p>
     *
     * @return FeatureSource the feature source represented by this info class
     *
     * @throws IOException when an error occurs.
     */
    public FeatureSource getFeatureSource() throws IOException {
        return getFeatureSource(false);
    }
    
    /**
     * If this layers has been setup to reproject data, skipReproject = true will
     * disable reprojection. This method is build especially for the rendering subsystem
     * that should be able to perform a full reprojection on its own, and do generalization
     * before reprojection (thus avoid to reproject all of the original coordinates)
     */
    public FeatureSource getFeatureSource(boolean skipReproject) throws IOException {
        if (!isEnabled() || (getDataStoreInfo().getDataStore() == null)) {
            throw new IOException("featureType: " + getName()
                + " does not have a properly configured " + "datastore");
        }

        DataStore dataStore = data.getDataStoreInfo(dataStoreId).getDataStore();
        FeatureSource realSource = dataStore.getFeatureSource(typeName);
        
        // avoid reprojection if the calling code can do it better
        int localSrsHandling = srsHandling;
        if(srsHandling == REPROJECT && skipReproject)
            localSrsHandling = LEAVE;

        if (((schema == null) || schema.isEmpty())) { // &&

            //(ftc.getDefinitionQuery() == null || ftc.getDefinitionQuery().equals( Query.ALL ))){
            return realSource;
        } else {
            CoordinateReferenceSystem resultCrs = null;
            GeometryAttributeType gat = realSource.getSchema().getDefaultGeometry();
            CoordinateReferenceSystem nativeCrs = gat != null ?  gat.getCoordinateSystem() : null;
            if(localSrsHandling == LEAVE && nativeCrs != null)
                resultCrs = nativeCrs;
            else
                resultCrs = getSRS(SRS);
            
            // make sure we create the appropriate schema, with the right crs
            FeatureType schema = getFeatureType(realSource);
            try {
                if(schema.getDefaultGeometry() != null 
                        && !CRS.equalsIgnoreMetadata(resultCrs, schema.getDefaultGeometry().getCoordinateSystem()))
                    schema = FeatureTypes.transform(schema, resultCrs);
            } catch(Exception e) {
                throw new DataSourceException("Problem forcing CRS onto feature type", e);
            }
        
            
            if (!implementsInterface(realSource.getClass(),
                        "org.geotools.data.VersioningFeatureSource")) {
                return GeoServerFeatureLocking.create(realSource, schema,
                        getDefinitionQuery(), resultCrs, localSrsHandling);
            } else {
                // support versioning only if it is in the classpath, use reflection to invoke
                // methods so that we don't get a compile time dependency
                try {
                Class clazz = Class.forName(
                        "org.vfny.geoserver.global.GeoServerVersioningFeatureSource");
                Method m = clazz.getMethod("create",
                        new Class[] {
                            Class.forName("org.geotools.data.VersioningFeatureSource"),
                            FeatureType.class, Filter.class, CoordinateReferenceSystem.class, int.class
                        });

                return (FeatureSource) m.invoke(null,
                    new Object[] {
                        realSource, schema, getDefinitionQuery(),
                        resultCrs, new Integer(localSrsHandling)
                    });
                } catch (Exception e) {
                    throw new DataSourceException("Creation of a versioning wrapper failed", e);
                }

            } 
        }
    }

    /**
     * Checks if a interface is implemented by looking at implemented interfaces using reflection
     * @param realSource
     * @param string
     * @return
     */
    private boolean implementsInterface(Class clazz, String interfaceName) {
        if (clazz.getName().equals(interfaceName)) {
            return true;
        }

        final Class[] ifaces = clazz.getInterfaces();

        for (int i = 0; i < ifaces.length; i++) {
            if (ifaces[i].getName().equals(interfaceName)) {
                return true;
            } else if (implementsInterface(ifaces[i], interfaceName)) {
                return true;
            }
        }

        if (clazz.getSuperclass() == null) {
            return false;
        } else {
            return implementsInterface(clazz.getSuperclass(), interfaceName);
        }
    }

    /*public static FeatureSource reTypeSource(FeatureSource source,
       FeatureTypeInfoDTO ftc) throws SchemaException {
       AttributeType[] attributes = new AttributeType[ftc.getSchemaAttributes()
                                                         .size()];
       List attributeDefinitions = ftc.getSchemaAttributes();
       int index = 0;
       FeatureType ft = source.getSchema();
       for (int i = 0; i < attributes.length; i++) {
           AttributeTypeInfoDTO attributeDTO = (AttributeTypeInfoDTO) ftc.getSchemaAttributes()
                                                                         .get(i);
           String xpath = attributeDTO.getName();
           attributes[i] = ft.getAttributeType(xpath);
           if (attributes[i] == null) {
               throw new NullPointerException("Error finding " + xpath
                   + " specified in you schema.xml file for " + ftc.getName()
                   + "FeatureType.");
           }
       }
       FeatureType myType = FeatureTypeFactory.newFeatureType(attributes,
               ftc.getName());
       return GeoServerFeatureLocking.create(source, myType,
           ftc.getDefinitionQuery());
       }*/

    /**
     * Returns the FeatureType's envelope in its native CRS (or user
     * declared CRS, if any).
     * <p>
     * Note the Envelope is cached in order to avoid a potential
     * performance penalty every time this value is requires (for example,
     * at every GetCapabilities request)
     * </p>
     *
     * @return Envelope of the feature source bounds.
     *
     * @throws IOException when an error occurs
     */
    public Envelope getBoundingBox() throws IOException {
        CoordinateReferenceSystem declaredCRS = getDeclaredCRS();
        CoordinateReferenceSystem nativeCRS = getNativeCRS();
        if ((nativeBBox == null) || nativeBBox.isNull()) {
            CoordinateReferenceSystem crs = srsHandling == LEAVE ? nativeCRS : declaredCRS;
            nativeBBox = getBoundingBox(crs);
        }

        if (!(nativeBBox instanceof ReferencedEnvelope)) {
            CoordinateReferenceSystem crs = srsHandling == LEAVE ? nativeCRS : declaredCRS;
            nativeBBox = new ReferencedEnvelope(nativeBBox, crs);
        }
        
        if(srsHandling == REPROJECT) {
            try {
                ReferencedEnvelope re = (ReferencedEnvelope) nativeBBox;
                nativeBBox = re.transform(declaredCRS, true);
            } catch(Exception e) {
                LOGGER.warning("Issues trying to transform native CRS");
            }
        }

        return nativeBBox;
    }

    private Envelope getBoundingBox(CoordinateReferenceSystem targetCrs)
        throws IOException {
        // compute original bounding box
        DataStore dataStore = data.getDataStoreInfo(dataStoreId).getDataStore();
        FeatureSource realSource = dataStore.getFeatureSource(typeName);
        Envelope bbox = FeatureSourceUtils.getBoundingBoxEnvelope(realSource);

        // check if the original CRS is not the declared one
        GeometryAttributeType defaultGeometry = realSource.getSchema().getDefaultGeometry();
        CoordinateReferenceSystem originalCRS = defaultGeometry.getCoordinateSystem();

        try {
            if (targetCrs != null && !CRS.equalsIgnoreMetadata(originalCRS, targetCrs)) {
                MathTransform xform = CRS.findMathTransform(originalCRS, targetCrs, true);

                // bbox = JTS.transform(bbox, null, xform, 10);
                if (bbox instanceof ReferencedEnvelope) {
                    bbox = ((ReferencedEnvelope) bbox).transform(targetCrs, true, 10);
                } else {
                    bbox = new ReferencedEnvelope(JTS.transform(bbox, null, xform, 10), targetCrs);
                }
            }
        } catch (Exception e) {
            LOGGER.severe(
                "Could not turn the original envelope in one into the declared CRS for type "
                + typeName);
            LOGGER.severe("Original CRS is " + originalCRS);
            LOGGER.severe("Declared CRS is " + targetCrs);
        }

        return new ReferencedEnvelope(bbox, targetCrs);
    }

    /**
     * getDefinitionQuery purpose.
     *
     * <p>
     * Returns the definition query for this feature source
     * </p>
     *
     * @return Filter the definition query
     */
    public Filter getDefinitionQuery() {
        return definitionQuery;
    }

    /**
     * getLatLongBoundingBox purpose.
     *
     * <p>
     * The feature source lat/long bounds.
     * </p>
     *
     * @return Envelope the feature source lat/long bounds.
     *
     * @throws IOException when an error occurs
     */
    public Envelope getLatLongBoundingBox() throws IOException {
        if (latLongBBox == null) {
            latLongBBox = getBoundingBox(getSRS(4326));
        }

        return latLongBBox;
    }

    /**
     * getSRS purpose.
     *
     * <p>
     * Proprietary identifier number
     * </p>
     *
     * @return int the SRS number.
     */
    public String getSRS() {
        return SRS + "";
    }

    /**
     * Returns the declared CRS, that is, the CRS specified in the feature type
     * editor form
     */
    public CoordinateReferenceSystem getDeclaredCRS() {
        return getSRS(SRS);
    }

    public CoordinateReferenceSystem getNativeCRS() throws IOException {
        GeometryAttributeType dg = getDefaultGeometry();

        if (dg == null) {
            return null;
        }

        return dg.getCoordinateSystem();
    }

    /**
     * Returns the default geometry for this feature type
     * @return
     * @throws IOException if the layer is not properly configured
     */
    GeometryAttributeType getDefaultGeometry() throws IOException {
        if (getDataStoreInfo().getDataStore() == null) {
            throw new IOException("featureType: " + getName()
                + " does not have a properly configured " + "datastore");
        }

        DataStore dataStore = data.getDataStoreInfo(dataStoreId).getDataStore();
        FeatureSource realSource = dataStore.getFeatureSource(typeName);

        return realSource.getSchema().getDefaultGeometry();
    }

    /**
     * If true, the layer does not have a default geometry
     * @return
     * @throws IOException
     */
    public boolean isGeometryless() throws IOException {
        return getDefaultGeometry() == null;
    }

    /**
     * Get XMLSchema for this FeatureType.
     *
     * <p>
     * Note this may require connection to the real geotools2 DataStore and as
     * such is subject to IOExceptions.
     * </p>
     *
     * <p>
     * You have been warned.
     * </p>
     *
     * @return XMLFragment
     *
     * @throws IOException DOCUMENT ME!
     */

    /* public synchronized String getXMLSchema(){
       if (xmlSchemaFrag == null) {
           StringWriter sw = new StringWriter();
           try {
               FeatureTypeInfoDTO dto = getGeneratedDTO();
               XMLConfigWriter.storeFeatureSchema(dto, sw);
           } catch (ConfigurationException e) {
                   e.printStackTrace();
           } catch (IOException e) {
                   e.printStackTrace();
                   xmlSchemaFrag = null;
           }
           xmlSchemaFrag = sw.toString();
           try{
           sw.close();
           }catch(IOException e){}
       }
       return xmlSchemaFrag;
       }*/

    /**
     * Will return our delegate with all information filled out
     *
     * <p>
     * This is a hack because we cache our DTO delegate, this method combines
     * or ftc delegate with possibly generated schema information for use by
     * XMLConfigWriter among others.
     * </p>
     *
     * <p>
     * Call this method to receive a complete featureTypeInfoDTO that incldues
     * all schema information.
     * </p>
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     */
    private synchronized FeatureTypeInfoDTO getGeneratedDTO()
        throws IOException {
        return DataTransferObjectFactory.create(dataStoreId, getFeatureType());
    }

    /**
     * getAttribute purpose.
     *
     * <p>
     * XLM helper method.
     * </p>
     *
     * @param elem The element to work on.
     * @param attName The attribute name to find
     * @param mandatory true is an exception is be thrown when the attr is not
     *        found.
     *
     * @return String the Attr value
     *
     * @throws ConfigurationException thrown when an error occurs.
     */
    protected String getAttribute(Element elem, String attName, boolean mandatory)
        throws ConfigurationException {
        Attr att = elem.getAttributeNode(attName);

        String value = null;

        if (att != null) {
            value = att.getValue();
        }

        if (mandatory) {
            if (att == null) {
                throw new ConfigurationException("element " + elem.getNodeName()
                    + " does not contains an attribute named " + attName);
            } else if ("".equals(value)) {
                throw new ConfigurationException("attribute " + attName + "in element "
                    + elem.getNodeName() + " is empty");
            }
        }

        return value;
    }

    /*private FeatureType getSchema(String schema) throws ConfigurationException{
       try{
               return getSchema(loadConfig(new StringReader(schema)));
       }catch(IOException e){
               throw new ConfigurationException("",e);
       }
       }*/

    /**
     * loadConfig purpose.
     *
     * <p>
     * Parses the specified file into a DOM tree.
     * </p>
     *
     * @param fromSrId The file to parse int a DOM tree.
     * @param bbox DOCUMENT ME!
     *
     * @return the resulting DOM tree
     */

    /*public static Element loadConfig(Reader fis) throws ConfigurationException {
       try {
           InputSource in = new InputSource(fis);
           DocumentBuilderFactory dfactory = DocumentBuilderFactory
               .newInstance();
           /*set as optimizations and hacks for geoserver schema config files
     * @HACK should make documents ALL namespace friendly, and validated. Some documents are XML fragments.
     * @TODO change the following config for the parser and modify config files to avoid XML fragmentation.
     */

    /*       dfactory.setNamespaceAware(false);
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
           String message =
               "trouble with parser to read org.vfny.geoserver.config.org.vfny.geoserver.config.xml, make sure class"
               + "path is correct, reading file ";
           LOGGER.warning(message);
           throw new ConfigurationException(message, pce);
       } catch (SAXException saxe) {
           String message = "trouble parsing XML " + ": " + saxe.getMessage();
           LOGGER.warning(message);
           throw new ConfigurationException(message, saxe);
       }
       }*/

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
     * Get abstract (description) of FeatureType.
     *
     * @return Short description of FeatureType
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Keywords describing content of FeatureType.
     *
     * <p>
     * Keywords are often used by Search engines or Catalog services.
     * </p>
     *
     * @return List the FeatureTypeInfo keywords
     */
    public List getKeywords() {
        return keywords;
    }

    /**
     * Metadata links providing metadata access for FeatureTypes.
     *
     * @return List the FeatureTypeInfo metadata links
     */
    public List getMetadataLinks() {
        return metadataLinks;
    }

    /**
     * getTitle purpose.
     *
     * <p>
     * returns the FeatureTypeInfo title
     * </p>
     *
     * @return String the FeatureTypeInfo title
     */
    public String getTitle() {
        return title;
    }

    /**
     * A valid schema name for this FeatureType.
     *
     * @return schemaName if provided or typeName+"_Type"
     */
    public String getSchemaName() {
        if (schemaName == null) {
            return typeName + "_Type";
        }

        return schemaName;
    }

    /**
     * setSchemaName purpose.
     *
     * <p>
     * Description ...
     * </p>
     *
     * @param string
     */
    public void setSchemaName(String string) {
        schemaName = string;
    }

    /**
     * getSchemaName purpose.
     *
     * <p>
     * Description ...
     * </p>
     *
     * @return
     */
    public String getSchemaBase() {
        return schemaBase;
    }

    /**
     * setSchemaName purpose.
     *
     * <p>
     * Description ...
     * </p>
     *
     * @param string
     */
    public void setSchemaBase(String string) {
        schemaBase = string;
    }

    //
    // FeatureTypeMetaData Interface
    //
    /**
     * Access the name of this FeatureType.
     * <p>
     * This is the typeName as provided by the real gt2 DataStore.
     * </p>
     *
     * @return String getName()
     * @see org.geotools.data.FeatureTypeMetaData#getTypeName()
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Access real geotools2 FeatureType.
     *
     * @return Schema information.
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureTypeMetaData#getFeatureType()
     */
    public FeatureType getFeatureType() throws IOException {
        return getFeatureType(getFeatureSource());
    }

    /**
     * Fixes the data store feature type so that it has the right CRS (only in case they are missing)
     * and the requiered base attributes
     */
    private FeatureType getFeatureType(FeatureSource fs)
        throws IOException {
        if (ft == null) {
            int count = 0;
            ft = fs.getSchema();

            URI namespace = ft.getNamespace(); //DJB:: change to #getNamespace() due to API change

            String[] baseNames = DataTransferObjectFactory.getRequiredBaseAttributes(schemaBase);
            AttributeType[] attributes = new AttributeType[schema.size() + baseNames.length];

            if (attributes.length > 0) {
                int errors = 0;

                for (; count < baseNames.length; count++) {
                    attributes[count - errors] = ft.getAttributeType(baseNames[count]);

                    if (attributes[count - errors] == null) {
                        // desired base attr is not availiable
                        errors++;
                    }
                }

                if (errors != 0) {
                    //resize array;
                    AttributeType[] tmp = new AttributeType[attributes.length - errors];
                    count = count - errors;

                    for (int i = 0; i < count; i++) {
                        tmp[i] = attributes[i];
                    }

                    attributes = tmp;
                }

                for (Iterator i = schema.iterator(); i.hasNext();) {
                    AttributeTypeInfo ati = (AttributeTypeInfo) i.next();
                    String attName = ati.getName();
                    attributes[count] = ft.getAttributeType(attName);

                    // force the user specified CRS if the data has no CRS, or reproject it 
                    // if necessary
                    if (Geometry.class.isAssignableFrom(attributes[count].getType())) {
                        GeometricAttributeType old = (GeometricAttributeType) attributes[count];

                        try {
                            if (old.getCoordinateSystem() == null) {
                                attributes[count] = new GeometricAttributeType(old, getSRS(SRS));
                                srsHandling = FORCE;
                            } else if(srsHandling == REPROJECT || srsHandling == FORCE) {
                                attributes[count] = new GeometricAttributeType(old, getSRS(SRS));
                            }
                        } catch (Exception e) {
                            e.printStackTrace(); //DJB: this is okay to ignore since (a) it should never happen (b) we'll use the default one (crs=null)
                        }
                    }

                    if (attributes[count] == null) {
                        throw new IOException("the FeatureType " + getName()
                            + " does not contains the configured attribute " + attName
                            + ". Check your schema configuration");
                    }

                    count++;
                }

                try {
                    ft = FeatureTypeFactory.newFeatureType(attributes, typeName, namespace);
                } catch (SchemaException ex) {
                } catch (FactoryConfigurationError ex) {
                }
            }
        }

        return ft;
    }

    /**
     * Implement getDataStoreMetaData.
     *
     * @return
     *
     * @see org.geotools.data.FeatureTypeMetaData#getDataStoreMetaData()
     */
    public DataStoreInfo getDataStoreMetaData() {
        return data.getDataStoreInfo(dataStoreId);
    }

    /**
     * FeatureType attributes names as a List.
     *
     * <p>
     * Convience method for accessing attribute names as a Collection. You may
     * use the names for AttributeTypeMetaData lookup or with the schema for
     * XPATH queries.
     * </p>
     *
     * @return List of attribute names
     *
     * @task REVISIT: This method sucks.  It didn't do the same thing as
     *       getAttributes, which it should have.  I fixed the root problem of
     *       why attribs.size() would equal 0.  So the second half of this
     *       method should probably be eliminated, as it should never be
     *       called. But I don't want to break code right before a release -
     *       ch.
     *
     * @see org.geotools.data.FeatureTypeMetaData#getAttributeNames()
     */
    public List getAttributeNames() {
        List attribs = schema;

        if (attribs.size() != 0) {
            List list = new ArrayList(attribs.size());

            for (Iterator i = attribs.iterator(); i.hasNext();) {
                AttributeTypeInfo at = (AttributeTypeInfo) i.next();
                list.add(at.getName());
            }

            return list;
        }

        List list = new ArrayList();

        try {
            FeatureType ftype = getFeatureType();
            AttributeType[] types = ftype.getAttributeTypes();
            list = new ArrayList(types.length);

            for (int i = 0; i < types.length; i++) {
                list.add(types[i].getName());
            }
        } catch (IOException e) {
        }

        return list;
    }

    /**
     * Returns a list of the attributeTypeInfo objects that make up this
     * FeatureType.
     *
     * @return list of attributeTypeInfo objects.
     */
    public List getAttributes() {
        return schema;
    }

    /**
     * Implement AttributeTypeMetaData.
     *
     * <p>
     * Description ...
     * </p>
     *
     * @param attributeName
     *
     * @return
     *
     * @see org.geotools.data.FeatureTypeMetaData#AttributeTypeMetaData(java.lang.String)
     */
    public synchronized AttributeTypeInfo AttributeTypeMetaData(String attributeName) {
        AttributeTypeInfo info = null;

        if (schema != null) {
            for (Iterator i = schema.iterator(); i.hasNext();) {
                AttributeTypeInfoDTO dto = (AttributeTypeInfoDTO) i.next();
                info = new AttributeTypeInfo(dto);
            }

            DataStore dataStore = data.getDataStoreInfo(dataStoreId).getDataStore();

            try {
                FeatureType ftype = dataStore.getSchema(typeName);
                info.sync(ftype.getAttributeType(attributeName));
            } catch (IOException e) {
            }
        } else {
            // will need to generate from Schema
            DataStore dataStore = data.getDataStoreInfo(dataStoreId).getDataStore();

            try {
                FeatureType ftype = dataStore.getSchema(typeName);
                info = new AttributeTypeInfo(ftype.getAttributeType(attributeName));
            } catch (IOException e) {
            }
        }

        return info;
    }

    /**
     * Implement containsMetaData.
     *
     * @param key
     *
     * @return
     *
     * @see org.geotools.data.MetaData#containsMetaData(java.lang.String)
     */
    public boolean containsMetaData(String key) {
        return meta.containsKey(key);
    }

    /**
     * Implement putMetaData.
     *
     * @param key
     * @param value
     *
     * @see org.geotools.data.MetaData#putMetaData(java.lang.String,
     *      java.lang.Object)
     */
    public void putMetaData(String key, Object value) {
        meta.put(key, value);
    }

    /**
     * Implement getMetaData.
     *
     * @param key
     *
     * @return
     *
     * @see org.geotools.data.MetaData#getMetaData(java.lang.String)
     */
    public Object getMetaData(String key) {
        return meta.get(key);
    }

    /**
     * getLegendURL purpose.
     *
     * <p>
     * returns the FeatureTypeInfo legendURL
     * </p>
     *
     * @return String the FeatureTypeInfo legendURL
     */

    // Modif C. Kolbowicz - 07/10/2004
    public LegendURL getLegendURL() {
        return this.legendURL;
    }

    //-- Modif C. Kolbowicz - 07/10/2004

    /**
     * Gets the schema.xml file associated with this FeatureType.  This is set
     * during the reading of configuration, it is not persisted as an element
     * of the FeatureTypeInfoDTO, since it is just whether the schema.xml file
     * was persisted, and its location.  If there is no schema.xml file then
     * this method will return a File object with the location where the schema
     * file would be located, but the file will return false for exists().
     */
    public File getSchemaFile() {
        return this.schemaFile;
    }

    /**
     *  simple way of getting epsg #.
     *  We cache them so that we dont have to keep reading the DB or the epsg.properties file.
     *   I cannot image a system with more than a dozen CRSs in it...
     *
     * @param epsg
     * @return
     */
    private CoordinateReferenceSystem getSRS(int epsg) {
        CoordinateReferenceSystem result = (CoordinateReferenceSystem) SRSLookup.get(new Integer(
                    epsg));

        if (result == null) {
            //make and add to hash
            try {
                result = CRS.decode("EPSG:" + epsg);
                SRSLookup.put(new Integer(epsg), result);
            } catch (NoSuchAuthorityCodeException e) {
                String msg = "Error looking up SRS for EPSG: " + epsg + ":"
                    + e.getLocalizedMessage();
                LOGGER.warning(msg);
            } catch (FactoryException e) {
                String msg = "Error looking up SRS for EPSG: " + epsg + ":"
                    + e.getLocalizedMessage();
                LOGGER.warning(msg);
            }
        }

        return result;
    }

    public String getDirName() {
        return dirName;
    }

    public String getWmsPath() {
        return wmsPath;
    }

    public void setWmsPath(String wmsPath) {
        this.wmsPath = wmsPath;
    }

    /**
     * This value is added the headers of generated maps, marking them as being both
     * "cache-able" and designating the time for which they are to remain valid.
     *  The specific header added is "Cache-Control: max-age="
     * @return a string representing the number of seconds to be added to the "Cache-Control: max-age=" header
     */
    public String getCacheMaxAge() {
        return cacheMaxAge;
    }

    /**
     *
     * @param cacheMaxAge a string representing the number of seconds to be added to the "Cache-Control: max-age=" header
     */
    public void setCacheMaxAge(String cacheMaxAge) {
        this.cacheMaxAge = cacheMaxAge;
    }

    /**
     * Should we add the cache-control: max-age header to maps containing this layer?
     * @return true if we should, false if we should omit the header
     */
    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    /**
     * Sets whether we should add the cache-control: max-age header to maps containing this layer
     * @param cachingEnabled true if we should add the header, false if we should omit the header
     */
    public void setCachingEnabled(boolean cachingEnabled) {
        this.cachingEnabled = cachingEnabled;
    }
    
    /**
     * Returns the maximum number of features to be served by WFS GetFeature for this feature
     * type (or 0 for no limit)
     * @return
     */
    public int getMaxFeatures() {
        return maxFeatures;
    }
    
    public void setMaxFeatures(int maxFeatures) {
        this.maxFeatures = maxFeatures;
    }
}
