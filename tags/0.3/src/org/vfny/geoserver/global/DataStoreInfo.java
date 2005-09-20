/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.global;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.vfny.geoserver.global.dto.DataStoreInfoDTO;


/**
 * This is the configuration iformation for one DataStore. This class can also
 * generate real datastores.
 *
 * @author Gabriel Rold?n
 * @author dzwiers
 * @version $Id: DataStoreInfo.java,v 1.14 2004/06/26 19:51:24 jive Exp $
 */
public class DataStoreInfo extends GlobalLayerSupertype {

	/**
	 * DataStoreInfo we are representing
	 * 
	 * @uml.property name="dataStore"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private DataStore dataStore = null;

	/**
	 * ref to the parent class's collection
	 * 
	 * @uml.property name="data"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private Data data;

	/**
	 * 
	 * @uml.property name="id" multiplicity="(0 1)"
	 */
	private String id;

    private String nameSpaceId;
    private boolean enabled;

	/**
	 * 
	 * @uml.property name="title" multiplicity="(0 1)"
	 */
	private String title;

    private String _abstract;
    private Map connectionParams;

	/**
	 * Storage for metadata
	 * 
	 * @uml.property name="meta"
	 * @uml.associationEnd elementType="java.lang.Object" qualifier="key:java.lang.String
	 * java.lang.Object" multiplicity="(0 -1)" ordering="ordered"
	 */
	private Map meta;


    /**
     * Directory associated with this DataStore.
     * 
     * <p>
     * This directory may be used for File based relative paths.
     * </p>
     */
    File baseDir;

    /**
     * URL associated with this DataStore.
     * 
     * <p>
     * This directory may be used for URL based relative paths.
     * </p>
     */
    URL baseURL;

    /**
     * DataStoreInfo constructor.
     * 
     * <p>
     * Stores the specified data for later use.
     * </p>
     *
     * @param config DataStoreInfoDTO the current configuration to use.
     * @param data Data a ref to use later to look up related informtion
     */
    public DataStoreInfo(DataStoreInfoDTO config, Data data) {
        this.data = data;
        meta = new HashMap();

        connectionParams = config.getConnectionParams();
        enabled = config.isEnabled();
        id = config.getId();
        nameSpaceId = config.getNameSpaceId();
        title = config.getTitle();
        _abstract = config.getAbstract();
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
     * @return DataStoreInfoDTO the generated object
     */
    Object toDTO() {
        DataStoreInfoDTO dto = new DataStoreInfoDTO();
        dto.setAbstract(_abstract);
        dto.setConnectionParams(connectionParams);
        dto.setEnabled(enabled);
        dto.setId(id);
        dto.setNameSpaceId(nameSpaceId);
        dto.setTitle(title);

        return dto;
    }

	/**
	 * getId purpose.
	 * 
	 * <p>
	 * Returns the dataStore's id.
	 * </p>
	 * 
	 * @return String the id.
	 * 
	 * @uml.property name="id"
	 */
	public String getId() {
		return id;
	}


    protected Map getParams() {
        Map params = new HashMap(connectionParams);
	params.put("namespace", getNameSpace().getURI());
	return getParams(params, data.getBaseDir().toString());
    }

    /**
     * Get Connect params.
     * 
     * <p>
     * This is used to smooth any relative path kind of issues for any file
     * URLS. This code should be expanded to deal with any other context
     * sensitve isses dataStores tend to have.
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @task REVISIT: cache these?
     */

    public static Map getParams(Map m, String baseDir) {
        Map params = Collections.synchronizedMap(new HashMap(m));

        for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();

            try {
                if ("url".equals(key) && value instanceof String) {
                    String path = (String) value;
		    LOGGER.finer("in string url");
                    if (path.startsWith("file:data/")) {
                        path = path.substring(5); // remove 'file:' prefix

                        File file = new File(baseDir, path);
                        entry.setValue(file.toURL().toExternalForm());
                    }
		    //Not sure about this
                } else if (value instanceof URL
                        && ((URL) value).getProtocol().equals("file")) {
		    LOGGER.finer("in URL url");
                    URL url = (URL) value;
                    String path = url.getPath();
		    LOGGER.finer("path is " + path);
		    if (path.startsWith("data/")){
			File file = new File(baseDir, path);
			entry.setValue(file.toURL());
		    }
                } /*else if ("dbtype".equals(key) && value instanceof String) {
                    String val = (String) value;

                    if ((val != null) && val.equals("postgis")) {
                        if (!params.containsKey("charset")) {
                            params.put("charset",
                                data.getGeoServer().getCharSet().toString());
                        }
                    }
		    } */
            } catch (MalformedURLException ignore) {
                // ignore attempt to fix relative paths
            }
        }

        return params;
    }

	/**
	 * By now just uses DataStoreFinder to find a new instance of a
	 * DataStoreInfo capable of process <code>connectionParams</code>. In the
	 * future we can see if it is better to cache or pool DataStores for
	 * performance, but definitely we shouldn't maintain a single
	 * DataStoreInfo as instance variable for synchronizing reassons
	 * 
	 * <p>
	 * JG: Umm we actually require a single DataStoreInfo for for locking &
	 * transaction support to work. DataStoreInfo is expected to be thread
	 * aware (that is why it has Transaction Support).
	 * </p>
	 * 
	 * @return DataStore
	 * 
	 * @throws IllegalStateException if this DataStoreInfo is disabled by
	 *         configuration
	 * @throws NoSuchElementException if no DataStoreInfo is found
	 * 
	 * @uml.property name="dataStore"
	 */
	public synchronized DataStore getDataStore()
		throws IllegalStateException,
		NoSuchElementException {
		if (!isEnabled()) {
			throw new IllegalStateException(
				"this datastore is not enabled, check your configuration");
		}

		Map m = getParams();

		if (dataStore == null) {
			try {
				dataStore = DataStoreFinder.getDataStore(m);
				LOGGER.fine("connection established by " + toString());
			} catch (Throwable ex) {
				throw new IllegalStateException("can't create the datastore "
					+ getId()
					+ ": "
					+ ex.getClass().getName()
					+ ": "
					+ ex.getMessage()
					+ "\n"
					+ ex.toString());
			}

			if (dataStore == null) {
				// If datastore is not present, then disable it
				// (although no change in config).
				enabled = false;
				LOGGER
					.fine("failed to establish connection with " + toString());
				throw new NoSuchElementException(
					"No datastore found capable of managing " + toString());
			}
		}

		return dataStore;
	}

	/**
	 * getTitle purpose.
	 * 
	 * <p>
	 * Returns the dataStore's title.
	 * </p>
	 * 
	 * @return String the title.
	 * 
	 * @uml.property name="title"
	 */
	public String getTitle() {
		return title;
	}

    /**
     * getAbstract purpose.
     * 
     * <p>
     * Returns the dataStore's abstract.
     * </p>
     *
     * @return String the abstract.
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * isEnabled purpose.
     * 
     * <p>
     * Returns true when the data store is enabled.
     * </p>
     *
     * @return true when the data store is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * getNameSpace purpose.
     * 
     * <p>
     * Returns the namespace for this datastore.
     * </p>
     *
     * @return NameSpaceInfo the namespace for this datastore.
     */
    public NameSpaceInfo getNameSpace() {
        return (NameSpaceInfo) data.getNameSpace(getNamesSpacePrefix());
    }

    /**
     * Access namespace id
     *
     * @return DOCUMENT ME!
     */
    public String getNamesSpacePrefix() {
        return nameSpaceId;
    }

    /**
     * Implement toString.
     *
     * @return String
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new StringBuffer("DataStoreConfig[namespace=").append(getNameSpace()
                                                                         .getPrefix())
                                                             .append(", enabled=")
                                                             .append(isEnabled())
                                                             .append(", abstract=")
                                                             .append(getAbstract())
                                                             .append(", connection parameters=")
                                                             .append(getParams())
                                                             .append("]")
                                                             .toString();
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
}
