/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.responses.wms.map;

import org.geotools.data.*;
import org.geotools.svg.SVGEncoder;
import org.geotools.svg.SVGWriter;

import org.vfny.geoserver.*;
import org.vfny.geoserver.config.FeatureTypeConfig;
import org.vfny.geoserver.requests.Request;
import org.vfny.geoserver.requests.wms.GetMapRequest;
import org.vfny.geoserver.responses.Response;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Handles a GetMap request that spects a map in SVG format.
 *
 * @author Gabriel Rold�n
 * @version $Id: SVGMapResponse.java,v 1.1.2.5 2003/11/27 19:44:33 jive Exp $
 */
public class SVGMapResponse extends GetMapDelegate {

    private static final Logger LOGGER = Logger.getLogger("org.vfny.geoserver.responses.wms.map");
    /** DOCUMENT ME!  */
    private static final String MIME_TYPE = "image/svg+xml";
    private SVGEncoder svgEncoder;
    private FeatureTypeConfig[] requestedLayers;
    private FeatureResults[] resultLayers;
    private List styles;
    private boolean writeHeader = true;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getContentType() {
        return MIME_TYPE;
    }

    public List getSupportedFormats()
    {
      return Collections.singletonList(MIME_TYPE);
    }

    /**
     * evaluates if this Map producer can generate the map format specified
     * by <code>mapFormat</code>
     *
     * @param mapFormat the mime type of the output map format requiered
     *
     * @return true if class can produce a map in the passed format
     */
    public boolean canProduce(String mapFormat)
    {
      return mapFormat.startsWith("image/svg");
    }


    public void abort()
    {
      LOGGER.fine("aborting SVG map response");
      if(svgEncoder != null)
      {
        LOGGER.info("aborting SVG encoder");
        svgEncoder.abort();
      }
    }

    /**
     * DOCUMENT ME!
     *
     * @param requestedLayers DOCUMENT ME!
     * @param resultLayers DOCUMENT ME!
     * @param styles DOCUMENT ME!
     *
     * @throws WmsException DOCUMENT ME!
     */
    protected void execute(FeatureTypeConfig[] requestedLayers,
        FeatureResults[] resultLayers, List styles) throws WmsException {
            
        GetMapRequest request = getRequest();
        this.requestedLayers = requestedLayers;
        this.resultLayers = resultLayers;
        this.styles = styles;
        this.svgEncoder = new SVGEncoder();

        // fast an easy way of configuring the SVG coordinates traslation
        // I assume that feature results are almost accurate with the bbox requested
        //
        svgEncoder.setReferenceSpace(getRequest().getBbox());
        svgEncoder.setWidth(String.valueOf(request.getWidth()));
        svgEncoder.setHeight(String.valueOf(request.getHeight()));
        
        // move to writeTo method?
        //svgEncoder.setWriteHeader(request.getWriteSvgHeader());
        writeHeader = request.getWriteSvgHeader();
        
    }

    /**
     * DOCUMENT ME!
     *
     * @param out DOCUMENT ME!
     *
     * @throws ServiceException DOCUMENT ME!
     * @throws WmsException DOCUMENT ME!
     */
    public void writeTo(OutputStream out) throws ServiceException, IOException {
        if( requestedLayers == null || resultLayers == null ){
            throw new IllegalStateException("Execute must be called prior to writeTo");
        }
        svgEncoder.encode(requestedLayers, resultLayers, writeHeader, out );
        
        // style configured in execture?
        // svgEncoder.encode(requestedLayers, resultLayers, styles, out);
    }
}
