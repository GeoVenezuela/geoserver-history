/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.responses.wms.map.svg;

import com.vividsolutions.jts.geom.*;

import org.geotools.feature.*;

import java.io.*;

import java.text.*;

import java.util.*;
import java.util.Locale;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Rold�n
 * @version $Id: SVGWriter.java,v 1.1 2004/03/14 16:15:22 groldan Exp $
 */
public class SVGWriter extends OutputStreamWriter {
    /**
     * a number formatter setted up to write SVG legible numbers ('.' as
     * decimal separator, no group separator
     */
    private static DecimalFormat formatter;

    static {
        Locale locale = new Locale("en", "US");
        DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(locale);
        decimalSymbols.setDecimalSeparator('.');
        formatter = new DecimalFormat();
        formatter.setDecimalFormatSymbols(decimalSymbols);

        //do not group
        formatter.setGroupingSize(0);

        //do not show decimal separator if it is not needed
        formatter.setDecimalSeparatorAlwaysShown(false);
        formatter.setDecimalFormatSymbols(null);

        //set default number of fraction digits
        formatter.setMaximumFractionDigits(5);

        //minimun fraction digits to 0 so they get not rendered if not needed
        formatter.setMinimumFractionDigits(0);
    }

    /** DOCUMENT ME! */
    private double minY;

    /** DOCUMENT ME! */
    private double maxY;

    /** DOCUMENT ME! */
    private int coordsSkipCount;

    /** DOCUMENT ME! */
    private int coordsWriteCount;

    /** DOCUMENT ME! */
    private SVGFeatureWriterHandler writerHandler;

    /** DOCUMENT ME!  */
    private SVGFeatureWriter featureWriter = null;

    /** DOCUMENT ME! */
    private double minCoordDistance;

    /** DOCUMENT ME! */
    private String attributeStyle;

    /** DOCUMENT ME! */
    private boolean pointsAsCircles;

    /** DOCUMENT ME!  */
    private EncoderConfig config;

    /**
     * Creates a new SVGWriter object.
     *
     * @param out DOCUMENT ME!
     * @param config DOCUMENT ME!
     */
    public SVGWriter(OutputStream out, EncoderConfig config) {
        super(out);
        this.config = config;

        Envelope space = config.getReferenceSpace();
        this.minY = space.getMinY();
        this.maxY = space.getMaxY();
    }

    /**
     * DOCUMENT ME!
     *
     * @param attributeName DOCUMENT ME!
     */
    public void setAttributeStyle(String attributeName) {
        this.attributeStyle = attributeName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param asCircles DOCUMENT ME!
     */
    public void setPointsAsCircles(boolean asCircles) {
        this.pointsAsCircles = asCircles;
    }

    /**
     * DOCUMENT ME!
     *
     * @param gtype DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void setGeometryType(Class gtype) {
        if (gtype == Point.class) {
            featureWriter = new PointWriter();
        } else if (gtype == MultiPoint.class) {
            featureWriter = new MultiPointWriter();
        } else if (gtype == LineString.class) {
            featureWriter = new LineStringWriter();
        } else if (gtype == MultiLineString.class) {
            featureWriter = new MultiLineStringWriter();
        } else if (gtype == Polygon.class) {
            featureWriter = new PolygonWriter();
        } else if (gtype == MultiPolygon.class) {
            featureWriter = new MultiPolygonWriter();
        } else {
            throw new IllegalArgumentException(
                "No SVG Feature writer defined for " + gtype);
        }

        if (config.isCollectGeometries()) {
            this.writerHandler = new CollectSVGHandler(featureWriter);
        } else {
            this.writerHandler = new SVGFeatureWriterHandler();
            this.writerHandler = new FIDSVGHandler(this.writerHandler);
            this.writerHandler = new BoundsSVGHandler(this.writerHandler);
            this.writerHandler = new AttributesSVGHandler(this.writerHandler);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param minCoordDistance DOCUMENT ME!
     */
    public void setMinCoordDistance(double minCoordDistance) {
        this.minCoordDistance = minCoordDistance;
    }

    /**
     * if a reference space has been set, returns a translated Y coordinate
     * wich is inverted based on the height of such a reference space,
     * otherwise just returns <code>y</code>
     *
     * @param y DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getY(double y) {
        return (maxY - y) + minY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param x DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getX(double x) {
        return x;
    }

    /**
     * DOCUMENT ME!
     *
     * @param numDigits DOCUMENT ME!
     */
    public void setMaximunFractionDigits(int numDigits) {
        formatter.setMaximumFractionDigits(numDigits);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMaximunFractionDigits() {
        return formatter.getMaximumFractionDigits();
    }

    /**
     * DOCUMENT ME!
     *
     * @param numDigits DOCUMENT ME!
     */
    public void setMinimunFractionDigits(int numDigits) {
        formatter.setMinimumFractionDigits(numDigits);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMinimunFractionDigits() {
        return formatter.getMinimumFractionDigits();
    }

    /**
     * DOCUMENT ME!
     *
     * @param d DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void write(double d) throws IOException {
        write(formatter.format(d));
    }

    /**
     * DOCUMENT ME!
     *
     * @param c DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void write(char c) throws IOException {
        super.write(c);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void newline() throws IOException {
        super.write('\n');
    }


    /**
     * DOCUMENT ME!
     *
     * @param ft
     *
     * @throws IOException si algo ocurre escribiendo a <code>out</code>
     */
    public void writeFeature(Feature ft) throws IOException {
        writerHandler.startFeature(featureWriter, ft);
        writerHandler.startGeometry(featureWriter, ft);
        writerHandler.writeGeometry(featureWriter, ft);
        writerHandler.endGeometry(featureWriter, ft);
        writerHandler.endFeature(featureWriter, ft);
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.1 $
     */
    private class SVGFeatureWriterHandler {
        /**
         * DOCUMENT ME!
         *
         * @param featureWriter DOCUMENT ME!
         * @param ft DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public void startFeature(SVGFeatureWriter featureWriter, Feature ft)
            throws IOException {
            featureWriter.startElement();
        }

        /**
         * DOCUMENT ME!
         *
         * @param featureWriter DOCUMENT ME!
         * @param ft DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public void endFeature(SVGFeatureWriter featureWriter, Feature ft)
            throws IOException {
            featureWriter.endElement();
        }

        /**
         * DOCUMENT ME!
         *
         * @param featureWriter DOCUMENT ME!
         * @param ft DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public void startGeometry(SVGFeatureWriter featureWriter, Feature ft)
            throws IOException {
            featureWriter.startGeometry();
        }

        /**
         * DOCUMENT ME!
         *
         * @param featureWriter DOCUMENT ME!
         * @param ft DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public void writeGeometry(SVGFeatureWriter featureWriter, Feature ft)
            throws IOException {
            featureWriter.writeGeometry(ft.getDefaultGeometry());
        }

        /**
         * DOCUMENT ME!
         *
         * @param featureWriter DOCUMENT ME!
         * @param ft DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public void endGeometry(SVGFeatureWriter featureWriter, Feature ft)
            throws IOException {
            featureWriter.endGeometry();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.1 $
     */
    private class CollectSVGHandler extends SVGFeatureWriterHandler {
        /** DOCUMENT ME! */
        private SVGFeatureWriter featureWriter;

        /**
         * Creates a new CollectSVGHandler object.
         *
         * @param featureWriter DOCUMENT ME!
         */
        public CollectSVGHandler(SVGFeatureWriter featureWriter) {
            this.featureWriter = featureWriter;
        }

        /**
         * DOCUMENT ME!
         *
         * @param ft DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public void writeFeature(Feature ft) throws IOException {
            featureWriter.writeGeometry(ft.getDefaultGeometry());
            write('\n');
        }
    }

    /**
     * decorator handler that adds the feature id as the "id" attribute
     */
    private class FIDSVGHandler extends SVGFeatureWriterHandler {
        /** DOCUMENT ME!  */
        private SVGFeatureWriterHandler handler;

        /**
         * Creates a new NormalSVGHandler object.
         *
         * @param handler DOCUMENT ME!
         */
        public FIDSVGHandler(SVGFeatureWriterHandler handler) {
            this.handler = handler;
        }

        /**
         * DOCUMENT ME!
         *
         * @param featureWriter DOCUMENT ME!
         * @param ft DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public void startFeature(SVGFeatureWriter featureWriter, Feature ft)
            throws IOException {
            handler.startFeature(featureWriter, ft);
            write(" id=\"");
            write(ft.getID());
            write("\"");
        }
    }

    /**
     * decorator handler that adds the feature id as the "id" attribute
     */
    private class BoundsSVGHandler extends SVGFeatureWriterHandler {
        /** DOCUMENT ME!  */
        private SVGFeatureWriterHandler handler;

        /**
         * Creates a new NormalSVGHandler object.
         *
         * @param handler DOCUMENT ME!
         */
        public BoundsSVGHandler(SVGFeatureWriterHandler handler) {
            this.handler = handler;
        }

        /**
         * DOCUMENT ME!
         *
         * @param featureWriter DOCUMENT ME!
         * @param ft DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public void startFeature(SVGFeatureWriter featureWriter, Feature ft)
            throws IOException {
            handler.startFeature(featureWriter, ft);

            Geometry geom = ft.getDefaultGeometry();
            Envelope env = geom.getEnvelopeInternal();
            write(" bounds=\"");
            write(env.getMinX());
            write(' ');
            write(env.getMinY());
            write(' ');
            write(env.getMaxX());
            write(' ');
            write(env.getMaxY());
            write('\"');
        }
    }

    /**
     * decorator handler that adds the feature id as the "id" attribute
     */
    private class AttributesSVGHandler extends SVGFeatureWriterHandler {
        /** DOCUMENT ME!  */
        private SVGFeatureWriterHandler handler;

        /**
         * Creates a new NormalSVGHandler object.
         *
         * @param handler DOCUMENT ME!
         */
        public AttributesSVGHandler(SVGFeatureWriterHandler handler) {
            this.handler = handler;
        }

        /**
         * DOCUMENT ME!
         *
         * @param featureWriter DOCUMENT ME!
         * @param ft DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public void startFeature(SVGFeatureWriter featureWriter, Feature ft)
            throws IOException {
            handler.startFeature(featureWriter, ft);

            FeatureType type = ft.getFeatureType();
            int numAtts = type.getAttributeCount();
            String name;
            Object value;

            for (int i = 0; i < numAtts; i++) {
                value = ft.getAttribute(i);

                if ((value != null) && !(value instanceof Geometry)) {
                    write(' ');
                    write("bfa:" + type.getAttributeType(i).getName());
                    write("=\"");
                    encodeAttribute(String.valueOf(value));
                    write('\"');
                }
            }
        }

        /**
         * Parses the passed string, and encodes the special characters (used in
         * xml for special purposes) with the appropriate codes. e.g. '&lt;' is
         * changed to '&amp;lt;'
         *
         * @param inData The string to encode into xml.
         *
         * @return the encoded string. Returns null, if null is passed as argument
         *
         * @task REVISIT: Once we write directly to out, as we should, this  method
         *       should be simpler, as we can just write strings with escapes
         *       directly to out, replacing as we iterate of chars to write them.
         */
        private void encodeAttribute(String inData) throws IOException
        {
            //return null, if null is passed as argument
            if (inData == null) {
                return;
            }

            //get the length of input String
            int length = inData.length();

            char charToCompare;

            //iterate over the input String
            for (int i = 0; i < length; i++) {
                charToCompare = inData.charAt(i);

                //if the ith character is special character, replace by code
                if (charToCompare == '"') {
                    write("&quot;");
                } else if(charToCompare > 127){
                    writeUnicodeEscapeSequence(charToCompare);
                } else {
                    write(charToCompare);
                }
            }
        }

        /**
         * returns the xml unicode escape sequence for the character <code>c</code>,
         * such as <code>"&#x00d1;"</code> for the character <code>'�'</code>
         */
        private void writeUnicodeEscapeSequence(char c) throws IOException
        {
          write("&#x");
          String hex = Integer.toHexString(c);
          int pendingZeros = 4 - hex.length();
          for(int i = 0; i < pendingZeros; i++)
              write('0');
          write(hex);
          write(';');
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.1 $
     */
    private abstract class SVGFeatureWriter {
        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected abstract void startElement() throws IOException;

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected abstract void startGeometry() throws IOException;

        /**
         * DOCUMENT ME!
         *
         * @param geom DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected abstract void writeGeometry(Geometry geom)
            throws IOException;

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void endGeometry() throws IOException {
            write("\"");
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void endElement() throws IOException {
            write("/>\n");
        }

        /**
         * Writes the content of the <b>d</b> attribute in a <i>path</i> SVG
         * element
         *
         * <p>
         * While iterating over the coordinate array passed as parameter, this
         * method performs a kind of very basic path generalization, verifying
         * that the distance between the current coordinate and the last
         * encoded one is greater than the minimun distance expressed by the
         * field <code>minCoordDistance</code> and established by the method
         * {@link #setReferenceSpace(Envelope, float)
         * setReferenceSpace(Envelope, blurFactor)}
         * </p>
         *
         * @param coords
         *
         * @throws IOException
         */
        protected void writePathContent(Coordinate[] coords)
            throws IOException {
            write('M');

            Coordinate prev = coords[0];
            Coordinate curr = null;
            write(getX(prev.x));
            write(' ');
            write(getY(prev.y));

            int nCoords = coords.length;
            write('l');

            for (int i = 1; i < nCoords; i++) {
                curr = coords[i];

                //let at least 3 points in case it is a polygon
                if ((i > 3) && (prev.distance(curr) <= minCoordDistance)) {
                    ++coordsSkipCount;

                    continue;
                }

                ++coordsWriteCount;
                write((getX(curr.x) - getX(prev.x)));
                write(' ');
                write(getY(curr.y) - getY(prev.y));
                write(' ');
                prev = curr;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param coords DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void writeClosedPathContent(Coordinate[] coords)
            throws IOException {
            writePathContent(coords);
            write('Z');
        }
    }

    /**
     *
     */
    private class PointWriter extends SVGFeatureWriter {
        /**
         * Creates a new PointWriter object.
         */
        public PointWriter() {
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void startElement() throws IOException {
            write(pointsAsCircles ? "<circle r='0.25%' fill='blue'" : "<use");
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        /**
         * protected void writeAttributes(Feature ft) throws IOException { if
         * (!pointsAsCircles) { write(" xlink:href=\"#"); if (attributeStyle
         * != null) { write(String.valueOf(ft.getAttribute(attributeStyle)));
         * } else { write("point"); } write("\""); }
         * super.writeAttributes(ft); }
         *
         * @throws IOException DOCUMENT ME!
         */
        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void startGeometry() throws IOException {
        }

        /**
         * overrides writeBounds for points to do nothing. You can get the
         * position of the point with the x and y attributes of the "use" SVG
         * element written to represent each point
         *
         * @param env DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void writeBounds(Envelope env) throws IOException {
        }

        /**
         * DOCUMENT ME!
         *
         * @param geom DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void writeGeometry(Geometry geom) throws IOException {
            Point p = (Point) geom;

            if (pointsAsCircles) {
                write(" cx=\"");
                write(getX(p.getX()));
                write("\" cy=\"");
                write(getY(p.getY()));
            } else {
                write(" x=\"");
                write(getX(p.getX()));
                write("\" y=\"");
                write(getY(p.getY()));
            }
        }
    }

    /**
     *
     */
    private class MultiPointWriter extends PointWriter {
        /**
         * Creates a new MultiPointWriter object.
         */
        public MultiPointWriter() {
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void startElement() throws IOException {
            write("<g ");
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void startGeometry() throws IOException {
            write("/>\n");
        }

        /**
         * DOCUMENT ME!
         *
         * @param geom DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void writeGeometry(Geometry geom) throws IOException {
            MultiPoint mp = (MultiPoint) geom;

            for (int i = 0; i < mp.getNumGeometries(); i++) {
                super.startElement();
                super.writeGeometry(mp.getGeometryN(i));
                super.endGeometry();
                super.endElement();
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void endElement() throws IOException {
            write("</g>\n");
        }
    }

    /**
     *
     */
    private class LineStringWriter extends SVGFeatureWriter {
        /**
         * Creates a new LineStringWriter object.
         */
        public LineStringWriter() {
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void startElement() throws IOException {
            write("<path");
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void startGeometry() throws IOException {
            write(" d=\"");
        }

        /**
         * DOCUMENT ME!
         *
         * @param geom DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void writeGeometry(Geometry geom) throws IOException {
            writePathContent(((LineString) geom).getCoordinates());
        }
    }

    /**
     *
     */
    private class MultiLineStringWriter extends LineStringWriter {
        /**
         * Creates a new MultiLineStringWriter object.
         */
        public MultiLineStringWriter() {
        }

        /**
         * DOCUMENT ME!
         *
         * @param geom DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void writeGeometry(Geometry geom) throws IOException {
            MultiLineString ml = (MultiLineString) geom;

            for (int i = 0; i < ml.getNumGeometries(); i++) {
                super.writeGeometry(ml.getGeometryN(i));
            }
        }
    }

    /**
     *
     */
    private class PolygonWriter extends SVGFeatureWriter {
        /**
         * Creates a new PolygonWriter object.
         */
        public PolygonWriter() {
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void startElement() throws IOException {
            write("<path");
        }

        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void startGeometry() throws IOException {
            write(" d=\"");
        }

        /**
         * DOCUMENT ME!
         *
         * @param geom DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void writeGeometry(Geometry geom) throws IOException {
            Polygon poly = (Polygon) geom;
            LineString shell = poly.getExteriorRing();
            int nHoles = poly.getNumInteriorRing();
            writeClosedPathContent(shell.getCoordinates());

            for (int i = 0; i < nHoles; i++)
                writeClosedPathContent(poly.getInteriorRingN(i).getCoordinates());
        }
    }

    /**
     *
     */
    private class MultiPolygonWriter extends PolygonWriter {
        /**
         * Creates a new MultiPolygonWriter object.
         */
        public MultiPolygonWriter() {
        }

        /**
         * DOCUMENT ME!
         *
         * @param geom DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void writeGeometry(Geometry geom) throws IOException {
            MultiPolygon mpoly = (MultiPolygon) geom;

            for (int i = 0; i < mpoly.getNumGeometries(); i++) {
                super.writeGeometry(mpoly.getGeometryN(i));
            }
        }
    }
}
