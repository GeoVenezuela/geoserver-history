/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

/**
 *    @author lreed@refractions.net
 */

package org.geoserver.wps.kvp;

import java.util.List;
import java.util.ArrayList;

import org.geoserver.ows.KvpParser;
import org.geoserver.ows.util.KvpUtils;

import net.opengis.ows11.CodeType;
import net.opengis.ows11.Ows11Factory;
import net.opengis.ows11.impl.Ows11FactoryImpl;

public class IdentifierKvpParser extends KvpParser
{
    public IdentifierKvpParser()
    {
        super("identifier", CodeType.class);

        this.setService("wps");
    }

    @Override
    public Object parse(String value) throws Exception
    {
        List<CodeType> values = new ArrayList<CodeType>();

        Ows11Factory owsFactory = new Ows11FactoryImpl();

        for(String str : (List<String>)KvpUtils.readFlat(value))
        {
            CodeType codeType = owsFactory.createCodeType();
            codeType.setValue(str);
            values.add(codeType);
        }

        return values;
    }
}