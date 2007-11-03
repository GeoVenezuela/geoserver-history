/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.bindings.GML2EncodingUtils;
import org.geotools.referencing.CRS;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Returns a clone of the provided filter where all geometries and bboxes that
 * do not have a CRS gets the specified default one. The working
 * assumption is that the filters specified are strictly compliant with the OGC
 * spec, so the first item is always a {@link PropertyName}, and the second
 * always a {@link Literal}
 * 
 * @author Andrea Aime - The Open Planning Project
 * 
 */
public class DefaultCRSFilterVisitor extends DuplicatingFilterVisitor {
    FeatureType featureType;
    private CoordinateReferenceSystem defaultCrs;

    public DefaultCRSFilterVisitor(FilterFactory2 factory, FeatureType featureType, 
            CoordinateReferenceSystem defaultCrs) {
        super(factory);
        this.featureType = featureType;
        this.defaultCrs = defaultCrs;
    }

//    /**
//     * Returns the CRS associated to a property in the feature type, or throws
//     * an exception if the property is not a geometric one
//     * 
//     * @param propertyName
//     * @return
//     */
//    private CoordinateReferenceSystem findPropertyCRS(PropertyName propertyName) {
//        AttributeType at = (AttributeType) propertyName.evaluate(featureType);
//        if (at instanceof GeometryAttributeType) {
//            GeometryAttributeType gat = (GeometryAttributeType) at;
//            return gat.getCoordinateSystem();
//        } else {
//            throw new IllegalArgumentException("Property '" + propertyName
//                    + "' is not a geometric one, the filter is invalid");
//        }
//    }

    public Object visit(BBOX filter, Object extraData) {
        // if no srs is specified we can't transform anyways
        String srs = filter.getSRS();
        if (srs != null && !"".equals(srs.trim()))
            return super.visit(filter, extraData);

        try {
            // grab the original envelope data
            double minx = filter.getMinX();
            double miny = filter.getMinY();
            double maxx = filter.getMaxX();
            double maxy = filter.getMaxY();
            String propertyName = filter.getPropertyName();
            String defaultSrs = GML2EncodingUtils.crs(defaultCrs);

            return getFactory(extraData).bbox(propertyName, minx, miny, maxx, maxy, defaultSrs);
        } catch (Exception e) {
            throw new RuntimeException("Could not decode srs '" + srs + "'", e);
        }

    }

    public Object visit(Beyond filter, Object extraData) {
        return new GeometryFilterTransformer() {

            Object cloneFilter(BinarySpatialOperator filter, Object extraData) {
                return DefaultCRSFilterVisitor.super.visit((Beyond) filter, extraData);
            }

            Object cloneFilter(BinarySpatialOperator bso, Object extraData, Expression ex1,
                    Expression ex2) {
                Beyond filter = (Beyond) bso;
                return factory.beyond(ex1, ex2, filter.getDistance(), filter.getDistanceUnits());
            }
        }.transform(filter, extraData);
    }

    public Object visit(Contains filter, Object extraData) {
        return new GeometryFilterTransformer() {

            Object cloneFilter(BinarySpatialOperator filter, Object extraData) {
                return DefaultCRSFilterVisitor.super.visit((Contains) filter, extraData);
            }

            Object cloneFilter(BinarySpatialOperator bso, Object extraData, Expression ex1,
                    Expression ex2) {
                return factory.contains(ex1, ex2);
            }
        }.transform(filter, extraData);
    }

    public Object visit(Crosses filter, Object extraData) {
        return new GeometryFilterTransformer() {

            Object cloneFilter(BinarySpatialOperator filter, Object extraData) {
                return DefaultCRSFilterVisitor.super.visit((Crosses) filter, extraData);
            }

            Object cloneFilter(BinarySpatialOperator bso, Object extraData, Expression ex1,
                    Expression ex2) {
                return factory.crosses(ex1, ex2);
            }
        }.transform(filter, extraData);
    }

    public Object visit(Disjoint filter, Object extraData) {
        return new GeometryFilterTransformer() {

            Object cloneFilter(BinarySpatialOperator filter, Object extraData) {
                return DefaultCRSFilterVisitor.super.visit((Disjoint) filter, extraData);
            }

            Object cloneFilter(BinarySpatialOperator bso, Object extraData, Expression ex1,
                    Expression ex2) {
                return factory.disjoint(ex1, ex2);
            }
        }.transform(filter, extraData);
    }

    public Object visit(DWithin filter, Object extraData) {
        return new GeometryFilterTransformer() {

            Object cloneFilter(BinarySpatialOperator filter, Object extraData) {
                return DefaultCRSFilterVisitor.super.visit((DWithin) filter, extraData);
            }

            Object cloneFilter(BinarySpatialOperator bso, Object extraData, Expression ex1,
                    Expression ex2) {
                DWithin filter = (DWithin) bso;
                return factory.dwithin(ex1, ex2, filter.getDistance(), filter.getDistanceUnits());
            }
        }.transform(filter, extraData);
    }

    public Object visit(Intersects filter, Object extraData) {
        return new GeometryFilterTransformer() {

            Object cloneFilter(BinarySpatialOperator filter, Object extraData) {
                return DefaultCRSFilterVisitor.super.visit((Intersects) filter, extraData);
            }

            Object cloneFilter(BinarySpatialOperator bso, Object extraData, Expression ex1,
                    Expression ex2) {
                return factory.intersects(ex1, ex2);
            }
        }.transform(filter, extraData);
    }

    public Object visit(Overlaps filter, Object extraData) {
        return new GeometryFilterTransformer() {

            Object cloneFilter(BinarySpatialOperator filter, Object extraData) {
                return DefaultCRSFilterVisitor.super.visit((Overlaps) filter, extraData);
            }

            Object cloneFilter(BinarySpatialOperator bso, Object extraData, Expression ex1,
                    Expression ex2) {
                return factory.overlaps(ex1, ex2);
            }
        }.transform(filter, extraData);
    }

    public Object visit(Touches filter, Object extraData) {
        return new GeometryFilterTransformer() {

            Object cloneFilter(BinarySpatialOperator filter, Object extraData) {
                return DefaultCRSFilterVisitor.super.visit((Touches) filter, extraData);
            }

            Object cloneFilter(BinarySpatialOperator bso, Object extraData, Expression ex1,
                    Expression ex2) {
                return factory.touches(ex1, ex2);
            }
        }.transform(filter, extraData);
    }

    public Object visit(Within filter, Object extraData) {
        return new GeometryFilterTransformer() {

            Object cloneFilter(BinarySpatialOperator filter, Object extraData) {
                return DefaultCRSFilterVisitor.super.visit((Within) filter, extraData);
            }

            Object cloneFilter(BinarySpatialOperator bso, Object extraData, Expression ex1,
                    Expression ex2) {
                return factory.within(ex1, ex2);
            }
        }.transform(filter, extraData);
    }

    /**
     * Factors out most of the logic needed to reproject a geometry filter, leaving subclasses
     * only the need to call the appropriate methods to create the new binary spatial filter
     * @author Andrea Aime - The Open Plannig Project
     *
     */
    private abstract class GeometryFilterTransformer {
        Object transform(BinarySpatialOperator filter, Object extraData) {
            // check working assumptions, first expression is a property
            if (!(filter.getExpression1() instanceof PropertyName))
                throw new IllegalArgumentException("Binary geometry filter, but first expression "
                        + "is not a property name? (it's a " + filter.getExpression1().getClass()
                        + ")");
            
            // second expression is a geometry literal
            if (!(filter.getExpression2() instanceof Literal))
                throw new IllegalArgumentException("Binary geometry filter, but second expression "
                        + "is not a literal? (it's a " + filter.getExpression1().getClass() + ")");
            Object value = ((Literal) filter.getExpression2()).getValue();
            if (!(value instanceof Geometry))
                throw new IllegalArgumentException("Binary geometry filter, but second expression "
                        + "is not a geometry literal? (it's a " + value.getClass() + ")");
            Geometry geom = (Geometry) value;

            // does it make sense to proceed?
            if (geom.getUserData() != null
                    && geom.getUserData() instanceof CoordinateReferenceSystem)
                return cloneFilter(filter, extraData);

            try {
                // clone the geometry and assign the new crs
                CoordinateReferenceSystem geomCRS = (CoordinateReferenceSystem) geom.getUserData();
                Geometry clone = geom.getFactory().createGeometry(geom);
                clone.setUserData(defaultCrs);
    
                // clone
                Expression ex1 = (Expression) filter.getExpression1().accept(
                        DefaultCRSFilterVisitor.this, extraData);
                Expression ex2 = factory.literal(clone);
                return cloneFilter(filter, extraData, ex1, ex2);
            } catch(Exception e) {
                throw new RuntimeException("Could not reproject geometry filter " + filter, e);
            }
        }

        /**
         * Straight cloning using cascaded visit
         * 
         * @param filter
         * @param extraData
         * @return
         */
        abstract Object cloneFilter(BinarySpatialOperator filter, Object extraData);

        /**
         * Clone with the provided parameters as first and second expressions
         * 
         * @param filter
         * @param extraData
         * @param ex1
         * @param ex2
         * @return
         */
        abstract Object cloneFilter(BinarySpatialOperator filter, Object extraData, Expression ex1,
                Expression ex2);
    }

}
