package org.geoserver.web.data.datastore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.geoserver.catalog.Catalog;
import org.geoserver.web.GeoServerBasePage;
import org.geoserver.web.data.DataPage;
import org.geoserver.web.data.datastore.panel.CheckBoxParamPanel;
import org.geoserver.web.data.datastore.panel.LabelParamPanel;
import org.geoserver.web.data.datastore.panel.PasswordParamPanel;
import org.geoserver.web.data.datastore.panel.TextParamPanel;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;
import org.vfny.geoserver.util.DataStoreUtils;

/**
 * Provides a form to configure a geotools DataStore
 * 
 * @author Gabriel Roldan
 */
public class DataStoreConfiguration extends GeoServerBasePage {

    private static final String dataStoreIdPropertyName = "Wicket_Data_Source_Name";

    /**
     * Holds datastore parameters. Properties will be settled by the form input
     * fields.
     */
    private final Map<String, Serializable> parametersMap;

    private String dataStoreInfoId;

    /**
     * Creates a new datastore configuration page to edit the properties of the
     * given data store
     * 
     * @param dataStoreInfoId
     *            the datastore id to modify
     */
    public DataStoreConfiguration(final String workspaceId, final String dataStoreInfoId,
            final Map<String, Serializable> connectionParameters) {

        final Catalog catalog = getCatalog();
        if (null == catalog.getWorkspace(workspaceId)) {
            throw new IllegalArgumentException("Workspace " + workspaceId + " not found");
        }
        if (null == catalog.getDataStore(dataStoreInfoId)) {
            throw new IllegalArgumentException("DataStore " + dataStoreInfoId + " not found");
        }

        final DataStoreFactorySpi dsFactory = DataStoreUtils.aquireFactory(connectionParameters);
        if (null == dsFactory) {
            throw new IllegalArgumentException(
                    "Can't get the DataStoreFactory for the given connection parameters");
        }

        parametersMap = new HashMap<String, Serializable>(connectionParameters);

        init(workspaceId, dataStoreInfoId, dsFactory);
    }

    /**
     * Creates a new datastore configuration page to create a new datastore of
     * the given type
     * 
     * @param dataStoreFactDisplayName
     *            the type of datastore to create, given by its factory display
     *            name
     */
    public DataStoreConfiguration(final String workspaceId, final String dataStoreFactDisplayName) {
        final DataStoreFactorySpi dsFact = DataStoreUtils.aquireFactory(dataStoreFactDisplayName);
        if (dsFact == null) {
            throw new IllegalArgumentException("Can't locate a datastore factory named '"
                    + dataStoreFactDisplayName + "'");
        }

        parametersMap = new HashMap<String, Serializable>();
        // pre-populate map with default values
        Param[] parametersInfo = dsFact.getParametersInfo();
        for (int i = 0; i < parametersInfo.length; i++) {
            Serializable value;
            if(parametersInfo[i].sample == null || parametersInfo[i].sample instanceof Serializable){
                value = (Serializable)parametersInfo[i].sample;
            }else{
                value = String.valueOf(parametersInfo[i].sample);
            }
            parametersMap.put(parametersInfo[i].key, value);
        }
        init(workspaceId, null, dsFact);
    }

    /**
     * 
     * @param dataStoreInfoId
     *            the id of the data store to edit, or {@code null} if about to
     *            configure a new data store
     * @param workspaceId
     *            the id for the workspace to attach the new datastore or the
     *            current datastore is attached to
     * @param connectionParameters
     *            the set of connection parameters, may be empty
     * @param dsFactory
     *            the datastore factory to use
     */
    private void init(final String dataStoreInfoId, final String workspaceId,
            final DataStoreFactorySpi dsFactory) {

        final List<ParamInfo> paramsInfo = new ArrayList<ParamInfo>();
        {
            Param[] dsParams = dsFactory.getParametersInfo();
            for (Param p : dsParams) {
                paramsInfo.add(new ParamInfo(p));
            }
        }

        final Form paramsForm = new Form("dataStoreForm");

        add(paramsForm);

        Panel dataStoreIdPanel;
        if (dataStoreInfoId == null) {
            dataStoreIdPanel = new TextParamPanel("dataStoreIdPanel", parametersMap,
                    dataStoreIdPropertyName, "Data Source Name", true);
        } else {
            dataStoreIdPanel = new LabelParamPanel("dataStoreIdPanel", parametersMap,
                    dataStoreIdPropertyName);
        }

        paramsForm.add(dataStoreIdPanel);

        ListView paramsList = new ListView("parameters", paramsInfo) {
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

        paramsForm.add(new Button("cancel") {
            @Override
            public void onSubmit() {
                setResponsePage(DataPage.class);
            }
        });

        paramsForm.add(new Button("submit") {
            @Override
            public void onSubmit() {
                Catalog catalog = getCatalog();
                setResponsePage(DataPage.class);
            }
        });

    }

    /**
     * Creates a form input component for the given datastore param based on its
     * type and metadata properties.
     * 
     * @param param
     * @return
     */
    private Panel getInputComponent(final String componentId, final Map<String, ?> paramsMap,
            final ParamInfo param) {

        final String paramName = param.getName();
        final String paramLabel = param.getName();
        final boolean required = param.isRequired();
        final Class binding = param.getBinding();

        Panel parameterPanel;
        if (Boolean.class == binding) {
            parameterPanel = new CheckBoxParamPanel(componentId, paramsMap, paramName, paramLabel);
        } else if (String.class == binding && param.isPassword()) {
            parameterPanel = new PasswordParamPanel(componentId, paramsMap, paramName, paramLabel);
        } else {
            parameterPanel = new TextParamPanel(componentId, paramsMap, paramName, paramLabel,
                    required);
        }
        return parameterPanel;
    }
}
