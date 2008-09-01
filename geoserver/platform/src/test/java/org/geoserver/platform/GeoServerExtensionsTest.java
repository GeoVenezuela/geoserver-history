/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.platform;

import static org.easymock.EasyMock.*;

import java.util.List;

import org.springframework.context.ApplicationContext;

import junit.framework.TestCase;

/**
 * Unit test suite for {@link GeoServerExtensions}
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 */
public class GeoServerExtensionsTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetApplicationContext() {
        ApplicationContext appContext1 = createMock(ApplicationContext.class);
        ApplicationContext appContext2 = createMock(ApplicationContext.class);

        GeoServerExtensions gse = new GeoServerExtensions();
        gse.setApplicationContext(appContext1);
        gse.extensionsCache.put(GeoServerExtensionsTest.class, new String[] { "fake" });

        assertSame(appContext1, gse.context);

        gse.setApplicationContext(appContext2);
        assertSame(appContext2, gse.context);
        assertEquals(0, gse.extensionsCache.size());
    }

    public void testExtensions() {
        ApplicationContext appContext = createMock(ApplicationContext.class);
        GeoServerExtensions gse = new GeoServerExtensions();
        gse.setApplicationContext(appContext);

        assertEquals(0, gse.extensionsCache.size());
        expect(appContext.getBeanNamesForType(GeoServerExtensionsTest.class)).andReturn(
                new String[] { "testKey", "fakeKey" });
        expect(appContext.getBean("testKey")).andReturn(this);
        // note I'm testing null is a valid value. If that's not the case, it
        // should be reflected in the code, but I'm writing the test after the
        // code so that's what it does
        expect(appContext.getBean("fakeKey")).andReturn(null);
        replay(appContext);

        List<GeoServerExtensionsTest> extensions = gse.extensions(GeoServerExtensionsTest.class);
        assertNotNull(extensions);
        assertEquals(2, extensions.size());
        assertTrue(extensions.contains(this));
        assertTrue(extensions.contains(null));

        assertEquals(1, gse.extensionsCache.size());
        assertTrue(gse.extensionsCache.containsKey(GeoServerExtensionsTest.class));
        assertNotNull(gse.extensionsCache.get(GeoServerExtensionsTest.class));
        assertEquals(2, gse.extensionsCache.get(GeoServerExtensionsTest.class).length);

        verify(appContext);
    }

    /**
     * If a context is explicitly provided that is not the one set through
     * setApplicationContext(), the extensions() method shall look into it and
     * bypass the cache
     */
    public void testExtensionsApplicationContext() {
        ApplicationContext appContext = createMock(ApplicationContext.class);
        ApplicationContext customAppContext = createMock(ApplicationContext.class);

        GeoServerExtensions gse = new GeoServerExtensions();
        gse.setApplicationContext(appContext);

        // setApplicationContext cleared the static cache
        assertEquals(0, GeoServerExtensions.extensionsCache.size());
        // set the expectation over the app context used as argument
        expect(customAppContext.getBeanNamesForType(GeoServerExtensionsTest.class)).andReturn(
                new String[] { "itDoesntMatterForThePurpose" });
        expect(customAppContext.getBean("itDoesntMatterForThePurpose")).andReturn(this);
        replay(customAppContext);
        replay(appContext);

        List<GeoServerExtensionsTest> extensions = GeoServerExtensions.extensions(
                GeoServerExtensionsTest.class, customAppContext);

        assertNotNull(extensions);
        assertEquals(1, extensions.size());
        assertSame(this, extensions.get(0));
        // cache should be untouched after this since our own context were used
        assertEquals(0, GeoServerExtensions.extensionsCache.size());

        verify(appContext);
        verify(customAppContext);
    }

    public void testBeanString() {
        ApplicationContext appContext = createMock(ApplicationContext.class);

        GeoServerExtensions gse = new GeoServerExtensions();

        gse.setApplicationContext(null);
        assertNull(GeoServerExtensions.bean("beanName"));

        gse.setApplicationContext(appContext);

        expect(appContext.getBean("beanName")).andReturn(null); // call #1
        expect(appContext.getBean("beanName")).andReturn(this); // call #2
        replay(appContext);

        assertNull(GeoServerExtensions.bean("beanName")); // call #1
        assertSame(this, GeoServerExtensions.bean("beanName")); // call #2

        verify(appContext);
    }

    public void _testBeanClassOfT() {
        fail("Not yet implemented");
    }

    public void _testBeanClassOfTApplicationContext() {
        fail("Not yet implemented");
    }

    public void _testOnApplicationEvent() {
        fail("Not yet implemented");
    }

    public void _testCheckContext() {
        fail("Not yet implemented");
    }

}
