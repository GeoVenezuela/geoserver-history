/* Copyright (c) 2001 Vision for New York - www.vfny.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root application directory.
 */

package org.vfny.geoserver.types;

import org.postgresql.util.*;
import java.sql.*;
import java.util.*;

/**
 * Implements an OGC simple type.
 *
 * @author Vision for New York
 * @author Rob Hranac 
 * @version 0.9 alpha, 11/01/01
 *
 */
public class Point extends Geometry 
{

	/**
	 * The X coordinate of the point.
	 */
	public double x;

	/**
	 * The Y coordinate of the point.
	 */
	public double y;

	/**
	 * The Z coordinate of the point.
	 */
	public double z;

	public Point() 
	{
		type = POINT;
	}

	public Point(double x, double y, double z) 
	{
		this();
		this.x = x;
		this.y = y;
		this.z = z;
		dimension = 3;
	}

	public Point(double x, double y) 
	{
		this();
		this.x = x;
		this.y = y;
		this.z = 0.0;
		dimension = 2;
	}
	
	public Point(String value) throws SQLException
	{
		this();
		value = value.trim();
		if ( value.indexOf("POINT") == 0 ) 
		{
			value = value.substring(5).trim();
		}
		PGtokenizer t = new PGtokenizer(PGtokenizer.removePara(value),' ');
		try {
			if ( t.getSize() == 3 ) {
				x = Double.valueOf(t.getToken(0)).doubleValue();
				y = Double.valueOf(t.getToken(1)).doubleValue();
				z = Double.valueOf(t.getToken(2)).doubleValue();
				dimension = 3;
			} else {
				x = Double.valueOf(t.getToken(0)).doubleValue();
				y = Double.valueOf(t.getToken(1)).doubleValue();
				z = 0.0;
				dimension = 2;
			}
		}
		catch(NumberFormatException e) {
			throw new PSQLException("postgis.Point",e.toString());
		}
	}

	public String toString() 
	{
		return "POINT (" + getValue() + ")";
	}

	public String getValue()
	{
		if ( dimension == 3 )
		{
			return x+" "+y+" "+z;
		} else {
			return x+" "+y;
		}
	}
	
	public double getX()
	{
		return x;
	}
	
	public double getY() 
	{
		return y;
	}
	
	public double getZ()
	{
		return z;
	}
	
	public void setX(double x)
	{
		this.x = x;
	}

	public void setY(double y)
	{
		this.y = y;
	}

	public void setZ(double z)
	{
		this.z = z;
	}
	
	public void setX(int x)
	{
		this.x = (double)x;
	}

	public void setY(int y)
	{
		this.y = (double)y;
	}

	public void setZ(int z)
	{
		this.z = (double)z;
	}

}
