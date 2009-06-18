/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.xacml.geoxacml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.geotools.xacml.geoxacml.config.GeoXACML;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.w3c.dom.Document;

import com.sun.xacml.PDP;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;

/**
 * Controller which acts as GeoXACML Policy Decision Point
 * 
 * Accepts onyl HTTP POST requests containing an XACML Request
 * 
 * Supported Parameters:
 * 
 * validate if true/TRUE, schema validation is performed
 * 
 * @author Christian Mueller
 * 
 */
public class GeoXACMLPDPController extends AbstractController {

    public static final String VALIDATE_PARAM = "validate";

    public GeoXACMLPDPController() {
        setSupportedMethods(new String[] { METHOD_POST });
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        PDP pdp = GeoXACMLConfig.getPDP();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setNamespaceAware(true);

        String booleanString = req.getParameter("validate");
        Boolean validate = new Boolean(booleanString);

        if (validate) {
            factory.setSchema(GeoXACML.getContextSchema());
            logger.info("Request validation enabled");
        }

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(req.getInputStream());

        RequestCtx request = RequestCtx.getInstance(doc.getDocumentElement());
        ResponseCtx response = pdp.evaluate(request);

        response.encode(resp.getOutputStream());
        return null;
    }

}
