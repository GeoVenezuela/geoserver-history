package org.vfny.geoserver.global;


import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.styling.Style;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.DataFormatConfig;
import org.vfny.geoserver.global.dto.CoverageInfoDTO;
import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author $Author: Simone Giannecchini (simboss_ml@tiscali.it) $ (last modification)
 */
public class MapLayerInfo extends GlobalLayerSupertype {
	public static int TYPE_VECTOR = 0;
	public static int TYPE_RASTER = 1;
	
	private FeatureTypeInfo feature;
	private CoverageInfo coverage;
	private int type;
	
    private String name;

    private String label;

    private String description;

    private String dirName;
    
    public MapLayerInfo() {
    	name = "";
    	label = "";
    	description = "";
    	dirName = "";
    	
    	coverage = null;
    	feature = null;
    	type = -1;
    }
    
    public MapLayerInfo(CoverageInfoDTO dto, Data data)
        throws ConfigurationException {

        name = dto.getName();
        label = dto.getLabel();
        description = dto.getDescription();
        dirName = dto.getDirName();
        
        coverage = new CoverageInfo(dto, data);
        feature = null;
        type = TYPE_RASTER;
    }

    public MapLayerInfo(FeatureTypeInfoDTO dto, Data data)
	    throws ConfigurationException {
	
	    name = dto.getName();
	    label = dto.getTitle();
	    description = dto.getAbstract();
	    dirName = dto.getDirName();
	    
	    feature = new FeatureTypeInfo(dto, data);
	    coverage = null;
	    type = TYPE_VECTOR;
    }

	/* (non-Javadoc)
	 * @see org.vfny.geoserver.global.GlobalLayerSupertype#toDTO()
	 */
	Object toDTO() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * getBoundingBox purpose.
     * 
     * <p>
     * The feature source bounds.
     * </p>
     *
     * @return Envelope the feature source bounds.
     *
     * @throws IOException when an error occurs
     */
    public Envelope getBoundingBox() throws IOException {
    	if( this.type == TYPE_VECTOR ) {
            return feature.getBoundingBox();
    	} else {
    		return coverage.getEnvelope();
    	}
    }

	public CoverageInfo getCoverage() {
		return coverage;
	}
	public void setCoverage(CoverageInfo coverage) {
        this.name = coverage.getName();
        this.label = coverage.getLabel();
        this.description = coverage.getDescription();
        this.dirName = coverage.getDirName();
        
		this.coverage = coverage;
		this.feature = null;
		this.type = TYPE_RASTER;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDirName() {
		return dirName;
	}
	public void setDirName(String dirName) {
		this.dirName = dirName;
	}
	public FeatureTypeInfo getFeature() {
		return feature;
	}
	public void setFeature(FeatureTypeInfo feature) {
	    this.name = feature.getName();
	    this.label = feature.getTitle();
	    this.description = feature.getAbstract();
	    this.dirName = feature.getDirName();
	    
		this.feature = feature;
		this.coverage = null;
	    this.type = TYPE_VECTOR;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	
	private URL getResource(String path, String baseDir) throws MalformedURLException{
		URL url = null;
		if (path.startsWith("file:data/")) {
			path = path.substring(5); // remove 'file:' prefix
			
			File file = new File(baseDir, path);
			url = file.toURL();
		} else {
			url = new URL(path);
		}
		
		return url;
	}

	private Feature wrapGcInFeature(GridCoverage gridCoverage)
	throws IllegalAttributeException, SchemaException {
		// create surrounding polygon
		PrecisionModel pm = new PrecisionModel();
		CoordinateSequenceFactory csf = DefaultCoordinateSequenceFactory.instance();
		GeometryFactory gf = new GeometryFactory(pm, 0);
		Coordinate[] coord = new Coordinate[5];
		Rectangle2D rect = ((GridCoverage2D) gridCoverage).getEnvelope2D().getBounds2D();
		coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
		coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
		coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
		coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
		coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());
		
		LinearRing ring = new LinearRing(csf.create(coord), gf);
		Polygon bounds = new Polygon(ring, null, gf);
		
		// create the feature type
		AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Polygon.class);
		AttributeType grid = AttributeTypeFactory.newAttributeType("grid", GridCoverage.class);
		
		FeatureType schema = null;
		AttributeType[] attTypes = {geom, grid};
		
		schema = FeatureTypeFactory.newFeatureType(attTypes, this.name);
		
		// create the feature
		Feature feature = schema.create(new Object[] {bounds, gridCoverage});
		
		return feature;
	}
	
	
	private GridCoverage getGridCoverage(HttpServletRequest request, CoverageInfo meta) throws IOException {
		GridCoverage2D coverage = null;
		
		try {
			String formatID = meta.getFormatId();
			DataConfig dataConfig = (DataConfig) request
						.getSession()
						.getServletContext()
						.getAttribute(DataConfig.CONFIG_KEY);
			DataFormatConfig dfConfig = dataConfig.getDataFormat(formatID);

			String realPath = request.getRealPath("/");
			URL url = getResource(dfConfig.getUrl(), realPath);

//			GridCoverageExchange gce = new StreamGridCoverageExchange();
//			GridCoverageReader reader = gce.getReader(url);
//			Format format = reader.getFormat();

			Format format = dfConfig.getFactory();
			GridCoverageReader reader = ((AbstractGridFormat) format).getReader(url);

			ParameterValueGroup params = format.getReadParameters();
			
			if( params != null ) {
				List list=params.values();
				Iterator it=list.iterator();
				while(it.hasNext())
				{
					ParameterValue param=((ParameterValue)it.next());
					ParameterDescriptor descr=(ParameterDescriptor)param.getDescriptor();
					
					Object value = null;
					String key = descr.getName().toString();
					
					try {
	    				if( key.equalsIgnoreCase("crs") ) {
							if( dfConfig.getParameters().get(key) != null && ((String) dfConfig.getParameters().get(key)).length() > 0 ) {
								CRSFactory crsFactory = FactoryFinder.getCRSFactory(null);
								CoordinateReferenceSystem crs = crsFactory.createFromWKT((String) dfConfig.getParameters().get(key));
								value = crs;
							} else {
								CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
								value = crs;
							}
						} else if( key.equalsIgnoreCase("envelope") ) {
							if( dfConfig.getParameters().get(key) != null && ((String) dfConfig.getParameters().get(key)).length() > 0 ) {
								String tmp = (String) dfConfig.getParameters().get(key);
								if( tmp.indexOf("[") > 0 && tmp.indexOf("]") > tmp.indexOf("[") ) {
									tmp = tmp.substring(tmp.indexOf("[") + 1, tmp.indexOf("]")).trim();
									tmp = tmp.replaceAll(",","");
									String[] strCoords = tmp.split(" ");
									double[] coords = new double[strCoords.length];
									if( strCoords.length == 4 ) {
										for( int iT=0; iT<4; iT++) {
											coords[iT] = Double.parseDouble(strCoords[iT].trim());
										}
										
										value = (org.opengis.spatialschema.geometry.Envelope) 
												new GeneralEnvelope(
													new double[] {coords[0], coords[1]},
													new double[] {coords[2], coords[3]}
												);
									}
								}
							}
						} else if( key.equalsIgnoreCase("values_palette") ) {
							if( dfConfig.getParameters().get(key) != null && ((String) dfConfig.getParameters().get(key)).length() > 0 ) {
								String tmp = (String) dfConfig.getParameters().get(key);
								String[] strColors = tmp.split(";");
								Vector colors = new Vector();
								for( int i=0; i<strColors.length; i++) {
									if(Color.decode(strColors[i]) != null) {
										colors.add(Color.decode(strColors[i]));
									}
								}
								
								value = colors.toArray(new Color[colors.size()]);
							}
							reader.getFormat().getReadParameters().parameter("values_palette").setValue(value);
						} else {
							Class[] clArray = {String.class};
							Object[] inArray = {dfConfig.getParameters().get(key)};
							value = param.getValue().getClass().getConstructor(clArray).newInstance(inArray);
	    				}
					} catch (Exception e) {
						value = null;
					}
					
					if( value != null )
						params.parameter(key).setValue(value);
				}
			}
			
			coverage = (GridCoverage2D) reader.read(
					params != null ?
					(GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[params.values().size()])
					: null
					);
		} catch (InvalidParameterValueException e) {
			throw new IOException(e.getMessage());
		} catch (ParameterNotFoundException e) {
			throw new IOException(e.getMessage());
		} catch (MalformedURLException e) {
			throw new IOException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		} catch (SecurityException e) {
			throw new IOException(e.getMessage());
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}
		
		return coverage;
	}
	
	public FeatureCollection getCoverageToFeatures(HttpServletRequest request)
	throws DataSourceException {
		FeatureCollection collection = FeatureCollections.newCollection();
		// last step, wrap, add the the feature collection and return
		try {
			GridCoverage gridCoverage = getGridCoverage(request, this.coverage);
			collection.add(wrapGcInFeature(gridCoverage));
		} catch (Exception e) {
			throw new DataSourceException("IO error", e);
		}
		
		return collection;
	}

	public GridCoverage getCoverageToLayer(HttpServletRequest request) {
		GridCoverage gridCoverage = null;
		try {
			gridCoverage = getGridCoverage(request, this.coverage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gridCoverage;
	}
	
	public Style getDefaultStyle() {
		if( this.type == TYPE_VECTOR )
			return this.feature.getDefaultStyle();
		else if( this.type == TYPE_RASTER )
			return this.coverage.getDefaultStyle();
		
		return null;
	}
}