/* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root 
 * application directory.
 */
package org.vfny.geoserver.responses;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import org.vfny.geoserver.config.ConfigInfo;
import org.vfny.geoserver.config.TypeInfo;

/**
 * Builds the GML response using standard, simple public methods.
 *
 * <p>This class acts as a "smart" string buffer in which to hold the final 
 * output and spends most of its time calling methods of member classes.  Note 
 * that this class does not guarantee that the created GML will be either 
 * valid or well formed.  Enclosing classes must call <code>GMLBuilder</code> 
 * methods in the correct sequence and with correct inputs in order to 
 * guarantee valid GML output.  The GMLBuilder class simply creates a 
 * convenient shell with its own internal common XML to assist with the GML 
 * generation process; it is not a replacement for a thoughtful GetFeature 
 * class.</p>
 *
 * <p>The member classes inside GMLBuilder all correspond to specific GML 
 * elements:<ul>
 * <li><code>FeatureTypeWriter</code>: feature collection (type)
 * <li><code>MemberWriter</code>: feature member
 * <li><code>AttributeWriter</code>: generic schema attribute (ie. W3C schema 
 * simple element of any type)
 * <li><code>GeometryWriter</code>: OGC geometry type (including collections)
 * </ul></p>
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @author Lassi Letho, Finnish Geodetic Institute
 * @version $VERSION$
 * @tasks TODO: Throw this class out, it's a mess.  Use a visitor, 
 * do it in geotools.
 */
public class GMLBuilder {

    /** Class logger */
    private static Logger LOGGER = 
        Logger.getLogger("org.vfny.geoserver.responses");

    /** Gets global server configuration information **/
    private ConfigInfo configInfo = ConfigInfo.getInstance();
    
    /** Spatial reference system for this response **/
    private String srs;
    
    /** Configures and writes feature type information **/
    private FeatureTypeWriter featureTypeWriter = new FeatureTypeWriter();
    
    /** Configures and writes feature member information **/
    private FeatureMemberWriter featureMemberWriter = 
        new FeatureMemberWriter();
    
    /** Configures and writes attribute information **/
    private AttributeWriter attributeWriter = new AttributeWriter();
    
    /** Configures and writes geometry information **/
    private GeometryWriter geometryWriter = new GeometryWriter();
    
    /** Final output buffer for this response **/
    private StringBuffer finalResult = new StringBuffer(20000);
    
    /** Running total of maximum features allowed for this response **/
    private int maxFeatures = 1000;
    
    /** Sets level of indendation and documentation for response **/
    private boolean verbose;

    private NumberFormat coordFormatter = NumberFormat.getInstance(Locale.US);

    private static final String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>";
    
    private static final String FEATURE_COLL_HEAD = "<wfs:FeatureCollection ";

    private static final String XMLNS_GML = 
	"xmlns:gml=\"http://www.opengis.net/gml\"";

    private static final String WFS_URI = "http://www.opengis.net/wfs";

    private static final String XMLNS_WFS = "xmlns:wfs=\"" + WFS_URI + "\"";

    //TODO: keep copies of these files locally, just reference them for
    //schema location instead of the opengis ones.
    private static final String WFS_LOC = 
	"http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd";

      private static final String SCHEMA_URI = "\"http://www.w3.org/2001/XMLSchema-instance\"";

    private static final String XS_NAMESPACE = "xmlns:xs=" + SCHEMA_URI;

    private static Map gmlMap = new HashMap();
    
    static {
	gmlMap.put("pointProperty", "gml:pointProperty");
	gmlMap.put("geometryProperty", "gml:geometryProperty");
	gmlMap.put("polygonProperty", "gml:polygonProperty");
	gmlMap.put("lineStringProperty", "gml:lineStringProperty");
	gmlMap.put("multiPointProperty", "gml:multiPointProperty");
	gmlMap.put("multiLineStringProperty", "gml:multiLineStringProperty");
	gmlMap.put("multiPolygonProperty", "gml:multiPolygonProperty");
	gmlMap.put("gmldescription", "gml:description");
	gmlMap.put("gmlname", "gml:name");
    }

    /**
     * Constructor to set verbosity
     * @param verbose Sets level of indendation and documentation for response
     */ 
    public GMLBuilder(boolean verbose) {
        this.verbose = verbose;
	if (coordFormatter instanceof DecimalFormat) {
	    ((DecimalFormat)coordFormatter).applyPattern("#.################");
	}
        finalResult.append(XML_HEADER);
	if (verbose){
	    finalResult.append("\n");
	}
    }
    
    
    /**
     * Adds a feature type start tag
     * @param featureType The GML feature type name.
     */ 
    public void initializeFeatureType (String featureType) {        
        // initialize both feature members and attributes with the feature type
        featureMemberWriter.initialize(featureType);
        attributeWriter.initialize(featureType);                
        
        // add feature type tag

    }

    /**
     * Adds the xml namespaces and FeatureCollection tag.
     */
    public void startFeatureCollection(String srs, TypeInfo typeInfo) {
	//hashmap of srs's, each holds its own string buffer, so we can
	//have multiple fcs, then they all combine at the end?
	featureTypeWriter.start(srs, typeInfo);        
	//for now we'll just hack - put all in the first srs.
    }

    /**
     * Adds a feature type end tag
     */ 
    public void endFeatureCollection () {
        featureTypeWriter.end();
    }
    
    
    /**
     * Add an attribute start and end tag, with enclosed attribtue value
     * @param name Attribute name
     * @param value Attribute value
     */ 
    public void addAttribute (String name, String value) {
        attributeWriter.write( name, value );
    }
    
    
    /**
     * Add XML to start a feature
     * @param fid Unique feature identifier
     */ 
    public void startFeature (String fid) {
        featureMemberWriter.start( fid );
    }
    
    
    /**
     * Add XML to end a feature
     */ 
    public void endFeature () {
        featureMemberWriter.end();
    }
    
    
    /**
     * Add an entire geometry of any type, including collections
     * @param geometry Geometry to add (may be a collection)
     * @param gid Geographic ID 
     */
    public void addGeometry(Geometry geometry, String gid) {
        geometryWriter.writeGeometry(geometry, gid);
    }

    
    /**
     * Initialize geometry object.  This method should be called once, before 
     * addGeometry.
     * @param geometry Geometry to initialize (may be a collection)
     * @param featureType Feature type name 
     * @param srs Spactial reference system for all geometries to be added
     * @param tagName Tag name for all geometries to be added
     */
    public void initializeGeometry(Class geometry, String featureType, 
                                   String srs, String tagName) {
        geometryWriter.initializeGeometry( geometry, featureType, srs,tagName);
    }
    
    
    /**
     * Return final GML object.
     *
     */ 
    public String getGML () {
        return finalResult.toString();
    }
    

    /**
     * Handles the feature type writing tasks for the main class.
     */
    private class FeatureTypeWriter {
              
	private int boxInsertPos; //= FEATURE_COLL_INIT.length() + 
	//XML_HEADER.length();

	private Geometry envelope;

	private String srs;

        /** No argument contructor. */ 
        public FeatureTypeWriter() {}        
        
        /**
         * Writes the feature type start tag, with differing levels of 
         * verbosity.
         * @param srs Spactial reference system for the bounding box
         */ 
        public void start(String srs, TypeInfo typeInfo) {
	    String xmlns = typeInfo.getXmlns();
            this.srs = srs;
	    String indent = ((verbose) ? "\n   " : " ");
	    String schIndent = indent + (verbose ? "                " : "");
	    finalResult.append(FEATURE_COLL_HEAD);
            finalResult.append(indent + "xmlns=\"" + xmlns + "\"");
	    finalResult.append(indent + XMLNS_GML);
	    finalResult.append(indent + XMLNS_WFS);
	    finalResult.append(indent + XS_NAMESPACE);
	    finalResult.append(indent + "xs:schemaLocation=\"" + xmlns + " " );
	    //REVISIT: describe request for all types in namespace?  That
	    //would fix the extension problem, like when there is an abstract
	    //type defined that all derive from.
            finalResult.append(schIndent  + configInfo.getUrl() + 
	    	 "/DescribeFeatureType?" //HACK: bad hard code here.
	    	 + "typeName=" + typeInfo.getFullName());
	    finalResult.append(schIndent + WFS_URI + schIndent + WFS_LOC + "\">");
	    boxInsertPos = finalResult.length();
	}
        
        /**
         * Writes an end tag for the feature collection/type.
         */ 
        public void end() {
	    Envelope geomEnv = geometryWriter.getEnvelope();
	    StringBuffer bbox =  new StringBuffer();
	    if (verbose) bbox.append("\n   ");
	    bbox.append("<gml:boundedBy>");
	    if (verbose) bbox.append("\n    ");
	    bbox.append("<gml:Box>");
	    if (verbose) bbox.append("\n     ");
	    bbox.append("<gml:coordinates>");
	    //REVISIT: we use default cs,ds ect. for coordinates.  Should we
	    //allow user configurable?  They can set it, we do it here?
	    bbox.append(coordFormatter.format(geomEnv.getMinX()) + "," + 
			coordFormatter.format(geomEnv.getMinY()) + " ");
	    bbox.append(coordFormatter.format(geomEnv.getMaxX()) + "," + 
			coordFormatter.format(geomEnv.getMaxY()));
	    bbox.append("</gml:coordinates>");
	    if (verbose) bbox.append("\n    ");
	    bbox.append("</gml:Box>");
	    if (verbose) bbox.append("\n   ");
	    bbox.append("</gml:boundedBy>");
	    finalResult.insert(boxInsertPos, bbox.toString());
	    if (verbose) finalResult.append("\n");
	    finalResult.append( "</wfs:FeatureCollection>" );
	}
	
    
    }
    /**
     * Handles the feature member writing tasks for the main class.
     *
     */
    private class FeatureMemberWriter {        
        /** XML fragment preceeding GID  **/
        private String featureMemberStart1;
        /** XML fragment ending GID **/
        private String featureMemberStart2;
        /** XML fragment closing tag **/
        private String featureMemberEnd;
        
        
        /**
         * No argument contructor.
         */ 
        public FeatureMemberWriter () {
        }
        
        
        /**
         * Initializes the feature member tags by feature type.
         * @param featureType Feature type name
         */ 
        public void initialize( String featureType ) {
            
            if(verbose) {
                featureMemberStart1 = "\n   <gml:featureMember>\n    <"  + 
                    featureType + " fid=\"";
                featureMemberStart2 = "\">";                                
                featureMemberEnd = "\n    </"  + featureType + 
                    ">\n   </gml:featureMember>";
            } else {                
                featureMemberStart1 = "<gml:featureMember><"  + 
                    featureType + " fid=\"";
                featureMemberStart2 = "\">";
                featureMemberEnd = "</"  + featureType + 
                    "></gml:featureMember>";
            }
        }
                
        /**
         * Writes start tag for a feature member
         * @param fid Feature ID (optional in GML specification)
         */ 
        public void start( String fid ) {
            finalResult.append(featureMemberStart1).append(fid).
                append(featureMemberStart2);
        }
                
        /**
         * Writes the end tag for the feature member
         */ 
        public void end() {
            finalResult.append( featureMemberEnd );
            maxFeatures--;
        }
        
    }
        
    /**
     * Handles the attribute writing tasks for the main class.
     */
    private class AttributeWriter {
        
        /** XML fragment **/
        private String attribute1;        
        /** XML fragment **/
        private String attribute2;
        /** XML fragment **/
        private String attribute3;
        /** XML fragment **/
        private String attribute4;
                
        /**
         * Writes the end tag for the feature member
         * @param featureType Feature collection type
         */ 
        public void initialize( String featureType ) {            
            attribute1 = verbose ? "\n      <" :"<";
            attribute2 = ">";
            attribute3 = "</";
            attribute4 = ">";
        }
                
        /**
         * Writes the end tag for the feature member
         * @param name Attribute name
         * @param value Attribute value as string
         */ 
        public void write( String name, String value ) { 
	    //should check if mandatory here, 
            if (value != null && !value.equals("")) {
		String gmlName = (String)gmlMap.get(name);
		if (gmlName != null) {
		    name = gmlName;
		}
		finalResult.append(attribute1).append(name);
		//If null and mandatory should prepend nil.
		//TODO: figure out minOccurs/nillable.
		if (value.equals("")) {
		    //should check attributeType for nillable.
		    finalResult.append(" xs:nil=\"true\"");
		}
		finalResult.append(attribute2).
                append(value).append(attribute3).append(name).
                append(attribute4);
	    } else {
		//if null don't write anything.
	    }
        
	}
    }
    /**
     * Handles the geometry writing tasks for the main class.
     */
    private class GeometryWriter {
                
        /** Internal representation of OGC SF Point **/
        private static final int POINT = 1;        
        /** Internal representation of OGC SF LineString **/
        private static final int LINESTRING = 2;        
        /** Internal representation of OGC SF Polygon **/
        private static final int POLYGON = 3;
        /** Internal representation of OGC SF MultiPoint **/
        private static final int MULTIPOINT = 4;        
        /** Internal representation of OGC SF MultiLineString **/
        private static final int MULTILINESTRING = 5;        
        /** Internal representation of OGC SF MultiPolygon **/
        private static final int MULTIPOLYGON = 6;        
        /** Internal representation of OGC SF MultiGeometry **/
        private static final int MULTIGEOMETRY = 7;        

	private static final String GEOM_OFFSET = "\n        ";


	
        /** XML fragment for any geometry type **/
        private String abstractGeometryStart1;        
        /** XML fragment for any geometry type **/
        private String abstractGeometryStart2;        
        /** XML fragment for any geometry type **/
        private String abstractGeometryEnd;        
        /** XML fragment for coordinate type **/
        private String coordinatesStart;        
        /** XML fragment for coordinate type **/
        private String coordinatesEnd;        
        /** XML fragment for coord type **/
        private String coordStart;        
        /** XML fragment for coord type **/
        private String coordEnd;
        
	

        /** Internal representation of coordinate delimeter (',' for GML is 
         * default) **/
        private String coordinateDelimeter = ",";
        
        /** Internal representation of tuple delimeter (' ' for GML is 
         * default) **/
        private String tupleDelimeter = " ";
        
        /** Memory for last geometry initialized **/
        private int geometryType = -1;

	private Envelope envelope = new Envelope();

        /** Empty constructor */ 
        public GeometryWriter() {}
                
        /**
         * Initializes a specific geometry for later writing.  If the 
         * initialized geometry and subsequent calls do not match, a run 
         * time exception will be thrown, since geometry type is not checked 
         * by internal methods.
         * @param geometry OGC SF type
         * @param featureType Feature collection type
         * @param srs Spatial reference system for the geometry
         * @param tagName Geometry tag name
         */ 
        private void initializeGeometry(Class geometry, String featureType, 
                                        String srs, String tagName) { 
            String geometryName = "";
            LOGGER.finest("checking type: " + geometry.toString());
	    String gmlName = (String)gmlMap.get(tagName);
	    if (gmlName != null) {
		tagName = gmlName;
	    }

            // set internal geometry representation
            if( geometry.equals(Point.class) ) {
                LOGGER.finest("found point");
                geometryType = POINT;
                geometryName = "Point";
            }
            else if( geometry.equals(LineString.class) ) {
                LOGGER.finest("found linestring");
                geometryType = LINESTRING;
                geometryName = "LineString";
            }
            else if( geometry.equals(Polygon.class) ) {
                LOGGER.finest("found polygon");
                geometryType = POLYGON;
                geometryName = "Polygon";
            }
            else if( geometry.equals(MultiPoint.class) ) {
                LOGGER.finest("found multi");
                geometryType = MULTIPOINT;
                geometryName = "MultiPoint";
            }
            else if( geometry.equals(MultiLineString.class) ) {
                geometryType = MULTILINESTRING;
                geometryName = "MultiLineString";
            }
            else if( geometry.equals(MultiPolygon.class) ) {
                geometryType = MULTIPOLYGON;
                geometryName = "MultiPolygon";
            }
            else if( geometry.equals(GeometryCollection.class) ) {
                geometryType = MULTIGEOMETRY;
                geometryName = "GeometryCollection";
            }
            
            
            // initialize the GML return parameter (if verbose)
            if(verbose) {
                // the start tags for the geometry (up to coordinates)
                abstractGeometryStart1 = "\n      <" + 
                    tagName + ">\n       <gml:" + geometryName + " gid=\"";
                abstractGeometryStart2 = "\" srsName=\"http://www.opengis.net"
                    + "/gml/srs/epsg.xml#" + srs + "\">";
               
                // post-coordinate end tags
                abstractGeometryEnd = "\n       </gml:" + geometryName + 
                    ">\n      </" + tagName + ">";
                // coordinates start tags
                coordinatesStart = "\n         <gml:coordinates decimal=\".\" cs=\",\" " +
                    "ts=\" \">";                             
                // coordinate end tags
                coordinatesEnd = "</gml:coordinates>";
            }
            else {                
                // the start tags for the geometry (up to coordinates)
                abstractGeometryStart1 = "<" + 
                    tagName + "><gml:" + geometryName + " gid=\"";
                abstractGeometryStart2 = "\" srsName=\"http://www.opengis.net"
                    + "/gml/srs/epsg.xml#" + srs + "\">";
                
                // the start tags for the geometry (up to coordinates)
                abstractGeometryEnd = "</gml:" + geometryName + "></" + 
                    tagName + ">";
                
                // coordinates start tags
                coordinatesStart = "<gml:coordinates>";             
                
                // coordinate end tags
                coordinatesEnd = "</gml:coordinates>";
            }
        }

	public Envelope getEnvelope(){
	    return envelope;
	}

	//call this automatically in init geometry?  
	public void resetEnvelope(){
	    envelope.init();
	}
        
        /**
         * Passes off geometry writing duties to correct method.
         * @param geometry OGC SF type
         * @param gid Feature collection type
         */ 
        private void writeGeometry(Geometry geometry, String gid) {
	    //user option to just use user defined bbox for whole dataset?
	    envelope.expandToInclude(geometry.getEnvelopeInternal());
	    switch(geometryType) {                                
            case POINT:
                writePoint((Point) geometry, gid);
                break;
            case LINESTRING:
                writeLineString((LineString) geometry, gid);
                break;
            case POLYGON:
                writePolygon((Polygon) geometry, gid);
                break;
            case MULTIPOINT:
                writeMultiPoint((GeometryCollection) geometry, gid);
                break;
            case MULTILINESTRING:
                writeMultiLineString((GeometryCollection) geometry, gid);
                break;
            case MULTIPOLYGON:
                writeMultiPolygon((GeometryCollection) geometry, gid);
                break;
            case MULTIGEOMETRY:
               writeMultiGeometry((GeometryCollection) geometry, gid);
                break;
            }
        }
        
        /**
         * Writes a point geometry.
         * @param geometry OGC SF type
         * @param gid Geometric ID
         */ 
        private void writePoint(Point geometry, String gid) {
            finalResult.append(abstractGeometryStart1 + gid + 
                               abstractGeometryStart2);
            writeCoordinates(geometry);
            finalResult.append( abstractGeometryEnd );
        }

        /**
         * Writes an internal (terse, without GID) point geometry.
         * @param geometry OGC SF Point type
         */ 
        private void writePoint(Point geometry) {            
	    if (verbose) finalResult.append(GEOM_OFFSET);
            finalResult.append( "<gml:" +geometry.getGeometryType()+">");
            writeCoordinates(geometry);
	    if (verbose) finalResult.append(GEOM_OFFSET);
            finalResult.append( "</gml:" + geometry.getGeometryType() + 
                                ">" );
        }
                
        /**
         * Writes a MultiPoint geometry.
         * @param geometry OGC SF MultiPoint type
         * @param gid Geometric ID
         */ 
        private void writeMultiPoint(GeometryCollection geometry, String gid) {
            finalResult.append(abstractGeometryStart1 + gid + 
                               abstractGeometryStart2 );
            for( int i = 0 ; i < geometry.getNumGeometries() ; i++ ) {
		if (verbose) finalResult.append(GEOM_OFFSET);
                finalResult.append("<gml:pointMember>");
                writePoint( (Point) geometry.getGeometryN(i) );
		if (verbose) finalResult.append(GEOM_OFFSET);
                finalResult.append("</gml:pointMember>");
            }
            finalResult.append( abstractGeometryEnd );
        }        
        
        /**
         * Writes a LineString geometry.
         * @param geometry OGC SF LineString type
         * @param gid Geometric ID
         */ 
        private void writeLineString(LineString geometry, String gid) {
            finalResult.append(abstractGeometryStart1 + gid + 
                               abstractGeometryStart2);
            writeCoordinates(geometry);
            finalResult.append( abstractGeometryEnd );
        }
        
        /**
         * Writes an internal (terse, without GID) LineString geometry.
         * @param geometry OGC SF LineString type
         */ 
        private void writeLineString(LineString geometry) {            
	    if (verbose) finalResult.append(GEOM_OFFSET);
            finalResult.append( "<gml:" + geometry.getGeometryType() + 
                                ">");
            writeCoordinates(geometry);
	    if (verbose) finalResult.append(GEOM_OFFSET);
            finalResult.append( "</gml:" + geometry.getGeometryType() + 
                                ">");
        }
                
        /**
         * Writes a MultiLineString geometry.
         * @param geometry OGC SF MultiLineString type
         */ 
        private void writeMultiLineString(GeometryCollection geometry, 
                                          String gid) {
            finalResult.append(abstractGeometryStart1 + gid + 
                               abstractGeometryStart2);
            for(int i = 0, n = geometry.getNumGeometries(); i < n; i++) {
                if (verbose) finalResult.append(GEOM_OFFSET);
		finalResult.append("<gml:lineStringMember>");
                writeLineString( (LineString) geometry.getGeometryN(i) );
		if (verbose) finalResult.append(GEOM_OFFSET);
                finalResult.append("</gml:lineStringMember>");
            }
            finalResult.append( abstractGeometryEnd );
        }        

        /**
         * Writes a Polygon geometry.
         * @param geometry OGC SF Polygon type
         * @param gid Geometric ID
         */ 
        private void writePolygon(Polygon geometry, String gid) {
	    if (verbose) {
		 finalResult.append(abstractGeometryStart1 + gid + 
                               abstractGeometryStart2 );
		 finalResult.append(GEOM_OFFSET + "<gml:outerBoundaryIs>" +
				    GEOM_OFFSET +" <gml:LinearRing>");
		 writeCoordinates(geometry.getExteriorRing());
		 finalResult.append(GEOM_OFFSET + " </gml:LinearRing>" + 
			    GEOM_OFFSET + "</gml:outerBoundaryIs>");            
		 if (geometry.getNumInteriorRing() > 0) {
		    
		     for( int i = 0 ; i < geometry.getNumInteriorRing() ; i++ ) {
			 finalResult.append("\n         <gml:innerBoundaryIs>");
			 finalResult.append("\n          <gml:LinearRing>");
			 writeCoordinates( geometry.getInteriorRingN(i) );
			 finalResult.append("\n          </gml:LinearRing>");
			 finalResult.append("\n        </gml:innerBoundaryIs>");
		     }

		 }
		 finalResult.append( abstractGeometryEnd );
	    } else {

            finalResult.append(abstractGeometryStart1 + gid + 
                               abstractGeometryStart2 );
            finalResult.append("<gml:outerBoundaryIs><gml:LinearRing" +
                               ">");
            writeCoordinates(geometry.getExteriorRing());
            finalResult.append("</gml:LinearRing></gml:" + 
                               "outerBoundaryIs>");            
            if (geometry.getNumInteriorRing() > 0) {
		for( int i = 0 ; i < geometry.getNumInteriorRing() ; i++ ) {
		    finalResult.append("<gml:innerBoundaryIs>");
		    finalResult.append("<gml:LinearRing>");
                    writeCoordinates( geometry.getInteriorRingN(i) );
		    finalResult.append("</gml:LinearRing>");
		    finalResult.append("</gml:innerBoundaryIs>");
		}
            }
            finalResult.append( abstractGeometryEnd );
	    }
	}

        /**
         * Writes an internal (terse, without GID) Polygon geometry.
         * @param geometry OGC SF Polygon type
         */ 
        private void writePolygon(Polygon geometry) {            
            finalResult.append("<gml:" + geometry.getGeometryType() + 
                               ">" );
            finalResult.append("<gml:outerBoundaryIs><gml:" +
                               "LinearRing>");
            writeCoordinates(geometry.getExteriorRing());
            finalResult.append("</gml:LinearRing></gml:" +
                               "outerBoundaryIs>");            
            if ( geometry.getNumInteriorRing() > 0 ) {
                finalResult.append("<gml:innerBoundaryIs>");
                for( int i = 0 ; i < geometry.getNumInteriorRing() ; i++ ) {
                    finalResult.append("<gml:LinearRing>");
                    writeCoordinates( geometry.getInteriorRingN(i) );
                    finalResult.append("</gml:LinearRing>");
                }
                finalResult.append("</gml:innerBoundaryIs>");
            }            
            finalResult.append( "</gml:" + geometry.getGeometryType() 
                                + ">" );
        }
                
        /**
         * Writes a MultiPolygon geometry.
         * @param geometry OGC SF MultiPolygon type
         * @param gid Geometric ID
         */ 
        private void writeMultiPolygon(GeometryCollection geometry, 
                                       String gid) {
            finalResult.append( abstractGeometryStart1 + gid + 
                                abstractGeometryStart2);
            for(int i = 0, n = geometry.getNumGeometries(); i < n; i++) {
		if (verbose) finalResult.append(GEOM_OFFSET);
                finalResult.append("<gml:polygonMember>");
                writePolygon((Polygon) geometry.getGeometryN(i));
		if (verbose) finalResult.append(GEOM_OFFSET);
                finalResult.append("</gml:polygonMember>");
            }            
            finalResult.append( abstractGeometryEnd );
        }        
        
        /**
         * Writes a MultiGeometry geometry.
         * @param geometry OGC SF MultiGeometry type
         * @param gid Geometric ID
         */ 
        private void writeMultiGeometry(GeometryCollection geometry, 
                                        String gid) {            
            finalResult.append( abstractGeometryStart1 + gid + 
                                abstractGeometryStart2 );            
            for(int i = 0, n = geometry.getNumGeometries(); i < n; i++) {
                if( geometry.getGeometryType().equals("Point") ) {
		    if (verbose) finalResult.append(GEOM_OFFSET);
                    finalResult.append("<gml:pointMember>");
                    writePoint( (Point) geometry.getGeometryN(i) );
		    if (verbose) finalResult.append(GEOM_OFFSET);
                    finalResult.append("</gml:pointMember>");
                }
                if( geometry.getGeometryType().equals("LineString") ) {
		    if (verbose) finalResult.append(GEOM_OFFSET);
                    finalResult.append("<gml:lineStringMember>");
                    writeLineString( (LineString) geometry.getGeometryN(i) );
		    if (verbose) finalResult.append(GEOM_OFFSET);
                    finalResult.append("</gml:lineStringMember>");
                }
                if( geometry.getGeometryType().equals("Polygon") ) {
		    if (verbose) finalResult.append(GEOM_OFFSET);
                    finalResult.append("<gml:polygonMember>");
                    writePolygon( (Polygon) geometry.getGeometryN(i) );
		    if (verbose) finalResult.append(GEOM_OFFSET);
                    finalResult.append("</gml:polygonMember>");
                }
            }            
            finalResult.append( abstractGeometryEnd );
        }
        
        
        /**
         * Writes coordinates from an arbitrary geometry type.
         * @param geometry OGC SF Geometry
         */ 
        private void writeCoordinates(Geometry geometry) {            
            int dimension = geometry.getDimension();
            Coordinate[] tempCoordinates = geometry.getCoordinates(); 
            finalResult.append( coordinatesStart );            
            for(int i = 0, n = geometry.getNumPoints(); i < n; i++) {
	        String xCoord = coordFormatter.format(tempCoordinates[i].x);
		String yCoord = coordFormatter.format(tempCoordinates[i].y);
		finalResult.append( xCoord + coordinateDelimeter + yCoord + 
				    tupleDelimeter);
            }
            finalResult.deleteCharAt( finalResult.length() - 1 );            
            finalResult.append( coordinatesEnd );
        }
    }
}
