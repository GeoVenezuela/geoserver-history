/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.form.data;

import com.vividsolutions.jts.geom.Envelope;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.geotools.data.DataStore;
import org.geotools.feature.FeatureType;
import org.vfny.geoserver.action.HTMLEncoder;
import org.vfny.geoserver.config.AttributeTypeInfoConfig;
import org.vfny.geoserver.config.ConfigRequests;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.DataStoreConfig;
import org.vfny.geoserver.config.FeatureTypeConfig;
import org.vfny.geoserver.config.StyleConfig;
import org.vfny.geoserver.global.UserContainer;
import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
import org.vfny.geoserver.global.dto.DataTransferObjectFactory;
import org.vfny.geoserver.requests.Requests;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;


/**
 * Form used to work with FeatureType information.
 *
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: cholmesny $ (last modification)
 * @version $Id: TypesEditorForm.java,v 1.20 2004/09/13 16:18:40 cholmesny Exp $
 */
public class TypesEditorForm extends ActionForm {
    static final List schemaBases;

    static {
        List bases = new ArrayList();
        bases.add("--");
        bases.addAll(DataTransferObjectFactory.schemaBaseMap.keySet());
        schemaBases = Collections.unmodifiableList(bases);
    }

    /** Identiy DataStore responsible for this FeatureType */
    private String dataStoreId;

    /** Identify Style used to render this feature type */
    private String styleId;

    /**
     * Name of featureType.
     * 
     * <p>
     * An exact match for typeName provided by a DataStore.
     * </p>
     */
    private String typeName;

    /**
     * Representation of the Spatial Reference System.
     * 
     * <p>
     * Empty represents unknown, usually assumed to be Cartisian Coordinates.
     * </p>
     */
    private String SRS;

    /** Title of this FeatureType */
    private String title;

    /** Representation of bounds info as parseable by Double */
    private String minX;

    /** Representation of bounds info as parseable by Double */
    private String minY;

    /** Representation of bounds info as parseable by Double */
    private String maxX;

    /** Representation of bounds info as parseable by Double */
    private String maxY;

    /** List of keywords, often grouped with brackets */
    private String keywords;

    /** FeatureType abstract */
    private String description;

    /**
     * One of a select list - simplest is AbstractBaseClass.
     * 
     * <p>
     * The value "--" will be used to indicate default schema completly
     * generated from FeatureType information at runtime.
     * </p>
     * 
     * <p>
     * When generated the schema will make use a schemaBase of
     * "AbstractFeatureType".
     * </p>
     */
    private String schemaBase;

    /**
     * The name of the complex element of type schemaBase.
     * 
     * <p>
     * We only need this for non generated schemaBase.
     * </p>
     */
    private String schemaName;

    /** List of AttributeDisplay and AttributeForm */
    private List attributes;

    /** List of attributes available for addition */
    private List addList;

    /** Action requested by user */
    private String action;

    /** Sorted Set of available styles */
    private SortedSet styles;

    /** Stores the name of the new attribute they wish to create */
    private String newAttribute;

    /**
     * Set up FeatureTypeEditor from from Web Container.
     * 
     * <p>
     * The key DataConfig.SELECTED_FEATURE_TYPE is used to look up the selected
     * from the web container.
     * </p>
     *
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);

        action = "";

        ServletContext context = getServlet().getServletContext();

        DataConfig config = ConfigRequests.getDataConfig(request);
        UserContainer user = Requests.getUserContainer(request);

        FeatureTypeConfig type = user.getFeatureTypeConfig();

        if (type == null) {
            System.out.println("Type is not there");

            // Not sure what to do, user must have bookmarked?
            return; // Action should redirect to Select screen?
        }

        this.dataStoreId = type.getDataStoreId();
        this.styleId = type.getDefaultStyle();

        description = type.getAbstract();

        Envelope bounds = type.getLatLongBBox();

        if ((bounds == null) || bounds.isNull()) {
            minX = "";
            minY = "";
            maxY = "";
            maxX = "";
        } else {
            minX = Double.toString(bounds.getMinX());
            minY = Double.toString(bounds.getMinY());
            maxX = Double.toString(bounds.getMaxX());
            maxY = Double.toString(bounds.getMaxY());
        }

        typeName = type.getName();
        SRS = Integer.toString(type.getSRS());
        title = type.getTitle();

        System.out.println("rest based on schemaBase: " + type.getSchemaBase());

        // Generate ReadOnly list of Attributes
        //
        DataStoreConfig dataStoreConfig = config.getDataStore(dataStoreId);
        FeatureType featureType = null;

        try {
            DataStore dataStore = dataStoreConfig.findDataStore(getServlet()
                                                                    .getServletContext());
            featureType = dataStore.getSchema(typeName);
        } catch (IOException e) {
            // DataStore unavailable!
        }

        if (((type.getSchemaBase() == null)
                || "--".equals(type.getSchemaBase()))
                || (type.getSchemaAttributes() == null)) {
            //We are using the generated attributes
            this.schemaBase = "--";
            this.schemaName = typeName + "_Type";
            this.attributes = new LinkedList();

            // Generate ReadOnly list of Attributes
            //
            List generated = DataTransferObjectFactory.generateAttributes(featureType);
            this.attributes = attributesDisplayList(generated);
            addList = Collections.EMPTY_LIST;
        } else {
            this.schemaBase = type.getSchemaBase();
            this.schemaName = type.getSchemaName();
            this.attributes = new LinkedList();

            //
            // Need to add read only AttributeDisplay for each required attribute
            // defined by schemaBase
            //
            List schemaAttributes = DataTransferObjectFactory
                .generateRequiredAttributes(schemaBase);
            attributes.addAll(attributesDisplayList(schemaAttributes));
            attributes.addAll(attributesFormList(type.getSchemaAttributes(),
                    featureType));
            addList = new ArrayList(featureType.getAttributeCount());

            for (int i = 0; i < featureType.getAttributeCount(); i++) {
                String attributeName = featureType.getAttributeType(i).getName();

                if (lookUpAttribute(attributeName) == null) {
                    addList.add(attributeName);
                }
            }
        }

        StringBuffer buf = new StringBuffer();

        for (Iterator i = type.getKeywords().iterator(); i.hasNext();) {
            String keyword = (String) i.next();
            buf.append(keyword);

            if (i.hasNext()) {
                buf.append(" ");
            }
        }

        this.keywords = buf.toString();

        styles = new TreeSet();

        for (Iterator i = config.getStyles().values().iterator(); i.hasNext();) {
            StyleConfig sc = (StyleConfig) i.next();
            styles.add(sc.getId());

            if (sc.isDefault()) {
                if ((styleId == null) || styleId.equals("")) {
                    styleId.equals(sc.getId());
                }
            }
        }

        Object attribute = styles;

        if (attribute instanceof org.vfny.geoserver.form.data.AttributeDisplay) {
            ;
        }
    }

    private Object lookUpAttribute(String name) {
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            Object attribute = i.next();

            if (attribute instanceof AttributeDisplay
                    && name.equals(((AttributeDisplay) attribute).getName())) {
                return attribute;
            }

            if (attribute instanceof AttributeForm
                    && name.equals(((AttributeForm) attribute).getName())) {
                return attribute;
            }
        }

        return null;
    }

    /**
     * Create a List of AttributeDisplay based on AttributeTypeInfoDTO.
     *
     * @param dtoList
     *
     * @return
     */
    private List attributesDisplayList(List dtoList) {
        List list = new ArrayList();
        int index = 0;

        for (Iterator i = dtoList.iterator(); i.hasNext(); index++) {
            Object next = i.next();
            //System.out.println(index + " attribute: " + next);
            list.add(new AttributeDisplay(
                    new AttributeTypeInfoConfig((AttributeTypeInfoDTO) next)));
        }

        return list;
    }

    /**
     * Create a List of AttributeForm based on AttributeTypeInfoDTO.
     *
     * @param dtoList
     * @param schema DOCUMENT ME!
     *
     * @return
     */
    private List attributesFormList(List dtoList, FeatureType schema) {
        List list = new ArrayList();

        for (Iterator i = dtoList.iterator(); i.hasNext();) {
            AttributeTypeInfoConfig config = (AttributeTypeInfoConfig) i.next();
            list.add(new AttributeForm(config,
                    schema.getAttributeType(config.getName())));
        }

        return list;
    }

    /**
     * Generate DTO attributes List for the TypesEditorAction.
     * 
     * <p>
     * This list only includes entries defined by the user, not those generated
     * by the schemaBase.
     * </p>
     * 
     * <p>
     * If the user has chosen -- then this list will be <code>null</code>.
     * </p>
     *
     * @return List of user supplied AttributeTypeInfoConfig, or
     *         <code>null</code>
     */
    public List toSchemaAttributes() {
        if ((schemaBase == null) || schemaBase.equals("--")) {
            return null;
        }

        List list = new ArrayList();

        for (Iterator i = attributes.iterator(); i.hasNext();) {
            Object obj = i.next();

            if (obj instanceof AttributeForm) {
                AttributeForm form = (AttributeForm) obj;
                list.add(form.toConfig());
            }
        }

        return list;
    }

    public ActionErrors validate(ActionMapping mapping,
        HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        Locale locale = (Locale) request.getLocale();
        MessageResources messages = servlet.getResources();
        final String BBOX = HTMLEncoder.decode(messages.getMessage(locale,
                    "config.data.calculateBoundingBox.label"));

        // Pass Attribute Management Actions through without
        // much validation.
        if (action.startsWith("Up") || action.startsWith("Down")
                || action.startsWith("Remove") || action.equals(BBOX)) {
            return errors;
        }

        // Check selected style exists
        DataConfig data = ConfigRequests.getDataConfig(request);

        if (!(data.getStyles().containsKey(styleId) || "".equals(styleId))) {
            errors.add("styleId",
                new ActionError("error.styleId.notFound", styleId));
        }

        // check name exists in current DataStore?
        if ("".equals(minX) || "".equals(minY) || "".equals(maxX)
                || "".equals(maxY)) {
            errors.add("latlongBoundingBox",
                new ActionError("error.latLonBoundingBox.required"));
        } else {
            try {
                Double.parseDouble(minX);
                Double.parseDouble(minY);
                Double.parseDouble(maxX);
                Double.parseDouble(maxY);
            } catch (NumberFormatException badNumber) {
                errors.add("latlongBoundingBox",
                    new ActionError("error.latLonBoundingBox.invalid", badNumber));
            }
        }

        return errors;
    }

    /**
     * Are belong to us.
     * 
     * <p>
     * What can I say it is near a deadline! Easy access for
     * <code>Editor.jsp</code>.
     * </p>
     *
     * @return Possible schemaBase options
     */
    public List getAllYourBase() {
        return schemaBases;
    }

    //
    // Generated Accessors for Editor.jsp
    //

    /**
     * Access attributes property.
     *
     * @return Returns the attributes.
     */
    public List getAttributes() {
        return attributes;
    }

    /**
     * Set attributes to attributes.
     *
     * @param attributes The attributes to set.
     */
    public void setAttributes(List attributes) {
        this.attributes = attributes;
    }

    /**
     * Access dataStoreId property.
     *
     * @return Returns the dataStoreId.
     */
    public String getDataStoreId() {
        return dataStoreId;
    }

    /**
     * Set dataStoreId to dataStoreId.
     *
     * @param dataStoreId The dataStoreId to set.
     */
    public void setDataStoreId(String dataStoreId) {
        this.dataStoreId = dataStoreId;
    }

    /**
     * Access abstact (or description) property.
     *
     * @return Returns the description.
     */
    public String getAbstract() {
        return description;
    }

    /**
     * Set abstact (or description) to description.
     *
     * @param description The description to set.
     */
    public void setAbstract(String description) {
        this.description = description;
    }

    /**
     * Access keywords property.
     *
     * @return Returns the keywords.
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Set keywords to keywords.
     *
     * @param keywords The keywords to set.
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Access name property.
     *
     * @return Returns the name.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Set name to name.
     *
     * @param name The name to set.
     */
    public void setTypeName(String name) {
        this.typeName = name;
    }

    /**
     * Access schemaBase property.
     *
     * @return Returns the schemaBase.
     */
    public String getSchemaBase() {
        return schemaBase;
    }

    /**
     * Set schemaBase to schemaBase.
     *
     * @param schemaBase The schemaBase to set.
     */
    public void setSchemaBase(String schemaBase) {
        this.schemaBase = schemaBase;
    }

    /**
     * Access sRS property.
     *
     * @return Returns the sRS.
     */
    public String getSRS() {
        return SRS;
    }

    /**
     * Set sRS to srs.
     *
     * @param srs The sRS to set.
     */
    public void setSRS(String srs) {
        SRS = srs;
    }

    /**
     * Access title property.
     *
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title to title.
     *
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }

    /**
     * DOCUMENT ME!
     *
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the maxX.
     */
    public String getMaxX() {
        return maxX;
    }

    /**
     * DOCUMENT ME!
     *
     * @param maxX The maxX to set.
     */
    public void setMaxX(String maxX) {
        this.maxX = maxX;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the maxY.
     */
    public String getMaxY() {
        return maxY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param maxY The maxY to set.
     */
    public void setMaxY(String maxY) {
        this.maxY = maxY;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the minX.
     */
    public String getMinX() {
        return minX;
    }

    /**
     * DOCUMENT ME!
     *
     * @param minX The minX to set.
     */
    public void setMinX(String minX) {
        this.minX = minX;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the minY.
     */
    public String getMinY() {
        return minY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param minY The minY to set.
     */
    public void setMinY(String minY) {
        this.minY = minY;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the styleId.
     */
    public String getStyleId() {
        return styleId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param styleId The styleId to set.
     */
    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the styles.
     */
    public SortedSet getStyles() {
        return styles;
    }

    /**
     * DOCUMENT ME!
     *
     * @param styles The styles to set.
     */
    public void setStyles(SortedSet styles) {
        this.styles = styles;
    }

    public Object getAttribute(int index) {
        return attributes.get(index);
    }

    public void setAttribute(int index, Object attribute) {
        attributes.set(index, attribute);
    }

    /**
     * Access newAttribute property.
     *
     * @return Returns the newAttribute.
     */
    public String getNewAttribute() {
        return newAttribute;
    }

    /**
     * Set newAttribute to newAttribute.
     *
     * @param newAttribute The newAttribute to set.
     */
    public void setNewAttribute(String newAttribute) {
        this.newAttribute = newAttribute;
    }

    /**
     * DOCUMENT ME!
     *
     * @return List of attributes available for addition
     */
    public List getCreateableAttributes() {
        return addList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the schemaName.
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param schemaName The schemaName to set.
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
