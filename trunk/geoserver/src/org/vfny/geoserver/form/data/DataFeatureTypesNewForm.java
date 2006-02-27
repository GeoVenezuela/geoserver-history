/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.vfny.geoserver.form.data;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.vfny.geoserver.config.DataConfig;


/**
 * DataFeatureTypesNewForm purpose.
 *
 * @author rgould, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: DataFeatureTypesNewForm.java,v 1.6 2004/03/09 01:37:39 dmzwiers Exp $
 */
public class DataFeatureTypesNewForm extends ActionForm {

	/**
	 * 
	 * @uml.property name="selectedNewFeatureType" multiplicity="(0 1)"
	 */
	String selectedNewFeatureType;

	/**
	 * 
	 * @uml.property name="request"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	HttpServletRequest request;


    public SortedSet getNewFeatureTypes() {
        DataConfig dataConfig = (DataConfig) request.getSession()
                                                    .getServletContext()
                                                    .getAttribute(DataConfig.CONFIG_KEY);

        TreeSet out = new TreeSet(dataConfig.getFeatureTypeIdentifiers(getServlet().getServletContext()));
        out.removeAll(dataConfig.getFeaturesTypes().keySet());

        return out;
    }

    public void reset(ActionMapping arg0, HttpServletRequest request) {
        super.reset(arg0, request);
        this.request = request;

        selectedNewFeatureType = "";
    }

    public ActionErrors validate(ActionMapping mapping,
        HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        return errors;
    }

	/**
	 * Access selectedNewFeatureType property.
	 * 
	 * @return Returns the selectedNewFeatureType.
	 * 
	 * @uml.property name="selectedNewFeatureType"
	 */
	public String getSelectedNewFeatureType() {
		return selectedNewFeatureType;
	}

	/**
	 * Set selectedNewFeatureType to selectedNewFeatureType.
	 * 
	 * @param selectedNewFeatureType The selectedNewFeatureType to set.
	 * 
	 * @uml.property name="selectedNewFeatureType"
	 */
	public void setSelectedNewFeatureType(String selectedNewFeatureType) {
		this.selectedNewFeatureType = selectedNewFeatureType;
	}

}
