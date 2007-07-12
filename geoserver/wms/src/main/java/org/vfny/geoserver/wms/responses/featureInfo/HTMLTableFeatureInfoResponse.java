/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.featureInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.geoserver.template.FeatureWrapper;
import org.geoserver.template.GeoServerTemplateLoader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * Produces a FeatureInfo response in HTML.  Relies on {@link AbstractFeatureInfoResponse} and
 * the feature delegate to do most of the work, just implements an HTML based
 * writeTo method.
 *
 * <p>
 * In the future James suggested that we allow some sort of template system, so
 * that one can control the formatting of the html output, since now we just
 * hard code some minimal header stuff. See
 * http://jira.codehaus.org/browse/GEOS-196
 * </p>
 *
 * @author James Macgill, PSU
 * @author Andrea Aime, TOPP
 * @version $Id$
 */
public class HTMLTableFeatureInfoResponse extends AbstractFeatureInfoResponse {
    private static Configuration templateConfig;

    static {
        // initialize the template engine, this is static to maintain a cache
        // over instantiations of kml writer
        templateConfig = new Configuration();
        templateConfig.setObjectWrapper(new FeatureWrapper());
    }
    
    GeoServerTemplateLoader templateLoader;
    
    
    /**
     *
     */
    public HTMLTableFeatureInfoResponse() {
        format = "text/html";
        supportedFormats = Collections.singletonList(format);
    }

    /**
     * Returns any extra headers that this service might want to set in the HTTP response object.
     * @see org.vfny.geoserver.Response#getResponseHeaders()
     */
    public HashMap getResponseHeaders() {
        return new HashMap();
    }

    /**
     * Writes the image to the client.
     *
     * @param out The output stream to write to.
     *
     * @throws org.vfny.geoserver.ServiceException For problems with geoserver
     * @throws java.io.IOException For problems writing the output.
     */
    public void writeTo(OutputStream out)
        throws org.vfny.geoserver.ServiceException, java.io.IOException {
        // setup the writer
        Charset charSet = getRequest().getGeoServer().getCharSet();
        OutputStreamWriter osw = new OutputStreamWriter(out, charSet);
        
        // if there is only one feature type loaded, we allow for header/footer customization,
        // otherwise we stick with the generic ones
        FeatureType templateFeatureType = null;
        if(results.size() == 1) {
            templateFeatureType = ((FeatureCollection) results.get(0)).getSchema();
        }
        Template header = getTemplate(templateFeatureType, "header.ftl");
        Template footer = getTemplate(templateFeatureType, "footer.ftl");
        
        try {
            header.process(null, osw);
            
            for (Iterator it = results.iterator(); it.hasNext();) {
                FeatureCollection fc = (FeatureCollection) it.next();
                if(fc.size() > 0) {
                    FeatureType ft = fc.getSchema();
                    Template content = getTemplate(ft, "content.ftl");
                    content.process(fc, osw);
                }
            }
            
            footer.process(null, osw);
        } catch(TemplateException e) {
            String msg = "Error occured processing template.";
            throw (IOException) new IOException(msg).initCause(e);
        }
        osw.flush();
    }

    public String getContentDisposition() {
        return null;
    }
    
    Template getTemplate(FeatureType featureType, String templateFileName) throws IOException {
        // setup template subsystem
        if(templateLoader == null) {
            templateLoader = new GeoServerTemplateLoader(getClass());
        }
        templateLoader.setFeatureType(featureType);

        synchronized (templateConfig) {
            templateConfig.setTemplateLoader(templateLoader);
            return templateConfig.getTemplate(templateFileName);
        }
    }
}
