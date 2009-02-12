package org.geoserver.catalog.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.geoserver.catalog.Catalog;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.rest.format.ReflectiveJSONFormat;
import org.geoserver.rest.format.ReflectiveXMLFormat;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public abstract class AbstractCatalogResource extends CatalogResourceBase {

    public AbstractCatalogResource(Context context,Request request, Response response, Class clazz,
        Catalog catalog) {
        
        super(context, request, response, clazz, catalog);
    }
   
    @Override
    protected ReflectiveXMLFormat createXMLFormat(Request request,Response response) {
        return new ReflectiveXMLFormat() {
            @Override
            protected void write(Object data, OutputStream output) throws IOException  {
                XStreamPersister p = new XStreamPersister.XML();
                p.setCatalog( catalog );
                
                configurePersister(p);
                p.save( data, output );
            }
            
            @Override
            protected Object read(InputStream in)
                    throws IOException {
                XStreamPersister p = new XStreamPersister.XML();
                p.setCatalog( catalog );
                
                configurePersister(p);
                return p.load( in, clazz );
            }
        };
    }
    
    @Override
    protected ReflectiveJSONFormat createJSONFormat(Request request,Response response) {
        return new ReflectiveJSONFormat() {
            @Override
            protected void write(Object data, OutputStream output)
                    throws IOException {
                XStreamPersister p = new XStreamPersister.JSON();
                p.setCatalog(catalog);
                
                configurePersister(p);
                p.save( data, output );
            }
            
            @Override
            protected Object read(InputStream input)
                    throws IOException {
                XStreamPersister p = new XStreamPersister.JSON();
                p.setCatalog(catalog);
                
                configurePersister(p);
                return p.load( input, clazz );
            }
        };
    }
    
    /**
     * Method for subclasses to perform additional configuration of the 
     * xstream instance used for serializing/de-serializing objects.
     */
    protected void configurePersister(XStreamPersister persister) {
    }
}
