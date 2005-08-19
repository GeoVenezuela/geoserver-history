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
 * DataCoveragesNewForm purpose.
 *
 * @author rgould, Refractions Research, Inc.
 * @author dmzwiers
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author $Author: Simone Giannecchini (simboss_ml@tiscali.it) $ (last modification)
 * @version $Id: DataCoveragesNewForm.java,v 1.6 2004/03/09 01:37:39 dmzwiers Exp $
 */
public class DataCoveragesNewForm extends ActionForm {

	/**
	 * 
	 * @uml.property name="selectedNewCoverage" multiplicity="(0 1)"
	 */
	String selectedNewCoverage;

	/**
	 * 
	 * @uml.property name="request"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	HttpServletRequest request;


    public SortedSet getNewCoverages() {
        DataConfig dataConfig = (DataConfig) request.getSession()
                                                    .getServletContext()
                                                    .getAttribute(DataConfig.CONFIG_KEY);

        TreeSet out = new TreeSet(dataConfig.getCoverageIdentifiers(getServlet().getServletContext()));
        out.removeAll(dataConfig.getCoverages().keySet());

        return out;
    }

    public void reset(ActionMapping arg0, HttpServletRequest request) {
        super.reset(arg0, request);
        this.request = request;

        selectedNewCoverage = "";
    }

    public ActionErrors validate(ActionMapping mapping,
        HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        return errors;
    }

	/**
	 * Access selectedNewCoverage property.
	 * 
	 * @return Returns the selectedNewCoverage.
	 * 
	 * @uml.property name="selectedNewCoverage"
	 */
	public String getSelectedNewCoverage() {
		return selectedNewCoverage;
	}

	/**
	 * Set selectedNewCoverage to selectedNewCoverage.
	 * 
	 * @param selectedNewCoverage The selectedNewCoverage to set.
	 * 
	 * @uml.property name="selectedNewCoverage"
	 */
	public void setSelectedNewCoverage(String selectedNewCoverage) {
		this.selectedNewCoverage = selectedNewCoverage;
	}

}
