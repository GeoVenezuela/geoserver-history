/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.global.dto;

import java.util.Map;


/**
 * Data Transfer Object for communication GeoServer Web Map Server information.
 *
 * <p>
 * Information required for GeoServer to set up a Web Map Service.
 * </p>
 *
 * <p>
 * Data Transfer object are used to communicate between the GeoServer
 * application and its configuration and persistent layers. As such the class
 * is final - to allow for its future use as an on-the-wire message.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @version $Id$
 */
public final class WMSDTO implements DataTransferObject {
    /** For the writer! */
    private boolean gmlPrefixing;

    /** The service parameters for this instance. */
    private ServiceDTO service;

    /** The current svg renderer **/
    private String svgRenderer;

    /** The antialisaing hint for the svg renderer **/
    private boolean svgAntiAlias;

    /** The interpolation rendering hint **/
    private Map baseMapLayers;
    private Map baseMapStyles;
    private String allowInterpolation;

    /**
     * WMS constructor.  does nothing
     */
    public WMSDTO() {
    }

    /**
     * WMS constructor.
     *
     * <p>
     * Creates a copy of the WMS provided. If the WMS provided  is null then
     * default values are used. All the data structures are cloned.
     * </p>
     *
     * @param other The WMS to copy.
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public WMSDTO(WMSDTO other) {
        if (other == null) {
            throw new NullPointerException("Data Transfer Object required");
        }

        service = (ServiceDTO) other.getService().clone();
        gmlPrefixing = other.isGmlPrefixing();
        svgRenderer = other.getSvgRenderer();
        svgAntiAlias = other.getSvgAntiAlias();
        allowInterpolation = other.getAllowInterpolation();
        baseMapLayers = other.getBaseMapLayers();
        baseMapStyles = other.getBaseMapStyles();
    }

    /**
     * Implement clone as a DeepCopy.
     *
     * @return A Deep Copy of this WMSDTO
     *
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new WMSDTO(this);
    }

    /**
     * Implement equals.
     *
     * <p>
     * recursively tests to determine if the object passed in is a copy of this
     * object.
     * </p>
     *
     * @param other The WMS object to test.
     *
     * @return true when the object passed is the same as this object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof WMSDTO)) {
            return false;
        }

        WMSDTO dto = (WMSDTO) other;

        boolean equals = (gmlPrefixing == dto.gmlPrefixing) && (svgAntiAlias == dto.svgAntiAlias)
            && (allowInterpolation == dto.allowInterpolation);

        if (equals) {
            if (service == null) {
                equals = dto.getService() == null;
            } else {
                equals = service.equals(dto.getService());
            }
        }

        if (equals) {
            if (svgRenderer == null) {
                equals = dto.getSvgRenderer() == null;
            } else {
                equals = svgRenderer.equals(dto.getSvgRenderer());
            }
        }

        if (equals) {
            if (baseMapLayers == null) {
                equals = dto.getBaseMapLayers() == null;
            } else {
                equals = baseMapLayers.equals(dto.getBaseMapLayers());
            }
        }

        if (equals) {
            if (baseMapStyles == null) {
                equals = dto.getBaseMapStyles() == null;
            } else {
                equals = baseMapStyles.equals(dto.getBaseMapStyles());
            }
        }

        return equals;
    }

    /**
     * Implement hashCode.
     *
     * @return Service hashcode or 0
     *
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (gmlPrefixing ? 1 : 0) | (svgAntiAlias ? 1 : 0)
        | ((allowInterpolation != null) ? 0 : allowInterpolation.hashCode())
        | ((service == null) ? 0 : service.hashCode())
        | ((svgRenderer == null) ? 0 : svgRenderer.hashCode())
        | ((baseMapLayers == null) ? 0 : baseMapLayers.hashCode())
        | ((baseMapStyles == null) ? 0 : baseMapStyles.hashCode());
    }

    /**
     * getService purpose.
     *
     * <p>
     * Description ...
     * </p>
     *
     * @return
     */
    public ServiceDTO getService() {
        return service;
    }

    /**
     * setService purpose.
     *
     * <p>
     * Description ...
     * </p>
     *
     * @param service
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public void setService(ServiceDTO service) {
        if (service == null) {
            throw new NullPointerException("ServiceDTO required");
        }

        this.service = service;
    }

    /**
     * isGmlPrefixing purpose.
     *
     * <p>
     * Description ...
     * </p>
     *
     * @return
     */
    public boolean isGmlPrefixing() {
        return gmlPrefixing;
    }

    /**
     * setGmlPrefixing purpose.
     *
     * <p>
     * Description ...
     * </p>
     *
     * @param b
     */
    public void setGmlPrefixing(boolean b) {
        gmlPrefixing = b;
    }

    /**
     * @return The constant identifying the current svg renderer.
     * @see org.vfny.geoserver.config.WMSConfig#SVG_SIMPLE
     * @see org.vfny.geoserver.config.WMSConfig#SVG_BATIK
     */
    public String getSvgRenderer() {
        return svgRenderer;
    }

    /**
     * @param The constant identifying the current svg renderer.
     * @see org.vfny.geoserver.config.WMSConfig#SVG_SIMPLE
     * @see org.vfny.geoserver.config.WMSConfig#SVG_BATIK
     */
    public void setSvgRenderer(String svgRenderer) {
        this.svgRenderer = svgRenderer;
    }

    /**
     * @param svgAntiAlias anti alias hint.
     */
    public void setSvgAntiAlias(boolean svgAntiAlias) {
        this.svgAntiAlias = svgAntiAlias;
    }

    /**
     * @return The value of the anti aliasing rendering hint.
     */
    public boolean getSvgAntiAlias() {
        return svgAntiAlias;
    }

    public void setBaseMapLayers(Map layers) {
        baseMapLayers = layers;
    }

    public Map getBaseMapLayers() {
        return baseMapLayers;
    }

    public void setBaseMapStyles(Map styles) {
        baseMapStyles = styles;
    }

    public Map getBaseMapStyles() {
        return baseMapStyles;
    }

    /**
     * @param allowInterpolation interpolation hint.
     */
    public void setAllowInterpolation(String allowInterpolation) {
        this.allowInterpolation = allowInterpolation;
    }

    /**
     * @return The value of the interpolation rendering hint.
     */
    public String getAllowInterpolation() {
        return allowInterpolation;
    }
}
