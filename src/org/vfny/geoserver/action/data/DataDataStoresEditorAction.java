/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.vfny.geoserver.action.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFactorySpi.Param;
import org.vfny.geoserver.action.ConfigAction;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.DataStoreConfig;
import org.vfny.geoserver.form.data.DataDataStoresEditorForm;
import org.vfny.geoserver.global.UserContainer;


/**
 * DOCUMENT ME!
 *
 * @author rgould To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DataDataStoresEditorAction extends ConfigAction {
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        UserContainer user, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        DataDataStoresEditorForm dataStoresForm = (DataDataStoresEditorForm) form;

        String dataStoreID = dataStoresForm.getDataStoreId();
        String namespace = dataStoresForm.getNamespaceId();
        String description = dataStoresForm.getDescription();

        DataConfig dataConfig = (DataConfig) getDataConfig();
        DataStoreConfig config = null;

        config = (DataStoreConfig) dataConfig.getDataStore(dataStoreID);

        // After extracting params into a map
        Map paramValues = new HashMap();
        Map paramTexts = new HashMap();

        Map params = dataStoresForm.getParams();

        DataStoreFactorySpi factory = config.getFactory();
        Param[] info = factory.getParametersInfo();

        // Convert Params into the kind of Map we actually need
        //
        for (Iterator i = params.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();

            Param param = DataStoreUtils.find(info, key);

            if (param == null) {

                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.cannotProcessConnectionParams"));
                saveErrors(request, errors);

                return mapping.findForward("dataConfigDataStores");
            }

            Object value;

            try {
                value = param.lookUp(params);
            } catch (IOException erp) {

                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.cannotProcessConnectionParams"));
                saveErrors(request, errors);

                return mapping.findForward("dataConfigDataStores");
            }

            if (value != null) {
                paramValues.put(key, value);

                String text = param.text(value);
                paramTexts.put(key, text);
            }
        }

        // put magic namespace into the mix
        //
        paramValues.put("namespace", dataStoresForm.getNamespaceId());
        paramTexts.put("namespace", dataStoresForm.getNamespaceId());

        if (!factory.canProcess(paramValues)) {
            // We could not use these params!
            //

            ActionErrors errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.cannotProcessConnectionParams"));
            saveErrors(request, errors);

            return mapping.findForward("dataConfigDataStores");
        }

        try {
            DataStore victim = factory.createDataStore(paramValues);
            System.out.println("temporary datastore:" + victim);

            if (victim == null) {
                // We *really* could not use these params!
                //
    
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.invalidConnectionParams"));
                saveErrors(request, errors);

                return mapping.findForward("dataConfigDataStores");
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();

            ActionErrors errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.exception", throwable.getMessage()));

            saveErrors(request, errors);

            return mapping.findForward("dataConfigDataStores");
        }

        boolean enabled = dataStoresForm.isEnabled();

        if (dataStoresForm.isEnabledChecked() == false) {
            enabled = false;
        }

        config.setEnabled(enabled);
        config.setNameSpaceId(namespace);
        config.setAbstract(description);
        config.setConnectionParams(paramTexts);

        dataConfig.addDataStore(config);

        request.getSession().removeAttribute("selectedDataStoreId");
        getApplicationState().notifyConfigChanged();

        return mapping.findForward("dataConfigDataStores");
    }
}
