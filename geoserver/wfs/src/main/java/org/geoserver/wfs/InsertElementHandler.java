/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import net.opengis.wfs.InsertElementType;
import net.opengis.wfs.InsertedFeatureType;
import net.opengis.wfs.TransactionResponseType;
import net.opengis.wfs.TransactionType;
import net.opengis.wfs.WfsFactory;
import org.eclipse.emf.ecore.EObject;
import org.geoserver.feature.ReprojectingFeatureCollection;
import org.geotools.data.FeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.namespace.QName;


/**
 * Handler for the insert element
 *
 * @author Andrea Aime - TOPP
 *
 */
public class InsertElementHandler implements TransactionElementHandler {
    /**
     * logger
     */
    static Logger LOGGER = Logger.getLogger("org.geoserver.wfs");
    private WFS wfs;
    private FilterFactory filterFactory;

    public InsertElementHandler(WFS wfs, FilterFactory filterFactory) {
        this.wfs = wfs;
        this.filterFactory = filterFactory;
    }

    public void checkValidity(EObject element, Map featureTypeInfos)
        throws WFSTransactionException {
        if ((wfs.getServiceLevel() & WFS.SERVICE_INSERT) == 0) {
            throw new WFSException("Transaction INSERT support is not enabled");
        }
    }

    public void execute(EObject element, TransactionType request, Map featureStores,
        TransactionResponseType response, TransactionListener listener)
        throws WFSTransactionException {
        LOGGER.finer("Transasction Insert:" + element);

        InsertElementType insert = (InsertElementType) element;
        long inserted = response.getTransactionSummary().getTotalInserted().longValue();

        try {
            // group features by their schema
            HashMap /* <FeatureType,FeatureCollection> */ schema2features = new HashMap();

            for (Iterator f = insert.getFeature().iterator(); f.hasNext();) {
                Feature feature = (Feature) f.next();
                FeatureType schema = feature.getFeatureType();
                FeatureCollection collection = (FeatureCollection) schema2features.get(schema);

                if (collection == null) {
                    collection = new DefaultFeatureCollection(null, schema) {
                            };
                    schema2features.put(schema, collection);
                }

                collection.add(feature);
            }

            // JD: change from set fo list because if inserting
            // features into different feature stores, they could very well
            // get given the same id
            // JD: change from list to map so that the map can later be
            // processed and we can report the fids back in the same order
            // as they were supplied
            HashMap schema2fids = new HashMap();

            for (Iterator c = schema2features.values().iterator(); c.hasNext();) {
                FeatureCollection collection = (FeatureCollection) c.next();
                FeatureType schema = collection.getSchema();

                QName elementName = new QName(schema.getNamespace().toString(), schema.getTypeName());
                FeatureStore store = (FeatureStore) featureStores.get(elementName);

                if (store == null) {
                    throw new WFSException("Could not locate FeatureStore for '" + elementName
                        + "'");
                }

                if (collection != null) {
                    // reprojection
                    CoordinateReferenceSystem target = schema.getDefaultGeometry()
                                                             .getCoordinateSystem();

                    if (target != null) {
                        collection = new ReprojectingFeatureCollection(collection, target);
                    }

                    // Need to use the namespace here for the
                    // lookup, due to our weird
                    // prefixed internal typenames. see
                    // http://jira.codehaus.org/secure/ViewIssue.jspa?key=GEOS-143

                    // Once we get our datastores making features
                    // with the correct namespaces
                    // we can do something like this:
                    // FeatureTypeInfo typeInfo =
                    // catalog.getFeatureTypeInfo(schema.getTypeName(),
                    // schema.getNamespace());
                    // until then (when geos-144 is resolved) we're
                    // stuck with:
                    // QName qName = (QName) typeNames.get( i );
                    // FeatureTypeInfo typeInfo =
                    // catalog.featureType( qName.getPrefix(),
                    // qName.getLocalPart() );

                    // this is possible with the insert hack above.
                    LOGGER.finer("Use featureValidation to check contents of insert");

                    // featureValidation(
                    // typeInfo.getDataStore().getId(), schema,
                    // collection );
                    List fids = (List) schema2fids.get(schema.getTypeName());

                    if (fids == null) {
                        fids = new LinkedList();
                        schema2fids.put(schema.getTypeName(), fids);
                    }

                    fids.addAll(store.addFeatures(collection));

                    //  tell the world we're just inserted data
                    Set ogcFids = new HashSet();

                    for (Iterator it = fids.iterator(); it.hasNext();) {
                        String fid = (String) it.next();
                        ogcFids.add(filterFactory.featureId(fid));
                    }

                    Filter fidFilter = filterFactory.id(ogcFids);
                    listener.dataStoreChange(new TransactionEvent(
                            TransactionEventType.POST_INSERT, store.getFeatures(fidFilter)));
                }
            }

            // report back fids, we need to keep the same order the
            // fids were reported in the original feature collection
            InsertedFeatureType insertedFeature = null;

            for (Iterator f = insert.getFeature().iterator(); f.hasNext();) {
                Feature feature = (Feature) f.next();
                FeatureType schema = feature.getFeatureType();

                // get the next fid
                LinkedList fids = (LinkedList) schema2fids.get(schema.getTypeName());
                String fid = (String) fids.removeFirst();

                insertedFeature = WfsFactory.eINSTANCE.createInsertedFeatureType();
                insertedFeature.setHandle(insert.getHandle());
                insertedFeature.getFeatureId().add(filterFactory.featureId(fid));

                response.getInsertResults().getFeature().add(insertedFeature);
            }

            // update the insert counter
            inserted += insert.getFeature().size();
        } catch (Exception e) {
            String msg = "Error perfomring insert";
            throw new WFSTransactionException(msg, e, insert.getHandle());
        }

        // update transaction summary
        response.getTransactionSummary().setTotalInserted(BigInteger.valueOf(inserted));
    }

    public Class getElementClass() {
        return InsertElementType.class;
    }

    public QName[] getTypeNames(EObject element) throws WFSTransactionException {
        InsertElementType insert = (InsertElementType) element;
        List typeNames = new ArrayList();

        if (!insert.getFeature().isEmpty()) {
            for (Iterator f = insert.getFeature().iterator(); f.hasNext();) {
                Feature feature = (Feature) f.next();

                String name = feature.getFeatureType().getTypeName();
                String namespaceURI = null;

                if (feature.getFeatureType().getNamespace() != null) {
                    namespaceURI = feature.getFeatureType().getNamespace().toString();
                }

                typeNames.add(new QName(namespaceURI, name));
            }
        } else {
            LOGGER.finer("Insert was empty - does not need a FeatuerSoruce");
        }

        return (QName[]) typeNames.toArray(new QName[typeNames.size()]);
    }
}
