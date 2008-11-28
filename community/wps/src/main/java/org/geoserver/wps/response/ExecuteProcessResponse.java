package org.geoserver.wps.response;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.opengis.wps10.ExecuteResponseType;
import net.opengis.wps10.ExecuteType;
import net.opengis.wps10.LiteralDataType;
import net.opengis.wps10.OutputDataType;
import net.opengis.wps10.OutputDefinitionType;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.geoserver.ows.Response;
import org.geoserver.ows.XmlObjectEncodingResponse;
import org.geoserver.platform.ExtensionPriority;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geoserver.wps.ComplexDataEncoderDelegate;
import org.geoserver.wps.Execute;
import org.geoserver.wps.WPSException;
import org.geotools.util.Converters;

/**
 * Encodes the Execute response either in the normal XML format or in the raw format
 * @author Andrea Aime
 *
 */
public class ExecuteProcessResponse extends Response {

    XmlObjectEncodingResponse standardResponse;

    public ExecuteProcessResponse(Class binding, String elementName, Class xmlConfiguration) {
        super(ExecuteResponseType.class);
        this.standardResponse = new XmlObjectEncodingResponse(binding, elementName, xmlConfiguration);
    }
    
    @Override
    public String getMimeType(Object value, Operation operation)
            throws ServiceException {
        ExecuteType execute = (ExecuteType) operation.getParameters()[0];
        if (execute.getResponseForm().getRawDataOutput() == null) {
            // normal execute response encoding
            return standardResponse.getMimeType(value, operation);
        } else {
            // raw response, let's see what the output is
            ExecuteResponseType response = (ExecuteResponseType) value;
            OutputDefinitionType definition = (OutputDefinitionType) response
                    .getOutputDefinitions().getOutput().get(0);
            return definition.getMimeType();
        }

    }

    @Override
    public void write(Object value, OutputStream output, Operation operation)
            throws IOException, ServiceException {
        ExecuteType execute = (ExecuteType) operation.getParameters()[0];
        if (execute.getResponseForm().getRawDataOutput() == null) {
            // normal execute response encoding
            standardResponse.write(value, output, operation);
        } else {
            // raw response, let's see what the output is
            ExecuteResponseType response = (ExecuteResponseType) value;
            OutputDataType result = (OutputDataType) response
                    .getProcessOutputs().getOutput().get(0);
            LiteralDataType literal = result.getData().getLiteralData();
            if (literal != null) {
                writeLiteral(output, literal);
            } else {
                writeComplex(output, result);
            }
        }
    }

    /**
     * Write out complex data assuming {@link Execute} has set up the proper
     * encoder as the output
     * 
     * @param output
     * @param result
     * @throws IOException
     */
    void writeComplex(OutputStream output, OutputDataType result)
            throws IOException {
        Object rawResult = result.getData().getComplexData().getData().get(0);
        if (rawResult instanceof ComplexDataEncoderDelegate) {
            ComplexDataEncoderDelegate delegate = (ComplexDataEncoderDelegate) rawResult;
            XMLSerializer xmls = new XMLSerializer(output, new OutputFormat());
            xmls.setNamespaces(true);

            try {
                delegate.encode(xmls);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new WPSException("An error occurred while encoding "
                        + "the results of the process", e);
            }
        } else {
            throw new WPSException("Cannot encode an object of class "
                    + rawResult.getClass() + " in raw form");
        }
    }

    /**
     * Write out literal results by converting them to strings
     * @param output
     * @param literal
     */
    void writeLiteral(OutputStream output, LiteralDataType literal) {
        PrintWriter writer = new PrintWriter(output);
        String converted = Converters.convert(literal, String.class);
        if (converted == null) {
            throw new WPSException("Could not convert the literal result "
                    + "to a string, please code a proper converter for class "
                    + literal.getClass());
        }
        writer.write(converted);
    }

    

}
