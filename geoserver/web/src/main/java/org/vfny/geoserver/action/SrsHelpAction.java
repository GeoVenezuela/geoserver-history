/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.action;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
DOCUMENT ME!
 *
 * @author Simone Giannecchini, GeoSolutions
 */
public class SrsHelpAction extends Action {
    private final static Logger LOGGER = Logger.getLogger(SrsHelpAction.class.toString());

    /**
     * This is a simple action - it reads in the GT2 supported EPSG
     * codes. DONE: once geosever support EPSG thats not in the properties
     * file, this should be a bit more abstract and get a list of all EPSG
     * defs from the GDSFactory (if possible).  Use toWKT() as its nicer to
     * read. Form has two properies - ids  (list of String - the epsg #) defs
     * (list of String - the epsg WKT definitions)
     *
     * @param mapping DOCUMENT ME!
     * @param form DOCUMENT ME!
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws ServletException DOCUMENT ME!
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        ArrayList defs = new ArrayList();
        ArrayList ids_string = new ArrayList();

        Set codes = CRS.getSupportedCodes("EPSG");

        // make an array of each code (as an int)
        ArrayList ids = new ArrayList();
        Iterator codeIt = codes.iterator();

        while (codeIt.hasNext()) {
            String code = (String) codeIt.next();
            String id = code.substring(code.indexOf(':') + 1); //just the number

            if (!ids.contains(Integer.valueOf(id))) {
                ids.add(Integer.valueOf(id));
            }
        }

        Collections.sort(ids); //sort to get them in order

        CoordinateReferenceSystem crs;
        String def;
        codeIt = ids.iterator();

        Integer id;

        while (codeIt.hasNext()) //for each id (in sorted order)
         {
            id = (Integer) codeIt.next();

            try { //get its definition
                crs = CRS.decode(new StringBuffer("EPSG:").append(id).toString());
                def = crs.toWKT();
                defs.add(def);
                ids_string.add(id.toString());
            } catch (Exception e) {
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            }
        }

        //send off to the .jsp
        DynaActionForm myForm = (DynaActionForm) form;

        myForm.set("srsDefinitionList", defs.toArray(new String[1]));
        myForm.set("srsIDList", ids_string.toArray(new String[1]));

        // return back to the admin demo
        //
        return mapping.findForward("success");
    }
}
