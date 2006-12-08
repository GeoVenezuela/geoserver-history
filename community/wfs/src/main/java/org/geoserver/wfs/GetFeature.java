/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import net.opengis.wfs.AllSomeType;
import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.GetFeatureWithLockType;
import net.opengis.wfs.LockFeatureResponseType;
import net.opengis.wfs.LockFeatureType;
import net.opengis.wfs.LockType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.WFSFactory;

import org.geoserver.data.GeoServerCatalog;
import org.geoserver.data.feature.AttributeTypeInfo;
import org.geoserver.data.feature.FeatureTypeInfo;
import org.geoserver.feature.ReprojectingFeatureCollection;
import org.geoserver.ows.EMFUtils;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.data.crs.ReprojectFeatureResults;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

import org.geotools.filter.AttributeExpression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.expression.AbstractExpressionVisitor;
import org.geotools.filter.expression.SimpleFeaturePropertyAccessorFactory;
import org.geotools.filter.visitor.AbstractFilterVisitor;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Web Feature Service GetFeature operation.
 * <p>
 * This operation returns an array of {@link org.geotools.feature.FeatureCollection} 
 * instances.
 * </p>
 * 
 * @author Rob Hranac, TOPP
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * 
 * @version $Id$
 */
public class GetFeature {
	
    /** Standard logging instance for class */
    private static final Logger LOGGER = Logger.getLogger(
            "org.vfny.geoserver.requests");

    /** The catalog */
    protected GeoServerCatalog catalog;
    
    /** The wfs configuration */
    protected WFS wfs;
    
    /** filter factory */
    protected FilterFactory filterFactory;
   
    /**
     * Creates the GetFeature operation.
     * 
     */
    public GetFeature( WFS wfs, GeoServerCatalog catalog) {
		this.wfs = wfs;
		this.catalog = catalog;
    }
    
    /**
     * @return The reference to the GeoServer catalog.
     */
    public GeoServerCatalog getCatalog() {
		return catalog;
	}
    
    /**
     * @return The reference to the WFS configuration.
     */
    public WFS getWFS() {
		return wfs;
    }
    
    /**
     * Sets the filter factory to use to create filters.
     * 
     * @param filterFactory
     */
    public void setFilterFactory(FilterFactory filterFactory) {
		this.filterFactory = filterFactory;
	}
    
    public FeatureCollectionType run( GetFeatureType request ) throws WFSException {
    	List queries = request.getQuery();
    	
    	if ( queries.isEmpty() ) {
    		throw new WFSException( "No query specified" );
		}
		
		if ( EMFUtils.isUnset( queries, "typeName") ) {
			String msg = "No feature types specified";
			throw new WFSException( msg );
		}
		
		// Optimization Idea
        //
        // We should be able to reduce this to a two pass opperations.
        //
        // Pass #1 execute
        // - Attempt to Locks Fids during the first pass
        // - Also collect Bounds information during the first pass
        //
        // Pass #2 writeTo
        // - Using the Bounds to describe our FeatureCollections
        // - Iterate through FeatureResults producing GML
        //
        // And allways remember to release locks if we are failing:
        // - if we fail to aquire all the locks we will need to fail and
        //   itterate through the the FeatureSources to release the locks
        //
        if ( request.getMaxFeatures() == null) {
        	request.setMaxFeatures( BigInteger.valueOf( Integer.MAX_VALUE ) );
        }
        int maxFeatures = request.getMaxFeatures().intValue();
        
        
        
        FeatureCollectionType result = WFSFactory.eINSTANCE.createFeatureCollectionType();
        int count = 0;	//should probably be long
        try {
        	for ( int i = 0; i < request.getQuery().size() && ( count <= maxFeatures ); i++ ) {
        		QueryType query = (QueryType) request.getQuery().get( i );
        		
            	FeatureTypeInfo meta = null;
            	if ( query.getTypeName().size() == 1 ) {
            		meta = featureTypeInfo( (QName) query.getTypeName().get( 0 ) );
            	}
            	else {
            		//TODO: a join is taking place
            	}
            	
                FeatureSource source = meta.featureSource();

                List atts = meta.getAttributes();
                List attNames = meta.attributeNames();
                
                //make sure property names are cool
                List propNames = query.getPropertyName(); 
                
                for (Iterator iter = propNames.iterator(); iter.hasNext();) {
                	String propName = (String) iter.next();
                	
                	//HACK: strip off namespace
                	if( propName.indexOf( ':' ) != -1 ) {
                		propName = propName.substring( propName.indexOf( ':') + 1 );
                	}
                	
                	if ( !attNames.contains( propName ) ) {
                        String mesg = "Requested property: " + propName + " is " + "not available " +
                        		"for " + query.getTypeName() + ".  " + "The possible propertyName " +
                				"values are: " + attNames;
                        
                        throw new WFSException( mesg );
                    }
                }

                //we must also include any properties that are mandatory ( even if not requested ),
                // ie. those with minOccurs > 0
                if (propNames.size() != 0) {
                    Iterator ii = atts.iterator();
                    List tmp = new LinkedList();

                    while (ii.hasNext()) {
                        AttributeTypeInfo ati = (AttributeTypeInfo) ii.next();
                        LOGGER.finer("checking to see if " + propNames + " contains" + ati);

                        if (( (ati.getMinOccurs() > 0) && (ati.getMaxOccurs() != 0) )
                                || propNames.contains(ati.getName())) {
                        	
                        	tmp.add(  ati.getName() );
                            
                        }
                    }

                    //replace property names
                    query.getPropertyName().clear();
                    query.getPropertyName().addAll( tmp );
                }

               //make sure filters are sane
                if ( query.getFilter() != null ) {
                	final FeatureType featureType = source.getSchema();
                    ExpressionVisitor visitor = new AbstractExpressionVisitor() {
                    	public Object visit(PropertyName name, Object data) {
                    		// case of multiple geometries being returned
                    		if ( name.evaluate( featureType ) == null ) {
                    			//we want to throw wfs exception, but cant
                    			throw new RuntimeException( "Illegal property name: " + name.getPropertyName() );
                    		}
                    		
                    		return name;
                    	};
                    };
                    query.getFilter().accept( new AbstractFilterVisitor( visitor ), null );
                }
                
                org.geotools.data.Query gtQuery = toDataQuery( query, maxFeatures - count, source );
                LOGGER.fine("Query is " + query + "\n To gt2: " + gtQuery );

                FeatureCollection features = source.getFeatures( gtQuery );
                count += features.size();
                
                //JD: TODO reoptimize
//                if ( i == request.getQuery().size() - 1 ) { 
//                	//DJB: dont calculate feature count if you dont have to. The MaxFeatureReader will take care of the last iteration
//                	maxFeatures -= features.getCount();
//                }

                //GR: I don't know if the featuresults should be added here for later
                //encoding if it was a lock request. may be after ensuring the lock
                //succeed?
                result.getFeature().add( features );
                
            }
            
        } 
        catch (IOException e) {
    		throw new WFSException( "Error occurred getting features", e, request.getHandle() );
        } 
        
        //locking
        if ( request instanceof GetFeatureWithLockType ) {
        	GetFeatureWithLockType withLockRequest = (GetFeatureWithLockType) request;
        	
        	LockFeatureType lockRequest = WFSFactory.eINSTANCE.createLockFeatureType();
        	lockRequest.setExpiry( withLockRequest.getExpiry() );
        	lockRequest.setHandle( withLockRequest.getHandle() );
        	lockRequest.setLockAction( AllSomeType.ALL_LITERAL );
        	
        	for ( int i = 0; i < request.getQuery().size(); i++ ) {
        		QueryType query = (QueryType) request.getQuery().get( i );
        		
        		LockType lock = WFSFactory.eINSTANCE.createLockType();
            	lock.setFilter( query.getFilter() );
            	lock.setHandle( query.getHandle() );
            	
            	//TODO: joins?
            	lock.setTypeName( (QName) query.getTypeName().get( 0 ) );
            	lockRequest.getLock().add( lock );
        	}
        	
        	LockFeature lockFeature = new LockFeature( wfs, catalog );
        	lockFeature.setFilterFactory( filterFactory );
        	
        	LockFeatureResponseType response = lockFeature.lockFeature( lockRequest );
        	result.setLockId( response.getLockId() );
        }
        
        result.setNumberOfFeatures( BigInteger.valueOf( count ) );
        result.setTimeStamp( Calendar.getInstance());
       
        return result;
    }
    
    /**
     * Get this query as a geotools Query.
     * 
     * <p>
     * if maxFeatures is a not positive value DefaultQuery.DEFAULT_MAX will be
     * used.
     * </p>
     * 
     * <p>
     * The method name is changed to toDataStoreQuery since this is a one way
     * conversion.
     * </p>
     *
     * @param maxFeatures number of features, or 0 for DefaultQuery.DEFAULT_MAX
     *
     * @return A Query for use with the FeatureSource interface
     * 
     */
    public org.geotools.data.Query toDataQuery( QueryType query, int maxFeatures, FeatureSource source ) 
    	throws WFSException {
    	
    	if ( maxFeatures <= 0  ) {
    		maxFeatures = DefaultQuery.DEFAULT_MAX;
    	}
        
    	String[] props = null;
    	if ( !query.getPropertyName().isEmpty() ) {
    		props = new String[ query.getPropertyName().size() ];
    		for ( int p = 0; p < query.getPropertyName().size(); p++ ) {
    			String propertyName = (String) query.getPropertyName().get( p ); 
    			props[ p ] = propertyName;
    		}
    	}

    	Filter filter = (Filter) query.getFilter();
        if ( filter == null ) {
        	filter = org.geotools.filter.Filter.NONE;
        }
        	
        //only handle non-joins for now
        QName typeName = (QName) query.getTypeName().get( 0 );
        DefaultQuery dataQuery = new DefaultQuery(
    		typeName.getLocalPart(), filter, maxFeatures, props, query.getHandle()
		);

        //figure out the crs the data is in
        CoordinateReferenceSystem crs = 
        	source.getSchema().getDefaultGeometry() != null ? 
    			source.getSchema().getDefaultGeometry().getCoordinateSystem() : null;
		if ( crs == null ) {
			//set to be the server default
			try {
				crs = CRS.decode( "EPSG:4326" );
				dataQuery.setCoordinateSystem( crs );
			} catch (Exception e) {
				//should never happen
				throw new RuntimeException( e );
			}
		}
		
        //handle reprojection
        if ( query.getSrsName() != null ) {
        	CoordinateReferenceSystem target;
			try {
				target = CRS.decode(  query.getSrsName().toString() );
			} 
			catch ( Exception e ) {
				String msg = "Unable to support srsName: " + query.getSrsName();
				throw new WFSException( msg , e );
			}
			
			//if the crs are not equal, then reproject
			if ( !crs.equals( target ) ) {
				dataQuery.setCoordinateSystemReproject( crs );	
			}
        	
        }
        
        //handle sorting
        if ( query.getSortBy() != null ) {
        	dataQuery.setSortBy( new SortBy[] { query.getSortBy() } );
        }
        
        return dataQuery;
    }
    
    FeatureTypeInfo featureTypeInfo( QName name ) throws WFSException, IOException {
    	
    	FeatureTypeInfo meta = 
    		catalog.featureType( name.getPrefix(), name.getLocalPart() );
    	
		if ( meta == null ) {
    		String msg = "Could not locate " + name + " in catalog.";
    		throw new WFSException( msg );
        }
    		
        return meta;
    }
    
}
