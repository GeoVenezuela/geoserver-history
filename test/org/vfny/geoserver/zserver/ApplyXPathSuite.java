/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.zserver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.FileNotFoundException;
import java.util.logging.Logger;


/**
 * Tests the NumericField number and string conversion.
 *
 * @author Chris Holmes, TOPP
 * @version $Id: ApplyXPathSuite.java,v 1.10 2004/04/02 11:35:45 cholmesny Exp $
 */
public class ApplyXPathSuite extends TestCase {
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger(
            "org.vfny.geoserver.zserver");

    /** Unit test data directory */
    private static final String DATA_DIRECTORY = System.getProperty("user.dir")
        + "/test/test-data/zserver";
    private String testPath = DATA_DIRECTORY + "/test1/metadata.xml";
    private String[] results = null;

    /**
     * Initializes the database and request handler.
     *
     * @param testName The name of the test to run.
     */
    public ApplyXPathSuite(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ApplyXPathSuite.class);
        LOGGER.fine("Creating ApplyXPath suite.");

        return suite;
    }

    public void setUp() {
    }

    public void testApply1() {
        String xpath = "//metadata/idinfo/citation/citeinfo/title/text()";
        LOGGER.fine("testing xpath: " + xpath);

        try {
            results = ApplyXPath.apply(testPath, xpath);
        } catch (java.io.FileNotFoundException e) {
            fail("couldn't find file: " + e);
        }

        assertTrue(results[0].equals("Water Supply Watersheds "));
    }

    public void testApply2() {
        String xpath2 = "//metadata/idinfo/spdom/bounding//";
        LOGGER.fine("testing xpath: " + xpath2);

        try {
            results = ApplyXPath.apply(testPath, xpath2);
        } catch (FileNotFoundException e) {
            fail("couldn't find file: " + e);
        }
 //TODO fix this, commented out for alpha release
 //       assertTrue(results.length == 9);
    }

    public void testApply3() {
        String xpath3 = "//metadata/idinfo/spdom/bounding/westbc/text()";
        LOGGER.fine("testing xpath: " + xpath3);

        try {
            results = ApplyXPath.apply(testPath, xpath3);
        } catch (FileNotFoundException e) {
            fail("couldn't find file: " + e);
        }

        assertTrue(results[0].equals("-83.9814 "));
    }

    public void testApply4() {
        String xpath4 = "//metadata/nadfme";
        LOGGER.fine("testing xpath: " + xpath4);

        try {
            results = ApplyXPath.apply(testPath, xpath4);
        } catch (FileNotFoundException e) {
            fail("couldn't find file: " + e);
        }

        assertTrue(results.length == 0);
    }

    public void testFileNotFound() {
        String xpath = "//";
        String failPath = DATA_DIRECTORY + "/blorg.xml";

        try {
            results = ApplyXPath.apply(failPath, xpath);
            fail("didn't throw FileNotFoundException");
        } catch (FileNotFoundException e) {
            LOGGER.finer("threw proper exception");
        }
    }
}
