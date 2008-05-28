package org.geoserver.catalog;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.opengis.util.ProgressListener;

/**
 * A raster or coverage based store.
 * 
 * @author Justin Deoliveira, The Open Planning project
 */
public interface CoverageStoreInfo extends StoreInfo {

    /**
     * The coverage store type.
     * 
     * @uml.property name="type"
     */
    String getType();

    /**
     * Sets the type of the coverage store.
     * 
     * @uml.property name="type"
     */
    void setType(String type);

    /**
     * The coverage store url.
     * 
     * @uml.property name="url"
     */
    String getURL();

    /**
     * Sets the coverage store url.
     * 
     * @uml.property name="url"
     */
    void setURL(String url);
    
    /**
     * The grid format.
     */
    AbstractGridFormat getFormat();

    
    /**
     * Returns the coverage resource from the store with the given name.
     */
    //CoverageResource getResource(String name, ProgressListener listener);

    /**
     * Returns the coverage resources provided by the store.
     */
    //Iterator<CoverageResource> getResources(ProgressListener monitor)
    //    throws IOException;
}
