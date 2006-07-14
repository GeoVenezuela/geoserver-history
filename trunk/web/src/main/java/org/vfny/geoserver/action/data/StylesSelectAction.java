/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.vfny.geoserver.action.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.vfny.geoserver.action.ConfigAction;
import org.vfny.geoserver.action.HTMLEncoder;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.StyleConfig;
import org.vfny.geoserver.form.data.StylesSelectForm;
import org.vfny.geoserver.global.UserContainer;


/**
 * Edit selected style
 * 
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: StylesSelectAction.java,v 1.3 2004/03/01 19:47:45 dmzwiers Exp $
 */
public class StylesSelectAction extends ConfigAction {
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        UserContainer user, HttpServletRequest request, HttpServletResponse response)    
        throws IOException, ServletException {
        
        final StylesSelectForm selectForm = (StylesSelectForm) form;
        final String action = selectForm.getAction();
        String styleId = selectForm.getSelectedStyle();
        if(styleId.endsWith("*")){
        	styleId = styleId.substring(0,styleId.lastIndexOf("*"));
        }
        Locale locale = (Locale) request.getLocale();
        
        DataConfig config = getDataConfig();
        MessageResources messages = getResources(request);        
        // Need locale wording for edit and delete
        final String EDIT = HTMLEncoder.decode(messages.getMessage(locale, "label.edit"));
        final String DELETE = HTMLEncoder.decode(messages.getMessage(locale, "label.delete"));
        final String DEFAULT = HTMLEncoder.decode(messages.getMessage(locale, "label.default"));
        
        StyleConfig style = config.getStyle( styleId );
        if( style == null ){
            ActionErrors errors = new ActionErrors();
            errors.add("selectedStyle",
                new ActionError("error.style.invalid", styleId ));            
            request.setAttribute(Globals.ERROR_KEY, errors);            
            return mapping.findForward("config.data.style");
        }
        // Something is selected lets do the requested action
        //
        if (action.equals(DELETE)) {
            config.removeStyle( styleId );
            getApplicationState().notifyConfigChanged();
            selectForm.setSelectedStyle(null);
            selectForm.reset(mapping, request);
            return mapping.findForward("config.data.style");
        }
        if (action.equals(DEFAULT)) {
        	Map m = config.getStyles();
        	Iterator i = m.values().iterator();
        	while(i.hasNext()){
        		StyleConfig sc = (StyleConfig)i.next();
        		if(sc.isDefault()){
        			if(sc.getId()!=null && !sc.getId().equals(styleId)){
        				sc.setDefault(false);
        	            getApplicationState().notifyConfigChanged();
        			}
        		}else{
        			if(sc.getId()!=null && sc.getId().equals(styleId)){
        				sc.setDefault(true);
        	            getApplicationState().notifyConfigChanged();
        			}
        		}
        	}
            selectForm.setSelectedStyle(null);
            selectForm.reset(mapping, request);
            return mapping.findForward("config.data.style");
        }
        if( action.equals(EDIT)){
            user.setStyle( new StyleConfig( style ) );
            return mapping.findForward("config.data.style.editor");            
        }
        ActionErrors errors = new ActionErrors();
        errors.add("submit",
            new ActionError("error.action.invalid", action ));            
        request.setAttribute(Globals.ERROR_KEY, errors);        
        return mapping.findForward("config.data.style");
    }
}