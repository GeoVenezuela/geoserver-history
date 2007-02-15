/* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.ResultTypeType;
import net.opengis.wfs.WFSFactory;
import org.geoserver.ows.Response;
import org.geoserver.ows.util.OwsUtils;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geotools.xml.Encoder;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Base class for a response to a WFS GetFeature operation.
 * <p>
 * The result of a GetFeature operation is an instance of
 * {@link FeatureCollectionType}. Subclasses are responsible for serializing
 * an instance of this type in {@link #write(FeatureCollectionType, OutputStream, Operation)}.
 * </p>
 * <p>
 * Subclasses also need declare the mime-type in which the format is encoded.
 * </p>
 *
 * @author Gabriel Rold?n, Axios Engineering
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class WFSGetFeatureOutputFormat extends Response {
    /**
     * Constructor which sets the outputFormat.
     *
     * @param outputFormat The well-known name of the format, not <code>null</code>
     */
    public WFSGetFeatureOutputFormat(String outputFormat) {
        super(FeatureCollectionType.class, outputFormat);
    }

    /**
     * Returns the mime type <code>text/xml</code>.
     * <p>
     * Subclasses should override this method to provide a diffent output
     * format.
     * </p>
     */
    public String getMimeType(Object value, Operation operation)
        throws ServiceException {
        return "text/xml";
    }

    /**
     * Ensures that the operation being executed is a GetFeature operation.
     * <p>
     * Subclasses may implement
     * </p>
     */
    public final boolean canHandle(Operation operation) {
        //GetFeature operation?
        if ("GetFeature".equalsIgnoreCase(operation.getId())
                || "GetFeatureWithLock".equalsIgnoreCase(operation.getId())) {
            //also check that the resultType is "results"
            GetFeatureType request = (GetFeatureType) OwsUtils.parameter(operation.getParameters(),
                    GetFeatureType.class);

            if (request.getResultType() == ResultTypeType.RESULTS_LITERAL) {
                //call subclass hook
                return canHandleInternal(operation);
            }
        }

        return false;
    }

    /**
     * Hook for subclasses to add addtional checks to {@link #canHandle(Operation)}.
     * <p>
     * Subclasses may override this method if need be, the default impelementation
     * returns <code>true</code>
     * </p>
     * @param operation The operation being performed.
     *
     * @return <code>true</code> if the output format can handle the operation,
     *         otherwise <code>false</code>
     */
    protected boolean canHandleInternal(Operation operation) {
        return true;
    }

    /**
     * Calls through to {@link #write(FeatureCollectionType, OutputStream, Operation)}.
     */
    public final void write(Object value, OutputStream output, Operation operation)
        throws IOException, ServiceException {
        write((FeatureCollectionType) value, output, operation);
    }

    /**
     * Serializes the feature collection in the format declared.
     *
     * @param featureCollection The feature collection.
     * @param output The output stream to serialize to.
     * @param getFeature The GetFeature operation descriptor.
     */
    protected abstract void write(FeatureCollectionType featureCollection, OutputStream output,
        Operation getFeature) throws IOException, ServiceException;
}
