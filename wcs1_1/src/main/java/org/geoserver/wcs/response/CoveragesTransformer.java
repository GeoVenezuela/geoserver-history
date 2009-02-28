/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wcs.response;

import java.util.logging.Logger;

import net.opengis.wcs11.GetCoverageType;

import org.geoserver.catalog.CoverageInfo;
import org.geoserver.ows.util.RequestUtils;
import org.geoserver.wcs.WCSInfo;
import org.geotools.util.logging.Logging;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Based on the <code>org.geotools.xml.transform</code> framework, does the
 * job of encoding a WCS 1.1.1 Coverages document (for a single coverage with no
 * metadata)
 * 
 * @author Andrea Aime, TOPP
 */
public class CoveragesTransformer extends TransformerBase {
    private static final Logger LOGGER = Logging.getLogger(CoveragesTransformer.class.getPackage()
            .getName());

    private static final String WCS_URI = "http://www.opengis.net/wcs/1.1.1";

    private static final String XSI_PREFIX = "xsi";

    private static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    private WCSInfo wcs;

    private GetCoverageType request;

    private String coverageLocation;

    /**
     * Creates a new WFSCapsTransformer object to be used when encoding the multipart output
     */
    public CoveragesTransformer(WCSInfo wcs, GetCoverageType request) {
        this(wcs, request, "cid:theCoverage");
    }

    public CoveragesTransformer(WCSInfo wcs, GetCoverageType request, String coverageLocation) {
        this.wcs = wcs;
        this.request = request;
        this.coverageLocation = coverageLocation;
        setNamespaceDeclarationEnabled(false);
    }

    public Translator createTranslator(ContentHandler handler) {
        return new CoveragesTranslator(handler);
    }

    private class CoveragesTranslator extends TranslatorSupport {
        private String proxifiedBaseUrl;

        /**
         * Creates a new WFSCapsTranslator object.
         * 
         * @param handler
         *            DOCUMENT ME!
         */
        public CoveragesTranslator(ContentHandler handler) {
            super(handler, null, null);
        }

        /**
         * Encode the object.
         * 
         * @param o
         *            The Object to encode.
         * 
         * @throws IllegalArgumentException
         *             if the Object is not encodeable.
         */
        public void encode(Object o) throws IllegalArgumentException {
            try {
                if (!(o instanceof CoverageInfo)) {
                    throw new IllegalArgumentException(new StringBuffer(
                            "Not a GetCapabilitiesType: ").append(o).toString());
                }

                CoverageInfo info = (CoverageInfo) o;

                final AttributesImpl attributes = new AttributesImpl();
                attributes.addAttribute("", "xmlns:wcs", "xmlns:wcs", "", WCS_URI);

                attributes.addAttribute("", "xmlns:xlink", "xmlns:xlink", "",
                        "http://www.w3.org/1999/xlink");
                attributes.addAttribute("", "xmlns:ogc", "xmlns:ogc", "",
                        "http://www.opengis.net/ogc");
                attributes.addAttribute("", "xmlns:ows", "xmlns:ows", "",
                        "http://www.opengis.net/ows/1.1");
                attributes.addAttribute("", "xmlns:gml", "xmlns:gml", "",
                        "http://www.opengis.net/gml");

                final String prefixDef = new StringBuffer("xmlns:").append(XSI_PREFIX).toString();
                attributes.addAttribute("", prefixDef, prefixDef, "", XSI_URI);

                final String locationAtt = new StringBuffer(XSI_PREFIX).append(":schemaLocation")
                        .toString();

                proxifiedBaseUrl = RequestUtils.proxifiedBaseURL(request.getBaseUrl(), wcs
                        .getGeoServer().getGlobal().getProxyBaseUrl());
                final String locationDef = WCS_URI + " " + proxifiedBaseUrl
                        + "schemas/wcs/1.1.1/wcsCoverages.xsd";
                attributes.addAttribute("", locationAtt, locationAtt, "", locationDef);

                start("wcs:Coverages", attributes);
                handleCoverage(info);
                end("wcs:Coverages");
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unexpected error occurred during describe coverage xml encoding", e);
            }
        }

        void handleCoverage(CoverageInfo ci) throws Exception {
            start("wcs:Coverage");
            element("ows:Title", ci.getTitle());
            element("ows:Abstract", ci.getDescription());
            element("ows:Identifier", ci.getName());
            final AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute("", "xlink:href", "xlink:href", "", coverageLocation);
            element("ows:Reference", "", attributes);
            end("wcs:Coverage");
        }

    }

}
