/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.global;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.vfny.geoserver.global.dto.ServiceDTO;


/**
 * Default configuration for services. This class represents all the
 * commonalities to the WFS and WMS services.
 * 
 * <p>
 * WFS wfs = new WFS(dto); Service serv = (Service)WFS;
 * System.out.println(serv.getName());
 * </p>
 *
 * <p>
 * WCS wcs = new WCS(dto); Service serv = (Service)WCS;
 * System.out.println(serv.getName());
 * </p>

 * @author Gabriel Rold�n
 * @author Chris Holmes
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last modification)
 * @version $Id: Service.java,v 1.7 2004/02/09 23:29:42 dmzwiers Exp $
 *
 * @see WMS
 * @see WFS
 * @see WCS
 */
public abstract class Service extends GlobalLayerSupertype {

	private boolean enabled;

	/**
	 * 
	 * @uml.property name="onlineResource" multiplicity="(0 1)"
	 */
	private URL onlineResource;

	/**
	 * 
	 * @uml.property name="name" multiplicity="(0 1)"
	 */
	private String name;

	/**
	 * 
	 * @uml.property name="title" multiplicity="(0 1)"
	 */
	private String title;

	private String serverAbstract;
	private String[] keywords = new String[0];

	/**
	 * 
	 * @uml.property name="fees" multiplicity="(0 1)"
	 */
	private String fees;

	/**
	 * 
	 * @uml.property name="accessConstraints" multiplicity="(0 1)"
	 */
	private String accessConstraints;

	/**
	 * 
	 * @uml.property name="maintainer" multiplicity="(0 1)"
	 */
	private String maintainer;

	/**
	 * 
	 * @uml.property name="metadataLink"
	 * @uml.associationEnd multiplicity="(1 1)"
	 */
	private MetaDataLink metadataLink;

	/**
	 * 
	 * @uml.property name="gs"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private GeoServer gs;

	/**
	 * 
	 * @uml.property name="dt"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private Data dt;


    /**
     * Service constructor.
     * 
     * <p>
     * Stores the new ServiceDTO data for this service.
     * </p>
     *
     * @param config
     *
     * @throws NullPointerException when the param is null
     */
    public Service(ServiceDTO dto) {
        if (dto == null) {
            throw new NullPointerException();
        }

        enabled = dto.isEnabled();
        name = dto.getName();
        title = dto.getTitle();
        serverAbstract = dto.getAbstract();
        keywords = dto.getKeywords();
        fees = dto.getFees();
        accessConstraints = dto.getAccessConstraints();
        maintainer = dto.getMaintainer();
        onlineResource = dto.getOnlineResource();
        metadataLink = dto.getMetadataLink();
    }
    
    /**
     * load purpose.
     * <p>
     * loads a new copy of data into this object.
     * </p>
     * @param dto
     */
    public void load(ServiceDTO dto) {
    	if (dto == null) {
    		throw new NullPointerException();
    	}

    	enabled = dto.isEnabled();
    	name = dto.getName();
    	title = dto.getTitle();
    	serverAbstract = dto.getAbstract();
    	keywords = dto.getKeywords();
    	fees = dto.getFees();
    	accessConstraints = dto.getAccessConstraints();
    	maintainer = dto.getMaintainer();
    	onlineResource = dto.getOnlineResource();
    	metadataLink = dto.getMetadataLink();
    }

    /**
     * isEnabled purpose.
     * 
     * <p>
     * Returns whether is service is enabled.
     * </p>
     *
     * @return true when enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

	/**
	 * getOnlineResource purpose.
	 * 
	 * <p>
	 * Returns the Online Resource for this Service.
	 * </p>
	 * 
	 * @return URL The Online resource.
	 * 
	 * @uml.property name="onlineResource"
	 */
	public URL getOnlineResource() {
		return onlineResource;
	}


    /**
     * getAbstract purpose.
     * 
     * <p>
     * A description of this service.
     * </p>
     *
     * @return String This Service's abstract.
     */
    public String getAbstract() {
        return serverAbstract;
    }

	/**
	 * getAccessConstraints purpose.
	 * 
	 * <p>
	 * A description of this service's access constraints.
	 * </p>
	 * 
	 * @return String This service's access constraints.
	 * 
	 * @uml.property name="accessConstraints"
	 */
	public String getAccessConstraints() {
		return accessConstraints;
	}

	/**
	 * getFees purpose.
	 * 
	 * <p>
	 * A description of the fees for this service.
	 * </p>
	 * 
	 * @return String the fees for this service.
	 * 
	 * @uml.property name="fees"
	 */
	public String getFees() {
		return fees;
	}


    /**
     * getKeywords purpose.
     * 
     * <p>
     * A list of the keywords for this service.
     * </p>
     *
     * @return List keywords for this service
     */
    public List getKeywords() {
        LinkedList ll = new LinkedList();
        String[] s = keywords;

        for (int i = 0; i < s.length; i++)
            ll.add(s[i]);

        return ll;
    }

	/**
	 * getMaintainer purpose.
	 * 
	 * <p>
	 * The name of the maintainer for this service.
	 * </p>
	 * 
	 * @return String maintainer for this service.
	 * 
	 * @uml.property name="maintainer"
	 */
	public String getMaintainer() {
		return maintainer;
	}

	/**
	 * getName purpose.
	 * 
	 * <p>
	 * The name for this service.
	 * </p>
	 * 
	 * @return String the service's name.
	 * 
	 * @uml.property name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * getTitle purpose.
	 * 
	 * <p>
	 * The title for this service.
	 * </p>
	 * 
	 * @return String the service's title.
	 * 
	 * @uml.property name="title"
	 */
	public String getTitle() {
		return title;
	}

    
    Object toDTO(){
    	ServiceDTO dto = new ServiceDTO();
    	dto.setAccessConstraints(accessConstraints);
    	dto.setEnabled(enabled);
    	dto.setFees(fees);
    	dto.setKeywords(keywords);
    	dto.setMaintainer(maintainer);
    	dto.setName(name);
    	dto.setOnlineResource(onlineResource);
    	dto.setAbstract(serverAbstract);
    	dto.setTitle(title);
    	dto.setMetadataLink(metadataLink);
    	return dto;
    }
	/**
	 * Access dt property.
	 * 
	 * @return Returns the dt.
	 */
	public Data getData() {
		return dt;
	}

	/**
	 * Set dt to dt.
	 *
	 * @param dt The dt to set.
	 */
	void setData(Data dt) {
		this.dt = dt;
	}

	/**
	 * Access gs property.
	 * 
	 * @return Returns the gs.
	 */
	public GeoServer getGeoServer() {
		return gs;
	}

	/**
	 * Set gs to gs.
	 *
	 * @param gs The gs to set.
	 */
	void setGeoServer(GeoServer gs) {
		this.gs = gs;
	}

	/**
	 * @return Returns the metadataLink.
	 * 
	 * @uml.property name="metadataLink"
	 */
	public MetaDataLink getMetadataLink() {
		return metadataLink;
	}

}
