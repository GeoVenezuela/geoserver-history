/* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v1_1_0;

import net.opengis.wfs.WfsFactory;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geoserver.ows.xml.v1_0.OWSConfiguration;
import org.geoserver.wfs.xml.FeatureTypeSchemaBuilder;
import org.geoserver.wfs.xml.PropertyTypePropertyExtractor;
import org.geoserver.wfs.xml.WFSHandlerFactory;
import org.geoserver.wfs.xml.filter.v1_1.FilterTypeBinding;
import org.geoserver.wfs.xml.filter.v1_1.PropertyNameTypeBinding;
import org.geoserver.wfs.xml.gml3.AbstractGeometryTypeBinding;
import org.geoserver.wfs.xml.gml3.CircleTypeBinding;
import org.geoserver.wfs.xml.xs.DateBinding;
import org.geotools.feature.FeatureType;
import org.geotools.filter.v1_1.OGC;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.gml2.FeatureTypeCache;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.gml3.bindings.GML;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Schemas;
import org.geotools.xs.bindings.XS;
import org.picocontainer.MutablePicoContainer;
import org.vfny.geoserver.global.Data;
import org.vfny.geoserver.global.FeatureTypeInfo;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;


public class WFSConfiguration extends Configuration {
    /**
     * catalog
     */
    Data catalog;

    /**
     * Schema builder
     */
    FeatureTypeSchemaBuilder schemaBuilder;

    public WFSConfiguration(Data catalog, FeatureTypeSchemaBuilder schemaBuilder) {
        super();

        this.catalog = catalog;
        this.schemaBuilder = schemaBuilder;

        addDependency(new OGCConfiguration());
        addDependency(new GMLConfiguration());
        addDependency(new OWSConfiguration());
    }

    public void addDependency(Configuration dependency) {
        //override to make public
        super.addDependency(dependency);
    }

    public String getNamespaceURI() {
        return WFS.NAMESPACE;
    }

    public String getSchemaFileURL() {
        return getSchemaLocationResolver().resolveSchemaLocation(null, WFS.NAMESPACE, "wfs.xsd");
    }

    public BindingConfiguration getBindingConfiguration() {
        return new WFSBindingConfiguration();
    }

    public XSDSchemaLocationResolver getSchemaLocationResolver() {
        return new WFSSchemaLocationResolver();
    }

    protected XSDSchemaLocator createSchemaLocator() {
        return new WFSSchemaLocator(this, catalog, schemaBuilder);
    }

    public void configureContext(MutablePicoContainer context) {
        super.configureContext(context);

        context.registerComponentInstance(WfsFactory.eINSTANCE);
        context.registerComponentInstance(new WFSHandlerFactory(catalog, schemaBuilder));
        context.registerComponentInstance(catalog);
        context.registerComponentImplementation( PropertyTypePropertyExtractor.class );
        
        //seed the cache with entries from the catalog
        FeatureTypeCache featureTypeCache = (FeatureTypeCache) context
            .getComponentInstanceOfType(FeatureTypeCache.class);

        try {
            Collection featureTypes = catalog.getFeatureTypeInfos().values();

            for (Iterator f = featureTypes.iterator(); f.hasNext();) {
                FeatureTypeInfo meta = (FeatureTypeInfo) f.next();
                FeatureType featureType = meta.getFeatureType();

                featureTypeCache.put(featureType);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void configureBindings(MutablePicoContainer container) {
        //register our custom bindings
        container.registerComponentImplementation(XS.DATE, DateBinding.class);
        container.registerComponentImplementation(OGC.Filter, FilterTypeBinding.class);
        container.registerComponentImplementation(OGC.PropertyNameType,
            PropertyNameTypeBinding.class);
        container.registerComponentImplementation(GML.CircleType, CircleTypeBinding.class);
        container.registerComponentImplementation(GML.AbstractGeometryType,
            AbstractGeometryTypeBinding.class);

        //remove bindings for MultiPolygon and MultiLineString
        //TODO: make this cite configurable
        Schemas.unregisterComponent(container, GML.MultiPolygonType);
    }
}
