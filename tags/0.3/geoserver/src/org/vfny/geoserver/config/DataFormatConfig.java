/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.opengis.coverage.grid.*;
import org.vfny.geoserver.action.data.DataFormatUtils;
import org.vfny.geoserver.global.dto.FormatInfoDTO;


/**
 * DataFormatInfo purpose.
 * 
 * <p>
 * Used to describe a coverage format, typically one specified in the catalog.xml
 * config file.
 * </p>
 * 
 * <p></p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last modification)
 */
public class DataFormatConfig {

	/**
	 * unique datasore identifier
	 */
	private String id;

private String type;

	private String url;

	/**
	 * wether this format is enabled
	 */
	private boolean enabled = true;

	/**
	 * a short description about this format
	 */
	private String title;


    /** a short description about this format */
    private String _abstract;

	/**
	 * connection parameters to create the DataFormatInfo
	 */
	private Map parameters;

	/**
	 * Config ONLY!
	 */
	private Format factory;


    /**
     * Create a new DataFormatConfig from a dataFormatId and factoryDescription
     * 
     * <p>
     * Creates a DataFormatInfo to represent an instance with default data.
     * </p>
     *
     * @param dataFormatId Description of Format (see DataFormatUtils)
     * @param factoryDescription DOCUMENT ME!
     *
     * @see defaultSettings()
     */
    public DataFormatConfig(String dataFormatId, String factoryDescription) {
        this(dataFormatId, DataFormatUtils.aquireFactory(factoryDescription));
    }

    /** Creates a new DataFormatConfig for the provided factory (Format). */
    public DataFormatConfig(String dataFormatId, Format factory) {
    	this.factory = factory;
        id = dataFormatId;
        type = factory.getName();
        url = "file:data/coverages/";
        enabled = true;
        title = "";
        _abstract = "";
        parameters = DataFormatUtils.defaultParams(factory);
    }

    /**
     * DataFormatInfo constructor.
     * 
     * <p>
     * Creates a copy of the DataFormatInfoDTO provided. All the datastructures
     * are cloned.
     * </p>
     *
     * @param dto The format to copy.
     */
    public DataFormatConfig(FormatInfoDTO dto) {
        reset(dto);
    }

    /**
     * Called to update Config class based on DTO information
     *
     * @param dto DOCUMENT ME!
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public void reset(FormatInfoDTO dto) {
        if (dto == null) {
            throw new NullPointerException("Non null FormatInfoDTO required");
        }

        factory = DataFormatUtils.aquireFactory(dto.getParameters(), dto.getType());

        id = dto.getId();
        type = dto.getType();
        url = dto.getUrl();
        enabled = dto.isEnabled();
        _abstract = dto.getAbstract();
        parameters = new HashMap(dto.getParameters());
    }

    /**
     * Implement loadDTO.
     * 
     * <p>
     * Populates the data fields with the DataFormatInfoDTO provided.
     * </p>
     *
     * @param df the DataFormatInfoDTO to use.
     *
     * @throws NullPointerException DOCUMENT ME!
     *
     * @see org.vfny.geoserver.config.DataStructure#loadDTO(java.lang.Object)
     */
    public void update(FormatInfoDTO df) {
        if (df == null) {
            throw new NullPointerException(
                "FormatInfo Data Transfer Object required");
        }

        id = df.getId();
        type = df.getType();
        url = df.getUrl();
        enabled = df.isEnabled();
        _abstract = df.getAbstract();

        try {
            parameters = new HashMap(df.getParameters()); //clone?
        } catch (Exception e) {
        	parameters = new HashMap();
        }
    }

    /**
     * Implement toDTO.
     * 
     * <p>
     * Create a DataFormatInfoDTO from the current config object.
     * </p>
     *
     * @return The data represented as a DataFormatInfoDTO
     *
     * @see org.vfny.geoserver.config.DataStructure#toDTO()
     */
    public FormatInfoDTO toDTO() {
    	FormatInfoDTO ds = new FormatInfoDTO();
        ds.setId(id);
        ds.setType(type);
        ds.setUrl(url);
        ds.setEnabled(enabled);
        ds.setAbstract(_abstract);

        try {
            ds.setParameters(new HashMap(parameters));
        } catch (Exception e) {
            // default already created  	
        }

        return ds;
    }

    /**
     * getAbstract purpose.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @return
     */
    public String getAbstract() {
        return _abstract;
    }

	/**
	 * getConnectionParams purpose.
	 * 
	 * <p>
	 * Description ...
	 * </p>
	 * 
	 * @return
	 */
	public Map getParameters() {
		return parameters;
	}


    /**
     * isEnabled purpose.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

	/**
	 * This is the Format ID
	 * 
	 * <p>
	 * 
	 * </p>
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * getTitle purpose.
	 * 
	 * <p>
	 * Description ...
	 * </p>
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}


    /**
     * setAbstract purpose.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @param string
     */
    public void setAbstract(String string) {
        if (string != null) {
            _abstract = string;
        }
    }

	/**
	 * setConnectionParams purpose.
	 * 
	 * <p>
	 * Description ...
	 * </p>
	 * 
	 * @param map
	 */
	public void setParameters(Map map) {
		parameters = map;
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
	 * setTitle purpose.
	 * 
	 * <p>
	 * Description ...
	 * </p>
	 * 
	 * @param string
	 * 
	 * @uml.property name="title"
	 */
	public void setTitle(String string) {
		if (string != null) {
			title = string;
		}
	}


    // Access to Dyanmic Content

    /**
     * It would be nice if we did not throw this away - but life is too short
     *
     * @return Real Format generated by this DataFormatConfig
     *
     * @throws IOException If Format could not be aquired
     */
    public Format findDataFormat(ServletContext sc) throws IOException {
        return DataFormatUtils.acquireFormat(type,sc);
    }

	/**
	 * @return Returns the factory.
	 */
	public Format getFactory() {
		return factory;
	}

	/**
	 * @param factory The factory to set.
	 */
	public void setFactory(Format factory) {
		this.factory = factory;
	}

/**
 * @return Returns the type.
 */
public String getType() {
	return type;
}

/**
 * @param type The type to set.
 */
public void setType(String type) {
	this.type = type;
}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

}