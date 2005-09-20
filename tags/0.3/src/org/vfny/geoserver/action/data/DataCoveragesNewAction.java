/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.vfny.geoserver.action.data;


import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.epsg.DefaultFactory;
//import org.geotools.referencing.crs.EPSGCRSAuthorityFactory;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.vfny.geoserver.action.ConfigAction;
import org.vfny.geoserver.config.CoverageConfig;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.DataFormatConfig;
import org.vfny.geoserver.form.data.DataCoveragesNewForm;
import org.vfny.geoserver.global.ConfigurationException;
import org.vfny.geoserver.global.UserContainer;


/**
 * DataCoveragesNewAction purpose.
 * 
 * <p>
 * Description of DataCoveragesNewAction ...
 * </p>
 * 
 * <p>
 * Capabilities:
 * </p>
 * 
 * <ul>
 * <li>
 * Coverage: description
 * </li>
 * </ul>
 * 
 * <p>
 * Example Use:
 * </p>
 * <pre><code>
 * DataCoveragesNewAction x = new DataCoveragesNewAction(...);
 * </code></pre>
 *
 * @author rgould, Refractions Research, Inc.
 * @author cholmesny
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author $Author: Simone Giannecchini (simboss_ml@tiscali.it) $ (last modification)
 * @version $Id: DataCoveragesNewAction.java,v 1.15 2004/09/17 16:34:47 cholmesny Exp $
 */
public class DataCoveragesNewAction extends ConfigAction {
    public final static String NEW_COVERAGE_KEY = "newCoverage";

    public ActionForward execute(ActionMapping mapping,
        ActionForm incomingForm, UserContainer user,  HttpServletRequest request,
        HttpServletResponse response) throws ConfigurationException {
        
        DataCoveragesNewForm form = (DataCoveragesNewForm) incomingForm;
        String selectedNewCoverage = form.getSelectedNewCoverage();

        DataConfig dataConfig = (DataConfig) request.getSession()
                                                    .getServletContext()
                                                    .getAttribute(DataConfig.CONFIG_KEY);
        String formatID = selectedNewCoverage;
        String coverageName = "";
		DataFormatConfig dfConfig = dataConfig.getDataFormat(formatID);
        GridCoverage2D gc = null;

        try {
			ServletContext sc = getServlet().getServletContext();
			URL url = getResource(dfConfig.getUrl(), sc.getRealPath("/"));


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
								//CRSFactory crsFactory = FactoryFinder.getCRSFactory(new Hints(Hints.CRS_AUTHORITY_FACTORY,EPSGCRSAuthorityFactory.class));
								CRSFactory crsFactory = FactoryFinder.getCRSFactory(new Hints(Hints.CRS_AUTHORITY_FACTORY,CRSAuthorityFactory.class));
								CoordinateReferenceSystem crs = crsFactory.createFromWKT((String) dfConfig.getParameters().get(key));
								value = crs;
							} else {
								//CRSAuthorityFactory crsFactory=FactoryFinder.getCRSAuthorityFactory("EPSG",new Hints(Hints.CRS_AUTHORITY_FACTORY,EPSGCRSAuthorityFactory.class));
								CRSAuthorityFactory crsFactory=FactoryFinder.getCRSAuthorityFactory("EPSG", new Hints(Hints.CRS_AUTHORITY_FACTORY, CRSAuthorityFactory.class));
								CoordinateReferenceSystem crs=(CoordinateReferenceSystem) crsFactory.createCoordinateReferenceSystem("EPSG:4326");
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
							} else {
								value = "#000000;#3C3C3C;#FFFFFF";
							}
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

			gc = (GridCoverage2D) reader.read(
					params != null ?
					(GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[params.values().size()])
					: null
					);
		} catch (InvalidParameterValueException e) {
			throw new ConfigurationException(e);
		} catch (ParameterNotFoundException e) {
			throw new ConfigurationException(e);
		} catch (MalformedURLException e) {
			throw new ConfigurationException(e);
		} catch (IllegalArgumentException e) {
			throw new ConfigurationException(e);
		} catch (SecurityException e) {
			throw new ConfigurationException(e);
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}
    	
        CoverageConfig cvConfig = new CoverageConfig(formatID, dfConfig.getType(), gc, true);

        request.setAttribute(NEW_COVERAGE_KEY, "true");
        request.getSession().setAttribute(DataConfig.SELECTED_COVERAGE, cvConfig);

        user.setCoverageConfig( cvConfig );
        return mapping.findForward("config.data.coverage.editor");
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
}
