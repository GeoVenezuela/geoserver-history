/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.data.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidator;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.data.store.panel.CheckBoxParamPanel;
import org.geoserver.web.data.store.panel.NamespacePanel;
import org.geoserver.web.data.store.panel.PasswordParamPanel;
import org.geoserver.web.data.store.panel.TextParamPanel;
import org.geoserver.web.data.store.panel.WorkspacePanel;
import org.geoserver.web.util.MapModel;
import org.geoserver.web.wicket.FileExistsValidator;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.util.logging.Logging;

/**
 * Abstract base class for adding/editing a {@link DataStoreInfo}, provides the UI components and a
 * template method {@link #onSaveDataStore(Form)} for the subclasses to perform the insertion or
 * update of the object.
 * 
 * @author Gabriel Roldan
 * @see DataAccessNewPage
 * @see DataAccessEditPage
 */
public abstract class AbstractDataAccessPage extends GeoServerSecuredPage {

    protected static final Logger LOGGER = Logging.getLogger("org.geoserver.web.data.store");

    /**
     * Key used to store the name assigned workspace
     */
    protected static final String WORKSPACE_PROPERTY = "Wicket_Workspace";

    /**
     * Key used to handle the datastore "namespace" property as a NamespaceInfo instead of a plain
     * String
     */
    protected static final String NAMESPACE_PROPERTY = "Wicket_Namespace";

    protected static final String DATASTORE_ID_PROPERTY = "Wicket_DataStore_ID";

    /**
     * Key used to store the name assigned to the datastore in {@code parametersMap} as its a
     * DataStoreInfo property and not a DataAccess one
     */
    protected static final String DATASTORE_NAME_PROPERTY_NAME = "Wicket_Data_Source_Name";

    /**
     * Key used to store the description assigned to the datastore in {@code parametersMap} as its a
     * DataStoreInfo property and not a DataAccess one
     */
    protected static final String DATASTORE_DESCRIPTION_PROPERTY_NAME = "Wicket_Data_Source_Description";

    /**
     * Key used to store the enabled property assigned to the datastore in {@code parametersMap} as
     * its a DataStoreInfo property and not a DataAccess one
     */
    protected static final String DATASTORE_ENABLED_PROPERTY_NAME = "Wicket_Data_Source_Enabled";

    /**
     * Holds datastore parameters. Properties will be settled by the form input fields.
     */
    protected final Map<String, Serializable> parametersMap;

    public AbstractDataAccessPage() {
        parametersMap = new HashMap<String, Serializable>();
    }

    /**
     * 
     * @param workspaceId
     *            the id for the workspace to attach the new datastore or the current datastore is
     *            attached to
     * 
     * @param dsFactory
     *            the datastore factory to use
     * @param isNew
     *            wheter to set up the UI for a new dataaccess or an existing one, some properties
     *            may need not to be editable if not a new one.
     */
    protected final void initUI(final DataStoreFactorySpi dsFactory, final boolean isNew) {
        WorkspaceInfo workspace = (WorkspaceInfo) parametersMap.get(WORKSPACE_PROPERTY);
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace not provided");
        }

        final List<ParamInfo> paramsInfo = new ArrayList<ParamInfo>();
        {
            Param[] dsParams = dsFactory.getParametersInfo();
            for (Param p : dsParams) {
                paramsInfo.add(new ParamInfo(p));
            }
        }

        final Form paramsForm = new Form("dataStoreForm");
        add(paramsForm);

        paramsForm.add(new Label("storeType", dsFactory.getDisplayName()));
        paramsForm.add(new Label("storeTypeDescription", dsFactory.getDescription()));

        final IModel wsModel = new MapModel(parametersMap, WORKSPACE_PROPERTY);
        final IModel wsLabelModel = new ResourceModel("AbstractDataAccessPage.workspace");
        final WorkspacePanel workspacePanel = new WorkspacePanel("workspacePanel", wsModel,
                wsLabelModel, true);
        paramsForm.add(workspacePanel);

        final TextParamPanel dataStoreNamePanel;
        if (isNew) {
            parametersMap.put(NAMESPACE_PROPERTY, getCatalog().getDefaultNamespace());
        } else {
            NamespaceInfo namespace = null;
            String uri = (String) parametersMap.get("namespace");
            if (uri != null) {
                namespace = getCatalog().getNamespaceByURI(uri);
            }
            parametersMap.put(NAMESPACE_PROPERTY, namespace);

            // dataStoreNamePanel = new LabelParamPanel("dataStoreNamePanel", new MapModel(
            // parametersMap, DATASTORE_NAME_PROPERTY_NAME), new ResourceModel(
            // "AbstractDataAccessPage.dataSrcName", "Data Source Name"));
        }

        // IValidator dsNameValidator = new StoreNameValidator(new MapModel(parametersMap,
        // WORKSPACE_PROPERTY), new MapModel(parametersMap, DATASTORE_ID_PROPERTY));

        dataStoreNamePanel = new TextParamPanel("dataStoreNamePanel", new MapModel(parametersMap,
                DATASTORE_NAME_PROPERTY_NAME), new ResourceModel(
                "AbstractDataAccessPage.dataSrcName", "Data Source Name"), true);
        paramsForm.add(dataStoreNamePanel);

        paramsForm.add(new TextParamPanel("dataStoreDescriptionPanel", new MapModel(parametersMap,
                DATASTORE_DESCRIPTION_PROPERTY_NAME), new ResourceModel("description",
                "Description"), false, (IValidator[]) null));

        paramsForm.add(new CheckBoxParamPanel("dataStoreEnabledPanel", new MapModel(parametersMap,
                DATASTORE_ENABLED_PROPERTY_NAME), new ResourceModel("enabled", "Enabled")));

        ListView paramsList = new ListView("parameters", paramsInfo) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem item) {
                ParamInfo parameter = (ParamInfo) item.getModelObject();
                Component inputComponent = getInputComponent("parameterPanel", parametersMap,
                        parameter);
                if (parameter.getTitle() != null) {
                    inputComponent.add(new SimpleAttributeModifier("title", parameter.getTitle()));
                }
                item.add(inputComponent);
            }
        };
        // needed for form components not to loose state
        paramsList.setReuseItems(true);

        paramsForm.add(paramsList);

        paramsForm.add(new BookmarkablePageLink("cancel", StorePage.class));

        paramsForm.add(new SubmitLink("save") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                onSaveDataStore(paramsForm);
            }
        });

        paramsForm.add(new FeedbackPanel("feedback"));

        // validate the selected workspace does not already contain a store with the same name
        final String dataStoreInfoId = (String) parametersMap.get(DATASTORE_ID_PROPERTY);
        StoreNameValidator storeNameValidator = new StoreNameValidator(workspacePanel
                .getFormComponent(), dataStoreNamePanel.getFormComponent(), dataStoreInfoId);
        paramsForm.add(storeNameValidator);
    }

    /**
     * Call back method called when the save button is hit. Subclasses shall override in order to
     * perform the action over the catalog, whether it is adding a new {@link DataStoreInfo} or
     * saving the edits to an existing one
     * 
     * @param paramsForm
     *            the form containing the parameter values
     */
    protected abstract void onSaveDataStore(final Form paramsForm);

    /**
     * Creates a form input component for the given datastore param based on its type and metadata
     * properties.
     * 
     * @param param
     * @return
     */
    private Panel getInputComponent(final String componentId, final Map<String, ?> paramsMap,
            final ParamInfo param) {

        final String paramName = param.getName();
        final String paramLabel = param.getName();
        final boolean required = param.isRequired();
        final Class<?> binding = param.getBinding();

        Panel parameterPanel;
        if ("namespace".equals(paramName)) {

            IModel namespaceModel = new MapModel(paramsMap, NAMESPACE_PROPERTY);
            IModel paramLabelModel = new ResourceModel(paramLabel, paramLabel);
            parameterPanel = new NamespacePanel(componentId, namespaceModel, paramLabelModel, true);

        } else if (Boolean.class == binding) {
            // TODO Add prefix for better i18n?
            parameterPanel = new CheckBoxParamPanel(componentId,
                    new MapModel(paramsMap, paramName), new ResourceModel(paramLabel, paramLabel));

        } else if (String.class == binding && param.isPassword()) {
            parameterPanel = new PasswordParamPanel(componentId,
                    new MapModel(paramsMap, paramName), new ResourceModel(paramLabel, paramLabel),
                    required);
        } else {
            TextParamPanel tp = new TextParamPanel(componentId, new MapModel(paramsMap, paramName),
                    new ResourceModel(paramLabel, paramLabel), required);
            // if it can be a reference to the local filesystem make sure it's valid
            if(paramName.equalsIgnoreCase("url"))
                tp.getFormComponent().add(new FileExistsValidator());
            // make sure the proper value is returned
            tp.getFormComponent().setType(param.getBinding());
            parameterPanel = tp;
        }
        return parameterPanel;
    }

}