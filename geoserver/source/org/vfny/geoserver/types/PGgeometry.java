/* Copyright (c) 2001 Vision for New York - www.vfny.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root application directory.
 */

package org.vfny.geoserver.types;

import org.postgresql.util.*;
import java.sql.*;

/**
 * Implements an OGC simple type.
 *
 * @author Vision for New York
 * @author Rob Hranac 
 * @version 0.9 alpha, 11/01/01
 *
 */
public class PGgeometry extends PGobject 
{

	Geometry geom;

	public PGgeometry() {}

	public PGgeometry(Geometry geom) {
		this.geom = geom;
	}

	public PGgeometry(String value) throws SQLException
	{
		setValue(value);
	}

	public void setValue(String value) throws SQLException
	{
		value = value.trim();
		if( value.startsWith("MULTIPOLYGON")) {
			geom = new MultiPolygon(value);
		} else if( value.startsWith("MULTILINESTRING")) {
			geom = new MultiLineString(value);
		} else if( value.startsWith("MULTIPOINT")) {
			geom = new MultiPoint(value);
		} else if( value.startsWith("POLYGON")) {
			geom = new Polygon(value);
		} else if( value.startsWith("LINESTRING")) {
			geom = new LineString(value);
		} else if( value.startsWith("POINT")) {
			geom = new Point(value);
		} else {
			throw new SQLException("Unknown type: " + value);
		}
	}

	public Geometry getGeometry() {
		return geom;
	}

	public int getGeoType() {
		return geom.type;
	}

	public String toString() {
		return geom.toString();
	}

	public String getValue() {
		return geom.toString();
	}

	public Object clone() 
	{
		PGgeometry obj = new PGgeometry(geom);
		obj.setType(type);
		return obj;
	}
	
}
