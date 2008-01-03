package org.geoserver.wcs.kvp;

import static org.vfny.geoserver.wcs.WcsException.WcsExceptionCode.InvalidParameterValue;

import org.geoserver.ows.KvpParser;
import org.vfny.geoserver.wcs.WcsException;

/**
 * Not really a parser, but a validity checker instead (ensures the specified type
 * is among the values foreseen by the standard and supported by GeoServer)
 * 
 * @author Andrea Aime
 * 
 */
public class GridTypeKvpParser extends KvpParser {
    public GridTypeKvpParser() {
        super("GridType", String.class);
    }

    @Override
    public Object parse(String value) throws Exception {
        GridType type = null;
        for (GridType currType : GridType.values()) {
            if (currType.getXmlConstant().equals(value)) {
                type = currType;
                break;
            }
        }

        if (type == null)
            throw new WcsException("Could not understand grid type '" + value + "'",
                    InvalidParameterValue, "GridType");

        if (type == GridType.GT2dGridIn3dCrs)
            throw new WcsException("GeoServer does not support type " + type.name(),
                    InvalidParameterValue, "GridType");

        return value;
    }
}
