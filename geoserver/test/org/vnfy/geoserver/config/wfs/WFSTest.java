/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.vnfy.geoserver.config.wfs;

import junit.framework.TestCase;

import org.vnfy.geoserver.config.Service;
/**
 * WFSTest purpose.
 * <p>
 * Description of WFSTest ...
 * <p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @version $Id: WFSTest.java,v 1.1.2.2 2003/12/31 00:36:51 dmzwiers Exp $
 */
public class WFSTest extends TestCase {

	private WFS a,b;
	/**
	 * Constructor for WFSTest.
	 * @param arg0
	 */
	public WFSTest(String arg0) {
		super(arg0);
		a = new WFS();
		a.setDescribeUrl("http://www.cs.uvic.ca/~dzwiers/");
	}

	/*
	 * Test for void NameSpace(NameSpace)
	 */
	public void testNameSpaceNameSpace() {
		//test requires equals.
		b = new WFS(a);
		assertTrue("Testing Contact(Contact)\nRelies on Contact.equals.",a.equals(b));
	}

	/*
	 * Test for Object clone()
	 */
	public void testClone() {
		//test requires equals.
		b =(WFS)a.clone();
		assertTrue("Testing Contact(Contact)\nRelies on Contact.equals.",a.equals(b));
	}

	/*
	 * Test for boolean equals(Object)
	 */
	public void testEqualsObject() {
		b = new WFS();
		b.setDescribeUrl("http://www.cs.uvic.ca/~dzwiers/");
		assertTrue(a.equals(b));

		b.setDescribeUrl("http://www.google.ca/");
		assertTrue(!a.equals(b));

		b.setDescribeUrl("http://www.cs.uvic.ca/~dzwiers/");
		Service s = new Service();
		s.setName("test");
		b.setService(s);
		assertTrue(!a.equals(b));
	}

}
