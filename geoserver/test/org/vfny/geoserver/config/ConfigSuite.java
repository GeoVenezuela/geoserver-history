/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.config;

import junit.framework.TestCase;
import org.geotools.resources.MonolineFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Tests the get feature request handling.
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @version $Id: ConfigSuite.java,v 1.8 2003/09/09 21:02:25 cholmesny Exp $
 */
public class ConfigSuite extends TestCase {
    /* Initializes the logger.  It's going to be initted anyways, might as
     well do so now*/
    static {
        Log4JFormatter.init("org.vfny.geoserver", Level.FINER);
        Log4JFormatter.init("org.geotools", Level.FINER);
    }

    /** Class logger */
    private static final Logger LOGGER = Logger.getLogger(
            "org.vfny.geoserver.config");

    /** Unit test data directory */
    private static final String CONFIG_DIR = System.getProperty("user.dir")
        + "/misc/unit/config/";

    /** Unit test data directory */
    private static final String TYPE_DIR = System.getProperty("user.dir")
        + "/misc/unit/featureTypes";
    private static final String OTHER_ROOT_DIR = "NewNameConfiguration";
    private static final String TEST_URI = "http://www.openplans.org/rail";

    //private static final String BAD_CONFIG_FILE = 
    //System.getProperty("user.dir") + "/misc/unit/requests/1.xml";
    private ConfigInfo config;
    private TypeRepository repo;

    /**
     * Constructor with super.
     *
     * @param testName the name of the test.
     */
    public ConfigSuite(String testName) {
        super(testName);
    }

    /**
     * Handles test set up details.
     */
    public void setUp() {
        config = ConfigInfo.getInstance(CONFIG_DIR);
        config.setTypeDir(TYPE_DIR);
        repo = TypeRepository.getInstance();
    }

    public void test1() throws Exception {
        LOGGER.fine(repo.toString());
        LOGGER.fine("has two types: " + (repo.typeCount() == 3));
        assertTrue(repo.typeCount() == 3);

        String prefix = config.getDefaultNSPrefix();
        LOGGER.fine("has roads: " + (repo.getType(prefix + ":roads") != null));
        assertTrue(repo.getType(prefix + ":roads") != null);
        LOGGER.fine("has rail: " + (repo.getType(prefix + ":rail") != null));
        assertTrue(repo.getType(prefix + ":rail") != null);
        LOGGER.fine("has ns01:rail: " + (repo.getType("ns01:rail") != null));
        assertTrue(repo.getType("ns01:rail") != null);
        LOGGER.fine("no cows: " + (repo.getType("cows") == null));
        assertTrue(repo.getType("cows") == null);
    }

    public void testServiceConfig() throws Exception {
        ServiceConfig config = ServiceConfig.getInstance(CONFIG_DIR
                + "service-config1.xml");
        LOGGER.fine("config is " + config + ", matches " + doConfigTest(config));
        assertTrue(doConfigTest(config));
    }

    public void testServiceConfigOld() throws Exception {
        ServiceConfig config = ServiceConfig.getInstance(CONFIG_DIR
                + "service-config2.xml");
        LOGGER.fine("config is " + config + ", matches " + doConfigTest(config));
        assertTrue(doConfigTest(config));
    }

    public void testNoConfigFile() {
        try {
            ServiceConfig config = ServiceConfig.getInstance(CONFIG_DIR
                    + "config.xml");

            //should throw error here.
            config.getTitle();
            fail();
        } catch (ConfigurationException e) {
            LOGGER.fine("successfully caught config exception: "
                + e.getMessage());
        }
    }

    public void testFeatureTypeFile() throws Exception {
        FeatureType featureT = FeatureType.getInstance(CONFIG_DIR + "info.xml");
        LOGGER.fine("config is " + featureT);
    }

    private boolean doConfigTest(ServiceConfig servConfig) {
        LOGGER.fine("title: " + servConfig.getTitle() + ", Fees: "
            + servConfig.getFees());

        return (servConfig.getName().equals("FreeFS")
        && servConfig.getTitle().equals("TOPP GeoServer")
        && servConfig.getAbstract().equals("This is a test server.")
        && servConfig.getFees().equals("none")
        && servConfig.getKeywords().get(0).equals("WFS")
        && servConfig.getKeywords().get(1).equals("OGC")
        && servConfig.getOnlineResource().equals("http://beta.openplans.org/geoserver"));
    }

    public void testWfsConfig() throws Exception {
        WfsConfig config = WfsConfig.getInstance(CONFIG_DIR + "wfs-config1.xml");
        LOGGER.fine("config is " + config);
        assertTrue(config.isVerbose());
        assertEquals(TEST_URI, config.getUriFromPrefix("rail"));
        assertEquals(TEST_URI,
            config.getUriFromPrefix(config.getDefaultPrefix()));

        //assertTrue(doConfigTest(config));
    }

    public void testWfsConfig2() throws Exception {
        WfsConfig config = WfsConfig.getInstance(CONFIG_DIR + "wfs-config2.xml",
                OTHER_ROOT_DIR);
        LOGGER.fine("config is " + config);
        assertTrue(!config.isVerbose());
        assertEquals(TEST_URI, config.getUriFromPrefix("rail"));
        assertEquals(TEST_URI,
            config.getUriFromPrefix(config.getDefaultPrefix()));
    }

    public void testConfigInfo() throws Exception {
        LOGGER.fine(config.getTitle() + ", " + config.getKeywords() + ", "
            + config.getUrl() + ", " + config.formatOutput() + ", "
            + config.getDefaultNSPrefix() + ", " + config.getNSUri("rail")
            + ", ");
        assertEquals("The Open Planning Project test server", config.getTitle());
        assertEquals("WFS, CITE, New York", config.getKeywords());
        assertEquals("http://beta.openplans.org/geoserver", config.getUrl());
        assertTrue(config.formatOutput());
        assertEquals("rail", config.getDefaultNSPrefix());
        assertEquals(TEST_URI, config.getNSUri("rail"));
    }

    public void testZConfig() throws Exception {
        ZServerConfig zConfig = ZServerConfig.getInstance(CONFIG_DIR
                + "configuration.xml");
        LOGGER.fine("run z= " + zConfig.run() + ", props is "
            + zConfig.getProps());

        assertTrue(zConfig.run());
        assertEquals("3210", zConfig.getProps().getProperty("port"));
    }

    public void testAdvanced() throws Exception {
        LOGGER.fine("max features is " + config.getMaxFeatures());
        LOGGER.fine("log level is " + config.getLogLevel());
        LOGGER.fine("prefix delimiter is " + config.getFilePrefixDelimiter());
        LOGGER.fine("format output is " + config.formatOutput());
        assertEquals(455, config.getMaxFeatures());
        assertEquals(Level.FINE, config.getLogLevel());
        assertEquals("--", config.getFilePrefixDelimiter());
        assertTrue(config.formatOutput());
    }
}
