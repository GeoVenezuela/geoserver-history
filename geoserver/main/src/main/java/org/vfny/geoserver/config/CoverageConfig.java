/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.config;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.InternationalString;
import org.vfny.geoserver.global.ConfigurationException;
import org.vfny.geoserver.global.CoverageDimension;
import org.vfny.geoserver.global.MetaDataLink;
import org.vfny.geoserver.global.dto.CoverageInfoDTO;
import org.vfny.geoserver.util.CoverageStoreUtils;
import org.vfny.geoserver.util.CoverageUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.units.Unit;


/**
 * User interface Coverage staging area.
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
 *         modification)
 * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
 *         modification)
 * @version $Id: FeatureTypeConfig.java,v 1.20 2004/03/09 10:59:56 jive Exp $
 */
public class CoverageConfig {
    /**
     *
     */
    private String formatId;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String wmsPath;

    /**
     *
     */
    private String label;

    /**
     *
     */
    private String description;

    /**
     *
     */
    private MetaDataLink metadataLink;

    /**
     *
     */
    private String dirName;

    /**
     *
     */
    private List keywords;

    /**
     *
     */
    private GeneralEnvelope envelope;

    /**
     *
     */
    private GeneralEnvelope lonLatWGS84Envelope;

    /**
     *
     */
    private GridGeometry grid;

    /**
     *
     */
    private CoverageDimension[] dimensions;

    /**
     *
     */
    private InternationalString[] dimentionNames;

    /**
     *
     */
    private List requestCRSs;

    /**
     *
     */
    private List responseCRSs;

    /**
     *
     */
    private String nativeFormat;

    /**
     *
     */
    private List supportedFormats;

    /**
     *
     */
    private String defaultInterpolationMethod;

    /**
     *
     */
    private List interpolationMethods;

    /**
     *
     */
    private String srsName;

    /**
     *
     */
    private String srsWKT;

    /**
     *
     */
    private CoordinateReferenceSystem crs;

    /**
     * The default style name.
     */
    private String defaultStyle;

    /**
     * Other WMS Styles
     */
    private ArrayList styles;

    /**
     * String representation of connection parameter values
     */
    private Map parameters;

    /**
     * Package visible constructor for test cases
     */
    CoverageConfig() {
    }

    /**
     * Creating a coverage config from gridcoverages information
     *
     * @param formatId
     * @param format
     * @param gc
     * @throws ConfigurationException
     */
    public CoverageConfig(String formatId, Format format, AbstractGridCoverage2DReader reader,
        HttpServletRequest request) throws ConfigurationException {
        if ((formatId == null) || (formatId.length() == 0)) {
            throw new IllegalArgumentException("formatId is required for CoverageConfig");
        }

        if (format == null) {
            throw new ConfigurationException(new StringBuffer("Cannot handle format: ").append(
                    formatId).toString());
        }

        this.formatId = formatId;

        final DataConfig dataConfig = getDataConfig(request);
        final CoverageStoreConfig cvConfig = dataConfig.getDataFormat(formatId);

        if (cvConfig == null) {
            // something is horribly wrong no FormatID selected!
            // The JSP needs to not include us if there is no
            // selected Format
            //
            throw new RuntimeException("selectedCoverageSetId required in Session");
        }

        crs = reader.getCrs();
        srsName = (((crs != null) && !crs.getIdentifiers().isEmpty())
            ? crs.getIdentifiers().toArray()[0].toString() : "UNKNOWN");
        srsWKT = ((crs != null) ? crs.toWKT() : "UNKNOWN");
        envelope = reader.getOriginalEnvelope();

        try {
            lonLatWGS84Envelope = CoverageStoreUtils.getWGS84LonLatEnvelope(envelope);
        } catch (IndexOutOfBoundsException e) {
            final ConfigurationException newEx = new ConfigurationException(new StringBuffer(
                        "Converting Envelope to Lat-Lon WGS84: ").append(e.toString()).toString());
            newEx.initCause(e);
            throw newEx;
        } catch (FactoryException e) {
            final ConfigurationException newEx = new ConfigurationException(new StringBuffer(
                        "Converting Envelope to Lat-Lon WGS84: ").append(e.toString()).toString());
            newEx.initCause(e);
            throw newEx;
        } catch (TransformException e) {
            final ConfigurationException newEx = new ConfigurationException(new StringBuffer(
                        "Converting Envelope to Lat-Lon WGS84: ").append(e.toString()).toString());
            newEx.initCause(e);
            throw newEx;
        }

        grid = new GridGeometry2D(reader.getOriginalGridRange(), reader.getOriginalEnvelope());

        /**
         * Now reading a fake small GridCoverage just to retrieve meta
         * information: - calculating a new envelope which is 1/20 of the
         * original one - reading the GridCoverage subset
         */
        final GridCoverage2D gc;

        try {
            final ParameterValueGroup readParams = format.getReadParameters();
            final Map parameters = CoverageUtils.getParametersKVP(readParams);

            double[] minCP = envelope.getLowerCorner().getCoordinates();
            double[] maxCP = new double[] {
                    minCP[0] + (envelope.getLength(0) / 20.0),
                    minCP[1] + (envelope.getLength(1) / 20.0)
                };
            final GeneralEnvelope subEnvelope = new GeneralEnvelope(minCP, maxCP);
            subEnvelope.setCoordinateReferenceSystem(reader.getCrs());

            parameters.put(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString(),
                new GridGeometry2D(reader.getOriginalGridRange(), subEnvelope));
            gc = (GridCoverage2D) reader.read(CoverageUtils.getParameters(readParams, parameters,
                        true));
            dimensions = parseCoverageDimesions(gc.getSampleDimensions());
        } catch (UnsupportedEncodingException e) {
            final ConfigurationException newEx = new ConfigurationException(new StringBuffer(
                        "Coverage dimensions: ").append(e.toString()).toString());
            newEx.initCause(e);
            throw newEx;
        } catch (IllegalArgumentException e) {
            final ConfigurationException newEx = new ConfigurationException(new StringBuffer(
                        "Coverage dimensions: ").append(e.toString()).toString());
            newEx.initCause(e);
            throw newEx;
        } catch (IOException e) {
            final ConfigurationException newEx = new ConfigurationException(new StringBuffer(
                        "Coverage dimensions: ").append(e.toString()).toString());
            newEx.initCause(e);
            throw newEx;
        }

        dimentionNames = gc.getDimensionNames();

        final DataConfig config = ConfigRequests.getDataConfig(request);
        StringBuffer cvName = new StringBuffer(gc.getName().toString());
        int count = 0;
        StringBuffer key;
        Map coverages;
        Set cvKeySet;
        boolean key_exists;
        String cvKey;
        Iterator it;

        while (true) {
            key = new StringBuffer(gc.getName().toString());

            if (count > 0) {
                key.append("_").append(count) /* .append("]") */;
            }

            coverages = config.getCoverages();
            cvKeySet = coverages.keySet();
            key_exists = false;

            for (it = cvKeySet.iterator(); it.hasNext();) {
                cvKey = ((String) it.next()).toLowerCase();

                if (cvKey.endsWith(key.toString().toLowerCase())) {
                    key_exists = true;
                }
            }

            if (!key_exists) {
                cvName = key;

                break;
            } else {
                count++;
            }
        }

        name = cvName.toString();
        wmsPath = "/";
        label = new StringBuffer(name).append(" is a ").append(format.getDescription()).toString();
        description = new StringBuffer("Generated from ").append(formatId).toString();
        metadataLink = new MetaDataLink();
        metadataLink.setAbout(format.getDocURL());
        metadataLink.setMetadataType("other");
        keywords = new ArrayList(10);
        keywords.add("WCS");
        keywords.add(formatId);
        keywords.add(name);
        nativeFormat = format.getName();
        dirName = new StringBuffer(formatId).append("_").append(name).toString();
        requestCRSs = new ArrayList(10);

        if ((gc.getCoordinateReferenceSystem2D().getIdentifiers() != null)
                && !gc.getCoordinateReferenceSystem2D().getIdentifiers().isEmpty()) {
            requestCRSs.add(((Identifier) gc.getCoordinateReferenceSystem2D().getIdentifiers()
                                            .toArray()[0]).toString());
        }

        responseCRSs = new ArrayList(10);

        if ((gc.getCoordinateReferenceSystem2D().getIdentifiers() != null)
                && !gc.getCoordinateReferenceSystem2D().getIdentifiers().isEmpty()) {
            responseCRSs.add(((Identifier) gc.getCoordinateReferenceSystem2D().getIdentifiers()
                                             .toArray()[0]).toString());
        }

        supportedFormats = new ArrayList(10);

        final List formats = CoverageStoreUtils.listDataFormats();
        String fName;
        Format fTmp;

        for (Iterator i = formats.iterator(); i.hasNext();) {
            fTmp = (Format) i.next();
            fName = fTmp.getName();

            if (fName.equalsIgnoreCase("WorldImage")) {
                /*
                 * final String[] formatNames = ImageIO.getReaderFormatNames();
                 * final int length = formatNames.length; for (int f=0; f<length;
                 * f++) { // TODO check if coverage can encode Format
                 * supportedFormats.add(formatNames[f]); }
                 */

                // TODO check if coverage can encode Format
                supportedFormats.add("GIF");
                supportedFormats.add("PNG");
                supportedFormats.add("JPEG");
                supportedFormats.add("TIFF");
            } else if (fName.toLowerCase().startsWith("geotiff")) {
                // TODO check if coverage can encode Format
                supportedFormats.add("GeoTIFF");
            } else {
                // TODO check if coverage can encode Format
                supportedFormats.add(fName);
            }
        }

        defaultInterpolationMethod = "nearest neighbor"; // TODO make me
                                                         // parametric

        interpolationMethods = new ArrayList(10);
        interpolationMethods.add("nearest neighbor");
        interpolationMethods.add("bilinear");
        interpolationMethods.add("bicubic");
        // interpolationMethods.add("bicubic_2");
        defaultStyle = "raster";
        styles = new ArrayList();

        /**
         * ReadParameters ...
         */
        parameters = CoverageUtils.getParametersKVP(format.getReadParameters());
    }

    public CoverageConfig(CoverageInfoDTO dto) {
        if (dto == null) {
            throw new NullPointerException("Non null CoverageInfoDTO required");
        }

        formatId = dto.getFormatId();
        name = dto.getName();
        wmsPath = dto.getWmsPath();
        label = dto.getLabel();
        description = dto.getDescription();
        metadataLink = dto.getMetadataLink();
        keywords = dto.getKeywords();
        crs = dto.getCrs();
        srsName = dto.getSrsName();
        srsWKT = dto.getSrsWKT();
        envelope = dto.getEnvelope();
        lonLatWGS84Envelope = dto.getLonLatWGS84Envelope();
        grid = dto.getGrid();
        dimensions = dto.getDimensions();
        dimentionNames = dto.getDimensionNames();
        nativeFormat = dto.getNativeFormat();
        dirName = dto.getDirName();
        requestCRSs = dto.getRequestCRSs();
        responseCRSs = dto.getResponseCRSs();
        supportedFormats = dto.getSupportedFormats();
        defaultInterpolationMethod = dto.getDefaultInterpolationMethod();
        interpolationMethods = dto.getInterpolationMethods();
        defaultStyle = dto.getDefaultStyle();
        styles = dto.getStyles();
        parameters = dto.getParameters();
    }

    /**
     * @param sampleDimensions
     * @return
     * @throws UnsupportedEncodingException
     */
    private CoverageDimension[] parseCoverageDimesions(GridSampleDimension[] sampleDimensions)
        throws UnsupportedEncodingException {
        final int length = sampleDimensions.length;
        CoverageDimension[] dims = new CoverageDimension[length];

        for (int i = 0; i < length; i++) {
            dims[i] = new CoverageDimension();
            dims[i].setName(sampleDimensions[i].getDescription().toString(Locale.getDefault()));

            StringBuffer label = new StringBuffer("GridSampleDimension".intern());
            final Unit uom = sampleDimensions[i].getUnits();

            if (uom != null) {
                label.append("(".intern());
                parseUom(label, uom);
                label.append(")".intern());
            }

            label.append("[".intern());
            label.append(sampleDimensions[i].getMinimumValue());
            label.append(",".intern());
            label.append(sampleDimensions[i].getMaximumValue());
            label.append("]".intern());
            dims[i].setDescription(label.toString());
            dims[i].setRange(sampleDimensions[i].getRange());

            final List categories = sampleDimensions[i].getCategories();

            Category cat = null;

            for (Iterator c_iT = categories.iterator(); c_iT.hasNext();) {
                cat = (Category) c_iT.next();

                if ((cat != null) && cat.getName().toString().equalsIgnoreCase("no data")) {
                    double min = cat.getRange().getMinimum();
                    double max = cat.getRange().getMaximum();

                    dims[i].setNullValues(((min == max) ? new Double[] { new Double(min) }
                                                        : new Double[] {
                            new Double(min), new Double(max)
                        }));
                }
            }

            /*
             * double[] nTemp = sampleDimensions[i].getNoDataValues(); if (nTemp !=
             * null) { final int ntLength = nTemp.length; Double[] nulls = new
             * Double[ntLength]; for (int nd = 0; nd < ntLength; nd++) {
             * nulls[nd] = new Double(nTemp[nd]); }
             * dims[i].setNullValues(nulls); }
             */
        }

        return dims;
    }

    /**
     * This method tries to put in order problems with 16 bits characters.
     *
     * @param label2
     * @param uom
     */
    private void parseUom(StringBuffer label2, Unit uom) {
        String uomString = uom.toString();
        uomString = uomString.replaceAll("�", "^2");
        uomString = uomString.replaceAll("�", "^3");
        uomString = uomString.replaceAll("�", "A");
        uomString = uomString.replaceAll("�", "");
        label2.append(uomString);
    }

    public CoverageInfoDTO toDTO() {
        CoverageInfoDTO c = new CoverageInfoDTO();
        c.setFormatId(formatId);
        c.setName(name);
        c.setWmsPath(wmsPath);
        c.setLabel(label);
        c.setDescription(description);
        c.setMetadataLink(metadataLink);
        c.setKeywords(keywords);
        c.setCrs(crs);
        c.setSrsName(srsName);
        c.setSrsWKT(srsWKT);
        c.setEnvelope(envelope);
        c.setLonLatWGS84Envelope(lonLatWGS84Envelope);
        c.setGrid(grid);
        c.setDimensions(dimensions);
        c.setDimensionNames(dimentionNames);
        c.setNativeFormat(nativeFormat);
        c.setDirName(dirName);
        c.setRequestCRSs(requestCRSs);
        c.setResponseCRSs(responseCRSs);
        c.setSupportedFormats(supportedFormats);
        c.setDefaultInterpolationMethod(defaultInterpolationMethod);
        c.setInterpolationMethods(interpolationMethods);
        c.setDefaultStyle(defaultStyle);
        c.setStyles(styles);
        c.setParameters(parameters);

        return c;
    }

    /**
     * Access Catalog Configuration Model from the WebContainer.
     *
     * @param request
     *
     * @return Configuration model for Catalog information.
     */
    protected DataConfig getDataConfig(HttpServletRequest request) {
        return (DataConfig) request.getSession().getServletContext()
                                   .getAttribute(DataConfig.CONFIG_KEY);
    }

    public String getKey() {
        return getFormatId() + DataConfig.SEPARATOR + getName();
    }

    public String toString() {
        return "CoverageConfig[name: " + name + " dewcription: " + description + " srsName: "
        + srsName + "]";
    }

    /**
     * @return Returns the defaultInterpolationMethod.
     *
     * @uml.property name="defaultInterpolationMethod"
     */
    public String getDefaultInterpolationMethod() {
        return defaultInterpolationMethod;
    }

    /**
     * @param defaultInterpolationMethod
     *            The defaultInterpolationMethod to set.
     *
     * @uml.property name="defaultInterpolationMethod"
     */
    public void setDefaultInterpolationMethod(String defaultInterpolationMethod) {
        this.defaultInterpolationMethod = defaultInterpolationMethod;
    }

    /**
     * @return Returns the description.
     *
     * @uml.property name="description"
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     *
     * @uml.property name="description"
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the dirName.
     *
     * @uml.property name="dirName"
     */
    public String getDirName() {
        return dirName;
    }

    /**
     * @param dirName
     *            The dirName to set.
     *
     * @uml.property name="dirName"
     */
    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    /**
     * @return Returns the envelope.
     *
     * @uml.property name="envelope"
     */
    public GeneralEnvelope getEnvelope() {
        return envelope;
    }

    /**
     * @param envelope
     *            The envelope to set.
     *
     * @uml.property name="envelope"
     */
    public void setEnvelope(GeneralEnvelope envelope) {
        this.envelope = envelope;
    }

    /**
     * @return Returns the formatId.
     *
     * @uml.property name="formatId"
     */
    public String getFormatId() {
        return formatId;
    }

    /**
     * @param formatId
     *            The formatId to set.
     *
     * @uml.property name="formatId"
     */
    public void setFormatId(String formatId) {
        this.formatId = formatId;
    }

    /**
     * @return Returns the interpolationMethods.
     *
     * @uml.property name="interpolationMethods"
     */
    public List getInterpolationMethods() {
        return interpolationMethods;
    }

    /**
     * @param interpolationMethods
     *            The interpolationMethods to set.
     *
     * @uml.property name="interpolationMethods"
     */
    public void setInterpolationMethods(List interpolationMethods) {
        this.interpolationMethods = interpolationMethods;
    }

    /**
     * @return Returns the keywords.
     *
     * @uml.property name="keywords"
     */
    public List getKeywords() {
        return keywords;
    }

    /**
     * @param keywords
     *            The keywords to set.
     *
     * @uml.property name="keywords"
     */
    public void setKeywords(List keywords) {
        this.keywords = keywords;
    }

    /**
     * @return Returns the label.
     *
     * @uml.property name="label"
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            The label to set.
     *
     * @uml.property name="label"
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns the metadataLink.
     *
     * @uml.property name="metadataLink"
     */
    public MetaDataLink getMetadataLink() {
        return metadataLink;
    }

    /**
     * @param metadataLink
     *            The metadataLink to set.
     *
     * @uml.property name="metadataLink"
     */
    public void setMetadataLink(MetaDataLink metadataLink) {
        this.metadataLink = metadataLink;
    }

    /**
     * @return Returns the name.
     *
     * @uml.property name="name"
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     *
     * @uml.property name="name"
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the nativeFormat.
     *
     * @uml.property name="nativeFormat"
     */
    public String getNativeFormat() {
        return nativeFormat;
    }

    /**
     * @param nativeFormat
     *            The nativeFormat to set.
     *
     * @uml.property name="nativeFormat"
     */
    public void setNativeFormat(String nativeFormat) {
        this.nativeFormat = nativeFormat;
    }

    /**
     * @return Returns the requestCRSs.
     *
     * @uml.property name="requestCRSs"
     */
    public List getRequestCRSs() {
        return requestCRSs;
    }

    /**
     * @param requestCRSs
     *            The requestCRSs to set.
     *
     * @uml.property name="requestCRSs"
     */
    public void setRequestCRSs(List requestCRSs) {
        this.requestCRSs = requestCRSs;
    }

    /**
     * @return Returns the responseCRSs.
     *
     * @uml.property name="responseCRSs"
     */
    public List getResponseCRSs() {
        return responseCRSs;
    }

    /**
     * @param responseCRSs
     *            The responseCRSs to set.
     *
     * @uml.property name="responseCRSs"
     */
    public void setResponseCRSs(List responseCRSs) {
        this.responseCRSs = responseCRSs;
    }

    /**
     * @return Returns the srsName.
     *
     * @uml.property name="srsName"
     */
    public String getSrsName() {
        return srsName;
    }

    /**
     * @param srsName
     *            The srsName to set.
     *
     * @uml.property name="srsName"
     */
    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    /**
     * @return Returns the supportedFormats.
     *
     * @uml.property name="supportedFormats"
     */
    public List getSupportedFormats() {
        return supportedFormats;
    }

    /**
     * @param supportedFormats
     *            The supportedFormats to set.
     *
     * @uml.property name="supportedFormats"
     */
    public void setSupportedFormats(List supportedFormats) {
        this.supportedFormats = supportedFormats;
    }

    /**
     *
     * @uml.property name="crs"
     */
    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    /**
     *
     * @uml.property name="crs"
     */
    public void setCrs(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     *
     * @uml.property name="grid"
     */
    public GridGeometry getGrid() {
        return grid;
    }

    /**
     *
     * @uml.property name="grid"
     */
    public void setGrid(GridGeometry grid) {
        this.grid = grid;
    }

    /**
     *
     * @uml.property name="dimentionNames"
     */
    public InternationalString[] getDimentionNames() {
        return dimentionNames;
    }

    /**
     *
     * @uml.property name="dimentionNames"
     */
    public void setDimentionNames(InternationalString[] dimentionNames) {
        this.dimentionNames = dimentionNames;
    }

    /**
     * @return Returns the dimensions.
     *
     * @uml.property name="dimensions"
     */
    public CoverageDimension[] getDimensions() {
        return dimensions;
    }

    /**
     * @param dimensions
     *            The dimensions to set.
     *
     * @uml.property name="dimensions"
     */
    public void setDimensions(CoverageDimension[] dimensions) {
        this.dimensions = dimensions;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public ArrayList getStyles() {
        return styles;
    }

    public void setStyles(ArrayList styles) {
        this.styles = styles;
    }

    public void addStyle(String style) {
        if (!this.styles.contains(style)) {
            this.styles.add(style);
        }
    }

    public String getSrsWKT() {
        return srsWKT;
    }

    public void setSrsWKT(String srsWKT) {
        this.srsWKT = srsWKT;
    }

    public GeneralEnvelope getLonLatWGS84Envelope() {
        return lonLatWGS84Envelope;
    }

    public String getWmsPath() {
        return wmsPath;
    }

    public void setWmsPath(String wmsPath) {
        this.wmsPath = wmsPath;
    }

    public Map getParameters() {
        return parameters;
    }

    public synchronized void setParameters(Map parameters) {
        this.parameters = parameters;
    }
}
