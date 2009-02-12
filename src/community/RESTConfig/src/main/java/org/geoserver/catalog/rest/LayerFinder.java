package org.geoserver.catalog.rest;

import org.geoserver.catalog.Catalog;
import org.geoserver.rest.RestletException;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

public class LayerFinder extends AbstractCatalogFinder {

    public LayerFinder(Catalog catalog) {
        super(catalog);
    }
    
    @Override
    public Resource findTarget(Request request, Response response) {
        String layer = (String) request.getAttributes().get( "layer" );
        
        if ( layer == null && request.getMethod() == Method.GET ) {
            return new LayerListResource(getContext(),request,response,catalog);
        }
        
        //ensure referenced resources exist
        if ( layer != null && catalog.getLayerByName(layer) == null ) {
            throw new RestletException( "No such layer: " + layer, Status.CLIENT_ERROR_NOT_FOUND );
        }
        
        return new LayerResource(null,request,response,catalog);
    }

}
