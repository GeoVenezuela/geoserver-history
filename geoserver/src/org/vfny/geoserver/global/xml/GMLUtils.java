/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.global.xml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility class defining GML constants, and utility functions.
 *
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: GMLUtils.java,v 1.6 2004/01/18 00:33:21 dmzwiers Exp $
 */
public class GMLUtils {
    /** Mappings by schema */
    static private Map schemas = new HashMap();
    
    /** Mappings by defined type */
    static private Map definitions = new HashMap();
    
    /** Mapping by propertyName */
    static private Map properties = new HashMap();
    
    /** Create a new maping */
    static Mapping map( String schema ){
        return map( schema, String.class, false );
    }
    /** Create a new maping */
    static Mapping map( String schema, Class type ){
        return map( schema, type, false );
    }    
    /** Create a new maping */
    static Mapping define( String schema, Class type ){
        return map( schema, type, true );
    }    
    /** Create new mapping */
    static Mapping map( String schema, Class type, boolean define ){
        Mapping mapping = new Mapping( schema, type );
        schemas.put( schema, mapping );
        if( define ){
            definitions.put( type, mapping );
        }
        if( schema.endsWith("PropertyType")){
            properties.put( mapping.schema, mapping );
        }
        return mapping;
    }
    
    public static final Mapping BOOLEAN = define("xs:boolean",Boolean.class );
    public static final Mapping DECIMAL = define("xs:decimal",BigDecimal.class );
    public static final Mapping INTEGER = define("xs:integer",BigInteger.class );    
    public static final Mapping NEGATIVEINTERGER = map("xs:negativeInteger",Integer.class );
    public static final Mapping POSTIVEINTEGER = map("xs:positiveInteger", Integer.class );
    public static final Mapping LONG = define("xs:long", Long.class );
    public static final Mapping INT = define("xs:int", Integer.class );    
    public static final Mapping SHORT = define("xs:short", Short.class);
    public static final Mapping BYTE = define("xs:byte", Byte.class );
    public static final Mapping UNSIGNEDLONG = map("xs:unsignedLong", Long.class );
    public static final Mapping UNSIGNEDSHORT = map("xs:unsignedShort", Short.class );    
    public static final Mapping UNSIGNEDINT = map("xs:unsignedInt", Integer.class );
    public static final Mapping UNSIGNEDBYTE = map("xs:unsginedByte", Byte.class );    
    public static final Mapping FLOAT = define("xs:float", Float.class );    
    public static final Mapping DOUBLE = define("xs:double", Double.class );
    public static final Mapping DATE = define("xs:date", Date.class );    
    public static final Mapping DATETIME = map("xs:dateTime", Long.class );
    public static final Mapping DURATION = map("xs:duration", Long.class );
    public static final Mapping GTYPE = map("xs:gDay", Long.class );
    public static final Mapping GMONTH = map("xs:gMonth", Long.class );
    public static final Mapping GMONTHDAY = map("xs:gMonthData", Long.class );
    public static final Mapping GYEAR = map("xs:gYear", Long.class );
    public static final Mapping GYEARMONTH = map("xs:gYearMonth", Long.class );
    public static final Mapping TIME = map("xs:time", Long.class );
    public static final Mapping ID = map("xs:ID" );
    public static final Mapping IDREF = map("xs:IDREF" );
    public static final Mapping ENTITY = map("xs:ENTITY" );
    public static final Mapping ENTITIES = map("xs:ENTITIES" );
    public static final Mapping NMTOKEN = map("xs:NMTOKENS" );
    public static final Mapping NOTATION = map("xs:NOTATION" );
    public static final Mapping STRING = define("xs:string", String.class );
    public static final Mapping NORMALIZEDSTRING = map("xs:normalizedString" );
    public static final Mapping TOKEN = map("xs:token" );    
    public static final Mapping QNAME = map("xs:QName" );
    public static final Mapping NAME = map("xs:Name" );
    public static final Mapping NCNAME = map("xs:NCName" );
    
    
    private static final String[] xmlSchemaTypes = {
        "boolean", "decimal","integer","negativeInteger","nonNegativeInteger",
        "positiveInteger", "long","int","short","byte","unsignedLong", 
        "unsignedShort", "unsignedInt","unsignedByte","float", 
        "double", "date","dateTime","duration", 
        "gDay", "gMonth", "gMonthDay", "gYear", "gYearMonth", "time",
        "ID", "IDREF", "IDREFS", "ENTITY", "ENTITIES", "NMTOKEN", "NMTOKENS",
        "NOTATION", "string", "normalizedString", "token", "QName", "Name",
        "NCName"
    };
    // gml Mappings for JTS Geometry
    public static final Mapping POINTTYPE = define("gml:PointType", Point.class );
    public static final Mapping LINESTRINGTYPE = define("gml:LineStringType", LineString.class );
    public static final Mapping LINEARRINGTYPE = define("gml:LinearRingType", LinearRing.class );    
    public static final Mapping BOXTYPE = define("gml:BoxType", Envelope.class );
    public static final Mapping POLYGONTYPE = define("gml:PolygonType", Polygon.class );
    public static final Mapping GEOMETRYCOLLECTIONTYPE = define("gml:GeometryCollectionType", GeometryCollection.class );    
    public static final Mapping MULTIPOINTTYPE= define("gml:MultiPointType", MultiPoint.class );
    public static final Mapping MULTILINESTRINGTYPE = define("gml:MultiLineStringType", MultiLineString.class );
    public static final Mapping MULTIPOLYGONTYPE = define("gml:MultiPolygonType", MultiPolygon.class );
    // gml Mappings 
    public static final Mapping COORDTYPE = define("gml:CoordType", Coordinate.class );
    public static final Mapping COORDINATESTYPE = define("gml:CoordinatesType", CoordinateList.class );
    
    // GML Propertry types - used by Cite Tests
    public static final Mapping POINTPROPERTYTYPE = map("gml:PointPropertyType", Point.class ); // named
    public static final Mapping POLYGONPROPERTYTYPE = map( "gml:PolygonPropertyType", Polygon.class ); // named
    public static final Mapping LINESTRINGPROPERTYTYPE = map("gml:LineStringPropertyType", LineString.class ); // named
    public static final Mapping MULTIPOINTPROPERTYTYPE = map("gml:MultiPointPropertyType", MultiPoint.class ); // named
    public static final Mapping MULTILINESTRINGPROPERTYTYPE = map("gml:MultiLineStringPropertyType", MultiLineString.class ); // named
    public static final Mapping MULTIPOLYGONPROPERTYTYPE = map("gml:MultiPolygonPropertyType", MultiPolygon.class );
    public static final Mapping MULTIGEOMETRYPROPERTYTYPE = map("gml:MultiGeometryPropertyType", GeometryCollection.class );
    // 
    public static final Mapping NULLTYPE = map("gml:NullType", Void.TYPE );
    
    private static final String[] gmlTypes = {
        "PointType", "LineStringType", "LinearRingType", "BoxType",
        "PolygonType", "GeometryCollectionType", "MultiPointType",
        "MultiLineStringType", "MultiPolygonType", "CoordType",
        "CoordinatesType", "PointPropertyType", "PolygonPropertyType",
        "LineStringPropertyType", "MultiPointPropertyType",
        "MultiLineStringPropertyType", "MultiPolygonPropertyType",
        "MultiGeometryPropertyType", "NullType"
    };
    public static final Mapping ABSTRACT = map("gml:Abstract" );
    
    public static final Mapping ABSTRACTFEATURETYPE = map("gml:AbstractFeatureType" );
    public static final Mapping ABSTRACTFEATURECOLLECTION = map("gml:AbstractFeatureCollectionBaseType" );
    public static final Mapping ABSTRACTFEATURECOLLECTIONTYPE = map("gml:AbstractFeatureCollectionType" );
    public static final Mapping GEOMETRYPROPERTYTYPE = map("gml:GeometryPropertyType" );
    public static final Mapping FEATUREASSOCIATIONTYPE = map("gml:FeatureAssociationType" );
    public static final Mapping BOUNDINGSHAPETYPE = map("gml:BoundingShapeType" );
    public static final Mapping ABSTRACTGEOMETRYTYPE = define("gml:AbstractGeometryType", Geometry.class );    
    public static final Mapping ABSTRACTGEOMETRYCOLLECTIONBASETYPE = map("gml:AbstractGeometryCollectionBaseType" );    
    public static final Mapping ASSOCIATIONATTRIBUTEGROUP = map("gml:AssociationAttributeGroup" );
    public static final Mapping GEOMETRYASSOCIATIONTYPE = map("gml:GeometryAssociationType" );    
    public static final Mapping POINTMEMBERTYPE = map("gml:PointMemberType" );
    public static final Mapping LINESTRINGMEMBERTYPE = map("gml:LineStringMemberType" );    
    public static final Mapping POLYGONMEMBERTYPE = map("gml:PolygonMemberType" );
    public static final Mapping LINEARRINGMEMBERTYPE = map("gml:LinearRingMemberType" );    
    
    private static final String[] baseGmlTypes = {
        "AbstractFeatureType", "AbstractFeatureCollectionBaseType",
        "AbstractFeatureCollectionType", "GeometryPropertyType",
        "FeatureAssociationType", "BoundingShapeType", "AbstractGeometryType",
        "AbstractGeometryCollectionBaseType", "AssociationAttributeGroup",
        "GeometryAssociationType", "PointMemberType", "LineStringMemberType",
        "PolygonMemberType", "LinearRingMemberType"
    };
    /** Utility class may not construct instance */
    private GMLUtils() {
    }
    static Class promotePrimativeType( Class type ){
        if( type == null || !type.isPrimitive() ){
            return type;
        }
        if( type == Integer.TYPE) return Integer.class;
        if( type == Long.TYPE) return Long.class;
        if( type == Short.TYPE) return Short.class;
        if( type == Byte.TYPE) return Byte.class;
        if( type == Double.TYPE) return Double.class;
        if( type == Float.TYPE) return Float.class;
        if( type == Boolean.TYPE) return Boolean.class;            
        return null;
    }    
    /**
     * Mapping for ref or null if not found.
     * <p>
     * ref is of the form prefix:typeName
     * </p>
     * @param ref prefix:typeName used to locate Mapping
     * @return Mapping for ref
     */
    public static Mapping type( String ref ){
    	Mapping r = null;
    	r = (Mapping) schemas.get( ref );
    	if (r!=null) return r;
    	int x = ref.indexOf(':')+1;
    	char y = ref.charAt(x);
    	r =  (Mapping) schemas.get( ref.substring(0,x)+Character.toUpperCase(y)+ref.substring(x+1) );
    	if (r!=null) return r;
    	r =  (Mapping) schemas.get( ref.substring(0,x)+Character.toUpperCase(y)+ref.substring(x+1)+"Type" );
    	if (r!=null) return r;
    	r =  (Mapping) schemas.get( ref.substring(0,x)+Character.toLowerCase(y)+ref.substring(x+1) );
    	if (r!=null) return r;
    	r =  (Mapping) schemas.get( ref.substring(0,x)+Character.toLowerCase(y)+ref.substring(x+1)+"Type" );
    	if (r!=null) return r;
    	return null;
    }  
    
    /** Locate property by complete "gml:*PropertyType" reference */
    public static Mapping property( String reference ){
    	return (Mapping) properties.get( reference );
    }
    /**
     * Mapping for type or null
     * <p>
     * May make use of property types if name and exact type match
     * the one of the gml properties.
     * </p>
     * <p>
     * If not one of the schema for defined types are checked.
     * </p>
     * <p>
     * If not a linear seach of the defined types is made
     * making use of isAssignable.
     * </p>
     * <p>
     * Finally all mappings are consulted using using a linear search
     * </p>
     * 
     * @param type Type to look up schema for
     * @return Mapping for type or null
     */
    public static Mapping schema( String name, Class type ){
        Mapping mapping;
        if( type.isPrimitive() ){
            type = promotePrimativeType( type );
        }        
        if( properties.containsKey( name )){
            mapping = (Mapping) properties.get( name );
            if( type == mapping.type ){                
                return mapping;
            }                                    
        }
        
        mapping = (Mapping) definitions.get( type );
        if( mapping != null ) {
            return mapping;
        }        
        
        for( Iterator i=definitions.values().iterator(); i.hasNext();){
            mapping = (Mapping) i.next();
            if( mapping.type.isAssignableFrom( type )){               
                return mapping;
            }
        }
        for( Iterator i=schemas.values().iterator(); i.hasNext();){
            mapping = (Mapping) i.next();
            if( mapping.type == type ){               
                return mapping;
            }
        }        
        for( Iterator i=schemas.values().iterator(); i.hasNext();){
            mapping = (Mapping) i.next();
            if( mapping.type.isAssignableFrom( type )){              
                return mapping;
            }
        }
        return null;
    }
    
    public static boolean isXMLSchemaElement(String s) {
    	return search(xmlSchemaTypes, s);
    }

    public static boolean isGMLSchemaElement(String s) {
        return search(gmlTypes, s);
    }

    public static boolean isGMLAbstractSchemaElement(String s) {
        return search(baseGmlTypes, s);
    }

    private static boolean search(String[] a, String s) {
        // deal with namespace
        int t = s.lastIndexOf(":");

        if (t != -1) {
System.out.println("$$$$$"+s+"^"+s.substring(t+1)+"^");
            s = s.substring(t+1);
        }

        //find s in list 
        for (int i = 0; i < a.length; i++){
            if (a[i].equals(s)) {
                return true;
            }
System.out.println("FALSE"+a[i]+"$$"+s+"$$");
        }
        return false;
    }

    public static String[] getGmlAbstractTypes(boolean namespace) {
        if (namespace) {
            return addNS(baseGmlTypes, "gml");
        }

        return baseGmlTypes;
    }

    public static String[] getGmlTypes(boolean namespace) {
        if (namespace) {
            return addNS(gmlTypes, "gml");
        }

        return gmlTypes;
    }

    public static String[] getXmlSchemaTypes(boolean namespace) {
        if (namespace) {
            return addNS(xmlSchemaTypes, "xs");
        }

        return xmlSchemaTypes;
    }

    private static String[] addNS(String[] a, String ns) {
        String[] b = new String[a.length];

        for (int i = 0; i < a.length; i++)
            b[i] = ns + ":" + a[i];

        return b;
    }
    
    /**
     * Used to store Java/GML type mappings for use with
     * GMLUtils.
     * 
     * @author jgarnett, Refractions Research, Inc.
     * @author $Author: dmzwiers $ (last modification)
     * @version $Id: GMLUtils.java,v 1.6 2004/01/18 00:33:21 dmzwiers Exp $
     */
    public static class Mapping {
        public final String prefix; // gml or xs
        public final String schema; // int or PointPropertyType 
        public final String name; // pointProperty - onlyed used by PropertyType
        public final Class type;    // Java class that best represents this
        
        public Mapping( String xmlSchema ){
            this( xmlSchema, Object.class );           
        }
        public Mapping( String xmlSchema, Class type ){
            String split[] = xmlSchema.split(":");
            this.prefix = split[0];
            this.schema = split[1];
            if(schema.endsWith("Type"))
            	name = schema.substring(0,schema.length()-4);
            else name = "";
            this.type = type;
        }
        public String toString() {
            return prefix+":"+schema;
        }
    }
    
}