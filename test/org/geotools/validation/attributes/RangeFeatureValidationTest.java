/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation.attributes;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.validation.RoadValidationResults;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * RangeFeatureValidationTest purpose.
 * <p>
 * Description of RangeFeatureValidationTest ...
 * <p>
 * Capabilities:
 * <ul>
 * <li></li>
 * </ul>
 * Example Use:
 * <pre><code>
 * RangeFeatureValidationTest x = new RangeFeatureValidationTest(...);
 * </code></pre>
 * 
 * @author bowens, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: RangeFeatureValidationTest.java,v 1.3 2004/01/21 18:42:26 jive Exp $
 */
public class RangeFeatureValidationTest extends TestCase {
	private RoadValidationResults results;
	private FeatureType type;
	private Feature feature;
	RangeFeatureValidation test;
	/**
	 * Constructor for RangeFeatureValidationTest.
	 * @param arg0
	 */
	public RangeFeatureValidationTest(String arg0) {
		super(arg0);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		GeometryFactory gf = new GeometryFactory();
		test = new RangeFeatureValidation();
		super.setUp();
		
		type = DataUtilities.createType(getName()+".road",
		"id:0,*geom:LineString,name:String");
		Coordinate[] coords = new Coordinate[]{ new Coordinate(1, 1), new Coordinate( 2, 2), new Coordinate (4, 2), new Coordinate (5, 1)};

		
		feature = type.create(new Object[] {
				new Integer(1),
				gf.createLineString(coords),
				"r1",
			},
			"road.rd1"
		);
		
		results = new RoadValidationResults();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		test = null;
		super.tearDown();
	}

	public void testRangeFeatureValidation() throws Exception {
		test.setPath("id");
		
		
		assertTrue(test.validate(feature, type, results));
		assertEquals(0,results.failedFeatures.size());
		test.setMin(5);
		assertTrue(!test.validate(feature, type, results));
		assertEquals(1,results.failedFeatures.size());		
	}

	public void testValidate() {
		//test.validate(feature, type, results);
	}

	public void testSetName() {
		test.setName("foo");
		assertEquals("foo", test.getName());
	}

	public void testGetName() {
		test.setName("bork");
		assertEquals("bork", test.getName());
	}

	public void testSetDescription() {
		test.setDescription("foo");
		assertEquals("foo", test.getDescription());
	}

	public void testGetDescription() {
		test.setDescription("bork");
		assertEquals("bork", test.getDescription());
	}

	public void testGetPriority() {
		//TODO Implement getPriority().
	}

	public void testSetTypeNames() {
		String[] names = { "one", "two" };
		test.setTypeNames(names);
		assertEquals(names, test.getTypeNames());
	}

	public void testGetTypeNames() {
		String[] names = { "one", "two" };
		test.setTypeNames(names);
		assertEquals(names, test.getTypeNames());
	}

	public void testGetMax() {
		test.setMax(100);
		assertEquals(100, test.getMax());
	}

	public void testGetMin() {
		test.setMin(10);
		assertEquals(10, test.getMin());
	}

	public void testGetPath() {
		test.setPath("path");
		assertEquals("path", test.getPath());
	}

	public void testSetMax() {
		test.setMax(500);
		assertEquals(500, test.getMax());

	}

	public void testSetMin() {
		test.setMin(5);
		assertEquals(5, test.getMin());
	}

	public void testSetPath() {
		test.setPath("path2");
		assertEquals("path2", test.getPath());
	}

}
