/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

/**
 *	@author lreed@refractions.net
 */

package org.geoserver.wps;

import net.opengis.wps.GetCapabilitiesType;
import net.opengis.wps.DescribeProcessType;
import net.opengis.wps.ExecuteType;
import net.opengis.wps.RequestBaseType;

import org.geotools.xml.transform.TransformerBase;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DefaultWebProcessingService implements WebProcessingService, ApplicationContextAware
{
    protected WPS  wps;

    protected ApplicationContext context;

    public DefaultWebProcessingService(WPS wps)
    {
        this.wps  = wps;
    }

    public TransformerBase getCapabilities(GetCapabilitiesType request) throws WPSException
    {
        return new GetCapabilities(this.wps).run(request);
    }

    public TransformerBase describeProcess(DescribeProcessType request) throws WPSException
    {
        return new DescribeProcess(this.wps).run(request);
    }

    public TransformerBase execute(ExecuteType request) throws WPSException
    {
        return new Execute(this.wps).run(request);
    }

    public void getSchema(RequestBaseType a) throws WPSException
    {

    }

    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        this.context = context;
    }
}
