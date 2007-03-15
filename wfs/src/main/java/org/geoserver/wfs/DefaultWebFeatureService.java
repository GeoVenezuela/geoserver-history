/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import net.opengis.wfs.DescribeFeatureTypeType;
import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.GetCapabilitiesType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.GetFeatureWithLockType;
import net.opengis.wfs.LockFeatureResponseType;
import net.opengis.wfs.LockFeatureType;
import net.opengis.wfs.TransactionResponseType;
import net.opengis.wfs.TransactionType;
import org.geotools.xml.transform.TransformerBase;
import org.opengis.filter.FilterFactory;
import org.vfny.geoserver.global.Data;
import org.vfny.geoserver.global.FeatureTypeInfo;


/**
 * Web Feature Service implementation.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class DefaultWebFeatureService implements WebFeatureService {
    /**
     * WFS service configuration.
     */
    protected WFS wfs;

    /**
     * The catalog
     */
    protected Data catalog;

    /**
     * Filter factory
     */
    protected FilterFactory filterFactory;

    public DefaultWebFeatureService(WFS wfs, Data catalog) {
        this.wfs = wfs;
        this.catalog = catalog;
    }

    /**
     * Sets the fitler factory.
     */
    public void setFilterFactory(FilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    /**
     * WFS GetCapabilities operation.
     *
     * @param request The get capabilities request.
     *
     * @return A transformer instance capable of serializing a wfs capabilities
     * document.
     *
     * @throws WFSException Any service exceptions.
     */
    public TransformerBase getCapabilities(GetCapabilitiesType request)
        throws WFSException {
        return new GetCapabilities(wfs, catalog).run(request);
    }

    /**
     * WFS DescribeFeatureType operation.
     *
     * @param request The describe feature type request.
     *
     * @return A set of feature type metadata objects.
     *
     * @throws WFSException Any service exceptions.
     */
    public FeatureTypeInfo[] describeFeatureType(DescribeFeatureTypeType request)
        throws WFSException {
        return new DescribeFeatureType(wfs, catalog).run(request);
    }

    /**
     * WFS GetFeature operation.
     *
     * @param request The get feature request.
     *
     * @return A feature collection type instance.
     *
     * @throws WFSException Any service exceptions.
     */
    public FeatureCollectionType getFeature(GetFeatureType request)
        throws WFSException {
        GetFeature getFeature = new GetFeature(wfs, catalog);
        getFeature.setFilterFactory(filterFactory);

        return getFeature.run(request);
    }

    /**
     * WFS GetFeatureWithLock operation.
     *
     * @param request The get feature with lock request.
     *
      * @return A feature collection type instance.
     *
     * @throws WFSException Any service exceptions.
     */
    public FeatureCollectionType getFeatureWithLock(GetFeatureWithLockType request)
        throws WFSException {
        return getFeature(request);
    }

    /**
     * WFS LockFeatureType operation.
     *
     * @param request The lock feature request.
     *
     * @return A lock feture response type.
     *
     * @throws WFSException An service exceptions.
     */
    public LockFeatureResponseType lockFeature(LockFeatureType request)
        throws WFSException {
        LockFeature lockFeature = new LockFeature(wfs, catalog);
        lockFeature.setFilterFactory(filterFactory);

        return lockFeature.lockFeature(request);
    }

    /**
     * WFS transaction operation.
     *
     * @param request The transaction request.
     *
     * @return A transaction response instance.
     *
     * @throws WFSException Any service exceptions.
     */
    public TransactionResponseType transaction(TransactionType request)
        throws WFSException {
        Transaction transaction = new Transaction(wfs, catalog);
        transaction.setFilterFactory(filterFactory);

        return transaction.transaction(request);
    }

    //the following operations are not part of the spec
    public void releaseLock(String lockId) throws WFSException {
        new LockFeature(wfs, catalog).release(lockId);
    }

    public void releaseAllLocks() throws WFSException {
        new LockFeature(wfs, catalog).releaseAll();
    }
}
