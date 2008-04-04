package org.geoserver.geosearch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geoserver.ows.util.KvpMap;
import org.geoserver.ows.util.KvpUtils;
import org.geoserver.rest.RESTUtils;
import org.geoserver.rest.RestletException;
import org.geoserver.wms.WebMapService;
import org.geoserver.wms.kvp.GetMapKvpRequestReader;
import org.geotools.util.logging.Logging;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.OutputRepresentation;
import org.vfny.geoserver.global.Data;
import org.vfny.geoserver.global.FeatureTypeInfo;
import org.vfny.geoserver.global.NameSpaceInfo;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.wms.requests.GetMapRequest;
import org.vfny.geoserver.wms.responses.GetMapResponse;

public class FeatureRestlet extends Restlet {
    private static Logger LOGGER = Logging.getLogger("org.geoserver.geosearch");

    private WMS myWMS;
    private Data catalog;
    private WebMapService webMapService;

    public void setWms(WMS wms){
        myWMS = wms;
    }

    public WMS getWms(){
        return myWMS;
    }

    public void setCatalog(Data catalog) {
        this.catalog = catalog;
    }
    
    public void setWebMapService(WebMapService webMapService) {
        this.webMapService = webMapService;
    }
    
    public FeatureRestlet() {
    }

    public void handle(Request request, Response response){

        if (request.getMethod().equals(Method.GET)){
            try {
                doGet(request, response);
            } 
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        } else {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
 
    }

    public void doGet(Request request, Response response) throws Exception{
        String layer = (String)request.getAttributes().get("layer");
        String namespace = (String)request.getAttributes().get("namespace");
        String feature = (String)request.getAttributes().get("feature");
        
        NameSpaceInfo ns = catalog.getNameSpace(namespace);
        if ( ns == null ) {
            throw new RestletException( "No such namespace:" + namespace, Status.CLIENT_ERROR_NOT_FOUND );
        }
        
        FeatureTypeInfo featureType = null;
        try {
            featureType = catalog.getFeatureTypeInfo(layer,ns.getUri());
        }
        catch( NoSuchElementException e ) {
            //ignore, handled later
        }
        if ( featureType == null ) {
            throw new RestletException( "No such layer:" + layer, Status.CLIENT_ERROR_NOT_FOUND);    
        }
            
        //create some kvp and pass through to GetMapKvpreader
        KvpMap raw = new KvpMap();
        raw.put("layers", namespace + ":" + layer);
        raw.put("format", "geosearch-kml");
        
        if ( feature != null ) {
            raw.put("featureid", layer + "." + feature);    
        }
        
        GetMapKvpRequestReader reader = new GetMapKvpRequestReader(getWms());
        reader.setHttpRequest( RESTUtils.getServletRequest( request ) );
        
        //parse into request object
        raw = KvpUtils.normalize(raw);
        KvpMap kvp = new KvpMap( raw );
        KvpUtils.parse( kvp );
        final GetMapRequest getMapRequest =  
            (GetMapRequest) reader.read( (GetMapRequest) reader.createRequest(), kvp, raw );
        getMapRequest.setBaseUrl( RESTUtils.getBaseURL(request));
        
        //delegate to wms reflector
        final GetMapResponse getMapResponse = webMapService.reflect(getMapRequest);
        
        //wrap resposne in a reslet output rep
        OutputRepresentation output = new OutputRepresentation( 
            new MediaType("application/xml+kml")  
        ) {
            public void write(OutputStream outputStream) throws IOException {
                getMapResponse.execute(getMapRequest);
                getMapResponse.writeTo(outputStream);
            }
        };
        response.setEntity( output );

        
    }
}

