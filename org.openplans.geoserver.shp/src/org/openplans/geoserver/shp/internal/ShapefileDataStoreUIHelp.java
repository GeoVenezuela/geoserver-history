package org.openplans.geoserver.shp.internal;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFactorySpi.Param;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.openplans.geoserver.catalog.ui.DataStoreUIHelp;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ShapefileDataStoreUIHelp extends DataStoreUIHelp
	implements ApplicationContextAware {

	ApplicationContext context;
	
	public ShapefileDataStoreUIHelp(Class dataStoreFactoryClass) {
		super(dataStoreFactoryClass);
	}

	public String getDescription(DataStoreFactorySpi factory, Param param) {
		if (param == ShapefileDataStoreFactory.URLP) {
			return context.getMessage("shapefile.url.description",null,null);
		}
		if (param == )
	}

	public void setApplicationContext(ApplicationContext applicationContext) 
		throws BeansException {
		context = applicationContext;
	}
}
