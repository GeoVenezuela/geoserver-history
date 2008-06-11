/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geoserver.data.test.MockData;
import org.geoserver.wfs.WFSTestSupport;
import org.geotools.gml2.bindings.GML;
import org.geotools.xml.Schemas;
import org.vfny.geoserver.global.FeatureTypeInfo;
import javax.xml.namespace.QName;


public class FeatureTypeInfoSchemaBuilderTest extends WFSTestSupport {
    public void testBuildGml2() throws Exception {
        FeatureTypeSchemaBuilder builder = new FeatureTypeSchemaBuilder.GML2(getWFS(),
                getCatalog(), getResourceLoader());

        FeatureTypeInfo lines = getCatalog().getFeatureTypeInfo(MockData.LINES);
        XSDSchema schema = builder.build(new FeatureTypeInfo[] { lines },
                getWFS().getOnlineResource().toExternalForm());

        assertNotNull(schema);

        XSDElementDeclaration element = Schemas.getElementDeclaration(schema, MockData.LINES);
        assertNotNull(element);

        assertTrue(element.getType() instanceof XSDComplexTypeDefinition);

        XSDElementDeclaration id = Schemas.getChildElementDeclaration(element,
                new QName(MockData.CGF_URI, "id"));
        assertNotNull(id);

        XSDElementDeclaration lineStringProperty = Schemas.getChildElementDeclaration(element,
                new QName(MockData.CGF_URI, "lineStringProperty"));
        assertNotNull(lineStringProperty);

        XSDTypeDefinition lineStringPropertyType = lineStringProperty.getType();
        assertEquals(GML.NAMESPACE, lineStringPropertyType.getTargetNamespace());
        assertEquals(GML.LINESTRINGPROPERTYTYPE.getLocalPart(), lineStringPropertyType.getName());

        XSDTypeDefinition geometryAssociationType = lineStringPropertyType.getBaseType();
        assertNotNull(geometryAssociationType);
        assertEquals(GML.NAMESPACE, geometryAssociationType.getTargetNamespace());
        assertEquals(GML.GEOMETRYASSOCIATIONTYPE.getLocalPart(), geometryAssociationType.getName());
    }
}
