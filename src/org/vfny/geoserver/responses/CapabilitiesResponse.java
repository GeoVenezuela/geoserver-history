/* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root 
 * application directory.
 */
package org.vfny.geoserver.responses;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import org.vfny.geoserver.requests.*;
import org.vfny.geoserver.config.*;
import org.vfny.geoserver.config.configuration.*;

/**
 * Handles a GetCapabilities request and creates a GetCapabilities response 
 * GML string.
 *
 * Therefore, the get response is assembled not as a monolithic document,
 * which would be much neater, but as a series of subdocuments.  Also, I 
 * have implemented some horrible hacks in the auto-generated code to
 * get it to work in places.  My advice: don't regenerate this code.
 *
 *@author Rob Hranac, TOPP
 *@version $VERSION$
 */
public class CapabilitiesResponse {

    /** Standard logging instance for class */
    private static final Logger LOGGER = 
        Logger.getLogger("org.vfny.geoserver.requests");
    
    /** Version of the response */
    private String version;
    
    /** Service requested */
    private String service;
    
    /** Version information for the server. */
    private VersionBean versionInfo = new VersionBean();
    
    /** Configuration information for the server. */
    private static ConfigurationBean config = ConfigurationBean.getInstance();
    
    /** XML Tag Type: start */
    private static final int TAG_START = 1;
    
    /** XML Tag Type: end */
    private static final int TAG_END = 2;
    
    /** XML Tag Type: only */
    private static final int TAG_ONLY = 3;
    
    
    /******************************************
      Convenience variables for XML subfunctions
     *******************************************/

    /** Operations capabilities file */
    private final String OPERATIONS_FILE = config.getCapabilitiesDir() + "operations.xml";
    
    /** Filter capabilities file */
    private final String FILTER_FILE = config.getCapabilitiesDir() + "filter.xml";
    
    /** Service metadata file */
    private final String SERVICE_METADATA_FILE = config.getCapabilitiesDir() + "serviceMetadata.xml";
    
    /** Operations signatures file */
    private final String OPERATIONS_SIGNATURES_FILE = config.getCapabilitiesDir() + "operationsSignatures.xml";

    /** Additional capabilities file */
    private final String ADDITIONAL_CAPABILITIES_FILE = config.getCapabilitiesDir() + "additionalCapabilities.xml";
    
    /** Final XML output stream elements and configuration object */
    private static XmlOutputStream xmlOutFinal = new XmlOutputStream(60000);
    
    /** Temporary XML output stream elements and configuration object */
    private static XmlOutputStream xmlOutTemp = new XmlOutputStream(60000);
    
    
    /**
     * Sets version and service.
     * @param request Request from the capabilities response server.
     */ 
    public CapabilitiesResponse(CapabilitiesRequest request) {
        version = request.getVersion();
        service = request.getService();
    }
    

    /**
     * Creates the XML response.
     *
     */
    public String getXmlResponse()
        throws WfsException {
        
        // Add xml objects to return stream
        // A TOTAL MESS
        // NEEDS TO BE FIXED
        
        xmlOutTemp.reset();
        xmlOutFinal.reset();
        
        if( version.equals("0.0.14") ) {
            addTag("WFS_Capabilities", TAG_START, 0 );
            service();
            capability();
            //xmlOutFinal.writeFile( OPERATIONS_FILE );
            
            addTag("FeatureTypeList", TAG_START, 2 );
            addFeatureTypeInfo( config.getTypeDir(), version );
            addTag("FeatureTypeList", TAG_END, 2 );
            
            xmlOutFinal.writeFile( FILTER_FILE );
            addTag("WFS_Capabilities", TAG_END, 0 );
        }
        else {
            xmlOutFinal.writeFile( SERVICE_METADATA_FILE );
            xmlOutFinal.writeFile( OPERATIONS_SIGNATURES_FILE );

            addTag("ContentMetadata", TAG_START, 3 );
            addTag("wfsfl:wfsFeatureTypeList", TAG_START, 6 );
            addFeatureTypeInfo( config.getTypeDir(), version );
            addTag("wfsfl:wfsFeatureTypeList", TAG_END, 6 );
            addTag("ContentMetadata", TAG_END, 3 );
            
            xmlOutFinal.writeFile( ADDITIONAL_CAPABILITIES_FILE );

        }
        return xmlOutFinal.toString();

    }
    
    
    /**
     * Internal utility that writes some header information.
     *
     */
    private void addHeaderInfo() {

        String encoding = "<?xml version='1.0' encoding='UTF-8'?>\n";
        String firstTag = "<WFS_Capabilities version='" + versionInfo.getWfsVersion() + "' sequence='" + versionInfo.getWfsUpdateSequence() + "'>\n";
        
        xmlOutFinal.write( encoding.getBytes(), 0, encoding.length() );
        xmlOutFinal.write( firstTag.getBytes(), 0, firstTag.length() );
    }
    
    
    /**
     * Internal utility that writes xml tags.
     *
     * @param tag.The XML tag name.
     * @param tagType.The XML tag type, defined in the class.
     * @param spaces.Spaces to be added to the XML tag.
     */
    private void addTag( String tag, int tagType, int spaces ) {
        
        String tempSpaces = new String();
        for( int i=0; i < spaces ; i++ ) {
            tempSpaces = tempSpaces + " ";
        }
        if ( tagType == TAG_END )
            tag = ("/").concat(tag);
        if ( tagType == TAG_ONLY )
            tag = tag.concat("/");
        tag = tempSpaces + "<" + tag;
        tag = tag.concat(">\n");
        
        xmlOutFinal.write(tag.getBytes(), 0, tag.length() );
    }
    

    /**
     * Adds service information to the XML output stream.
     *
     */
    private void service()
        throws WfsException {
        
        try {
            xmlOutFinal.write( config.getServiceXml( versionInfo.getWfsName() ).getBytes() );
        } catch (IOException e) {
            throw new WfsException( e, "Error appending to XML file", CapabilitiesResponse.class.getName() );
        }
        
    }
    
    
    /**
     * Adds capability information to the XML output stream.
     * 
     */
    private void capability()
        throws WfsException {
        
        String tempCapabilityInfo = new String();
        
        tempCapabilityInfo = "\n  <Capability>\n    <Request>";
        tempCapabilityInfo = tempCapabilityInfo + tempReturnCapability("Capabilities");
        tempCapabilityInfo = tempCapabilityInfo + tempReturnCapability("DescribeFeatureType");
        tempCapabilityInfo = tempCapabilityInfo + tempReturnCapability("GetFeature");
        tempCapabilityInfo = tempCapabilityInfo + "\n    </Request>\n  </Capability>";
        
        try {
            xmlOutFinal.write(tempCapabilityInfo.getBytes());
        } catch (IOException e) {
            throw new WfsException( e, "Error appending to XML file", CapabilitiesResponse.class.getName() );
        }
    }

    
    /**
     * Adds capability information to the XML output stream.
     *
     */
    private String tempReturnCapability(String request) {
        
        String url = config.getUrl();
        String tempCapability = new String();
        
        tempCapability = "\n      <" + request + ">";
        if (request.equals("DescribeFeatureType") )
            tempCapability = tempCapability + "\n        <SchemaDescriptionLanguage><XMLSCHEMA/></SchemaDescriptionLanguage>";
        if (request.equals("GetFeature") )
            tempCapability = tempCapability + "\n        <ResultFormat><GML2/></ResultFormat>";
        tempCapability = tempCapability + "\n        <DCPType><HTTP><Get onlineResource='" + url + "/" + request + "?'/></HTTP></DCPType>";
        tempCapability = tempCapability + "\n        <DCPType><HTTP><Post onlineResource='" + url + "/" + request + "'/></HTTP></DCPType>\n      </" + request + ">";
        
        return tempCapability;
    }
    
    
    /**
     * Adds feature type metadata to the XML output stream.
     * 
     * @param targetDirectoryName The directory in which to search for files.
     * @param responseVersion The expected version of the WFS response.
     */
    private void addFeatureTypeInfo(String targetDirectoryName, String responseVersion)
        throws WfsException {
        
        // holds final response variable
        String tempResponse = new String();
        
        // iterated convenience variables
        File currentDirectory = new File( targetDirectoryName);
        String currentFeatureType = new String();
        String currentFileName = new String();

        // keeps master list of files within the directory
        String[] files = currentDirectory.list();
        File[] file = currentDirectory.listFiles();
                        
        // Loop through all files in the directory
        for (int i = 0; i < files.length; i++) {
            // assign temp variables; convenience/confusion lesseners only
            currentFileName = file[i].getName();
            addFeatureType( currentFileName, responseVersion);
        }
        
    }

    
    
    /**
     * Adds feature type metadata to the XML output stream.
     * 
     * @param featureTypeName The directory in which to search for files.
     * @param responseVersion The expected version of the WFS response.
     */
    private void addFeatureType(String featureTypeName, String responseVersion) 
        throws WfsException {
        
        FeatureTypeBean responseFeatureType = new FeatureTypeBean( featureTypeName );
        String tempResponse = responseFeatureType.getCapabilitiesXml( responseVersion );
        
        try {
            xmlOutFinal.write(tempResponse.getBytes());
        } catch (Exception e) {
            throw new WfsException( e, "Could not write XML output file", CapabilitiesResponse.class.getName() );
        }
        
    }

    
    /**
     * Internal utility to write a root element to the temporary buffer, then final buffer.
     * Validates, marshals, strips encoding content, and writes to final buffer.
     *
     * @param xmlBranch.The XML branch root element (JAXB class).
     *
     private void writeToBuffer(MarshallableRootElement xmlBranch)
     throws WfsException {
     
     xmlOutTemp.reset();
     
     try {
     xmlBranch.validate();
     xmlBranch.marshal(xmlOutTemp);
     xmlOutTemp.writeToClean(xmlOutFinal);
     }
     catch (StructureValidationException e) {
     throw new WfsException( e, "Internal XML file is not valid", CapabilitiesResponse.class.getName() );
     }
     catch (IOException e) {
     throw new WfsException( e, "Had problems reading internal XML file", CapabilitiesResponse.class.getName() );
     }
     }*/

    

}


