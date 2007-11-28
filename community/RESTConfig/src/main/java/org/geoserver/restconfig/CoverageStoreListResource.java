package org.geoserver.restconfig;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.vfny.geoserver.config.DataConfig;

public class CoverageStoreListResource extends MapResource {

	private DataConfig myDataConfig;

	public CoverageStoreListResource(Context context, Request request, Response response, DataConfig config){
        super(context, request, response);
        myDataConfig = config;
	}
	
	@Override
	public Map getMap() {
		Map m = new HashMap();
		m.put("coveragestores", myDataConfig.getDataFormatIds());
		return m;
	}

	@Override
	public Map getSupportedFormats() {
		Map m = new HashMap();
		m.put("html", new HTMLFormat("HTMLTemplates/coveragestores.ftl"));
		m.put("json", new JSONFormat());
		m.put(null, m.get("html"));
		return m;
	}
}
