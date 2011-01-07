package org.geoserver.wcs;

import static org.geoserver.data.test.MockData.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.data.test.MockData;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.CatalogMode;
import org.geoserver.security.CoverageAccessLimits;
import org.geoserver.security.ResourceAccessManager;
import org.geoserver.security.TestResourceAccessManager;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.filter.Filter;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.SpringSecurityException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.vfny.geoserver.wcs.WcsException;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Performs integration tests using a mock {@link ResourceAccessManager}
 * 
 * @author Andrea Aime - GeoSolutions
 * 
 */
public class ResourceAccessManagerWCSTest extends AbstractGetCoverageTest {

    /**
     * Add the test resource access manager in the spring context
     */
    protected String[] getSpringContextLocations() {
        String[] base = super.getSpringContextLocations();
        String[] extended = new String[base.length + 1];
        System.arraycopy(base, 0, extended, 0, base.length);
        extended[base.length] = "classpath:/org/geoserver/wcs/ResourceAccessManagerContext.xml";
        return extended;
    }

    /**
     * Enable the Spring Security auth filters
     */
    @Override
    protected List<javax.servlet.Filter> getFilters() {
        return Collections.singletonList((javax.servlet.Filter) GeoServerExtensions
                .bean("filterChainProxy"));
    }

    /**
     * Add the users
     */
    @Override
    protected void populateDataDirectory(MockData dataDirectory) throws Exception {
        super.populateDataDirectory(dataDirectory);
        File security = new File(dataDirectory.getDataDirectoryRoot(), "security");
        security.mkdir();

        File users = new File(security, "users.properties");
        Properties props = new Properties();
        props.put("admin", "geoserver,ROLE_ADMINISTRATOR");
        props.put("cite", "cite,ROLE_DUMMY");
        props.put("cite_noworld", "cite,ROLE_DUMMY");
        props.put("cite_noworld_challenge", "cite,ROLE_DUMMY");
        props.put("cite_usa", "cite,ROLE_DUMMY");
        props.store(new FileOutputStream(users), "");
    }

    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();

        // populate the access manager
        TestResourceAccessManager tam = (TestResourceAccessManager) applicationContext
                .getBean("testResourceAccessManager");
        Catalog catalog = getCatalog();
        CoverageInfo world = catalog.getCoverageByName(getLayerId(MockData.WORLD));

        // limits for mr cite_noworld: can't access the world layer
        tam.putLimits("cite_noworld", world, new CoverageAccessLimits(CatalogMode.HIDE,
                Filter.EXCLUDE, null, null));

        // limits for mr cite_noworld: can't access the world layer
        tam.putLimits("cite_noworld_challenge", world, new CoverageAccessLimits(
                CatalogMode.CHALLENGE, Filter.EXCLUDE, null, null));

        // limits the area to north america
        MultiPolygon rasterFilter = (MultiPolygon) new WKTReader().read("MULTIPOLYGON(((-120 30, -120 60, -60 60, -60 30, -120 30)))");
        tam.putLimits("cite_usa", world, new CoverageAccessLimits(CatalogMode.HIDE, null,
                rasterFilter, null));
    }

    Map<String, Object> getWorld() {
        Map<String, Object> raw = baseMap();
        final String getLayerId = getLayerId(WORLD);
        raw.put("identifier", getLayerId);
        raw.put("format", "image/tiff");
        raw.put("BoundingBox", "-90,-180,90,180,urn:ogc:def:crs:EPSG:6.6:4326");
        raw.put("store", "false");
        raw.put("GridBaseCRS", "urn:ogc:def:crs:EPSG:6.6:4326");
        return raw;
    }

    public void testNoLimits() throws Exception {
        Map<String, Object> raw = getWorld();

        authenticate("cite", "cite");
        GridCoverage[] coverages = executeGetCoverageKvp(raw);
        
        // basic checks
        assertEquals(1, coverages.length);
        GridCoverage2D coverage = (GridCoverage2D) coverages[0];
        final CoordinateReferenceSystem wgs84Flipped = CRS.decode("urn:ogc:def:crs:EPSG:6.6:4326");
        assertEquals(wgs84Flipped, coverage.getEnvelope().getCoordinateReferenceSystem());
        assertEquals(-90.0, coverage.getEnvelope().getMinimum(0));
        assertEquals(-180.0, coverage.getEnvelope().getMinimum(1));
        assertEquals(90.0, coverage.getEnvelope().getMaximum(0));
        assertEquals(180.0, coverage.getEnvelope().getMaximum(1));
        
        // make sure it has not been cropped
        int[] value = new int[3];
        
        // some point in USA
        coverage.evaluate((DirectPosition) new DirectPosition2D(wgs84Flipped, 40, -90), value);
        assertTrue(value[0] > 0);
        assertTrue(value[1] > 0);
        assertTrue(value[2] > 0);
        
        // some point in Europe
        coverage.evaluate((DirectPosition) new DirectPosition2D(wgs84Flipped, 45, 12), value);
        assertTrue(value[0] > 0);
        assertTrue(value[1] > 0);
        assertTrue(value[2] > 0);
    }

    public void testNoAccess() throws Exception {
        Map<String, Object> raw = getWorld();

        authenticate("cite_noworld", "cite");
        try {
            executeGetCoverageKvp(raw);
            fail("This should have failed with an exception");
        } catch (WcsException e) {
            // we should have got some complaint about the layer not being there
            assertTrue(e.getMessage().matches(".*wcs:World.*"));
        }
    }

    public void testChallenge() throws Exception {
        Map<String, Object> raw = getWorld();

        authenticate("cite_noworld_challenge", "cite");
        try {
            executeGetCoverageKvp(raw);
            fail("This should have failed with a security exception");
        } catch (Throwable e) {
            // make sure we are dealing with some security exception
            SpringSecurityException se = null;
            while(e.getCause() != null && e.getCause() != e) {
                e = e.getCause();
                if(e instanceof SpringSecurityException) {
                    se = (SpringSecurityException) e;
                }
            }
            
            if(e == null) {
                fail("We should have got some sort of SpringSecurityException");
            } else {
                // some mumbling about not having enough privileges
                assertTrue(se.getMessage().contains("World"));
                assertTrue(se.getMessage().contains("privileges"));
            }
            
        }
    }
    
    public void testRasterFilterUSA() throws Exception {
        Map<String, Object> raw = getWorld();

        authenticate("cite_usa", "cite");
        GridCoverage[] coverages = executeGetCoverageKvp(raw);
        
        // basic checks
        assertEquals(1, coverages.length);
        GridCoverage2D coverage = (GridCoverage2D) coverages[0];
        final CoordinateReferenceSystem wgs84Flipped = CRS.decode("urn:ogc:def:crs:EPSG:6.6:4326");
        assertEquals(wgs84Flipped, coverage.getEnvelope().getCoordinateReferenceSystem());
        assertEquals(-90.0, coverage.getEnvelope().getMinimum(0));
        assertEquals(-180.0, coverage.getEnvelope().getMinimum(1));
        assertEquals(90.0, coverage.getEnvelope().getMaximum(0));
        assertEquals(180.0, coverage.getEnvelope().getMaximum(1));
        
        // make sure it has been cropped
        int[] value = new int[3];
        
        // some point in USA
        coverage.evaluate((DirectPosition) new DirectPosition2D(wgs84Flipped, 40, -90), value);
        assertTrue(value[0] > 0);
        assertTrue(value[1] > 0);
        assertTrue(value[2] > 0);
        
        // some point in Europe (should have been cropped, we should get the bkg value)
        coverage.evaluate((DirectPosition) new DirectPosition2D(wgs84Flipped, 45, 12), value);
        assertEquals(0, value[0]);
        assertEquals(0, value[1]);
        assertEquals(0, value[2]);
    }

    @Override
    protected void authenticate(String username, String password) {
        // override as this is not a test going through the servlet filters
        GrantedAuthority ga = new GrantedAuthorityImpl("MOCKROLE");
        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(
                username, null, new GrantedAuthority[] { ga });
        SecurityContextHolder.getContext().setAuthentication(user);
    }
}
