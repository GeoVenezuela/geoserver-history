/* Copyright (c) 2001 - 2004 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.global.dto;

import java.util.HashMap;
import java.util.Map;


/**
 * Data Transfer Object for GeoServer DataStore information.
 * 
 * <p>
 * Used to describe a datastore, typically one specified in the catalog.xml
 * config file.
 * </p>
 * 
 * <p>
 * Data Transfer object are used to communicate between the GeoServer
 * application and its configuration and persistent layers. As such the class
 * is final - to allow for its future use as an on-the-wire message.
 * </p>
 * 
 * <prefix>Example:<code>
 * DataStoreInfoDTO dsiDto = new DataStoreInfoDTO();
 * dsiDto.setIde("myDataStore");
 * dsiDto.setEnabled(true);
 * dsiDto.setTile("My Data Store");
 * Map m = new HashMap();
 * m.put("key","param");
 * dsiDto.setConnectionParams(m);
 * </code></prefix>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @version $Id: DataStoreInfoDTO.java,v 1.1.2.4 2004/01/09 09:34:19 jive Exp $
 */
public final class DataStoreInfoDTO implements DataStructure {
    /** unique datasore identifier */
    private String id;

    /** unique namespace to refer to this datastore */
    private String nameSpaceId;

    /** true if this data store is enabled */
    private boolean enabled;

    /** The title of this data store */
    private String title;

    /** a short description about this data store */
    private String _abstract;

    /**
     * Connection parameters to create the DataStoreInfo
     * <p>
     * Limitied to Strings for both Keys and Values.
     */
    private Map connectionParams;

    /**
     * DataStoreInfo constructor.
     * 
     * <p>
     * Creates a DataStoreInfo to represent an instance with default data.
     * </p>
     *
     * @see defaultSettings()
     */
    public DataStoreInfoDTO() {
		id = "";
		nameSpaceId = "";
		enabled = false;
		title = "";
		_abstract = "";
		connectionParams = new HashMap();
    }

    /**
     * DataStoreInfo constructor.
     * 
     * <p>
     * Creates a copy of the DataStoreInfo provided. If the DataStoreInfo
     * provided  is null then default values are used. All the datastructures
     * are cloned.
     * </p>
     *
     * @param ds The datastore to copy.
     */
    public DataStoreInfoDTO(DataStoreInfoDTO dto) {
        if (dto == null) {
        	throw new NullPointerException("Non-Null DataStoreDTO is requried");
        }

        id = dto.getId();
        nameSpaceId = dto.getNameSpaceId();
        enabled = dto.isEnabled();
        _abstract = dto.getAbstract();

        connectionParams = new HashMap( dto.getConnectionParams() );
        /*
        try {
            connectionParams = CloneLibrary.clone(dto.getConnectionParams()); //clone?
        } catch (Exception e) {
            connectionParams = new HashMap();
        }
        */
    }

    /**
     * Implement clone.
     * 
     * <p>
     * creates a clone of this object
     * </p>
     *
     * @return A copy of this DataStoreInfo
     *
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new DataStoreInfoDTO(this);
    }

    /**
     * Implement equals.
     * 
     * <p>
     * recursively tests to determine if the object passed in is a copy of this
     * object.
     * </p>
     *
     * @param obj The DataStoreInfo object to test.
     *
     * @return true when the object passed is the same as this object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
    	if(obj == null || !(obj instanceof DataStoreInfoDTO))
    		return false;
        DataStoreInfoDTO ds = (DataStoreInfoDTO) obj;
        boolean r = true;
        r = r && (id == ds.getId());
        r = r && (nameSpaceId == ds.getNameSpaceId());
        r = r && (enabled == ds.isEnabled());
        r = r && (_abstract == ds.getAbstract());

        if (connectionParams != null) {
            r = r && connectionParams.equals(ds.getConnectionParams());
        } else if (ds.getConnectionParams() != null) {
            return false;
        }

        return r;
    }

	/**
	 * Implement hashCode.
	 * 
	 * @see java.lang.Object#hashCode()
	 * 
	 * @return Service hashcode or 0
	 */
	public int hashCode() {
		int r = 1;
		
		if (id != null) {
			r *= id.hashCode();
		}

		if (nameSpaceId != null) {
			r *= nameSpaceId.hashCode();
		}

		if (_abstract != null) {
			r *= _abstract.hashCode();
		}
		
		return r;
	}

    /**
     * Short description of DataStore
     * 
     * @return Short description
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Map of param:value both of which are represented as text.
     * 
     * <p>
     * The map is based on String Keys, and String values.
     * </p>
     *
     * @return Map of Params for DataStoreFactoryAPI use
     */
    public Map getConnectionParams() {
        return connectionParams;
    }

    /**
     * Value is <code>true</code> if the DataStore should be enabled. 
     *
     * @return ture if DataStore shoudl be enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Unique identifier representing this DataStore.
     * <p>
     * This value is used to refer to this DataStore by FeatureTypeInfoDTO.
     * </p>
     * @return an identifier, non null
     */
    public String getId() {
        return id;
    }

    /**
     * Namespace <code>prefix</code> for this DataStore.
     * 
     * @return <code>prefix</code> used for GML encoding
     */
    public String getNameSpaceId() {
        return nameSpaceId;
    }

    /**
     * Title for DataStore, used in error messages & configuration.
     * 
     * @return Title dor the DataStore
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the DataStore abstract.
     * 
     * @param description
     */
    public void setAbstract(String description) {
        _abstract = description;
    }

    /**
     * Provide DataStore connectin parameters. 
     * 
     * <p>
     * Map is limited to text based keys and values
     * </p>
     *
     * @param map
     */
    public void setConnectionParams(Map map) {
        if (map != null) {
            connectionParams = map;
        }
    }

    /**
     * setEnabled purpose.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @param b
     */
    public void setEnabled(boolean b) {
        enabled = b;
    }

    /**
     * setId purpose.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @param string
     */
    public void setId(String identifier) {
        id = identifier;        
    }

    /**
     * setNameSpace purpose.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @param support
     */
    public void setNameSpaceId(String prefix) {
        nameSpaceId = prefix;        
    }

    /**
     * setTitle purpose.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @param string
     */
    public void setTitle(String dataStoreTitle) {
        title = dataStoreTitle;        
    }
}
