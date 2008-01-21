/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wcs.responses.coverage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.image.WorldImageWriter;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.global.GeoServer;
import org.vfny.geoserver.wcs.responses.CoverageResponseDelegate;


/**
 * DOCUMENT ME!
 *
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
 *         modification)
 * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
 *         modification)
 */
public class IMGCoverageResponseDelegate implements CoverageResponseDelegate {
    
    private static final Set<String> FORMATS = new HashSet<String>(Arrays.asList(
        "image/bmp", "image/gif", "image/tiff", "image/png", "image/jpeg"));
    
    /**
     *
     * @uml.property name="sourceCoverage"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private GridCoverage2D sourceCoverage;
    private String outputFormat;

    public IMGCoverageResponseDelegate() {
    }

    public boolean canProduce(String outputFormat) {
        return outputFormat != null && (
                FORMATS.contains(outputFormat.toLowerCase()) ||
                FORMATS.contains("image/" + outputFormat.toLowerCase()));
    }
    
    public Set<String> getSupportedFormats() {
        return FORMATS;
    }

    public void prepare(String outputFormat, GridCoverage2D coverage)
        throws IOException {
        this.outputFormat = outputFormat.startsWith("image/") ? outputFormat.substring(6) 
                : outputFormat;
        this.sourceCoverage = coverage;
    }

    public String getContentType(GeoServer gs) {
        return new StringBuffer("image/").append(outputFormat.toLowerCase()).toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getContentEncoding() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getContentDisposition() {
        return (outputFormat.equalsIgnoreCase("tiff") || outputFormat.equalsIgnoreCase("tif"))
        ? new StringBuffer("attachment;filename=").append(this.sourceCoverage.getName()).append(".")
                                                  .append(outputFormat).toString() : null;
    }

    public void encode(OutputStream output) throws ServiceException, IOException {
        if (sourceCoverage == null) {
            throw new IllegalStateException("It seems prepare() has not been called or has not succeed");
        }

        final GridCoverageWriter writer = new WorldImageWriter(output);

        // writing parameters for Image
        final Format writerParams = writer.getFormat();
        final ParameterValueGroup writeParameters = writerParams.getWriteParameters();
        final ParameterValue format = writeParameters.parameter("Format");
        format.setValue(this.outputFormat.toLowerCase());

        // writing
        writer.write(sourceCoverage, new GeneralParameterValue[] {format});

        // freeing everything
        output.flush();
        writer.dispose();
        this.sourceCoverage.dispose(false);
        this.sourceCoverage = null;
    }
}
