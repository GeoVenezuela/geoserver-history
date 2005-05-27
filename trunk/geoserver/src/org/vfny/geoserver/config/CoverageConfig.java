/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.config;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.units.Unit;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;
import org.vfny.geoserver.global.ConfigurationException;
import org.vfny.geoserver.global.CoverageCategory;
import org.vfny.geoserver.global.CoverageDimension;
import org.vfny.geoserver.global.MetaDataLink;
import org.vfny.geoserver.global.dto.CoverageInfoDTO;

import com.vividsolutions.jts.geom.Envelope;

/**
 * User interface FeatureType staging area.
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author $Author: Simone Giannecchini (simboss_ml@tiscali.it) $ (last modification)
 * @version $Id: FeatureTypeConfig.java,v 1.20 2004/03/09 10:59:56 jive Exp $
 */
public class CoverageConfig {
	private String formatId;
    private String name;
    private String label;
    private String description;
    private MetaDataLink metadataLink;
    private String dirName;
    private List keywords;
    private Envelope envelope;
	private GridGeometry grid;
	private CoverageDimension[] dimensions;
	private InternationalString[] dimentionNames;
    private List requestCRSs;
    private List responseCRSs;
    private String nativeFormat;
    private List supportedFormats;
    private String defaultInterpolationMethod;
    private List interpolationMethods;
    private String srsName; 
    private CoordinateReferenceSystem crs;

    /**
     * Package visible constructor for test cases
     */
    CoverageConfig() {
    }
    
    public CoverageConfig(String formatId, String formatType, GridCoverage2D gc, boolean generate) throws ConfigurationException {
        if ((formatId == null) || (formatId.length() == 0)) {
            throw new IllegalArgumentException(
                "formatId is required for CoverageConfig");
        }

        this.formatId = formatId;
        envelope = new Envelope();
        GeneralEnvelope gEnvelope=(GeneralEnvelope)gc.getEnvelope();
        if( generate ) {
        	envelope.init(gEnvelope.getLowerCorner().getOrdinate(0),
        			gEnvelope.getUpperCorner().getOrdinate(0),
        			gEnvelope.getLowerCorner().getOrdinate(1),
        			gEnvelope.getUpperCorner().getOrdinate(1)) ;
        }
        
		grid = gc.getGridGeometry();
		try {
			dimensions = parseCoverageDimesions(gc.getSampleDimensions());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dimentionNames = gc.getDimensionNames();
        crs = gc.getCoordinateReferenceSystem2D();
        srsName = (crs != null ? crs.getName().toString() : "WGS84");

        Format[] formats = GridFormatFinder.getFormatArray();
		Format format = null;
    	for( int i = 0; i < formats.length; i++ ) {
    		if( formats[i].getName().equals(formatType) ) {
    			format = formats[i];
    			break;
    		}
    	}
    	
        if (format == null) {
        	throw new ConfigurationException("Cannot handle format: " + formatId);
        }

        name = formatId + "_Coverage";
        label = format.getName() + "_Type";
        description = "Generated from " + formatId;
        metadataLink = new MetaDataLink();
        metadataLink.setAbout(format.getDocURL());
        metadataLink.setMetadataType("other");
        keywords = new LinkedList();
        keywords.add("WCS");
        keywords.add(formatId);
        keywords.add(name);
        nativeFormat = format.getName(); // ?
        dirName = formatId + "_" + name;
        requestCRSs = new LinkedList(); // ?
        responseCRSs = new LinkedList(); // ?
        supportedFormats = new LinkedList(); // ?
        defaultInterpolationMethod = "nearest neighbor"; // ?
        interpolationMethods = new LinkedList(); // ?
    }

    /**
	 * @param sampleDimensions
	 * @return
     * @throws UnsupportedEncodingException
	 */
	private CoverageDimension[] parseCoverageDimesions(GridSampleDimension[] sampleDimensions) throws UnsupportedEncodingException {
		final int length=sampleDimensions.length;
		CoverageDimension[] dims = new CoverageDimension[length];
		
		for(int i=0;i<length;i++) {
			dims[i] = new CoverageDimension();
			dims[i].setName("dim_".intern() + i);
			StringBuffer label=new StringBuffer("GridSampleDimension".intern());
			final Unit uom=sampleDimensions[i].getUnits();
			if(uom!=null)
			{
				label.append("(".intern());
				uom.appendTo(label);
				label.append(")".intern());
			}
			label.append("[".intern());
			label.append(sampleDimensions[i].getMinimumValue());
			label.append(",".intern());
			label.append(sampleDimensions[i].getMaximumValue());
			label.append("]".intern());	
			dims[i].setDescription(label.toString());
			final int numCategories=sampleDimensions[i].getCategories().size();
			Category[] cats = new Category[numCategories];
			CoverageCategory[] dimCats = new CoverageCategory[numCategories];
			int j=0;
			for(Iterator c_iT=sampleDimensions[i].getCategories().iterator();c_iT.hasNext();j++) {
				Category cat = (Category) c_iT.next();
				dimCats[j] = new CoverageCategory();
				dimCats[j].setName(cat.getName().toString());
				dimCats[j].setLabel(cat.toString());
				dimCats[j].setInterval(cat.getRange());
			}
			dims[i].setCategories(dimCats);
			double[] nTemp = sampleDimensions[i].getNoDataValues();
			if(nTemp != null) {
				final int ntLength=nTemp.length;
				Double[] nulls = new Double[ntLength];
				for(int nd=0;nd<ntLength;nd++) {
					nulls[nd] = new Double(nTemp[nd]);
				}
				dims[i].setNullValues(nulls);
			}
		}
		
		return dims;
	}

	public CoverageConfig(CoverageInfoDTO dto) {
        if (dto == null) {
            throw new NullPointerException(
                "Non null CoverageInfoDTO required");
        }

        formatId = dto.getFormatId();
        name = dto.getName();
        label = dto.getLabel();
        description = dto.getDescription();
        metadataLink = dto.getMetadataLink();
        keywords = dto.getKeywords();
        crs = dto.getCrs();
        srsName = dto.getSrsName();
        envelope = dto.getEnvelope();
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
    }

    public CoverageInfoDTO toDTO() {
    	CoverageInfoDTO c = new CoverageInfoDTO();
        c.setFormatId(formatId);
        c.setName(name);
        c.setLabel(label);
        c.setDescription(description);
        c.setMetadataLink(metadataLink);
        c.setKeywords(keywords);
        c.setCrs(crs);
        c.setSrsName(srsName);
        c.setEnvelope(envelope);
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

        return c;
    }


    public String getKey() {
        return getFormatId() + DataConfig.SEPARATOR + getName();
    }    

    public String toString() {
	return "CoverageConfig[name: " + name + " dewcription: " + description
	    + " srsName: " + srsName + "]";
    }
	/**
	 * @return Returns the defaultInterpolationMethod.
	 */
	public String getDefaultInterpolationMethod() {
		return defaultInterpolationMethod;
	}
	/**
	 * @param defaultInterpolationMethod The defaultInterpolationMethod to set.
	 */
	public void setDefaultInterpolationMethod(String defaultInterpolationMethod) {
		this.defaultInterpolationMethod = defaultInterpolationMethod;
	}
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return Returns the dirName.
	 */
	public String getDirName() {
		return dirName;
	}
	/**
	 * @param dirName The dirName to set.
	 */
	public void setDirName(String dirName) {
		this.dirName = dirName;
	}
	/**
	 * @return Returns the envelope.
	 */
	public Envelope getEnvelope() {
		return envelope;
	}
	/**
	 * @param envelope The envelope to set.
	 */
	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}
	/**
	 * @return Returns the formatId.
	 */
	public String getFormatId() {
		return formatId;
	}
	/**
	 * @param formatId The formatId to set.
	 */
	public void setFormatId(String formatId) {
		this.formatId = formatId;
	}
	/**
	 * @return Returns the interpolationMethods.
	 */
	public List getInterpolationMethods() {
		return interpolationMethods;
	}
	/**
	 * @param interpolationMethods The interpolationMethods to set.
	 */
	public void setInterpolationMethods(List interpolationMethods) {
		this.interpolationMethods = interpolationMethods;
	}
	/**
	 * @return Returns the keywords.
	 */
	public List getKeywords() {
		return keywords;
	}
	/**
	 * @param keywords The keywords to set.
	 */
	public void setKeywords(List keywords) {
		this.keywords = keywords;
	}
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return Returns the metadataLink.
	 */
	public MetaDataLink getMetadataLink() {
		return metadataLink;
	}
	/**
	 * @param metadataLink The metadataLink to set.
	 */
	public void setMetadataLink(MetaDataLink metadataLink) {
		this.metadataLink = metadataLink;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the nativeFormat.
	 */
	public String getNativeFormat() {
		return nativeFormat;
	}
	/**
	 * @param nativeFormat The nativeFormat to set.
	 */
	public void setNativeFormat(String nativeFormat) {
		this.nativeFormat = nativeFormat;
	}
	/**
	 * @return Returns the requestCRSs.
	 */
	public List getRequestCRSs() {
		return requestCRSs;
	}
	/**
	 * @param requestCRSs The requestCRSs to set.
	 */
	public void setRequestCRSs(List requestCRSs) {
		this.requestCRSs = requestCRSs;
	}
	/**
	 * @return Returns the responseCRSs.
	 */
	public List getResponseCRSs() {
		return responseCRSs;
	}
	/**
	 * @param responseCRSs The responseCRSs to set.
	 */
	public void setResponseCRSs(List responseCRSs) {
		this.responseCRSs = responseCRSs;
	}
	/**
	 * @return Returns the srsName.
	 */
	public String getSrsName() {
		return srsName;
	}
	/**
	 * @param srsName The srsName to set.
	 */
	public void setSrsName(String srsName) {
		this.srsName = srsName;
	}
	/**
	 * @return Returns the supportedFormats.
	 */
	public List getSupportedFormats() {
		return supportedFormats;
	}
	/**
	 * @param supportedFormats The supportedFormats to set.
	 */
	public void setSupportedFormats(List supportedFormats) {
		this.supportedFormats = supportedFormats;
	}
	public CoordinateReferenceSystem getCrs() {
		return crs;
	}
	public void setCrs(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}

	public GridGeometry getGrid() {
		return grid;
	}

	public void setGrid(GridGeometry grid) {
		this.grid = grid;
	}

	public InternationalString[] getDimentionNames() {
		return dimentionNames;
	}

	public void setDimentionNames(InternationalString[] dimentionNames) {
		this.dimentionNames = dimentionNames;
	}
	/**
	 * @return Returns the dimensions.
	 */
	public CoverageDimension[] getDimensions() {
		return dimensions;
	}
	/**
	 * @param dimensions The dimensions to set.
	 */
	public void setDimensions(CoverageDimension[] dimensions) {
		this.dimensions = dimensions;
	}
}
