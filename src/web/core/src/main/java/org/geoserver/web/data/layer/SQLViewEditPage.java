/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.data.layer;

import java.io.IOException;
import java.util.logging.Level;

import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.ResourcePool;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.data.resource.ResourceConfigurationPage;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geotools.jdbc.VirtualTable;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Allows editing a SQL view and then going back to 
 * @author Andrea Aime - OpenGeo
 */
public class SQLViewEditPage extends SQLViewAbstractPage {
    ResourceConfigurationPage previusPage;
    String originalName;
    FeatureTypeInfo tinfo;

    public SQLViewEditPage(FeatureTypeInfo type, ResourceConfigurationPage previousPage) throws IOException {
        super(type.getStore().getWorkspace().getName(), type.getStore().getName(), 
                type.getMetadata().get(FeatureTypeInfo.JDBC_VIRTUAL_TABLE, VirtualTable.class), true);
        VirtualTable vt = type.getMetadata().get(FeatureTypeInfo.JDBC_VIRTUAL_TABLE, VirtualTable.class);
        tinfo = type;
        originalName = vt.getName();
        this.previusPage = previousPage;
    }

    @Override
    protected void onSave() {
        try {
            testViewDefinition(false);
            VirtualTable vt = buildVirtualTable();
            
            ResourcePool pool = GeoServerApplication.get().getCatalog().getResourcePool();
            if(tinfo != null) {
                tinfo.getMetadata().put(FeatureTypeInfo.JDBC_VIRTUAL_TABLE, vt);
                CoordinateReferenceSystem crs = pool.getFeatureType(tinfo).getCoordinateReferenceSystem();
                if(crs != null) {
                    tinfo.setNativeCRS(crs);
                }
            } else {
                tinfo.getMetadata().put(FeatureTypeInfo.JDBC_VIRTUAL_TABLE, vt);
                CoordinateReferenceSystem crs = pool.getFeatureType(tinfo).getCoordinateReferenceSystem();
                if(crs != null) {
                    tinfo.setNativeCRS(crs);
                }
            }
            
            previusPage.updateResource(tinfo);
            setResponsePage(previusPage);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create feature type", e);
            error(new ParamResourceModel("creationFailure", this, getFirstErrorMessage(e))
                    .getString());
        }
    }
    
    protected void onCancel() {
        setResponsePage(previusPage);
    }


}