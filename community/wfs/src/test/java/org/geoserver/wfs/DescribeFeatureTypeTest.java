package org.geoserver.wfs;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.data.feature.FeatureTypeInfo;
import org.geoserver.util.ReaderUtils;
import org.geoserver.wfs.http.DescribeFeatureTypeResponse;
import org.w3c.dom.Element;

public class DescribeFeatureTypeTest extends WFSTestSupport {

	public void testDescribeFeatureType() throws Exception {
		
		DescribeFeatureType op = new DescribeFeatureType( wfs, catalog );
		
		ArrayList typeName = new ArrayList();
		typeName.add( qname( BASIC_POLYGONS_TYPE ) );
		op.setTypeName( typeName );
		op.setOutputFormat( "XMLSCHEMA" );
		
		FeatureTypeInfo[] infos = op.describeFeatureType();
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		new DescribeFeatureTypeResponse().write( infos, output, op );
		
		String result = new String( output.toByteArray() );
		Element schemaDoc = ReaderUtils.parse( new StringReader ( result ) );
		assertEquals( 1, schemaDoc.getElementsByTagName( "xs:complexType" ).getLength() );
		
		Element ctElement = 
			 (Element) schemaDoc.getElementsByTagName( "xs:complexType" ).item( 0 );
		assertEquals( BASIC_POLYGONS_TYPE + "_Type", ctElement.getAttribute("name") );
	
		assertEquals( 
			2, ctElement.getElementsByTagName("xs:element").getLength() 
		);
		Element geomElement = 
			(Element) ctElement.getElementsByTagName( "xs:element" ).item( 0 );
		Element idElement = 
			(Element) ctElement.getElementsByTagName( "xs:element" ).item( 1 );
		
		assertEquals( "the_geom", geomElement.getAttribute( "name" ) );
		assertEquals( "ID", idElement.getAttribute( "name" ) );
	
		Element eElement = 
			(Element) schemaDoc.getElementsByTagName( "xs:element" ).item( 2 );
		assertEquals( BASIC_POLYGONS_TYPE, eElement.getAttribute( "name" ) );
	}
	
	QName qname( String name ) {
		return new QName( CITE_URI, name, CITE_PREFIX );
	}
}
