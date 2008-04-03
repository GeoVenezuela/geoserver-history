package org.geoserver.geosearch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.geoserver.ows.KvpParser;
import org.geoserver.ows.util.CaseInsensitiveMap;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.rest.RESTUtils;
import org.geoserver.wms.WebMapService;
import org.geoserver.wms.kvp.GetMapKvpRequestReader;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.util.logging.Logging;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.geometry.BoundingBox;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.OutputRepresentation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.global.Data;
import org.vfny.geoserver.global.FeatureTypeInfo;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.wms.requests.GetMapRequest;
import org.vfny.geoserver.wms.responses.GetMapResponse;
import org.vfny.geoserver.wms.responses.map.kml.GeoSearchMapProducerFactory;
import org.vfny.geoserver.wms.servlets.GetMap;

public class LayerRestlet extends GeoServerProxyAwareRestlet implements ApplicationContextAware {
    private static Logger LOGGER = Logging.getLogger("org.geoserver.geosearch");

    private Data myData;
    private DataConfig myDataConfig;
    private WMS myWMS;
    private GetMap myGetMap;
    private ApplicationContext myContext;
    private WebMapService webMapService;

    private static Namespace KML = Namespace.getNamespace("http://earth.google.com/kml/2.2");
    private static Namespace ATOM = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom");

    public void setWebMapService(WebMapService webMapService) {
        this.webMapService = webMapService;
    }
    
    public void setData(Data d){
        myData = d;
    }

    public void setGetMap(GetMap gm){
        myGetMap = gm;
    }

    public void setWms(WMS wms){
        myWMS = wms;
    }

    public void setDataConfig(DataConfig dc){
        myDataConfig = dc;
    }

    public void setApplicationContext(ApplicationContext context){
        myContext = context;
    }

    public Data getData(){
        return myData;
    }

    public GetMap getGetMap(){
        return myGetMap;
    }

    public WMS getWms(){
        return myWMS;
    }

    public DataConfig getDataConfig(){
        return myDataConfig;
    }

    public ApplicationContext getApplicationContext(){
        return myContext;
    }

    public LayerRestlet() {
    }

    public void handle(Request request, Response response){
        try{
            if (request.getMethod().equals(Method.GET)){
                doGet(request, response);
            } else {
                response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception e){
            LOGGER.severe("Badness (" + e + ") while handling layer request!");
            e.printStackTrace();
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    public void doGet(Request request, Response response) throws Exception{
        String namespace = (String)request.getAttributes().get("namespace");
        String layername = (String)request.getAttributes().get("layer");
        String format = (String)request.getAttributes().get("format");
        
        GeoSearchMapProducerFactory.BASE_URL = getBaseURL(request);
        if (request.getMethod().equals(Method.GET)) {
            GetMapKvpRequestReader reader = new GetMapKvpRequestReader(getWms());
            reader.setHttpRequest( RESTUtils.getServletRequest( request ) );
            
            Map raw = new HashMap();
            raw.put("layers", namespace + ":" + layername); 
            // raw.put("styles", "polygon");
            raw.put("format", (format == null ? "kmlgeosearch" : format));
            raw.put("srs", "epsg:4326");
            raw.put("bbox", "-180,-90,180,90");
            raw.put("height", "600");
            raw.put("width", "800");
            
            final GetMapRequest gmreq = (GetMapRequest) reader.read((GetMapRequest) reader.createRequest(), parseKvp(raw), raw);

            final GetMapResponse gmresp = new GetMapResponse(getWms(), getApplicationContext());
            response.setEntity(new OutputRepresentation(new MediaType("application/xml+kml")){
                    public void write(OutputStream os){
                    try{
                    gmresp.execute(gmreq);
                    gmresp.writeTo(os);
                    } catch (IOException ioe){
                    // blah, this will never happen
                    }
                    }

                    });

        } else {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    /**
     * Parses a raw set of kvp's into a parsed set of kvps.
     *
     * @param kvp Map of String,String.
     */
    protected Map parseKvp(Map /*<String,String>*/ raw)
        throws Exception {
            //parse the raw values
            List parsers = GeoServerExtensions.extensions(KvpParser.class, getApplicationContext());
            Map kvp = new CaseInsensitiveMap(new HashMap());

            for (Iterator e = raw.entrySet().iterator(); e.hasNext();) {
                Map.Entry entry = (Map.Entry) e.next();
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                Object parsed = null;

                for (Iterator p = parsers.iterator(); p.hasNext();) {
                    KvpParser parser = (KvpParser) p.next();

                    if (key.equalsIgnoreCase(parser.getKey())) {
                        parsed = parser.parse(val);

                        if (parsed != null) {
                            break;
                        }
                    }
                }

                if (parsed == null) {
                    parsed = val;
                }

                kvp.put(key, parsed);
            }

            return kvp;
        }


    private Document buildKML(String namespace, String layername){
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = findFeatures(namespace, layername);
        Document doc = new Document();
        Element kml = new Element("kml", KML);
        Element docElem = new Element("Document", KML);
        Element name = new Element("name", KML);
        name.setText(namespace + ":" + layername);
        docElem.addContent(name);

        Iterator it = features.iterator();
        while (it.hasNext()){
            SimpleFeature f = (SimpleFeature)it.next();
            Element feature = encodeFeature(f, "http://localhost:8080/geoserver/api/geosearch/" + namespace + "/" + layername);
            docElem.addContent(feature);
        }

        kml.addContent(docElem);
        doc.setRootElement(kml);
        return doc;
    }

    private Element encodeFeature(SimpleFeature f, String parentURL){
        Element feature = new Element("Placemark", KML);
        Element name = new Element("name", KML);
        name.setText(f.getID());
        Element lookAt = getLookAt(f.getBounds());
        // Element geometry = new Element("Geometry", KML);
        Element link = new Element("link", ATOM);
        link.setAttribute("rel", "self");
        link.setAttribute("type", "application/vnd.google-earth.kml+xml");
        link.setAttribute("href", parentURL + "/" + getNumericID(f.getID()));
        feature.addContent(name).addContent(lookAt).addContent(link);
        return feature;
    }

    private static String getNumericID(String featureID){
        int index = featureID.lastIndexOf(".");
        return featureID.substring(index);
    }

    private static Element getLookAt(BoundingBox bbox){
        return NamespaceIndexRestlet.getLookAt(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
    }

    private FeatureCollection<SimpleFeatureType, SimpleFeature> findFeatures(String namespace, String layername){
        try{
            String typename = namespace + ":" + layername;
            FeatureTypeInfo info = getData().getFeatureTypeInfo(typename);
            FeatureSource<SimpleFeatureType, SimpleFeature> source = info.getFeatureSource();
            DefaultQuery query = new DefaultQuery(DefaultQuery.ALL);
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
            query.setSortBy(new SortBy[]{ff.sort("fid",SortOrder.ASCENDING)});
            query.setMaxFeatures(100); // TODO: make feature limit a parameter to the request
            FeatureCollection<SimpleFeatureType, SimpleFeature> results = source.getFeatures(query);
            return results;
        } catch (IOException ioe){
            System.out.println("IOException while retrieving data:  ");
            ioe.printStackTrace();
            return null;
        }
    }
}

