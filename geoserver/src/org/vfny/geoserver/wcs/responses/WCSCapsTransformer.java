/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wcs.responses;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.geometry.JTS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;
import org.vfny.geoserver.Request;
import org.vfny.geoserver.global.CoverageInfo;
import org.vfny.geoserver.global.MetaDataLink;
import org.vfny.geoserver.global.Service;
import org.vfny.geoserver.global.WCS;
import org.vfny.geoserver.util.requests.CapabilitiesRequest;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.jts.geom.Envelope;


/**
 * DOCUMENT ME!
 * 
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author $Author: Simone Giannecchini (simboss_ml@tiscali.it) $ (last modification)
 */
public class WCSCapsTransformer extends TransformerBase {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(WCSCapsTransformer.class.getPackage()
                                                                                  .getName());

    protected static final String WCS_URI = "http://www.opengis.net/wcs";

    /** DOCUMENT ME! */
    private static final String HTTP_GET = "Get";

    /** DOCUMENT ME! */
    private static final String HTTP_POST = "Post";

    /** DOCUMENT ME! */
    protected static final String WFS_URI = "http://www.opengis.net/wcs";

    /** DOCUMENT ME! */
    protected static final String CUR_VERSION = "1.0.0";

    /** DOCUMENT ME! */
    protected static final String XSI_PREFIX = "xsi";

    /** DOCUMENT ME! */
    protected static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    /** DOCUMENT ME! */
    protected Request request;

    /**
     * Creates a new WFSCapsTransformer object.
     */
    public WCSCapsTransformer() {
        super();
        setNamespaceDeclarationEnabled(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param handler DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Translator createTranslator(ContentHandler handler) {
        return new WCSCapsTranslator(handler);
    }

    /**
     * DOCUMENT ME!
     *
     * @author Gabriel Roldan, Axios Engineering
     * @version $Id
     */
    private static class WCSCapsTranslator extends TranslatorSupport {
        /** DOCUMENT ME!  */
        private static final String EPSG = "EPSG:";

        /** DOCUMENT ME! */
        private CapabilitiesRequest request;

        /**
         * Creates a new WFSCapsTranslator object.
         *
         * @param handler DOCUMENT ME!
         */
        public WCSCapsTranslator(ContentHandler handler) {
            super(handler, null, null);
        }

        /**
         * Encode the object.
         *
         * @param o The Object to encode.
         *
         * @throws IllegalArgumentException if the Object is not encodeable.
         */
        public void encode(Object o) throws IllegalArgumentException {
            if (!(o instanceof CapabilitiesRequest)) {
                throw new IllegalArgumentException(
                    "Not a CapabilitiesRequest: " + o);
            }

            this.request = (CapabilitiesRequest) o;

            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute("", "version", "version", "", CUR_VERSION);
            attributes.addAttribute("", "xmlns", "xmlns", "", WCS_URI);

            attributes.addAttribute("", "xmlns:xlink", "xmlns:xlink", "",
            	"http://www.w3.org/1999/xlink");
            attributes.addAttribute("", "xmlns:ogc", "xmlns:ogc", "",
                "http://www.opengis.net/ogc");
            attributes.addAttribute("", "xmlns:gml", "xmlns:gml", "",
            	"http://www.opengis.net/gml");

            String prefixDef = "xmlns:" + XSI_PREFIX;
            attributes.addAttribute("", prefixDef, prefixDef, "", XSI_URI);

            String locationAtt = XSI_PREFIX + ":schemaLocation";
            String locationDef = WCS_URI + " " + request.getSchemaBaseUrl()
                + "wcs/1.0.0/" + "wcsCapabilities.xsd";
            attributes.addAttribute("", locationAtt, locationAtt, "", locationDef);
            start("WCS_Capabilities", attributes);

            handleService();
            handleCapabilities();

            end("WCS_Capabilities");
        }

        
        /**
         * Handles the service section of the capabilities document.
         *
         * @param config The OGC service to transform.
         *
         * @throws SAXException For any errors.
         */
        private void handleService() {
        	WCS wcs = request.getWCS();
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute("", "version", "version", "", CUR_VERSION);
            start("Service", attributes);
            handleMetadataLink(wcs.getMetadataLink());
            element("description", wcs.getAbstract());
            element("name", wcs.getName());
            element("label", wcs.getTitle());
            handleKeywords(wcs.getKeywords());
            handleContact(wcs);

            String fees = wcs.getFees();
            if ((fees == null) || "".equals(fees)) {
                fees = "NONE";
            }
            element("fees", fees);

            String accessConstraints = wcs.getAccessConstraints();
            if ((accessConstraints == null) || "".equals(accessConstraints)) {
                accessConstraints = "NONE";
            }
            element("accessConstraints", accessConstraints);
            end("Service");
        }

        /**
         * DOCUMENT ME!
         *
         * @param serviceConfig DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        private void handleCapabilities() {
        	WCS wcs = request.getWCS();
            start("Capability");
            handleRequest(wcs);
    		handleExceptions(wcs);
            handleVendorSpecifics(wcs);
            end("Capability");
            
            handleContentMetadata(wcs);
        }
        
        /**
         * Handles the request portion of the document, printing out the
         * capabilities and where to bind to them.
         *
         * @param config The global wms.
         *
         * @throws SAXException For any problems.
         */
        private void handleRequest(WCS config) {
            start("Request");
            handleCapability(config, "GetCapabilities");
            handleCapability(config, "DescribeCoverage");
            handleCapability(config, "GetCoverage");
            end("Request");
        }
        
        private void handleCapability(WCS config, String capabilityName) {
        AttributesImpl attributes = new AttributesImpl();
        start(capabilityName);

        start("DCPType");
        start("HTTP");

        String url = "";
        String baseUrl = request.getBaseUrl() + "wcs";

        if (request.isDispatchedRequest()) {
            url = baseUrl + "?";
        } else {
            url = baseUrl + "/" + capabilityName + "?";
        }

    	attributes.addAttribute("","xlink:href","xlink:href","",url);

        start("Get");
        	start("OnlineResource", attributes);
        	end("OnlineResource");
        end("Get");
        end("HTTP");
        end("DCPType");

        attributes = new AttributesImpl();

        if (request.isDispatchedRequest()) {
            url = baseUrl;
        } else {
            url = baseUrl + "/" + capabilityName;
        }

    	attributes.addAttribute("","xlink:href","xlink:href","",url);

    	start("DCPType");
        start("HTTP");
        start("Post");
    		start("OnlineResource", attributes);
    		end("OnlineResource");
        end("Post");
        end("HTTP");
        end("DCPType");
        end(capabilityName);
    }
        
        /**
         * DOCUMENT ME!
         *
         * @param kwords DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        private void handleKeywords(List kwords) {
            start("keywords");

            if (kwords != null) {
                for (Iterator it = kwords.iterator(); it.hasNext();) {
                	element("keyword", it.next().toString());
                }
            }

            end("keywords");
        }

        /**
         * Handles contacts.
         *
         * @param config the service.
         */
        private void handleContact(Service config) {
            String tmp = "";

            if (((config.getGeoServer().getContactPerson() != null) && (config.getGeoServer().getContactPerson() != "")) || ((config.getGeoServer().getContactOrganization() != null) && (config.getGeoServer().getContactOrganization() != ""))) {
                start("responsibleParty");

                tmp = config.getGeoServer().getContactPerson();
                if ((tmp != null) && (tmp != "")) {
                    element("individualName", tmp);
                    
                    tmp = config.getGeoServer().getContactOrganization();
                    if ((tmp != null) && (tmp != "")) {
                        element("organisationName", tmp);
                    }
                } else {
                    tmp = config.getGeoServer().getContactOrganization();
                    if ((tmp != null) && (tmp != "")) {
                        element("organisationName", tmp);
                    }
                }

                tmp = config.getGeoServer().getContactPosition();
                if ((tmp != null) && (tmp != "")) {
                    element("positionName", tmp);
                }

                start("contactInfo");
                
    	            start("phone");
    		            tmp = config.getGeoServer().getContactVoice();
    		            if ((tmp != null) && (tmp != "")) {
    		                element("voice", tmp);
    		            }
    		
    		            tmp = config.getGeoServer().getContactFacsimile();
    		            if ((tmp != null) && (tmp != "")) {
    		                element("facsimile", tmp);
    		            }
    	            end("phone");
    	
    	            start("address");
    		            tmp = config.getGeoServer().getAddressType();
    		            if ((tmp != null) && (tmp != "")) {
    		            	String addr = "";
    			            addr = config.getGeoServer().getAddress();
    			            if ((addr != null) && (addr != "")) {
    			                element("deliveryPoint", tmp + " " + addr);
    			            }	
    		            } else {
    		            	tmp = config.getGeoServer().getAddress();
    			            if ((tmp != null) && (tmp != "")) {
    			                element("deliveryPoint", tmp);
    			            }	
    		            }

    	            	tmp = config.getGeoServer().getAddressCity();
    		            if ((tmp != null) && (tmp != "")) {
    		                element("city", tmp);
    		            }

    		            tmp = config.getGeoServer().getAddressState();
    		            if ((tmp != null) && (tmp != "")) {
    		                element("administrativeArea", tmp);
    		            }

    		            tmp = config.getGeoServer().getAddressPostalCode();
    		            if ((tmp != null) && (tmp != "")) {
    		                element("postalCode", tmp);
    		            }

    		            tmp = config.getGeoServer().getAddressCountry();
    		            if ((tmp != null) && (tmp != "")) {
    		                element("country", tmp);
    		            }

    		            tmp = config.getGeoServer().getContactEmail();
    		            if ((tmp != null) && (tmp != "")) {
    		                element("electronicMailAddress", tmp);
    		            }
    	            end("address");
    	            
    	            tmp = config.getGeoServer().getOnlineResource();
    	            if ((tmp != null) && (tmp != "")) {
    	            	AttributesImpl attributes = new AttributesImpl();
    	            	attributes.addAttribute("","xlink:href","xlink:href","",tmp);
    	            	start("onlineResource", attributes);
    	            	end("onlineResource");
    	            }
    		            
                end("contactInfo");

                end("responsibleParty");
            }
        }

        /**
         * Handles the printing of the exceptions information, prints the formats
         * that GeoServer can return exceptions in.
         *
         * @param config The wms service global config.
         *
         * @throws SAXException For any problems.
         */
        private void handleExceptions(WCS config) {
            start("Exception");

            String[] formats = config.getExceptionFormats();

            for (int i = 0; i < formats.length; i++) {
                element("Format", formats[i]);

                if (i < (formats.length - 1)) {
                }
            }

            end("Exception");
        }

        /**
         * Handles the vendor specific capabilities.  Right now there are none, so
         * we do nothing.
         *
         * @param config The global config that may contain vendor specifics.
         *
         * @throws SAXException For any problems.
         */
        private void handleVendorSpecifics(WCS config) {
        }

        private void handleEnvelope(CoordinateReferenceSystem crs, Envelope envelope) {
			try {
				if( !crs.getName().getCode().equalsIgnoreCase("WGS 84") ) {
					final CRSFactory crsFactory = FactoryFinder.getCRSFactory();
					final CoordinateOperationFactory opFactory = FactoryFinder.getCoordinateOperationFactory();
					final CoordinateReferenceSystem targetCRS = crsFactory.createFromWKT(
				    		"GEOGCS[\"WGS 84\",\n" 								 + 
				    		"DATUM[\"WGS_1984\",\n"								 + 
				    		"  SPHEROID[\"WGS 84\",\n" 							 + 
				    		"    6378137.0, 298.257223563,\n" 					 + 
				    		"    AUTHORITY[\"EPSG\",\"7030\"]],\n" 				 +
				    		"  AUTHORITY[\"EPSG\",\"6326\"]],\n"				 + 
				    		"  PRIMEM[\"Greenwich\", 0.0,\n" 					 +
				    		"    AUTHORITY[\"EPSG\",\"8901\"]],\n"				 + 
				    		"  UNIT[\"degree\", 0.017453292519943295],\n"		 + 
				    		"  AXIS[\"Lon\", EAST],\n"							 +
				    		"  AXIS[\"Lat\", NORTH],\n"							 +
				    		"AUTHORITY[\"EPSG\",\"4326\"]]");
					
					
				    final CoordinateReferenceSystem sourceCRS = crs;
				    
				    final CoordinateOperation operation = opFactory.createOperation(sourceCRS, targetCRS);

				    MathTransform2D mathTransform = (MathTransform2D) operation.getMathTransform();
				    
					Envelope targetEnvelope = JTS.transform(envelope, mathTransform);

		        	AttributesImpl attributes = new AttributesImpl();
					attributes.addAttribute("", "srsName", "srsName", "", "WGS84(DD)");
		        	start("lonLatEnvelope", attributes);
		        		element("gml:pos", targetEnvelope.getMinX() + " " + targetEnvelope.getMinY());
		        		element("gml:pos", targetEnvelope.getMaxX() + " " + targetEnvelope.getMaxY());
		        	end("lonLatEnvelope");
			    } else {
		        	AttributesImpl attributes = new AttributesImpl();
					attributes.addAttribute("", "srsName", "srsName", "", "WGS84(DD)");
		        	start("lonLatEnvelope", attributes);
		        		element("gml:pos", envelope.getMinX() + " " + envelope.getMinY());
		        		element("gml:pos", envelope.getMaxX() + " " + envelope.getMaxY());
		        	end("lonLatEnvelope");
			    }
			} catch (OperationNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param metadataLink DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        private void handleMetadataLink(MetaDataLink mdl) {
        	if( mdl != null ) {
                AttributesImpl attributes = new AttributesImpl();
                if( mdl.getAbout() != null && mdl.getAbout() != "" ) {
                    attributes.addAttribute("", "about", "about", "", mdl.getAbout());
                }

//                if( mdl.getType() != null && mdl.getType() != "" ) {
//                    attributes.addAttribute("", "type", "type", "", mdl.getType());
//                }

                if( mdl.getMetadataType() != null && mdl.getMetadataType() != "" ) {
                    attributes.addAttribute("", "metadataType", "metadataType", "", mdl.getMetadataType());
                }

                start("metadataLink", attributes);
//                characters(mdl.getContent());
                end("metadataLink");
        	}
        }
        
        private String getBboxElementName() {
            return "LatLongBoundingBox";
        }

        private void handleContentMetadata(WCS config) {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute("", "version", "version", "", CUR_VERSION);

            start("ContentMetadata", attributes);
            for (Iterator i = config.getData().getCoverageInfos().keySet().iterator(); i.hasNext(); ) {
            	handleCoverageOfferingBrief(config, (CoverageInfo) config.getData().getCoverageInfos().get(i.next()));
            }
            end("ContentMetadata");
        }

        private void handleCoverageOfferingBrief(WCS config, CoverageInfo cv) {
            start("CoverageOfferingBrief");
            String tmp;
            
            	handleMetadataLink(cv.getMetadataLink());
            	tmp = cv.getDescription();
            	if ((tmp != null) && (tmp != "")) {
            		element("description", tmp);
            	}
            	tmp = cv.getName();
            	if ((tmp != null) && (tmp != "")) {
            		element("name", tmp);
            	}
            	tmp = cv.getLabel();
            	if ((tmp != null) && (tmp != "")) {
            		element("label", tmp);
            	}
                handleEnvelope(cv.getCrs(), cv.getEnvelope());
                handleKeywords(cv.getKeywords());
                
            end("CoverageOfferingBrief");
        }
    }
}
