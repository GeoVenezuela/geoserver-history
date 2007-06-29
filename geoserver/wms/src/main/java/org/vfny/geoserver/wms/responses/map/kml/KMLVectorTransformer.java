/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.map.kml;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.template.FeatureWrapper;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.map.MapLayer;
import org.geotools.renderer.style.LineStyle2D;
import org.geotools.renderer.style.MarkStyle2D;
import org.geotools.renderer.style.PolygonStyle2D;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style2D;
import org.geotools.renderer.style.TextStyle2D;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.NumberRange;
import org.geotools.xml.transform.Translator;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.vfny.geoserver.global.GeoServer;
import org.vfny.geoserver.wms.WMSMapContext;
import org.vfny.geoserver.wms.responses.featureInfo.FeatureTemplate;
import org.vfny.geoserver.wms.responses.map.kml.KMLGeometryTransformer.KMLGeometryTranslator;
import org.xml.sax.ContentHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import freemarker.template.Configuration;


/**
 * Transforms a feature collection to a kml document consisting of nested
 * "Style" and "Placemark" elements for each feature in the collection.
 * <p>
 * Usage:
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class KMLVectorTransformer extends KMLTransformerBase {
    /**
     * logger
     */
    static Logger LOGGER = Logger.getLogger("org.geoserver.kml");

    /**
     * The template configuration used for placemark descriptions
     */
    static Configuration templateConfig;

    static {
        //initialize the template engine, this is static to maintain a cache 
        // over instantiations of kml writer
        templateConfig = new Configuration();
        templateConfig.setObjectWrapper(new FeatureWrapper());
    }

    /**
     * Tolerance used to compare doubles for equality
     */
    static final double TOLERANCE = 1e-6;

    /**
     * The map context
     */
    final WMSMapContext mapContext;

    /**
     * The map layer being transformed
     */
    final MapLayer mapLayer;

    /**
     * The scale denominator.
     *
     * TODO: calcuate a real value based on image size to bbox ratio, as image
     * size has no meanining for KML yet this is a fudge.
     */
    double scaleDenominator = 1;
    //NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);

    /**
     * used to create 2d style objects for features
     */
    SLDStyleFactory styleFactory = new SLDStyleFactory();
    
    /**
     * Caching the feature template
     */
    FeatureTemplate template = new FeatureTemplate();

    public KMLVectorTransformer(WMSMapContext mapContext, MapLayer mapLayer) {
        this.mapContext = mapContext;
        this.mapLayer = mapLayer;

        setNamespaceDeclarationEnabled(false);
    }

  /**
     * Sets the scale denominator.
     */
    public void setScaleDenominator(double scaleDenominator) {
        this.scaleDenominator = scaleDenominator;
    }

    public Translator createTranslator(ContentHandler handler) {
        return new KMLTranslator(handler);
    }

    class KMLTranslator extends KMLTranslatorSupport {
        /**
         * Geometry transformer
         */
        KMLGeometryTransformer.KMLGeometryTranslator geometryTranslator;

        public KMLTranslator(ContentHandler contentHandler) {
            //super(contentHandler, "kml", "http://earth.google.com/kml/2.0" );
            super(contentHandler);

            KMLGeometryTransformer geometryTransformer = new KMLGeometryTransformer();
            //geometryTransformer.setUseDummyZ( true );
            geometryTransformer.setOmitXMLDeclaration(true);
            geometryTransformer.setNamespaceDeclarationEnabled(true);

            GeoServer config = mapContext.getRequest().getGeoServer();
            geometryTransformer.setNumDecimals(config.getNumDecimals());

            geometryTranslator = (KMLGeometryTranslator) geometryTransformer.createTranslator(contentHandler);
        }

        public void encode(Object o) throws IllegalArgumentException {
            FeatureCollection features = (FeatureCollection) o;
            FeatureType featureType = features.getSchema();

            if (isStandAlone()) {
            	start( "kml" );
            }
            
            //start the root document, name it the name of the layer
            start("Document");
            element("name", mapLayer.getTitle());
            
            //get the styles for hte layer
            FeatureTypeStyle[] featureTypeStyles = filterFeatureTypeStyles(mapLayer.getStyle(),
                    featureType);

            encode( features, featureTypeStyles );
//            for (int i = 0; i < featureTypeStyles.length; i++) {
//                encode(features, featureTypeStyles[i]);
//            }

            //encode the legend
            //encodeLegendScreenOverlay();
            
            end("Document");
            
            if (isStandAlone()) {
            	end( "kml" );
            }
        }

        protected void encode(FeatureCollection features, FeatureTypeStyle[] styles) {
        	//grab a feader and process
            FeatureIterator reader = features.features();

            try {
                while (reader.hasNext()) {
                    Feature feature = (Feature) reader.next();

                    try {
                        encode(feature, styles);
                    } catch (Throwable t) {
                        //TODO: perhaps rethrow hte exception
                        String msg = "Failure tranforming feature to KML:" + feature.getID();
                        LOGGER.log(Level.WARNING, msg, t);
                    }
                }
            } finally {
                //make sure we always close
                features.close(reader);
            }
        }

        protected void encode(Feature feature, FeatureTypeStyle[] styles) {
            //get the feature id
            String featureId = featureId(feature);

            //start the document
            //start("Document");

            //element("name", featureId);
            //element("title", mapLayer.getTitle());

            //encode the styles, keep track of any labels provided by the 
            // styles
            encodeStyle(feature, styles);

            encodePlacemark(feature, styles);

            //end("Document");
        }

        /**
         * Encodes the provided set of rules as KML styles.
         */
        protected void encodeStyle(Feature feature, FeatureTypeStyle[] styles) {
            //start the style
            start("Style",
                KMLUtils.attributes(new String[] { "id", "GeoServerStyle" + feature.getID() }));

            //encode the icon
            encodeIconStyle(feature, styles);
            
            //encode hte Line/Poly styles
            for ( int j = 0; j < styles.length ; j++ ) {
            	Rule[] rules = filterRules(styles[j], feature);

                for (int i = 0; i < rules.length; i++) {
                    encodeStyle(feature, rules[i].getSymbolizers());
                }

            }
            
            //end the style
            end("Style");
        }

        /**
         * Encodes an IconStyle for a feature.
         */
        protected void encodeIconStyle(Feature feature, FeatureTypeStyle[] styles ) {
        	  //encode the style for the icon
            //start IconStyle
            start("IconStyle");

            //make transparent if they didn't ask for attributes
            if (!mapContext.getRequest().getKMattr()) {
                encodeColor("00ffffff");
            }

            //start Icon
            start("Icon");

            if (((feature.getDefaultGeometry() != null)
                    && feature.getDefaultGeometry() instanceof Point)
                    || feature.getDefaultGeometry() instanceof MultiPoint) {
                //do nothing, this is handled by encodePointStyle
            } else if ((feature.getDefaultGeometry() != null)
                    && (feature.getDefaultGeometry() instanceof LineString
                    || feature.getDefaultGeometry() instanceof MultiLineString)) {
                //line
                element("href", "root://icons/palette-3.png");
                element("x", "160");
                element("y", "0");
                element("w", "32");
                element("h", "32");
            } else if (((feature.getDefaultGeometry() != null)
                    && feature.getDefaultGeometry() instanceof Polygon)
                    || feature.getDefaultGeometry() instanceof MultiPolygon) {
                //polygon
                element("href", "root://icons/palette-3.png");
                element("x", "224");
                element("y", "32");
                element("w", "32");
                element("h", "32");
            } else {
                //default
            }

            end("Icon");

            //end IconStyle
            end("IconStyle");

        }
        
        /**
         * Encodes the provided set of symbolizers as KML styles.
         */
        protected void encodeStyle(Feature feature, Symbolizer[] symbolizers) {
          
            for (int i = 0; i < symbolizers.length; i++) {
                Symbolizer symbolizer = symbolizers[i];
                LOGGER.finer(new StringBuffer("Applying symbolizer ").append(symbolizer).toString());

                //create a 2-D style
                Style2D style = styleFactory.createStyle(
            		feature, symbolizer, new NumberRange( scaleDenominator, scaleDenominator ));

                //split out each type of symbolizer
                if (symbolizer instanceof TextSymbolizer) {
                    encodeTextStyle((TextStyle2D) style, (TextSymbolizer) symbolizer);
                }

                if (symbolizer instanceof PolygonSymbolizer) {
                    encodePolygonStyle((PolygonStyle2D) style, (PolygonSymbolizer) symbolizer);
                }

                if (symbolizer instanceof LineSymbolizer) {
                    encodeLineStyle((LineStyle2D) style, (LineSymbolizer) symbolizer);
                }

                if (symbolizer instanceof PointSymbolizer) {
                    encodePointStyle(style, (PointSymbolizer) symbolizer);
                }
            }
        }

        /**
         * Encodes a KML IconStyle + PolyStyle from a polygon style and symbolizer.
         */
        protected void encodePolygonStyle(PolygonStyle2D style, PolygonSymbolizer symbolizer) {
            //star the polygon style
            start("PolyStyle");

            //fill
            if (symbolizer.getFill() != null) {
                //get opacity
                double opacity = SLD.opacity(symbolizer.getFill());

                if (Double.isNaN(opacity)) {
                    //none specified, default to full opacity
                    opacity = 1.0;
                }

                encodeColor(SLD.color(symbolizer.getFill()), opacity);
            } else {
                //make it transparent
                encodeColor("00aaaaaa");
            }

            //outline
            if (symbolizer.getStroke() != null) {
                element("outline", "1");
            } else {
                element("outline", "0");
            }

            end("PolyStyle");

            //if stroke specified add line style as well
            if (symbolizer.getStroke() != null) {
                start("LineStyle");

                //opacity
                double opacity = SLD.opacity(symbolizer.getStroke());

                if (Double.isNaN(opacity)) {
                    //none specified, default to full opacity
                    opacity = 1.0;
                }

                encodeColor(colorToHex((Color) style.getContour(), opacity));

                //width
                int width = SLD.width(symbolizer.getStroke());

                if (width != SLD.NOTFOUND) {
                    element("width", Integer.toString(width));
                }

                end("LineStyle");
            }
        }

        /**
         * Encodes a KML IconStyle + LineStyle from a polygon style and symbolizer.
         */
        protected void encodeLineStyle(LineStyle2D style, LineSymbolizer symbolizer) {
            start("LineStyle");

            //stroke
            if (symbolizer.getStroke() != null) {
                //opacity
                double opacity = SLD.opacity(symbolizer.getStroke());

                if (Double.isNaN(opacity)) {
                    //default to full opacity
                    opacity = 1.0;
                }

                encodeColor((Color) style.getContour(), opacity);

                //width
                int width = SLD.width(symbolizer.getStroke());

                if (width != SLD.NOTFOUND) {
                    element("width", Integer.toString(width));
                }
            } else {
                //default
                encodeColor("ffaaaaaa");
                element("width", "1");
            }

            end("LineStyle");
        }

        /**
         * Encodes a KML IconStyle from a point style and symbolizer.
         */
        protected void encodePointStyle(Style2D style, PointSymbolizer symbolizer) {
            start("IconStyle");

            if (style instanceof MarkStyle2D) {
                Mark mark = SLD.mark(symbolizer);

                if (mark != null) {
                    double opacity = SLD.opacity(mark.getFill());

                    if (Double.isNaN(opacity)) {
                        //default to full opacity
                        opacity = 1.0;
                    }

                    encodeColor(SLD.color(mark.getFill()), opacity);
                } else {
                    //default
                    encodeColor("ffaaaaaa");
                }
            } else {
                //default
                encodeColor("ffaaaaaa");
            }

            element("colorMode", "normal");

            // placemark icon
            String iconHref = null;

            //if the point symbolizer uses an external graphic use it
            if ((symbolizer.getGraphic() != null)
                    && (symbolizer.getGraphic().getExternalGraphics() != null)
                    && (symbolizer.getGraphic().getExternalGraphics().length > 0)) {
                ExternalGraphic graphic = symbolizer.getGraphic().getExternalGraphics()[0];

                try {
                    if ("file".equals(graphic.getLocation().getProtocol())) {
                        //it is a local file, reference locally from "styles" directory
                        File file = new File(graphic.getLocation().getFile());
                        iconHref = mapContext.getRequest().getBaseUrl() + "styles/"
                            + file.getName();
                    } else {
                        //TODO: should we check for http:// and use it directly?
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error processing external graphic:" + graphic, e);
                }
            }

            if (iconHref == null) {
                iconHref = "root://icons/palette-4.png";
            }

            start("Icon");

            element("href", iconHref);
            element("x", "32");
            element("y", "128");
            element("w", "32");
            element("h", "32");

            end("Icon");

            end("IconStyle");
        }

        /**
         * Encodes a KML LabelStyle from a text style and symbolizer.
         */
        protected void encodeTextStyle(TextStyle2D style, TextSymbolizer symbolizer) {
            start("LabelStyle");

            if (symbolizer.getFill() != null) {
                double opacity = SLD.opacity(symbolizer.getFill());

                if (Double.isNaN(opacity)) {
                    //default to full opacity
                    opacity = 1.0;
                }

                encodeColor(SLD.color(symbolizer.getFill()), opacity);
            } else {
                //default
                encodeColor("ffaaaaaa");
            }

            end("LabelStyle");
        }

        /**
         * Encodes a KML Placemark from a feature and optional name.
         */
        protected void encodePlacemark(Feature feature, FeatureTypeStyle[] styles) {
            Geometry geometry = featureGeometry(feature);
            Coordinate centroid = geometryCentroid(geometry);

            start("Placemark");

            //encode name + description only if kmattr was specified
            if (mapContext.getRequest().getKMattr()) {
            	//name
            	try {
            		encodePlacemarkName( feature, styles );
            	}
            	catch( IOException e ) {
            		String msg = "Error occured processing 'title' template.";
            		LOGGER.log( Level.WARNING, msg, e );
                }
            	
            	//description
                try {
                    encodePlacemarkDescription(feature);
                } catch (IOException e) {
                	String msg = "Error occured processing 'description' template.";
            		LOGGER.log( Level.WARNING, msg, e );
                }
            }
           
            //look at
            encodePlacemarkLookAt(centroid);

            //style reference
            element("styleUrl", "#GeoServerStyle" + feature.getID());

            //geometry
            encodePlacemarkGeometry(geometry, centroid);

            end("Placemark");
        }

        /**
         * Encodes a KML Placemark name from a feature by processing a
         * template.
         */
        protected void encodePlacemarkName(Feature feature, FeatureTypeStyle[] styles )
        	throws IOException {
        	
        	//order to use when figuring out what the name / label of a 
        	// placemark should be:
        	// 1. the title template for features
        	// 2. a text sym with a label from teh sld
        	// 3. nothing ( do not use fid )

        	String title = template.title( feature );	
        	
        	//ensure not empty and != fid
        	if ( title == null || "".equals( title ) || feature.getID().equals( title ) ) {
        		//try sld
        		StringBuffer label = new StringBuffer();
        		for ( int i = 0; i < styles.length; i++ ) {
        			Rule[] rules = filterRules(styles[i], feature );
        			for ( int j = 0; j < rules.length; j++ ) {
        				Symbolizer[] syms = rules[j].getSymbolizers();
        				for ( int k = 0; k < syms.length; k++) {
        					if ( syms[k] instanceof TextSymbolizer ) {
        						Expression e = SLD.textLabel((TextSymbolizer) syms[k]);
                                Object object = e.evaluate(feature);
                                String value = null;

                                if (object instanceof String) {
                                    value = (String) object;
                                } else {
                                    if (object != null) {
                                        value = object.toString();
                                    }
                                }

                                if ((value != null) && !"".equals(value.trim())) {
                                    label.append(value);
                                }
        					}
        				}
        			}
        		}
        		
        		if ( label.length() > 0 ) {
        			title = label.toString();
        		}
        		else {
        			title = null;
        		}
                
        	}
        	
        	if ( title != null ) {
            	start("name");
                cdata(title);
                end("name");	
            }
            
        }
        
        /**
         * Encodes a KML Placemark description from a feature by processing a
         * template.
         */
        protected void encodePlacemarkDescription(Feature feature)
            throws IOException {
            
            String description = template.description( feature );
        
            if (description != null) {
                start("description");
                cdata(description);
                end("description");
            }
        }

        /**
         * Encods a KML Placemark LookAt from a geometry + centroid.
         */
        protected void encodePlacemarkLookAt(Coordinate centroid) {
            start("LookAt");

            element("longitude", Double.toString(centroid.x));
            element("latitude", Double.toString(centroid.y));
            element("range", "700");
            element("tilt", "10.0");
            element("heading", "10.0");

            end("LookAt");
        }

        /**
         * Encodes a KML Placemark geometry from a geometry + centroid.
         */
        protected void encodePlacemarkGeometry(Geometry geometry, Coordinate centroid) {
    		//if point, just encode a single point, otherwise encode the geometry
            // + centroid
            if ( geometry instanceof Point ) {
                encodeGeometry( geometry );
            }
            else {
            	start("MultiGeometry");

                //the centroid
                start("Point");

                if (!Double.isNaN(centroid.z)) {
                    element("coordinates", centroid.x + "," + centroid.y + "," + centroid.z);
                } else {
                    element("coordinates", centroid.x + "," + centroid.y);
                }

                end("Point");

                //the actual geometry
                encodeGeometry(geometry);

                end("MultiGeometry");
            }
        	
        }

        /**
         * Encodes a KML geometry.
         */
        protected void encodeGeometry(Geometry geometry) {
            if (geometry instanceof GeometryCollection) {
                //unwrap the collection
                GeometryCollection collection = (GeometryCollection) geometry;

                for (int i = 0; i < collection.getNumGeometries(); i++) {
                    encodeGeometry(collection.getGeometryN(i));
                }
            } else {
                geometryTranslator.encode(geometry);
            }
        }

        /**
         * Encodes a color element from its color + opacity representation.
         *
         * @param color The color to encode.
         * @param opacity The opacity ( alpha ) of the color.
         */
        void encodeColor(Color color, double opacity) {
            encodeColor(colorToHex(color, opacity));
        }

        /**
         * Encodes a color element from its hex representation.
         *
         * @param hex The hex value ( with alpha ) of the color.
         *
         */
        void encodeColor(String hex) {
            element("color", hex);
        }


    	/**
    	 * Checks if a rule can be triggered at the current scale level
    	 * 
    	 * @param r
    	 *            The rule
    	 * @return true if the scale is compatible with the rule settings
    	 */
        boolean isWithInScale(Rule r) {
    		return ((r.getMinScaleDenominator() - TOLERANCE) <= scaleDenominator)
    				&& ((r.getMaxScaleDenominator() + TOLERANCE) > scaleDenominator);
    	}
        
        /**
         * Returns the id of the feature removing special characters like
         * '&','>','<','%'.
         */
        String featureId(Feature feature) {
            String id = feature.getID();
            id = id.replaceAll("&", "");
            id = id.replaceAll(">", "");
            id = id.replaceAll("<", "");
            id = id.replaceAll("%", "");

            return id;
        }

        /**
         * Rreturns the geometry for the feature reprojecting if necessary.
         */
        Geometry featureGeometry(Feature f) {
            // get the geometry
            Geometry geom = f.getDefaultGeometry();

            //JD: reprojection done in KMLTransformer
//            if (!CRS.equalsIgnoreMetadata(sourceCrs, mapContext.getCoordinateReferenceSystem())) {
//                try {
//                    MathTransform transform = CRS.findMathTransform(sourceCrs,
//                            mapContext.getCoordinateReferenceSystem(), true);
//                    geom = JTS.transform(geom, transform);
//                } catch (TransformException e) {
//                    LOGGER.severe(e.getLocalizedMessage());
//                } catch (FactoryException e) {
//                    LOGGER.severe(e.getLocalizedMessage());
//                }
//            }

            return geom;
        }

        /**
         * Returns the centroid of the geometry, handling  a geometry collection.
         * <p>
         * In the case of a collection a multi point containing the centroid of
         * each geometry in the collection is calculated. The first point in
         * the multi point is returned as the cetnroid.
         * </p>
         */
        Coordinate geometryCentroid(Geometry g) {
            //TODO: should the collecftion case return the centroid of hte 
            // multi point?
            if (g instanceof GeometryCollection) {
            	GeometryCollection gc = (GeometryCollection) g;
            	
            	//check for case of single geometry
            	if ( gc.getNumGeometries() == 1 ) {
            		g = gc.getGeometryN(0);
            	}
            	else {
            		Coordinate[] pts = new Coordinate[gc.getNumGeometries()];

                    for (int t = 0; t < gc.getNumGeometries(); t++) {
                        pts[t] = gc.getGeometryN(t).getCentroid().getCoordinate();
                    }

                    return g.getFactory().createMultiPoint(pts).getCoordinates()[0];
            	}
            } 
            
            if ( g instanceof Point ) {
            	//thats easy
            	return g.getCoordinate();
            }
            else if ( g instanceof LineString ) {
            	//make sure the point we return is actually on the line
            	double tol = 1E-6;
            	double mid = g.getLength() / 2d;
            	
            	Coordinate[] coords = g.getCoordinates();
            	
            	//walk along the linestring until we get to a point where we 
            	// have two coordinates that straddle the midpoint
            	double len = 0d;
            	for ( int i = 1; i < coords.length; i++) {
            		LineSegment line = new LineSegment( coords[i-1],coords[i] );
            		len += line.getLength();
            		
            		if ( Math.abs( len - mid ) < tol ) {
            			//close enough
            			return line.getCoordinate(1);
            		}
            		
            		if ( len > mid ) {
            			//we have gone past midpoint
            			return line.pointAlong( 1 - ((len-mid)/line.getLength()) );
            		}
            	}
            	
            	//should never get there
            	return g.getCentroid().getCoordinate();
            }
            else {
            	//return the actual centroid
                return g.getCentroid().getCoordinate();
            }
        }

        /**
         * Utility method to convert an int into hex, padded to two characters.
         * handy for generating colour strings.
         *
         * @param i Int to convert
         * @return String a two character hex representation of i
         * NOTE: this is a utility method and should be put somewhere more useful.
         */
        String intToHex(int i) {
            String prelim = Integer.toHexString(i);

            if (prelim.length() < 2) {
                prelim = "0" + prelim;
            }

            return prelim;
        }

        /**
         * Utility method to convert a Color and opacity (0,1.0) into a KML
         * color ref.
         *
         * @param c The color to convert.
         * @param opacity Opacity / alpha, double from 0 to 1.0.
         *
         * @return A String of the form "#AABBGGRR".
         */
        String colorToHex(Color c, double opacity) {
            return new StringBuffer().append(intToHex(new Float(255 * opacity).intValue()))
                                     .append(intToHex(c.getBlue())).append(intToHex(c.getGreen()))
                                     .append(intToHex(c.getRed())).toString();
        }

        /**
         * Filters the feature type styles of <code>style</code> returning only
         * those that apply to <code>featureType</code>
         * <p>
         * This methods returns feature types for which
         * <code>featureTypeStyle.getFeatureTypeName()</code> matches the name
         * of the feature type of <code>featureType</code>, or matches the name of
         * any parent type of the feature type of <code>featureType</code>. This
         * method returns an empty array in the case of which no rules match.
         * </p>
         * @param style The style containing the feature type styles.
         * @param featureType The feature type being filtered against.
         *
         */
        protected FeatureTypeStyle[] filterFeatureTypeStyles(Style style, FeatureType featureType) {
            FeatureTypeStyle[] featureTypeStyles = style.getFeatureTypeStyles();

            if ((featureTypeStyles == null) || (featureTypeStyles.length == 0)) {
                return new FeatureTypeStyle[0];
            }

            ArrayList filtered = new ArrayList(featureTypeStyles.length);

            for (int i = 0; i < featureTypeStyles.length; i++) {
                FeatureTypeStyle featureTypeStyle = featureTypeStyles[i];
                String featureTypeName = featureTypeStyle.getFeatureTypeName();

                //does this style have any rules
                if (featureTypeStyle.getRules() == null || featureTypeStyle.getRules().length == 0 ) {
                	continue;
                }
                
                //does this style apply to the feature collection
                if (featureType.getTypeName().equalsIgnoreCase(featureTypeName)
                        || featureType.isDescendedFrom(null, featureTypeName)) {
                    filtered.add(featureTypeStyle);
                }
            }

            return (FeatureTypeStyle[]) filtered.toArray(new FeatureTypeStyle[filtered.size()]);
        }

        /**
         * Filters the rules of <code>featureTypeStyle</code> returnting only
         * those that apply to <code>feature</code>.
         * <p>
         * This method returns rules for which:
         * <ol>
         *  <li><code>rule.getFilter()</code> matches <code>feature</code>, or:
         *  <li>the rule defines an "ElseFilter", and the feature matches no
         *  other rules.
         * </ol>
         * This method returns an empty array in the case of which no rules
         * match.
         * </p>
         * @param featureTypeStyle The feature type style containing the rules.
         * @param feature The feature being filtered against.
         *
         */
        Rule[] filterRules(FeatureTypeStyle featureTypeStyle, Feature feature) {
            Rule[] rules = featureTypeStyle.getRules();

            if ((rules == null) || (rules.length == 0)) {
                return new Rule[0];
            }

            ArrayList filtered = new ArrayList(rules.length);

            //process the rules, keep track of the need to apply an else filters
            boolean match = false;
            boolean hasElseFilter = false;

            for (int i = 0; i < rules.length; i++) {
                Rule rule = rules[i];
                LOGGER.finer(new StringBuffer("Applying rule: ").append(rule.toString()).toString());

                //does this rule have an else filter
                if (rule.hasElseFilter()) {
                    hasElseFilter = true;

                    continue;
                }

                //is this rule within scale?
                if ( !isWithInScale(rule)) {
                	continue;
                }
                
                //does this rule have a filter which applies to the feature
                Filter filter = rule.getFilter();

                if ((filter == null) || filter.evaluate(feature)) {
                    match = true;

                    filtered.add(rule);
                }
            }

            //if no rules mached the feautre, re-run through the rules applying
            // any else filters
            if (!match && hasElseFilter) {
                //loop through again and apply all the else rules
                for (int i = 0; i < rules.length; i++) {
                    Rule rule = rules[i];

                    if (rule.hasElseFilter()) {
                        filtered.add(rule);
                    }
                }
            }

            return (Rule[]) filtered.toArray(new Rule[filtered.size()]);
        }
    }
}
