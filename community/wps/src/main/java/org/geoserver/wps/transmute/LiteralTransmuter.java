/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

/**
 * @author lreed@refractions.net
 */

package org.geoserver.wps.transmute;

public interface LiteralTransmuter extends Transmuter
{
    String getType();

    Object decode(String str);

    Object encode(Object obj);
}