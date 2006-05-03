package org.vfny.geoserver.global;

/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

import java.io.File;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import org.vfny.geoserver.global.xml.ReaderUtils;

/**
 *   This class allows for abstracting the location of the Geoserver Data directory.  Some people call this "GEOSERVER_HOME".
 *   
 *   Inside this directory should be two more directories:
 *     a. "WEB-INF/"  Inside this is a catalog.xml
 *     b. "data/"     Inside this is a set of other directories.
 *    
 *   For the exact content of these directories, see any existing geoserver install's server/geoserver directory.
 * 
 *   In order to find the geoserver data directory the following steps take place:
 *  
 *    1. search for the "GEOSERVER_DATA_DIR" system property.
 *         this will most likely have come from "java -DGEOSERVER_DATA_DIR=..." or from you web container
 *    2. search for a "GEOSERVER_DATA_DIR" in the web.xml document
 *        <context-param>
 *             <param-name>GEOSERVER_DATA_DIR</param-name>
 *             <param-value>...</param-value>
 *         </context-param>
 *    3. It defaults to the old behavior - ie. the application root - usually "server/geoserver" in your .WAR.
 *  
 * 
 *    NOTE: a set method is currently undefined because you should either modify you web.xml or
 *          set the environment variable and re-start geoserver.
 * 
 * @author dblasby
 *
 */
public class GeoserverDataDirectory
{
	private static final Logger LOGGER = Logger.getLogger("org.vfny.geoserver.global");
	
    //caches the dataDir
    private static File dataDir;

    private static boolean isTrueDataDir = false;


	/**
	 *   See the class documentation for more details.
	 *   1. search for the "GEOSERVER_DATA_DIR" system property. 
	 *   2. search for a "GEOSERVER_DATA_DIR" in the web.xml document
	 *   3. It defaults to the old behavior - ie. the application root - usually "server/geoserver" in your .WAR.
	 * @return  location of the geoserver data dir
	 */
	static public File getGeoserverDataDirectory(ServletContext servContext)
	{
        //caching this, so we're not looking up everytime, and more 
	    //importantly, so we can actually look up this stuff without
        //having to pass in a ServletContext. This should be fine, since we
	    //don't allow a set method, as we recommend restarting GeoServer,
	    //so it should always get a ServletContext in the startup routine.
	    //If this assumption can't be made, then we can't allow data_dir
	    //_and_ webapp options with relative data/ links -ch
	    if (dataDir == null) {
	    	
			//see if there's a system property
			String prop = System.getProperty("GEOSERVER_DATA_DIR");
			if (prop != null && !prop.equals(""))
			{
				 //its defined!!
			    isTrueDataDir = true;
				dataDir = new File(prop);
				LOGGER.info("Data_dir: " + dataDir.getPath());
				return dataDir;
			}
			
			
			//try the webxml
			String loc = servContext.getInitParameter("GEOSERVER_DATA_DIR");
			if (loc != null)
			{
				//its defined!!
			    isTrueDataDir = true;
				dataDir = new File(loc);
				LOGGER.info("Data_dir: " + dataDir.getPath());
				return dataDir;
			}
			
			//return default
	        isTrueDataDir = false;
			String rootDir = servContext.getRealPath("/");
			dataDir = new File (rootDir);
			LOGGER.info("Data_dir: " + dataDir.getPath());
	    }
	    
	    return dataDir;
	}

    /**
     * Returns whether GeoServer is using a true data directory, loaded from
     * outside the webapp, or if its defaulting to the webapp embedded dataDir.
     * We're in the process of moving away from storing anything in the webapp
     * but are keeping this to ease the transition.  
     *
     * @return <tt>true</tt> if the directory being used for loading is not 
     *         embedded in the webapp.
     */
    static public boolean isTrueDataDir() {
	return isTrueDataDir;
    }

    /** 
     * Utility method to find the approriate sub-data dir config.  This is 
     * a helper for the fact that we're transitioning away from the WEB-INF
     * type of hacky storage, but during the transition things can be in
     * both places.  So this method takes the root file, the dataDir, and
     * a name of a directory that is stored in the data dir, and checks for
     * it in the data/ dir (the old way), and directly in the dir (the new way)
     * 
     * @param root Generally the Data Directory, the directory to try to find
     *             the config file in.
     * @param dirName The name of the directory to find in the data Dir.
     * @return The proper config directory.
     * @throws ConfigurationException if the directory could not be found at all.    */
    public static File findConfigDir(File root, String dirName) throws ConfigurationException {
        File configDir;
	try {
	    configDir = ReaderUtils.checkFile(new File(root, dirName), true);
	} catch (ConfigurationException confE) {
	    File dataDir = new File(root, "data/"); //check in data/, the old way.
	    configDir = ReaderUtils.checkFile(new File(dataDir, dirName), true);
	}
	return configDir;
    } 
}


