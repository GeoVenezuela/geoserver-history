/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.requests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.vfny.geoserver.global.WFS;
import org.vfny.geoserver.testdata.MockUtils;
import org.vfny.geoserver.util.requests.readers.KvpRequestReader;
import org.vfny.geoserver.util.requests.readers.XmlRequestReader;
import org.vfny.geoserver.wfs.requests.DescribeRequest;
import org.vfny.geoserver.wfs.requests.readers.DescribeKvpReader;
import org.vfny.geoserver.wfs.requests.readers.DescribeXmlReader;
import org.vfny.geoserver.wfs.servlets.Describe;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Tests the get capabilities request handling.
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @version $Id: DescribeSuite.java,v 1.8 2004/01/31 00:17:52 jive Exp $
 */
public class DescribeSuiteTest extends RequestTestCase {
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger("org.vfny.geoserver.requests");

    /** Describe request */
    private Describe service = null;

    /** Base request for comparison */
    private DescribeRequest[] baseRequest = new DescribeRequest[10];

    /**
             * Constructor with super.
             *
             * @param testName The name of this test.
             */
    public DescribeSuiteTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DescribeSuiteTest.class);

        return suite;
    }

    public void setUp() {
        WFS wfs = new WFS(MockUtils.newWfsDto());

        service = new Describe(wfs);

        baseRequest[0] = new DescribeRequest(service);
        baseRequest[0].addFeatureType("rail");
        baseRequest[0].setVersion("0.0.15");

        baseRequest[1] = new DescribeRequest(service);
        baseRequest[1].addFeatureType("rail");
        baseRequest[1].addFeatureType("roads");
        baseRequest[1].setVersion("0.0.15");
    }

    protected XmlRequestReader getXmlReader() {
        return new DescribeXmlReader(service);
    }

    protected KvpRequestReader getKvpReader(Map kvps) {
        return new DescribeKvpReader(kvps, service);
    }

    /**
     * Gets a BufferedReader from the file to be passed as if it were
     * from a servlet.
     *
     * @throws Exception If anything goes wrong.
     */

    /*private static BufferedReader readFile(String filename)
       throws Exception {
       LOGGER.finer("about to read: " + DATA_DIRECTORY + "/" + filename);
       File inputFile = new File(DATA_DIRECTORY + "/" + filename);
       Reader inputStream = new FileReader(inputFile);
       return new BufferedReader(inputStream);
       }*/

    /**
     * Check to make sure that a standard XML request is handled
     * correctly.
     *
     * @throws Exception If anything goes wrong.
     */
    public void testXml1() throws Exception {
        // instantiates an XML request reader, returns request object
        //DescribeRequest request = XmlRequestReader.readDescribeFeatureType(readFile(
        //          "4.xml"));
        assertTrue(runXmlTest(baseRequest[0], "4", true));

        //LOGGER.fine("XML 1 test passed: " + baseRequest[0].equals(request));
        //LOGGER.finer("base request: " + baseRequest[0].toString());
        //LOGGER.finer("read request: " + request.toString());
        //assertTrue(baseRequest[0].equals(request));
    }

    /**
     * Check to make sure that a standard XML request is handled
     * correctly.
     *
     * @throws Exception If anything goes wrong.
     */
    public void testXml2() throws Exception {
        assertTrue(runXmlTest(baseRequest[1], "5", true));

        // instantiates an XML request reader, returns request object
        //DescribeRequest request = XmlRequestReader.readDescribeFeatureType(readFile(
        //            "5.xml"));
        //        LOGGER.fine("XML 2 test passed: " + baseRequest[1].equals(request));
        //LOGGER.finer("base request: " + baseRequest[1].toString());
        //LOGGER.finer("read request: " + request.toString());
        //assertTrue(baseRequest[1].equals(request));
    }

    /**
     * Checks to make sure that a standard KVP request is handled
     * correctly.
     *
     * @throws Exception If anything goes wrong.
     */
    public void testKvp1() throws Exception {
        String requestString = "service=WFS&typename=rail";

        //DescribeRequest request = reader.getRequest();
        //LOGGER.fine("KVP 1 test passed: " + baseRequest[0].equals(request));
        //LOGGER.finer("base request: " + baseRequest[0].toString());
        //LOGGER.finer("read request: " + request.toString());
        assertTrue(runKvpTest(baseRequest[0], requestString, true));
    }

    /**
     * Checks to make sure that a standard non-matching KVP request is
     * handled correctly.
     *
     * @throws Exception If anything goes wrong.
     */
    public void testKvp2() throws Exception {
        String requestString = "service=WFS&typename=rail,roads";
        assertTrue(runKvpTest(baseRequest[1], requestString, true));
    }
}
