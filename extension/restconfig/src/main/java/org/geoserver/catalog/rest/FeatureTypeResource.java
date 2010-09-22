/* Copyright (c) 2001 - 2009 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.catalog.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.geoserver.catalog.AttributeTypeInfo;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.event.CatalogListener;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.rest.RestletException;
import org.geoserver.rest.format.DataFormat;
import org.geotools.data.DataStore;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.vividsolutions.jts.geom.Geometry;

public class FeatureTypeResource extends AbstractCatalogResource {

    public FeatureTypeResource(Context context, Request request,Response response, Catalog catalog) {
        super(context, request, response, FeatureTypeInfo.class, catalog);
    }

    @Override
    protected DataFormat createHTMLFormat(Request request, Response response) {
        return new ResourceHTMLFormat(FeatureTypeInfo.class,request,response,this);
    }
    
    @Override
    protected Object handleObjectGet() {
        String workspace = getAttribute( "workspace");
        String datastore = getAttribute( "datastore");
        String featureType = getAttribute( "featuretype" );

        if ( datastore == null ) {
            LOGGER.fine( "GET feature type" + workspace + "," + featureType );
            
            //grab the corresponding namespace for this workspace
            NamespaceInfo ns = catalog.getNamespaceByPrefix( workspace );
            if ( ns != null ) {
                return catalog.getFeatureTypeByName(ns,featureType);
            }

            throw new RestletException( "", Status.CLIENT_ERROR_NOT_FOUND );
        }

        LOGGER.fine( "GET feature type" + datastore + "," + featureType );
        DataStoreInfo ds = catalog.getDataStoreByName(workspace, datastore);
        return catalog.getFeatureTypeByDataStore( ds, featureType );
    }

    @Override
    public boolean allowPost() {
        return getAttribute("featuretype") == null;
    }
    
    @Override
    protected String handleObjectPost(Object object) throws Exception {
        String workspace = getAttribute( "workspace");
        String dataStore = getAttribute( "datastore");

        FeatureTypeInfo featureType = (FeatureTypeInfo) object;
         
        //ensure the store matches up
        if ( featureType.getStore() != null ) {
            if ( !dataStore.equals( featureType.getStore().getName() ) ) {
                throw new RestletException( "Expected datastore " + dataStore +
                " but client specified " + featureType.getStore().getName(), Status.CLIENT_ERROR_FORBIDDEN );
            }
        }
        else {
            featureType.setStore( catalog.getDataStoreByName( workspace, dataStore ) );
        }
        
        //ensure workspace/namespace matches up
        if ( featureType.getNamespace() != null ) {
            if ( !workspace.equals( featureType.getNamespace().getPrefix() ) ) {
                throw new RestletException( "Expected workspace " + workspace +
                    " but client specified " + featureType.getNamespace().getPrefix(), Status.CLIENT_ERROR_FORBIDDEN );
            }
        }
        else {
            featureType.setNamespace( catalog.getNamespaceByPrefix( workspace ) );
        }
        featureType.setEnabled(true);
        
        // now, does the feature type exist? If not, create it
        DataStoreInfo ds = catalog.getDataStoreByName( workspace, dataStore );
        String typeName = featureType.getName();
        if(featureType.getNativeName() != null) {
            typeName = featureType.getNativeName(); 
        } 
        boolean typeExists = false;
        DataStore gtds = (DataStore) ds.getDataStore(null);
        for(String name : gtds.getTypeNames()) {
            if(name.equals(typeName)) {
                typeExists = true;
                break;
            }
        }
        if(!typeExists) {
            gtds.createSchema(buildFeatureType(featureType));
            // the attributes created might not match up 1-1 with the actual spec due to
            // limitations of the data store, have it re-compute them
            featureType.getAttributes().clear();
            List<String> typeNames = Arrays.asList(gtds.getTypeNames());
            // handle Oracle oddities
            // TODO: use the incoming store capabilites API to better handle the name transformation
            if(!typeNames.contains(typeName) && typeNames.contains(typeName.toUpperCase())) {
                featureType.setNativeName(featureType.getName().toLowerCase());
            }
        }
        
        CatalogBuilder cb = new CatalogBuilder(catalog);
        cb.initFeatureType( featureType );
        
        if ( featureType.getStore() == null ) {
            //get from requests
            featureType.setStore( ds );
        }
        
        NamespaceInfo ns = featureType.getNamespace();
        if ( ns != null && !ns.getPrefix().equals( workspace ) ) {
            //TODO: change this once the two can be different and we untie namespace
            // from workspace
            LOGGER.warning( "Namespace: " + ns.getPrefix() + " does not match workspace: " + workspace + ", overriding." );
            ns = null;
        }
        
        if ( ns == null){
            //infer from workspace
            ns = catalog.getNamespaceByPrefix( workspace );
            featureType.setNamespace( ns );
        }
        
        featureType.setEnabled(true);
        catalog.add( featureType );
        
        //create a layer for the feature type
        catalog.add(new CatalogBuilder(catalog).buildLayer(featureType));
        
        LOGGER.info( "POST feature type" + dataStore + "," + featureType.getName() );
        return featureType.getName();
    }
    
    SimpleFeatureType buildFeatureType(FeatureTypeInfo fti) {
        // basic checks
        if(fti.getName() == null) {
            throw new RestletException("Trying to create new feature type inside the store, " +
            		"but no feature type name was specified", Status.CLIENT_ERROR_BAD_REQUEST);
        } else if(fti.getAttributes() == null || fti.getAttributes() == null) {
            throw new RestletException("Trying to create new feature type inside the store, " +
            		"but no attributes were specified", Status.CLIENT_ERROR_BAD_REQUEST);
        }
        
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        if(fti.getNativeName() != null) {
            builder.setName(fti.getNativeName());
        } else {
            builder.setName(fti.getName());
        }
        if(fti.getNativeCRS() != null) {
            builder.setCRS(fti.getNativeCRS());
        } else if(fti.getCRS() != null) {
            builder.setCRS(fti.getCRS());
        } else if(fti.getSRS() != null) {
            builder.setSRS(fti.getSRS());
        }
        for (AttributeTypeInfo ati : fti.getAttributes()) {
            if(ati.getLength() != null && ati.getLength() > 0) {
                builder.length(ati.getLength());
            }
            builder.nillable(ati.isNillable());
            builder.add(ati.getName(), ati.getBinding());
        }
        return builder.buildFeatureType();
    }

    @Override
    public boolean allowPut() {
        return getAttribute("featuretype") != null;
    }

    @Override
    protected void handleObjectPut(Object object) throws Exception {
        FeatureTypeInfo ft = (FeatureTypeInfo) object;
        
        String workspace = getAttribute("workspace");
        String datastore = getAttribute("datastore");
        String featuretype = getAttribute("featuretype");
        
        DataStoreInfo ds = catalog.getDataStoreByName(workspace, datastore);
        FeatureTypeInfo original = catalog.getFeatureTypeByDataStore( ds,  featuretype );
        new CatalogBuilder(catalog).updateFeatureType(original,ft);
        catalog.save( original );
        
        clear(original);
        
        LOGGER.info( "PUT feature type" + datastore + "," + featuretype );
    }
    
    @Override
    public boolean allowDelete() {
        return getAttribute("featuretype") != null;
    }
    
    @Override
    public void handleObjectDelete() throws Exception {
        String workspace = getAttribute("workspace");
        String datastore = getAttribute("datastore");
        String featuretype = getAttribute("featuretype");
        
        DataStoreInfo ds = catalog.getDataStoreByName(workspace, datastore);
        FeatureTypeInfo ft = catalog.getFeatureTypeByDataStore( ds,  featuretype );
        catalog.remove( ft );
        clear(ft);
        
        LOGGER.info( "DELETE feature type" + datastore + "," + featuretype );
    }
    
    void clear(FeatureTypeInfo info) {
        catalog.getResourcePool().clear(info);
        catalog.getResourcePool().clear(info.getStore());
    }

    @Override
    protected void configurePersister(XStreamPersister persister, DataFormat format) {
        persister.setHideFeatureTypeAttributes();
        persister.setCallback( new XStreamPersister.Callback() {
            @Override
            protected void postEncodeReference(Object obj, String ref,
                    HierarchicalStreamWriter writer, MarshallingContext context) {
                if ( obj instanceof NamespaceInfo ) {
                    NamespaceInfo ns = (NamespaceInfo) obj;
                    encodeLink( "/namespaces/" + encode(ns.getPrefix()), writer);
                }
                if ( obj instanceof DataStoreInfo ) {
                    DataStoreInfo ds = (DataStoreInfo) obj;
                    encodeLink( "/workspaces/" + encode(ds.getWorkspace().getName()) + 
                        "/datastores/" + encode(ds.getName()), writer );
                }
            }
            
            @Override
            protected void postEncodeFeatureType(FeatureTypeInfo ft,
                    HierarchicalStreamWriter writer, MarshallingContext context) {
                try {
                    writer.startNode("attributes");
                    context.convertAnother(ft.attributes());
                    writer.endNode();
                } catch (IOException e) {
                    throw new RuntimeException("Could not get native attributes", e);
                }
            }
        });
    }
}
